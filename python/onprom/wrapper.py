import logging
import time
import re
from pathlib import Path
from datetime import datetime
from functools import wraps
from typing import Any

from tqdm.auto import tqdm
import neo4j
import dateutil.parser

# Setup standard Python logger
logger = logging.getLogger("OnpromWrapper")
if not logger.handlers:
    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter('%(asctime)s %(levelname)-8s %(message)s', datefmt='%Y-%m-%d %H:%M:%S'))
    logger.addHandler(handler)
    logger.setLevel(logging.DEBUG)


def simple_tracelog(func):
    """Lightweight self-contained tracing decorator replacing external tracelog."""

    @wraps(func)
    def inner(*args, **kwargs):
        logger.debug(f"Entering {func.__name__}")
        result = func(*args, **kwargs)
        logger.debug(f"Exiting {func.__name__}")
        return result

    return inner


class OnpromWrapper:
    def __init__(self, config_dataset: dict | Any = None):
        self.__db_name = None
        self._log = None
        self._config = config_dataset
        self._files_loaded = False
        self._entities_loaded = False
        self._gateway = None

    def _get_config_value(self, keys, default=None):
        """Helper to support tuple-key lookups on standard dictionaries or config objects."""
        if isinstance(keys, str):
            keys = (keys,)
        cur = self._config
        for k in keys:
            if hasattr(cur, "__getitem__"):
                try:
                    cur = cur[k]
                except (KeyError, TypeError, IndexError):
                    return default
            else:
                return default
        return cur if cur is not None else default

    def get_stats(self):
        return self._gateway.entry_point.getStats()

    @simple_tracelog
    def __enter__(self):
        from py4j.java_gateway import JavaGateway, GatewayParameters
        self._gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True))
        self._gateway.entry_point.resetConfiguration()
        return self

    @simple_tracelog
    def get_entity_types(self) -> set[str]:
        self.get_log()
        return self._log.getObjectTypes()

    @simple_tracelog
    def set_entities(self, entities: set[str], static_types: set[str]):
        self._entities_loaded = True
        self._gateway.entry_point.loadEntitiesWithTypes(entities, static_types)

    @simple_tracelog
    def load_files(self, entities_loaded: set[str] = None, static_types: set[str] = None) -> None:
        if self._config is None:
            raise ValueError("No configuration provided. Cannot load files")

        config_location = Path(self._get_config_value(("basic_constants", "absolute_code_location"), ".")) / "resources"
        if hasattr(self._config, "location") and self._config.location:
            config_location = self._config.location

        owl_location = config_location / self._get_config_value(("onprom", "sources", "schema_location"), "")
        obda_location = config_location / self._get_config_value(("onprom", "sources", "mapping_location"), "")
        annotation_location = config_location / self._get_config_value(("onprom", "sources", "annotation_location"), "")
        properties_location = config_location / self._get_config_value(("onprom", "sources", "properties_location"), "")

        if entities_loaded:
            assert static_types is not None
            self.set_entities(entities_loaded, static_types)

        self._gateway.entry_point.loadFile(str(properties_location))
        self._gateway.entry_point.loadFile(str(obda_location))
        self._gateway.entry_point.loadFile(str(owl_location), True)
        q_file = self._gateway.entry_point.loadFile(str(annotation_location), True)

        if annotation_location.suffix == ".ann" and q_file:
            p = Path(q_file)
            # Update config dict value if possible
            if isinstance(self._config, dict):
                self._config.setdefault("onprom", {}).setdefault("sources", {})["annotation_location"] = p.name
        self._files_loaded = True

    @simple_tracelog
    def get_entities(self):
        return self._gateway.entry_point.getEntities()

    @simple_tracelog
    def get_static_types(self):
        return self._gateway.entry_point.getStaticTypes()

    @simple_tracelog
    def get_partial_log(self):
        if self._entities_loaded:
            return self._gateway.entry_point.getPartialLog()
        return self._gateway.entry_point.getPartialLog(None, None)

    @simple_tracelog
    def load_owl(self, path: str):
        return self._gateway.entry_point.getOntologyFromFile(path)

    @simple_tracelog
    def get_rel_types(self, path: str):
        return self._gateway.entry_point.getRelationsFromFile(path)

    @simple_tracelog
    def get_log(self):
        if not self._files_loaded:
            raise RuntimeError("Files for generating log have not been loaded. Please run load_files first!")
        if self._log is None:
            logger.info("Getting log from original source. This might take some time...")
            self._log = self.get_partial_log()
        return self._log

    def _create_database(self, driver, db_name: str):
        """Self-contained version of Common.create_database."""
        with driver.session(database="neo4j") as session:
            session.run("CREATE DATABASE $name IF NOT EXISTS DEFAULT LANGUAGE CYPHER 25", name=db_name)
        max_wait_time = 180
        check_after = 5
        waited_for = 0
        while True:
            try:
                with driver.session(database=db_name) as session:
                    session.run("RETURN \"hi\"")
            except neo4j.exceptions.ClientError as e:
                if waited_for >= max_wait_time:
                    driver.close()
                    raise TimeoutError(f"Could not create database {db_name} after {max_wait_time} seconds!") from e
                time.sleep(check_after)
                waited_for += check_after
            else:
                break

    @simple_tracelog
    def __delete_all_nodes(self, driver):
        if not self._config:
            raise ValueError("No configuration provided. Cannot delete nodes")

        event_label = self._get_config_value(("entity_rel_types", "Event"), "Event")
        entity_label = self._get_config_value(("entity_rel_types", "Entity"), "Entity")

        if self.__db_name == "neo4j":
            with driver.session(database=self.__db_name) as session:
                session.run(f"MATCH (n:{event_label}) CALL (n) {{ DETACH DELETE n }} IN TRANSACTIONS OF 1000 ROWS")
            with driver.session(database=self.__db_name) as session:
                session.run(f"MATCH (o:{entity_label}) CALL (o) {{ DETACH DELETE o }} IN TRANSACTIONS OF 1000 ROWS")
        else:
            with driver.session(database="neo4j") as session:
                session.run(f"DROP DATABASE " + self.__db_name)
            self._create_database(driver, self.__db_name)

    @staticmethod
    def to_some_type(tp: str, val: str):
        if tp == "Integer":
            return int(val)
        if tp == "String" or tp == "Other":
            return val
        if tp == "Boolean":
            return bool(val)
        if "time" in tp.lower() or "Date" in tp.lower():
            try:
                parsed = dateutil.parser.parse(val)
                return parsed
            except Exception:
                return val
        if tp == "Float":
            return float(val)
        return val

    @simple_tracelog
    def write_log_to_neo4j(self, basic_config: Any):
        if not self._config:
            raise ValueError("No configuration provided. Cannot write log to neo4j")
        self.get_log()
        logger.debug("Deleting previous graph for this dataset, creating indices")

        neo4j_conn = getattr(basic_config, "driver", basic_config)
        supports_multidb = True
        if hasattr(basic_config, "driver_supports_multi_db"):
            supports_multidb = basic_config.driver_supports_multi_db()

        if not self.__db_name:
            self.__db_name = self._get_config_value("database_name", "neo4j") if supports_multidb else "neo4j"

        self.__delete_all_nodes(neo4j_conn)

        event_label = self._get_config_value(("entity_rel_types", "Event"), "Event")
        ev_id_key = self._get_config_value(("prop_keys", "ev_id_label"), "id")
        act_key = self._get_config_value(("prop_keys", "act_label"), "activity")
        time_key = self._get_config_value(("prop_keys", "time_label"), "timestamp")
        obj_id_key = self._get_config_value(("prop_keys", "obj_id_label"), "id")
        entity_label = self._get_config_value(("entity_rel_types", "Entity"), "Entity")
        corr_rel = self._get_config_value(("entity_rel_types", "CORR"), "CORR")

        events = self._log.getEvents()
        with neo4j_conn.session(database=self.__db_name) as session:
            logger.debug(f"Inserting {len(events)} events")
            for event_id, event in tqdm(events.items(), desc="Created events", total=len(events)):
                props = {k: self.to_some_type(v.getType(), v.getThing().getRawValue()) for k, v in
                         event.getVmap().items()}
                props.update({
                    ev_id_key: event_id,
                    act_key: event.getActivity(),
                    time_key: self.to_some_type("timestamp", event.getTimestampAsString())
                })
                session.run("CREATE (node:$($labels) $properties)", labels=[event_label], properties=props)

        with neo4j_conn.session(database=self.__db_name) as session:
            objects = self._log.getObjects()
            logger.debug(f"Inserting {len(objects)} objects")
            for object_id, log_object in tqdm(objects.items(), desc="Objects created", total=len(objects)):
                props = {k: self.to_some_type(v.getType(), v.getThing().getRawValue()) for k, v in
                         log_object.getOvmap().items()}
                props.update({obj_id_key: object_id})
                session.run("CREATE (node:$($labels) $properties)", labels=[entity_label, log_object.getType()],
                            properties=props)

        with neo4j_conn.session(database=self.__db_name) as session:
            logger.debug("Creating CORR relations")
            for event_id, event in tqdm(events.items(), desc="CORR relations created for", total=len(events)):
                session.run(
                    f"MATCH (n:{event_label}) WHERE n.{ev_id_key} = $event_id "
                    f"MATCH (o) WHERE o.{obj_id_key} IN $objects "
                    f"CREATE (n)-[:{corr_rel}]->(o)",
                    event_id=event_id,
                    objects=list(event.getOmap())
                )

    @simple_tracelog
    def __exit__(self, exc_type, exc_value, traceback):
        if self._gateway:
            self._gateway.close()
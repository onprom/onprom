# ocel-support
Support to the OCEL standard

## Available commands:

### Importing / Exporting / Validation

**log_object = ocel.import_log(log_path)**: imports a log from the specified path (both JSON-OCEL and XML-OCEL).
**ocel.export_log(log_object, log_path)**: exports a log into the specified path (both JSON-OCEL and XML-OCEL).
**is_valid = ocel.validate(log_path, schema_path)**: validates the specified log against the schema (both JSON-OCEL and XML-OCEL).

### Interface

**ocel.get_events(log_object)**: returns a Python dictionary associating event identifiers to their
pr (activity, timestamp, object map, attribute map).
**ocel.get_objects(log_object)**: returns a Python dictionary associating object identifiers to their attributes
(object type, attribute map).
**ocel.get_attribute_names(log_object)**: returns a list of attribute names involved in events/objects of the log.
**ocel.get_object_types(log_object)**: returns a list of object types involved in events/objects of the log.
**ocel.get_version(log_object)**: gets the OCEL version (as a string) from the current OCEL object.
**ocel.get_global_event(log_object)**: gets the global values of attributes for the events.
**ocel.get_global_object(log_object)**: gets the global values of attributes for the objects.

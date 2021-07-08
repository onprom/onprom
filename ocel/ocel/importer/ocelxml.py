from datetime import datetime
from lxml import etree, objectify


def parse_xml(value, tag_str_lower):
    if "float" in tag_str_lower:
        return float(value)
    elif "date" in tag_str_lower:
        return datetime.fromisoformat(value)
    return str(value)


def apply(input_path, parameters=None):
    if parameters is None:
        parameters = {}

    parser = etree.XMLParser(remove_comments=True)
    tree = objectify.parse(input_path, parser=parser)
    root = tree.getroot()

    log = {}
    log["ocel:events"] = {}
    log["ocel:objects"] = {}

    for child in root:
        if child.tag.lower().endswith("global"):
            scope = child.get("scope")
            if scope == "event":
                log["ocel:global-event"] = {}
                for child2 in child:
                    log["ocel:global-event"][child2.get("key")] = child2.get("value")
            elif scope == "object":
                log["ocel:global-object"] = {}
                for child2 in child:
                    log["ocel:global-object"][child2.get("key")] = child2.get("value")
            elif scope == "log":
                log["ocel:global-log"] = {}
                for child2 in child:
                    if child2.get("key") == "attribute-names":
                        log["ocel:global-log"]["ocel:attribute-names"] = []
                        for child3 in child2:
                            log["ocel:global-log"]["ocel:attribute-names"].append(child3.get("value"))
                    elif child2.get("key") == "object-types":
                        log["ocel:global-log"]["ocel:object-types"] = []
                        for child3 in child2:
                            log["ocel:global-log"]["ocel:object-types"].append(child3.get("value"))
                    elif child2.get("key") == "version":
                        log["ocel:global-log"]["ocel:version"] = child2.get("value")
                    elif child2.get("key") == "ordering":
                        log["ocel:global-log"]["ocel:ordering"] = child2.get("value")

        if child.tag.lower().endswith("events"):
            for event in child:
                eve = {}
                for child2 in event:
                    if child2.get("key") == "id":
                        eve["ocel:id"] = child2.get("value")
                    elif child2.get("key") == "timestamp":
                        eve["ocel:timestamp"] = datetime.fromisoformat(child2.get("value"))
                    elif child2.get("key") == "activity":
                        eve["ocel:activity"] = child2.get("value")
                    elif child2.get("key") == "omap":
                        omap = []
                        for child3 in child2:
                            omap.append(child3.get("value"))
                        eve["ocel:omap"] = omap
                    elif child2.get("key") == "vmap":
                        eve["ocel:vmap"] = {}
                        for child3 in child2:
                            eve["ocel:vmap"][child3.get("key")] = parse_xml(child3.get("value"), child3.tag.lower())
                log["ocel:events"][eve["ocel:id"]] = eve
                del eve["ocel:id"]
        elif child.tag.lower().endswith("objects"):
            for object in child:
                obj = {}
                for child2 in object:
                    if child2.get("key") == "id":
                        obj["ocel:id"] = child2.get("value")
                    elif child2.get("key") == "type":
                        obj["ocel:type"] = child2.get("value")
                    elif child2.get("key") == "ovmap":
                        obj["ocel:ovmap"] = {}
                        for child3 in child2:
                            obj["ocel:ovmap"][child3.get("key")] = parse_xml(child3.get("value"), child3.tag.lower())
                log["ocel:objects"][obj["ocel:id"]] = obj
                del obj["ocel:id"]

    return log

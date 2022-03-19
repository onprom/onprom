from lxml import etree


def get_type(t0):
    if "float" in str(t0).lower() or "double" in str(t0).lower():
        return "float"
    elif "object" in str(t0).lower():
        return "string"
    else:
        return "string"


def apply(log, output_path, parameters=None):
    if parameters is None:
        parameters = {}

    prefix = "ocel:"

    root = etree.Element("log")

    global_event = etree.SubElement(root, "global")
    global_event.set("scope", "event")
    for k, v in log[prefix +"global-event"].items():
        child = etree.SubElement(global_event, "string")
        child.set("key", k.split(prefix)[-1])
        child.set("value", v)

    global_object = etree.SubElement(root, "global")
    global_object.set("scope", "event")

    for k, v in log[prefix +"global-object"].items():
        child = etree.SubElement(global_object, "string")
        child.set("key", k.split(prefix)[-1])
        child.set("value", v)

    global_log = etree.SubElement(root, "global")
    global_log.set("scope", "log")
    attribute_names = etree.SubElement(global_log, "list")
    attribute_names.set("key", "attribute-names")
    object_types = etree.SubElement(global_log, "list")
    object_types.set("key", "object-types")
    for k in log[prefix +"global-log"][prefix +"attribute-names"]:
        subel = etree.SubElement(attribute_names, "string")
        subel.set("key", "attribute-name")
        subel.set("value", k)
    for k in log[prefix +"global-log"][prefix +"object-types"]:
        subel = etree.SubElement(object_types, "string")
        subel.set("key", "object-type")
        subel.set("value", k)
    if prefix + "version" in log[prefix + "global-log"]:
        version = etree.SubElement(global_log, "string")
        version.set("key", "version")
        version.set("value", log[prefix + "global-log"][prefix + "version"])
    if prefix + "ordering" in log[prefix + "global-log"]:
        ordering = etree.SubElement(global_log, "string")
        ordering.set("key", "ordering")
        ordering.set("value", log[prefix + "global-log"][prefix + "ordering"])
    events = etree.SubElement(root, "events")
    for k, v in log[prefix + "events"].items():
        event = etree.SubElement(events, "event")
        event_id = etree.SubElement(event, "string")
        event_id.set("key", "id")
        event_id.set("value", str(k))
        event_activity = etree.SubElement(event, "string")
        event_activity.set("key", "activity")
        event_activity.set("value", v[prefix+"activity"])
        event_timestamp = etree.SubElement(event, "date")
        event_timestamp.set("key", "timestamp")
        event_timestamp.set("value", v[prefix+"timestamp"].isoformat())
        event_omap = etree.SubElement(event, "list")
        event_omap.set("key", "omap")
        for k2 in v[prefix+"omap"]:
            obj = etree.SubElement(event_omap, "string")
            obj.set("key", "object-id")
            obj.set("value", k2)
        event_vmap = etree.SubElement(event, "list")
        event_vmap.set("key", "vmap")
        for k2, v2 in v[prefix+"vmap"].items():
            attr = etree.SubElement(event_vmap, get_type(v2))
            attr.set("key", k2)
            attr.set("value", str(v2))
    objects = etree.SubElement(root, "objects")
    for k, v in log[prefix+"objects"].items():
        object = etree.SubElement(objects, "object")
        object_id = etree.SubElement(object, "string")
        object_id.set("key", "id")
        object_id.set("value", str(k))
        object_type = etree.SubElement(object, "string")
        object_type.set("key", "type")
        object_type.set("value", v[prefix+"type"])
        object_ovmap = etree.SubElement(object, "list")
        object_ovmap.set("key", "ovmap")
        for k2, v2 in v[prefix+"ovmap"].items():
            if str(v2).lower() != "nan" and str(v2).lower() != "nat":
                object_att = etree.SubElement(object_ovmap, get_type(v2))
                object_att.set("key", k2)
                object_att.set("value", str(v2))

    tree = etree.ElementTree(root)
    tree.write(output_path, pretty_print=True, xml_declaration=True, encoding="utf-8")

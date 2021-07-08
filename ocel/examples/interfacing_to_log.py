import ocel


def execute_script():
    log = ocel.import_log("../logs/minimal.jsonocel")
    print("version of the log:")
    print(ocel.get_version(log))
    print("attribute names:")
    print(ocel.get_attribute_names(log))
    print("object types:")
    print(ocel.get_object_types(log))
    print("events:")
    print(ocel.get_events(log))
    print("objects:")
    print(ocel.get_objects(log))
    print("global of event:")
    print(ocel.get_global_event(log))
    print("global of object:")
    print(ocel.get_global_object(log))


if __name__ == "__main__":
    execute_script()

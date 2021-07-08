def get_events(log):
    return log["ocel:events"]


def get_objects(log):
    return log["ocel:objects"]


def get_attribute_names(log):
    return log["ocel:global-log"]["ocel:attribute-names"]


def get_object_types(log):
    return log["ocel:global-log"]["ocel:object-types"]


def get_version(log):
    return log["ocel:global-log"]["ocel:version"]


def get_global_event(log):
    return log["ocel:global-event"]


def get_global_object(log):
    return log["ocel:global-object"]

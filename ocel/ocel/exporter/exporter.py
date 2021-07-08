from ocel.exporter import oceljson, ocelxml


def apply(log, output_path, parameters=None):
    if ".json" in output_path:
        return oceljson.apply(log, output_path, parameters=parameters)
    else:
        return ocelxml.apply(log, output_path, parameters=parameters)

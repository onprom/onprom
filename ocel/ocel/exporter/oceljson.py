import datetime
import json


def myconverter(o):
    if isinstance(o, datetime.datetime):
        return o.isoformat()


def apply(log, output_path, parameters=None):
    if parameters is None:
        parameters = {}

    json.dump(log, open(output_path, "w"), indent=2, default=myconverter)

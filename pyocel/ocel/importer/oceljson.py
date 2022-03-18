import json
from datetime import datetime


def apply(input_path, parameters=None):
    if parameters is None:
        parameters = {}
    log_obj = json.load(open(input_path, "rb"))
    for ek in log_obj["ocel:events"]:
        eve = log_obj["ocel:events"][ek]
        eve["ocel:timestamp"] = datetime.fromisoformat(eve["ocel:timestamp"])
    return log_obj

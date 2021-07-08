import json
import jsonschema
from jsonschema import validate


def apply(input_path, validation_path, parameters=None):
    if parameters is None:
        parameters = {}
    file_content = json.load(open(input_path, "rb"))
    schema_content = json.load(open(validation_path, "rb"))
    try:
        validate(instance=file_content, schema=schema_content)
        return True
    except jsonschema.exceptions.ValidationError as err:
        return False

import lxml


def apply(input_path, validation_path, parameters=None):
    xml_file = lxml.etree.parse(input_path)
    xml_validator = lxml.etree.XMLSchema(file=validation_path)
    is_valid = xml_validator.validate(xml_file)
    return is_valid

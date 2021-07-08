from ocel import exporter, importer, validation
from ocel.interface import *


__version__ = '0.0.3.1'
__doc__ = "OCEL (object-centric event log) support for Python"
__author__ = 'PADS'
__author_email__ = 'a.berti@pads.rwth-aachen.de'
__maintainer__ = 'PADS'
__maintainer_email__ = "a.berti@pads.rwth-aachen.de"


def import_log(log_path):
    return importer.importer.apply(log_path)


def export_log(log, log_path):
    return exporter.exporter.apply(log, log_path)


def validate(log_path, schema_path):
    return validation.validate.apply(log_path, schema_path)

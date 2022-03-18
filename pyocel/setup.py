from os.path import dirname, join

import ocel
from setuptools import setup


def read_file(filename):
    with open(join(dirname(__file__), filename)) as f:
        return f.read()


setup(
    name="ocel-standard",
    version=ocel.__version__,
    description=ocel.__doc__.strip(),
    long_description=read_file('README.md'),
    author=ocel.__author__,
    author_email=ocel.__author_email__,
    py_modules=[ocel.__name__],
    include_package_data=True,
    packages=['ocel', 'ocel.exporter', 'ocel.importer', 'ocel.validation'],
    url='http://www.pm4py.org',
    license='GPL-3.0',
    install_requires=[
        "jsonschema",
        "lxml"
    ]
)

# onprom Python Client

A Python wrapper for interacting with the `onprom` Ontology-Based Data Access (OBDA) log extraction engine via Py4J.

## Prerequisites
* **Java 21+** (to run the `pythoninterface` gateway server)
* **Python 3.12+**

## Installation

1. Build the Java gateway JAR from the repository root:
   ```bash
   mvn clean package -DskipTests
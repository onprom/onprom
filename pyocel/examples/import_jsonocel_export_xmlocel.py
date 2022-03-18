import ocel


def execute_script():
    print("validated input", ocel.validate("../logs/minimal.jsonocel", "../schemas/schema.json"))
    log = ocel.import_log("../logs/minimal.jsonocel")
    ocel.export_log(log, "log1.xmlocel")
    print("validated output", ocel.validate("log1.xmlocel", "../schemas/schema.xml"))
    ocel.import_log("log1.xmlocel")


if __name__ == "__main__":
    execute_script()

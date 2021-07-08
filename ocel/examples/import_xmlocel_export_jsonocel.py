import ocel


def execute_script():
    print("validated input", ocel.validate("../logs/minimal.xmlocel", "../schemas/schema.xml"))
    log = ocel.import_log("../logs/minimal.xmlocel")
    ocel.export_log(log, "log2.jsonocel")
    print("validated output", ocel.validate("log2.jsonocel", "../schemas/schema.json"))
    ocel.import_log("log2.jsonocel")


if __name__ == "__main__":
    execute_script()

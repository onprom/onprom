import ocel


def execute_script():
    print("validated input", ocel.validate("../logs/minimal.jsonocel", "../schemas/schema.json"))
    log = ocel.import_log("../logs/minimal.jsonocel")
    ocel.export_log(log, "log1.jsonocel")
    print("validated output", ocel.validate("log1.jsonocel", "../schemas/schema.json"))
    ocel.import_log("log1.jsonocel")


if __name__ == "__main__":
    execute_script()

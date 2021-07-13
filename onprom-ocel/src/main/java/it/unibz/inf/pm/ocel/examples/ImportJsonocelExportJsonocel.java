package it.unibz.inf.pm.ocel.examples;

import it.unibz.inf.pm.ocel.OcelInitial;
import org.dom4j.DocumentException;

public class ImportJsonocelExportJsonocel {

//    def execute_script():
//    print("validated input", ocel.validate("../logs/minimal.jsonocel", "../schemas/schema.json"))
//    log = ocel.import_log("../logs/minimal.jsonocel")
//            ocel.export_log(log, "log1.jsonocel")
//    print("validated output", ocel.validate("log1.jsonocel", "../schemas/schema.json"))
//            ocel.import_log("log1.jsonocel")

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("validated input:");
            OcelInitial.validate("ocel/logs/minimal.jsonocel", "ocel/schemas/schema.json");
            Object log = OcelInitial.import_log("ocel/logs/minimal.jsonocel");
            OcelInitial.export_log(log,"ocel/examples/log1.jsonocel");

            System.out.println("\nvalidated output:");
            OcelInitial.validate("ocel/examples/log1.jsonocel", "ocel/schemas/schema.json");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}

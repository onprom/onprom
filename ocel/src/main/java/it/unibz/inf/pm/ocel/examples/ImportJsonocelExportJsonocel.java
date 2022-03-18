package it.unibz.inf.pm.ocel.examples;

import it.unibz.inf.pm.ocel.OcelInitial;
import org.dom4j.DocumentException;

public class ImportJsonocelExportJsonocel {

    public static void main(String[] args) throws Exception {
        try {
            System.out.print("validated input:");
            OcelInitial.validate("ocel/logs/minimal.jsonocel", "ocel/schemas/schema.json");
            Object log = OcelInitial.import_log("ocel/logs/minimal.jsonocel");
            OcelInitial.export_log(log,"ocel/examples/log1.jsonocel");

            System.out.print("\nvalidated output:");
            OcelInitial.validate("ocel/examples/log1.jsonocel", "ocel/schemas/schema.json");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}

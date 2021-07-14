package it.unibz.inf.pm.ocel.examples;

import it.unibz.inf.pm.ocel.OcelInitial;
import org.dom4j.DocumentException;

import java.util.Map;

public class ImportXmlocelExportJsonocel {

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("validated input:");
            OcelInitial.validate("ocel/logs/minimal.xmlocel", "ocel/schemas/schema.xml");
            Map log = (Map)OcelInitial.import_log("ocel/logs/minimal.xmlocel");
            OcelInitial.export_log(log,"ocel/examples/log2.jsonocel");

            System.out.println("\nvalidated output:");
            OcelInitial.validate("ocel/examples/log2.jsonocel", "ocel/schemas/schema.json");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}

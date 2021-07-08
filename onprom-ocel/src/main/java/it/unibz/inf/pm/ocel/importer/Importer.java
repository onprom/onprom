package it.unibz.inf.pm.ocel.importer;

import org.dom4j.DocumentException;

import java.util.Map;

public class Importer {
    public static Map apply(String input_path, String ... parameters) throws DocumentException {
        if (input_path.indexOf(".json") != -1){
            return Oceljson.apply(input_path);
        }else{
            return Ocelxml.apply(input_path);
        }
    }
}

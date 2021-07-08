package it.unibz.inf.pm.ocel.exporter;

import org.dom4j.DocumentException;

import java.util.Map;

public class Exporter {
    public static void apply(Map log, String output_path,String ... parameters) throws DocumentException {
        if (output_path.indexOf(".json") != -1){
            Oceljson.apply(output_path);
        }else{
            Ocelxml.apply(log, output_path);
        }
    }
}

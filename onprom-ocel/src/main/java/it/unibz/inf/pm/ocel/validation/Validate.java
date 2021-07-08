package it.unibz.inf.pm.ocel.validation;


import org.dom4j.DocumentException;

import java.util.Map;

public class Validate {
    public static boolean apply(String input_path, String validation_path, String parameters) throws DocumentException {
        if (input_path.indexOf(".json") != -1){
            return Oceljson.apply(input_path, validation_path, parameters);
        }else{
            return Ocelxml.apply(input_path,validation_path, parameters);
        }
    }
}

package it.unibz.inf.pm.ocel.validation;


import org.dom4j.DocumentException;

import java.io.IOException;

public class Validate {
    public static boolean apply(String input_path, String validation_path, String parameters) throws DocumentException, IOException {
        if (input_path.indexOf(".json") != -1){
            return Oceljson.apply(input_path, validation_path, parameters);
        }else{
            return Ocelxml.apply(input_path,validation_path, parameters);
        }
    }
}

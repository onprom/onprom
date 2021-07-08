package it.unibz.inf.pm.ocel.validation;

import it.unibz.inf.pm.ocel.util.XMLValidateUtil;

public class Ocelxml {
    public static boolean apply(String input_path, String validation_path, String parameters){
        return XMLValidateUtil.validateXMLByXSD(input_path,validation_path,parameters);
    }
}

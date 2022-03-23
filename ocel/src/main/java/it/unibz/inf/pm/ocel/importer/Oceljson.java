package it.unibz.inf.pm.ocel.importer;

import it.unibz.inf.pm.ocel.util.JsonUtil;

import java.io.IOException;

public class Oceljson {

    public static Object apply(String input_path, String ... parameters) throws IOException {
        if(parameters.length == 0)
        {
            return JsonUtil.readJsonfileToObject(input_path);
        }else{
            return JsonUtil.readJsonToMap(input_path);
        }


    }
}

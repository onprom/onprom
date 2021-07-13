package it.unibz.inf.pm.ocel.validation;

import com.fasterxml.jackson.databind.JsonNode;
import it.unibz.inf.pm.ocel.util.JsonSchemaUtil;
import it.unibz.inf.pm.ocel.util.JsonUtil;

import java.io.IOException;

public class Oceljson {
    public static boolean apply(String input_path, String validation_path, String ... parameters) throws IOException {
        JsonNode jsonNode = JsonSchemaUtil.strToJsonNode(JsonUtil.readJsonfileToObject(input_path).toJSONString());
        JsonNode schemaNode = JsonSchemaUtil.schemaToJsonNode(validation_path);
        if(JsonSchemaUtil.validateJson(jsonNode,schemaNode))
        {
            return true;
        }else {
            return false;
        }
    }
}

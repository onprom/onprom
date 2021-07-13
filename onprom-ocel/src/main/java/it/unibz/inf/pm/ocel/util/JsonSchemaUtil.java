package it.unibz.inf.pm.ocel.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class JsonSchemaUtil {

    /**
     * @param jsonStr validate json string
     */
    public static JsonNode strToJsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = JsonLoader.fromString(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }


    /**
     * @param jsonFilePath jsonSchema file path
     */
    public static JsonNode schemaToJsonNode(String jsonFilePath) {
        JsonNode jsonSchemaNode = null;
        try {
            jsonSchemaNode = new JsonNodeReader().fromReader(new FileReader(ResourceUtils.getFile(jsonFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonSchemaNode;
    }

    /**
     * @param jsonNode   json data node
     * @param schemaNode jsonSchema constrain node
     */
    public static boolean validateJson(JsonNode jsonNode, JsonNode schemaNode) {
        //fge validate weather the json data fit json schema
        ProcessingReport report = JsonSchemaFactory.byDefault().getValidator().validateUnchecked(schemaNode, jsonNode);
        if (report.isSuccess()) {
            return true;
        } else {
            Iterator<ProcessingMessage> it = report.iterator();
            StringBuilder ms = new StringBuilder();
            ms.append("json format error: ");
            while (it.hasNext()) {
                ProcessingMessage pm = it.next();
                if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                    ms.append(pm);
                }
            }
            System.err.println(ms);
            return false;
        }
    }
}

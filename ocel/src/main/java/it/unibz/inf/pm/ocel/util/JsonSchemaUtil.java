/*
 * ocel
 *
 * JsonSchemaUtil.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.pm.ocel.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

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
        try {
            return new JsonNodeReader().fromInputStream(JsonNode.class.getResourceAsStream(jsonFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param jsonNode   json data node
     * @param schemaNode jsonSchema constrain node
     */
    public static boolean validateJson(JsonNode jsonNode, JsonNode schemaNode) {
        //fge validate weather the json data fit json schema
        ProcessingReport report = JsonSchemaFactory.byDefault().getValidator().validateUnchecked(schemaNode, jsonNode);
        if (report.isSuccess()) {
            System.out.println("True");
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

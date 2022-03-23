/*
 * ocel
 *
 * Oceljson.java
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

package it.unibz.inf.pm.ocel.validation;

import com.fasterxml.jackson.databind.JsonNode;
import it.unibz.inf.pm.ocel.util.JsonSchemaUtil;
import it.unibz.inf.pm.ocel.util.JsonUtil;

import java.io.IOException;

public class Oceljson {
    public static boolean apply(String input_path, String validation_path, String ... parameters) throws IOException {
        JsonNode jsonNode = JsonSchemaUtil.strToJsonNode(JsonUtil.readJsonFile(input_path));
        JsonNode schemaNode = JsonSchemaUtil.schemaToJsonNode(validation_path);
        return JsonSchemaUtil.validateJson(jsonNode, schemaNode);
    }
}

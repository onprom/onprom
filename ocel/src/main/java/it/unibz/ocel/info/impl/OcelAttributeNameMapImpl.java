/*
 * ocel
 *
 * OcelAttributeNameMapImpl.java
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

package it.unibz.ocel.info.impl;

import it.unibz.ocel.info.OcelAttributeNameMap;
import it.unibz.ocel.model.OcelAttribute;

import java.util.HashMap;


public class OcelAttributeNameMapImpl implements OcelAttributeNameMap {
    private final String name;
    private HashMap<String, String> mapping;

    public OcelAttributeNameMapImpl(String name) {
        this.name = name;
        this.mapping = new HashMap();
    }

    public String getMappingName() {
        return this.name;
    }

    public String map(OcelAttribute attribute) {
        return this.map(attribute.getKey());
    }

    public String map(String attributeKey) {
        return this.mapping.get(attributeKey);
    }

    public void registerMapping(OcelAttribute attribute, String alias) {
        this.registerMapping(attribute.getKey(), alias);
    }

    public void registerMapping(String attributeKey, String alias) {
        this.mapping.put(attributeKey, alias);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attribute name map: ");
        sb.append(this.name);

        for (String key : this.mapping.keySet()) {
            sb.append("\n");
            sb.append(key);
            sb.append(" -> ");
            sb.append(this.mapping.get(key));
        }

        return sb.toString();
    }
}


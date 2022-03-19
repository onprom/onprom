/*
 * ocel
 *
 * OcelGlobalAttributeNameMap.java
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

package it.unibz.ocel.info;

import it.unibz.ocel.info.impl.OcelAttributeNameMapImpl;
import it.unibz.ocel.model.OcelAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class OcelGlobalAttributeNameMap implements OcelAttributeNameMap {
    public static final String MAPPING_STANDARD = "EN";
    public static final String MAPPING_ENGLISH = "EN";
    public static final String MAPPING_GERMAN = "DE";
    public static final String MAPPING_DUTCH = "NL";
    public static final String MAPPING_FRENCH = "FR";
    public static final String MAPPING_ITALIAN = "IT";
    public static final String MAPPING_SPANISH = "ES";
    public static final String MAPPING_PORTUGUESE = "PT";
    private static final OcelGlobalAttributeNameMap singleton = new OcelGlobalAttributeNameMap();
    private HashMap<String, OcelAttributeNameMapImpl> mappings = new HashMap();
    private OcelAttributeNameMapImpl standardMapping = new OcelAttributeNameMapImpl("EN");

    private OcelGlobalAttributeNameMap() {
        this.mappings.put("EN", this.standardMapping);
    }

    public static OcelGlobalAttributeNameMap instance() {
        return singleton;
    }

    public Collection<String> getAvailableMappingNames() {
        return Collections.unmodifiableCollection(this.mappings.keySet());
    }

    public Collection<OcelAttributeNameMap> getAvailableMappings() {
        HashSet<OcelAttributeNameMap> result = new HashSet(this.mappings.values());
        return Collections.unmodifiableCollection(result);
    }

    public OcelAttributeNameMap getMapping(String name) {
        OcelAttributeNameMapImpl mapping = this.mappings.get(name);
        if (mapping == null) {
            mapping = new OcelAttributeNameMapImpl(name);
            this.mappings.put(name, mapping);
        }

        return mapping;
    }

    public OcelAttributeNameMap getStandardMapping() {
        return this.standardMapping;
    }

    public String mapSafely(OcelAttribute attribute, OcelAttributeNameMap mapping) {
        return this.mapSafely(attribute.getKey(), mapping);
    }

    public String mapSafely(String attributeKey, OcelAttributeNameMap mapping) {
        String alias = null;
        if (mapping != null) {
            alias = mapping.map(attributeKey);
        }

        if (alias == null) {
            alias = this.standardMapping.map(attributeKey);
        }

        if (alias == null) {
            alias = attributeKey;
        }

        return alias;
    }

    public String mapSafely(OcelAttribute attribute, String mappingName) {
        return this.mapSafely(attribute, this.mappings.get(mappingName));
    }

    public String mapSafely(String attributeKey, String mappingName) {
        return this.mapSafely(attributeKey, this.mappings.get(mappingName));
    }

    public void registerMapping(String mappingName, String attributeKey, String alias) {
        OcelAttributeNameMapImpl mapping = (OcelAttributeNameMapImpl)this.getMapping(mappingName);
        mapping.registerMapping(attributeKey, alias);
    }

    public String getMappingName() {
        return "EN";
    }

    public String map(OcelAttribute attribute) {
        return this.standardMapping.map(attribute);
    }

    public String map(String attributeKey) {
        return this.standardMapping.map(attributeKey);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Global attribute name map.\n\nContained maps:\n\n");

        for (OcelAttributeNameMapImpl map : this.mappings.values()) {
            sb.append(map.toString());
            sb.append("\n\n");
        }

        return sb.toString();
    }
}

/*
 * ocel
 *
 * OcelAttributeMapImpl.java
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

package it.unibz.ocel.model.impl;

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;

import java.util.HashMap;
import java.util.Map;


public class OcelAttributeMapImpl extends HashMap<String, OcelAttribute> implements OcelAttributeMap {
    private static final long serialVersionUID = 2701256420845748051L;

    public OcelAttributeMapImpl() {
        this(0);
    }

    public OcelAttributeMapImpl(int size) {
        super(size);
    }

    public OcelAttributeMapImpl(Map<String, OcelAttribute> template) {
        super(template.size());

        for (String key : template.keySet()) {
            this.put(key, template.get(key));
        }

    }

    public Object clone() {
        OcelAttributeMapImpl clone = new OcelAttributeMapImpl(this.size());

        for (OcelAttribute value : this.values()) {
            clone.put(value.getKey(), (OcelAttribute) value.clone());
        }

        return clone;
    }
}

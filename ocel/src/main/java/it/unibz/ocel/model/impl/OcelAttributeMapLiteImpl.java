/*
 * ocel
 *
 * OcelAttributeMapLiteImpl.java
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

import com.google.common.collect.ForwardingMap;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.Serializable;
import java.util.Map;


public final class OcelAttributeMapLiteImpl extends ForwardingMap<String, OcelAttribute> implements OcelAttributeMap, Serializable {
    private static final long serialVersionUID = 6928328637653669033L;
    private Map<String, OcelAttribute> attributeMap;

    public OcelAttributeMapLiteImpl() {
        this.attributeMap = this.createInternalMap();
    }

    public OcelAttributeMapLiteImpl(int initialSize) {
        this.attributeMap = this.createInternalMap(initialSize);
    }

    private Map<String, OcelAttribute> createInternalMap() {
        return new Object2ObjectOpenHashMap(4);
    }

    private Map<String, OcelAttribute> createInternalMap(int initialSize) {
        return new Object2ObjectOpenHashMap(initialSize);
    }

    protected Map<String, OcelAttribute> delegate() {
        return this.attributeMap;
    }

    public Object clone() {
        OcelAttributeMapLiteImpl clone = new OcelAttributeMapLiteImpl();
        if (!this.isEmpty()) {
            clone.attributeMap = this.createInternalMap(this.size());

            for (OcelAttribute attr : this.values()) {
                clone.attributeMap.put(attr.getKey(), (OcelAttribute) attr.clone());
            }

        }
        return clone;
    }
}

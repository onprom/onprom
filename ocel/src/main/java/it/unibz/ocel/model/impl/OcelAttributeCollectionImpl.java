/*
 * ocel
 *
 * OcelAttributeCollectionImpl.java
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

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeCollection;

import java.util.Collection;

public abstract class OcelAttributeCollectionImpl extends OcelAttributeLiteralImpl implements OcelAttributeCollection {
    private static final long serialVersionUID = 4322597532345796274L;
    protected Collection<OcelAttribute> collection;

    public OcelAttributeCollectionImpl(String key) {
        super(key, "", null);
    }

    public OcelAttributeCollectionImpl(String key, OcelExtension extension) {
        super(key, "", extension);
    }

    public void addToCollection(OcelAttribute attribute) {
        if (this.collection != null) {
            this.collection.add(attribute);
        } else {
            throw new NullPointerException("Cannot add attribute to collection that is null");
        }
    }

    public void removeFromCollection(OcelAttribute attribute) {
        if (this.collection != null) {
            this.collection.remove(attribute);
        }

    }

    public Collection<OcelAttribute> getCollection() {
        return this.collection != null ? this.collection : this.getAttributes().values();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        String sep = "[";

        for (OcelAttribute attribute : this.getCollection()) {
            buf.append(sep);
            sep = ",";
            buf.append(attribute.getKey());
            buf.append(":");
            buf.append(attribute);
        }

        if (buf.length() == 0) {
            buf.append("[");
        }

        buf.append("]");
        return buf.toString();
    }

    public Object clone() {
        return super.clone();
    }
}

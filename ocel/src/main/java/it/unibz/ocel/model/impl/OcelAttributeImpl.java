/*
 * ocel
 *
 * OcelAttributeImpl.java
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
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelAttributeUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public abstract class OcelAttributeImpl implements OcelAttribute {
    private static final long serialVersionUID = 2570374546119649178L;
    private final String key;
    private final OcelExtension extension;
    private OcelAttributeMap attributes;

    protected OcelAttributeImpl(String key) {
        this(key, null);
    }

    protected OcelAttributeImpl(String key, OcelExtension extension) {
        this.key = key;
        this.extension = extension;
    }

    public String getKey() {
        return this.key;
    }

    public OcelExtension getExtension() {
        return this.extension;
    }

    public OcelAttributeMap getAttributes() {
        if (this.attributes == null) {
            this.attributes = new OcelAttributeMapLazyImpl(OcelAttributeMapImpl.class);
        }

        return this.attributes;
    }

    public void setAttributes(OcelAttributeMap attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttributes() {
        return this.attributes != null && !this.attributes.isEmpty();
    }

    public Set<OcelExtension> getExtensions() {
        return this.attributes != null ? OcelAttributeUtils.extractExtensions(this.getAttributes()) : Collections.emptySet();
    }

    public Object clone() {
        OcelAttributeImpl clone = null;

        try {
            clone = (OcelAttributeImpl)super.clone();
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            return null;
        }

        if (this.attributes != null) {
            clone.attributes = (OcelAttributeMap)this.getAttributes().clone();
        }

        return clone;
    }

    public boolean equals(Object obj) {
        if (obj instanceof OcelAttribute) {
            OcelAttribute other = (OcelAttribute)obj;
            return other.getKey().equals(this.key);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public int compareTo(OcelAttribute o) {
        return this.key.compareTo(o.getKey());
    }

    public void accept(OcelVisitor visitor, OcelAttributable parent) {
        visitor.visitAttributePre(this, parent);
        Iterator i$;
        OcelAttribute attribute;
        if (this instanceof OcelAttributeCollection) {
            i$ = ((OcelAttributeCollection)this).getCollection().iterator();

            while(i$.hasNext()) {
                attribute = (OcelAttribute)i$.next();
                attribute.accept(visitor, this);
            }
        } else if (this.attributes != null) {
            i$ = this.getAttributes().values().iterator();

            while(i$.hasNext()) {
                attribute = (OcelAttribute)i$.next();
                attribute.accept(visitor, this);
            }
        }

        visitor.visitAttributePost(this, parent);
    }
}

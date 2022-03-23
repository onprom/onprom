/*
 * ocel
 *
 * OcelObjectImpl.java
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
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.id.OcelIDFactory;
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelAttributeUtils;

import java.util.Set;

public class OcelObjectImpl implements OcelObject {
    private OcelID id;
    private OcelAttributeMap attributes;

    public OcelObjectImpl() {
        this(OcelIDFactory.instance().createId(), new OcelAttributeMapImpl());
    }

    public OcelObjectImpl(OcelID id) {
        this(id, new OcelAttributeMapImpl());
    }

    public OcelObjectImpl(OcelAttributeMap attributes) {
        this(OcelIDFactory.instance().createId(), attributes);
    }

    public OcelObjectImpl(OcelID id, OcelAttributeMap attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public OcelAttributeMap getAttributes() {
        return this.attributes;
    }

    public void setAttributes(OcelAttributeMap attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttributes() {
        return !this.attributes.isEmpty();
    }

    public Set<OcelExtension> getExtensions() {
        return OcelAttributeUtils.extractExtensions(this.attributes);
    }

    public Object clone() {
        OcelObjectImpl clone;
        try {
            clone = (OcelObjectImpl)super.clone();
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            return null;
        }

        clone.id = OcelIDFactory.instance().createId();
        clone.attributes = (OcelAttributeMap)this.attributes.clone();
        return clone;
    }

    public boolean equals(Object o) {
        return o instanceof OcelObjectImpl && ((OcelObjectImpl) o).id.equals(this.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public OcelID getID() {
        return this.id;
    }

    public void setID(OcelID id) {
        this.id = id;
    }

    public void accept(OcelVisitor visitor, OcelEvent event) {
        visitor.visitObjectPre(this, event);

        for (OcelAttribute attribute : this.attributes.values()) {
            attribute.accept(visitor, this);
        }

        visitor.visitObjectPost(this, event);
    }
}

/*
 * ocel
 *
 * OcelEventImpl.java
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


public class OcelEventImpl extends ArrayList<OcelObject> implements OcelEvent {
    private OcelID id;
    private OcelAttributeMap attributes;

    public OcelEventImpl() {
        this(OcelIDFactory.instance().createId(), new OcelAttributeMapImpl());
    }

    public OcelEventImpl(OcelID id) {
        this(id, new OcelAttributeMapImpl());
    }

    public OcelEventImpl(OcelAttributeMap attributes) {
        this(OcelIDFactory.instance().createId(), attributes);
    }

    public OcelEventImpl(OcelID id, OcelAttributeMap attributes) {
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
        OcelEventImpl clone;
        try {
            clone = (OcelEventImpl)super.clone();
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }

        clone.id = OcelIDFactory.instance().createId();
        clone.attributes = (OcelAttributeMap)this.attributes.clone();
        return clone;
    }

    public boolean equals(Object o) {
        return o instanceof OcelEventImpl && ((OcelEventImpl) o).id.equals(this.id);
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

    public int insertObject(OcelObject object) {
        if (this.size() == 0) {
            this.add(object);
            return 0;
        } else {
            OcelAttribute insTsAttr = object.getAttributes().get("id");
            if (insTsAttr == null) {
                this.add(object);
                return this.size() - 1;
            } else {
                Date insTs = ((OcelAttributeTimestamp)insTsAttr).getValue();

                for(int i = this.size() - 1; i >= 0; --i) {
                    OcelAttribute refTsAttr = this.get(i).getAttributes().get("id");
                    if (refTsAttr == null) {
                        this.add(object);
                        return this.size() - 1;
                    }

                    Date refTs = ((OcelAttributeTimestamp)refTsAttr).getValue();
                    if (!insTs.before(refTs)) {
                        this.add(i + 1, object);
                        return i + 1;
                    }
                }

                this.add(0, object);
                return 0;
            }
        }
    }

    @Override
    public void accept(OcelVisitor visitor, OcelLog log) {
        visitor.visitLogPre(log);

        for (OcelAttribute attribute : this.attributes.values()) {
            attribute.accept(visitor, this);
        }

        visitor.visitLogPost(log);
    }

    public void accept(OcelVisitor visitor, OcelTrace trace) {
        visitor.visitEventPre(this, trace);

        for (OcelAttribute attribute : this.attributes.values()) {
            attribute.accept(visitor, this);
        }

        visitor.visitEventPost(this, trace);
    }
}

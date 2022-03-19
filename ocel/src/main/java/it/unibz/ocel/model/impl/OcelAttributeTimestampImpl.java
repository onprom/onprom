/*
 * ocel
 *
 * OcelAttributeTimestampImpl.java
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
import it.unibz.ocel.model.OcelAttributeTimestamp;

import java.util.Date;


public class OcelAttributeTimestampImpl extends OcelAttributeImpl implements OcelAttributeTimestamp {
    private static final long serialVersionUID = -4627152242051009472L;
    private Date value;

    public OcelAttributeTimestampImpl(String key, Date value) {
        this(key, value, null);
    }

    public OcelAttributeTimestampImpl(String key, Date value, OcelExtension extension) {
        super(key, extension);
        this.setValue(value);
    }

    public OcelAttributeTimestampImpl(String key, long millis) {
        this(key, millis, null);
    }

    public OcelAttributeTimestampImpl(String key, long millis, OcelExtension extension) {
        this(key, new Date(millis), extension);
    }

    public Date getValue() {
        return this.value;
    }

    public void setValue(Date value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in timestamp attribute!");
        } else {
            this.value = value;
        }
    }

    public long getValueMillis() {
        return this.value.getTime();
    }

    public void setValueMillis(long value) {
        this.value.setTime(value);
    }

    public String toString() {
        synchronized(FORMATTER) {
            return FORMATTER.format(this.value);
        }
    }

    public Object clone() {
        OcelAttributeTimestampImpl clone = (OcelAttributeTimestampImpl)super.clone();
        clone.value = new Date(clone.value.getTime());
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeTimestamp)) {
            return false;
        } else {
            OcelAttributeTimestamp other = (OcelAttributeTimestamp)obj;
            return super.equals(other) && this.value.equals(other.getValue());
        }
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeTimestamp)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : this.value.compareTo(((OcelAttributeTimestamp)other).getValue());
        }
    }
}


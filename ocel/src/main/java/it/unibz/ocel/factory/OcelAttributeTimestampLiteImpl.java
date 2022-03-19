/*
 * ocel
 *
 * OcelAttributeTimestampLiteImpl.java
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

package it.unibz.ocel.factory;

import com.google.common.primitives.Longs;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeTimestamp;
import it.unibz.ocel.model.impl.OcelAttributeImpl;

import java.util.Date;


final class OcelAttributeTimestampLiteImpl extends OcelAttributeImpl implements OcelAttributeTimestamp {
    private static final long serialVersionUID = -5883868464604814930L;
    private long value;

    public OcelAttributeTimestampLiteImpl(String key, Date value) {
        this(key, value, null);
    }

    public OcelAttributeTimestampLiteImpl(String key, Date value, OcelExtension extension) {
        this(key, value.getTime(), extension);
    }

    public OcelAttributeTimestampLiteImpl(String key, long millis) {
        this(key, millis, null);
    }

    public OcelAttributeTimestampLiteImpl(String key, long millis, OcelExtension extension) {
        super(key, extension);
        this.value = millis;
    }

    public Date getValue() {
        return new Date(this.value);
    }

    public void setValue(Date value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in timestamp attribute!");
        } else {
            this.value = value.getTime();
        }
    }

    public long getValueMillis() {
        return this.value;
    }

    public void setValueMillis(long value) {
        this.value = value;
    }

    public String toString() {
        synchronized(FORMATTER) {
            return FORMATTER.format(new Date(this.value));
        }
    }

    public Object clone() {
        OcelAttributeTimestampLiteImpl clone = (OcelAttributeTimestampLiteImpl)super.clone();
        clone.value = this.value;
        return clone;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OcelAttributeTimestamp)) {
            return false;
        } else {
            OcelAttributeTimestamp other = (OcelAttributeTimestamp)obj;
            return super.equals(other) && this.value == other.getValueMillis();
        }
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeTimestamp)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Longs.compare(this.value, ((OcelAttributeTimestamp)other).getValueMillis());
        }
    }
}

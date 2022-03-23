/*
 * ocel
 *
 * OcelAttributeLiteralImpl.java
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
import it.unibz.ocel.model.OcelAttributeLiteral;

import java.util.Objects;

public class OcelAttributeLiteralImpl extends OcelAttributeImpl implements OcelAttributeLiteral {
    private static final long serialVersionUID = -1844032762689490775L;
    private String value;

    public OcelAttributeLiteralImpl(String key, String value) {
        this(key, value, null);
    }

    public OcelAttributeLiteralImpl(String key, String value, OcelExtension extension) {
        super(key, extension);
        this.setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in literal attribute!");
        } else {
            this.value = value;
        }
    }

    public String toString() {
        return this.value;
    }

    public Object clone() {
        OcelAttributeLiteralImpl clone = (OcelAttributeLiteralImpl)super.clone();
        clone.value = this.value;
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeLiteral)) {
            return false;
        } else {
            OcelAttributeLiteral other = (OcelAttributeLiteral)obj;
            return super.equals(other) && this.value.equals(other.getValue());
        }
    }

    public int hashCode() {
        return Objects.hash(this.getKey(), this.value);
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeLiteral)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : this.value.compareTo(((OcelAttributeLiteral)other).getValue());
        }
    }
}

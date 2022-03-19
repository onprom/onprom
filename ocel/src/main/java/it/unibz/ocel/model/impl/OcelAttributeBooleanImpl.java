/*
 * ocel
 *
 * OcelAttributeBooleanImpl.java
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
import it.unibz.ocel.model.OcelAttributeBoolean;

import java.util.Objects;


public class OcelAttributeBooleanImpl extends OcelAttributeImpl implements OcelAttributeBoolean {
    private static final long serialVersionUID = -4696555899349337644L;
    private boolean value;

    public OcelAttributeBooleanImpl(String key, boolean value) {
        this(key, value, null);
    }

    public OcelAttributeBooleanImpl(String key, boolean value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String toString() {
        return this.value ? "true" : "false";
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeBoolean)) {
            return false;
        } else {
            OcelAttributeBoolean other = (OcelAttributeBoolean)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(this.getKey(), this.value);
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeBoolean)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Boolean.compare(this.value, ((OcelAttributeBoolean) other).getValue());
        }
    }
}


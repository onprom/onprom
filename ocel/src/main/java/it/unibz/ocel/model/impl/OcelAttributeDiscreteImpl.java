/*
 * ocel
 *
 * OcelAttributeDiscreteImpl.java
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
import it.unibz.ocel.model.OcelAttributeDiscrete;

import java.util.Objects;


public class OcelAttributeDiscreteImpl extends OcelAttributeImpl implements OcelAttributeDiscrete {
    private static final long serialVersionUID = 2209799959584107671L;
    private long value;

    public OcelAttributeDiscreteImpl(String key, long value) {
        this(key, value, null);
    }

    public OcelAttributeDiscreteImpl(String key, long value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String toString() {
        return Long.toString(this.value);
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeDiscrete)) {
            return false;
        } else {
            OcelAttributeDiscrete other = (OcelAttributeDiscrete)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(this.getKey(), this.value);
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeDiscrete)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Long.compare(this.value, ((OcelAttributeDiscrete) other).getValue());
        }
    }
}


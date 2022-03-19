/*
 * ocel
 *
 * OcelAttributeContinuousImpl.java
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
import it.unibz.ocel.model.OcelAttributeContinuous;

import java.util.Objects;


public class OcelAttributeContinuousImpl extends OcelAttributeImpl implements OcelAttributeContinuous {
    private static final long serialVersionUID = -1789813595800348876L;
    private double value;

    public OcelAttributeContinuousImpl(String key, double value) {
        this(key, value, null);
    }

    public OcelAttributeContinuousImpl(String key, double value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String toString() {
        return Double.toString(this.value);
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeContinuous)) {
            return false;
        } else {
            OcelAttributeContinuous other = (OcelAttributeContinuous)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(this.getKey(), this.value);
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeContinuous)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Double.compare(this.value, ((OcelAttributeContinuous) other).getValue());
        }
    }
}

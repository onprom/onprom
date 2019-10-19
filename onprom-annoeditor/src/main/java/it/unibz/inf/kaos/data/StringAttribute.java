/*
 * onprom-annoeditor
 *
 * StringAttribute.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
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
package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Attribute of annotations which may have a navigation to a string attribute in a class
 * or a static string value
 * <p>
 * @author T. E. Kalayci on 17/11/16.
 */
public class StringAttribute extends NavigationalAttribute implements Cloneable {
    private String value;

    public StringAttribute() {
        super();
    }

    public StringAttribute(String _value) {
        this();
        value = _value;
    }

    public StringAttribute(Set<DiagramShape> _path, UMLClass _cls, Attribute _attr) {
        super(_path, _cls, _attr);
        this.value = null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String _value) {
        reset();
        this.value = _value;
    }

    public String toString() {
        if (getAttribute() == null && value != null) {
            return value;
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StringAttribute) {
            StringAttribute other = (StringAttribute) object;
            if (other.getValue() != null && getValue() != null) {
                return other.getValue().equals(getValue());
            }
        }
        return super.equals(object);
    }
}
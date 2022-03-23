/*
 * onprom-dynamiceditor
 *
 * DynamicNavigationalAttribute.java
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

package it.unibz.inf.onprom.data;

import it.unibz.inf.onprom.interfaces.DiagramShape;

import java.util.Set;

/**
 * Created by T. E. Kalayci on 14-Nov-2017.
 */
public class DynamicNavigationalAttribute implements DynamicAttribute {
    private boolean partOfURI;
    private NavigationalAttribute attribute;

    DynamicNavigationalAttribute() {

    }

    public DynamicNavigationalAttribute(String value) {
        attribute = new StringAttribute(value);
    }

    public DynamicNavigationalAttribute(NavigationalAttribute navigationalAttribute) {
        attribute = navigationalAttribute;
    }

    public NavigationalAttribute getAttribute() {
        return attribute;
    }

    @Override
    public Set<DiagramShape> getPath() {
        return attribute.getPath();
    }

    @Override
    public void setPath(Set<DiagramShape> path) {
        attribute.setPath(path);
    }

    @Override
    public UMLClass getRelatedClass() {
        return attribute.getUmlClass();
    }

    @Override
    public boolean isPartOfURI() {
        return partOfURI;
    }

    @Override
    public void setPartOfURI(final boolean partOfURI) {
        this.partOfURI = partOfURI;
    }

    public String toString() {
        return attribute.toString();
    }
}

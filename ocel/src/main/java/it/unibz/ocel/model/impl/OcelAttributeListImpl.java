/*
 * ocel
 *
 * OcelAttributeListImpl.java
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
import it.unibz.ocel.model.OcelAttributeList;

import java.util.ArrayList;

public class OcelAttributeListImpl extends OcelAttributeCollectionImpl implements OcelAttributeList {
    private static final long serialVersionUID = 5584421551344100844L;

    public OcelAttributeListImpl(String key) {
        super(key, null);
        this.collection = new ArrayList();
    }

    public OcelAttributeListImpl(String key, OcelExtension extension) {
        super(key, extension);
        this.collection = new ArrayList();
    }

    public Object clone() {
        OcelAttributeListImpl clone = (OcelAttributeListImpl)super.clone();
        clone.collection = new ArrayList(this.collection);
        return clone;
    }
}


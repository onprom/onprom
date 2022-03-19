/*
 * ocel
 *
 * OcelAttributeContainerImpl.java
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
import it.unibz.ocel.model.OcelAttributeContainer;


public class OcelAttributeContainerImpl extends OcelAttributeCollectionImpl implements OcelAttributeContainer {
    private static final long serialVersionUID = -2171609637065248221L;

    public OcelAttributeContainerImpl(String key) {
        super(key, null);
    }

    public OcelAttributeContainerImpl(String key, OcelExtension extension) {
        super(key, extension);
        this.collection = null;
    }

    public Object clone() {
        OcelAttributeContainerImpl clone = (OcelAttributeContainerImpl)super.clone();
        clone.collection = null;
        return clone;
    }
}

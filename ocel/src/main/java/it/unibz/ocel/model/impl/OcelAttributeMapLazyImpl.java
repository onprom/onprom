/*
 * ocel
 *
 * OcelAttributeMapLazyImpl.java
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

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;

import java.util.*;


public class OcelAttributeMapLazyImpl<T extends OcelAttributeMap> implements OcelAttributeMap {
    private static final Set<Map.Entry<String, OcelAttribute>> EMPTY_ENTRYSET = Collections.unmodifiableSet(new HashSet(0));
    private static final Set<String> EMPTY_KEYSET = Collections.unmodifiableSet(new HashSet(0));
    private static final Collection<OcelAttribute> EMPTY_ENTRIES = Collections.unmodifiableCollection(new ArrayList(0));
    private Class<T> backingStoreClass;
    private T backingStore = null;

    public OcelAttributeMapLazyImpl(Class<T> implementingClass) {
        this.backingStoreClass = implementingClass;
        this.backingStore = null;
    }

    public Class<T> getBackingStoreClass() {
        return this.backingStoreClass;
    }

    public synchronized void clear() {
        this.backingStore = null;
    }

    public synchronized boolean containsKey(Object key) {
        return this.backingStore != null && this.backingStore.containsKey(key);
    }

    public synchronized boolean containsValue(Object value) {
        return this.backingStore != null && this.backingStore.containsValue(value);
    }

    public synchronized Set<Map.Entry<String, OcelAttribute>> entrySet() {
        return this.backingStore != null ? this.backingStore.entrySet() : EMPTY_ENTRYSET;
    }

    public synchronized OcelAttribute get(Object key) {
        return this.backingStore != null ? this.backingStore.get(key) : null;
    }

    public synchronized boolean isEmpty() {
        return this.backingStore == null || this.backingStore.isEmpty();
    }

    public synchronized Set<String> keySet() {
        return this.backingStore != null ? this.backingStore.keySet() : EMPTY_KEYSET;
    }

    public synchronized OcelAttribute put(String key, OcelAttribute value) {
        if (this.backingStore == null) {
            try {
//                this.backingStore = (OcelAttributeMap)this.backingStoreClass.newInstance();
                this.backingStore = this.backingStoreClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.backingStore.put(key, value);
    }

    public synchronized void putAll(Map<? extends String, ? extends OcelAttribute> t) {
        if (t.size() > 0) {
            if (this.backingStore == null) {
                try {
//                    this.backingStore = (OcelAttributeMap)this.backingStoreClass.newInstance();
                    this.backingStore = this.backingStoreClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            this.backingStore.putAll(t);
        }

    }

    public synchronized OcelAttribute remove(Object key) {
        return this.backingStore != null ? this.backingStore.remove(key) : null;
    }

    public synchronized int size() {
        return this.backingStore != null ? this.backingStore.size() : 0;
    }

    public synchronized Collection<OcelAttribute> values() {
        return this.backingStore != null ? this.backingStore.values() : EMPTY_ENTRIES;
    }

    public Object clone() {
        try {
            OcelAttributeMapLazyImpl<T> clone = (OcelAttributeMapLazyImpl)super.clone();
            if (this.backingStore != null) {
//                clone.backingStore = (OcelAttributeMap)this.backingStore.clone();
                clone.backingStore = (T) this.backingStore.clone();
            }

            return clone;
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
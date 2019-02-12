/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor.util;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class EfficientHashMap<V> {

    private TIntIntHashMap map;
    private ArrayList<V> values;

    public EfficientHashMap() {
        values = new ArrayList<>();
        map = new TIntIntHashMap();
    }


    public void put(String key, V value) {
        int index = values.size();
        this.map.put(key.hashCode(), index);
        this.values.add(value);
    }

    public boolean containsKey(String key) {
        return this.map.containsKey(key.hashCode());
    }

    public V get(String key) {
        return this.values.get(map.get(key.hashCode()));
    }

    public Collection<V> values() {
        return this.values;
    }

}

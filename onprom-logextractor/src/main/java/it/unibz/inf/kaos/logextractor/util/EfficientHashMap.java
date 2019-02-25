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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class EfficientHashMap<V> {

    private final Map<String, Integer> map;
    private final ArrayList<V> values;

    public EfficientHashMap() {
        map = new HashMap<>();
        values = new ArrayList<>();
    }

    public void put(String key, V value) {
        this.map.put(key, values.size());
        values.add(value);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public int size() {
        return values.size();
    }

    public V get(String key) {
        return values.get(map.get(key));
    }

    public Collection<V> values() {
        return this.values;
    }

}

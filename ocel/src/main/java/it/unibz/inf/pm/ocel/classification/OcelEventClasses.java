/*
 * ocel
 *
 * OcelEventClasses.java
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

package it.unibz.inf.pm.ocel.classification;


import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;

import java.util.*;


public class OcelEventClasses {
    protected OcelEventClassifier classifier;
    protected HashMap<String, OcelEventClass> classMap;

    public OcelEventClasses(OcelEventClassifier classifier) {
        this.classifier = classifier;
        this.classMap = new HashMap();
    }

    public static synchronized OcelEventClasses deriveEventClasses(OcelEventClassifier classifier, OcelLog log) {
        OcelEventClasses nClasses = new OcelEventClasses(classifier);
        nClasses.register(log);
        nClasses.harmonizeIndices();
        return nClasses;
    }

    public OcelEventClassifier getClassifier() {
        return this.classifier;
    }

    public Collection<OcelEventClass> getClasses() {
        return this.classMap.values();
    }

    public int size() {
        return this.classMap.size();
    }

    public OcelEventClass getClassOf(OcelEvent event) {
        return this.classMap.get(this.classifier.getClassIdentity(event));
    }

    public OcelEventClass getByIdentity(String classIdentity) {
        return this.classMap.get(classIdentity);
    }

    public OcelEventClass getByIndex(int index) {
        Iterator i$ = this.classMap.values().iterator();

        OcelEventClass eventClass;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            eventClass = (OcelEventClass)i$.next();
        } while(eventClass.getIndex() != index);

        return eventClass;
    }

    public void register(OcelLog log) {

    }



    public void register(OcelEvent event) {
        this.register(this.classifier.getClassIdentity(event));
    }

    public synchronized void register(String classId) {
        OcelEventClass eventClass = this.classMap.get(classId);
        if (eventClass == null && classId != null) {
            eventClass = new OcelEventClass(classId, this.classMap.size());
            this.classMap.put(classId, eventClass);
        }

        if (eventClass != null) {
            eventClass.incrementSize();
        }

    }

    public synchronized void harmonizeIndices() {
        ArrayList<OcelEventClass> classList = new ArrayList(this.classMap.values());
        Collections.sort(classList);
        this.classMap.clear();

        for(int i = 0; i < classList.size(); ++i) {
            OcelEventClass original = classList.get(i);
            OcelEventClass harmonized = new OcelEventClass(original.getId(), i);
            harmonized.setSize(original.size());
            this.classMap.put(harmonized.getId(), harmonized);
        }

    }

    public boolean equals(Object o) {
        return o instanceof OcelEventClasses && ((OcelEventClasses) o).getClassifier().equals(this.classifier);
    }

    public String toString() {
        return "Event classes defined by " + this.classifier.name();
    }
}

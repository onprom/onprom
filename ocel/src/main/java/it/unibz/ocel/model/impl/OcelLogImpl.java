/*
 * ocel
 *
 * OcelLogImpl.java
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

import it.unibz.ocel.classification.OcelEventClassifier;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.info.OcelLogInfo;
import com.google.common.collect.Multimap;
import it.unibz.ocel.model.*;

import java.util.*;


public class OcelLogImpl extends ArrayList implements OcelLog {
    private static final long serialVersionUID = -9192919845877466525L;
    private OcelAttributeMap attributes;
    private Set<OcelExtension> extensions;
    private List<OcelEventClassifier> classifiers;
    private List<OcelAttribute> globalTraceAttributes;
    private List<OcelAttribute> globalEventAttributes;
    private List<OcelAttribute> globalObjectAttributes;
    private List<OcelAttribute> globalLogAttributes;
    private OcelEventClassifier cachedClassifier;
    private OcelLogInfo cachedInfo;

    private Set<OcelEvent> events;
    private Set<OcelObject> objects;
    Multimap<OcelEvent, OcelObject> eventObjectsMap;

    public OcelLogImpl(OcelAttributeMap attributeMap) {
        this.attributes = attributeMap;
        this.extensions = new HashSet();
        this.classifiers = new ArrayList();
        this.globalTraceAttributes = new ArrayList();
        this.globalEventAttributes = new ArrayList();
        this.globalObjectAttributes = new ArrayList();
        this.globalLogAttributes = new ArrayList();
        this.cachedClassifier = null;
        this.cachedInfo = null;
    }

    private OcelLogImpl(OcelAttributeMap attributeMap, int initialCapacity) {
        super(initialCapacity);
        this.attributes = attributeMap;
        this.extensions = new HashSet();
        this.classifiers = new ArrayList();
        this.globalTraceAttributes = new ArrayList();
        this.globalEventAttributes = new ArrayList();
        this.globalObjectAttributes = new ArrayList();
        this.globalLogAttributes = new ArrayList();
        this.cachedClassifier = null;
        this.cachedInfo = null;
    }

    public OcelAttributeMap getAttributes() {
        return this.attributes;
    }

    public void setAttributes(OcelAttributeMap attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttributes() {
        return !this.attributes.isEmpty();
    }

    public Set<OcelExtension> getExtensions() {
        return this.extensions;
    }

    public Object clone() {
        OcelLogImpl clone = new OcelLogImpl((OcelAttributeMap) this.attributes.clone(), this.size());
        clone.extensions = new HashSet(this.extensions);
        clone.classifiers = new ArrayList(this.classifiers);
        clone.globalTraceAttributes = new ArrayList(this.globalTraceAttributes);
        clone.globalEventAttributes = new ArrayList(this.globalEventAttributes);
        clone.globalObjectAttributes = new ArrayList(this.globalObjectAttributes);
        clone.globalLogAttributes = new ArrayList(this.globalLogAttributes);
        clone.cachedClassifier = null;
        clone.cachedInfo = null;

        for (Object o : this) {
            OcelTrace trace = (OcelTrace) o;
            clone.add(trace);
        }

        return clone;
    }

    public List<OcelEventClassifier> getClassifiers() {
        return this.classifiers;
    }

    public List<OcelAttribute> getGlobalEventAttributes() {
        return this.globalEventAttributes;
    }

    @Override
    public List<OcelAttribute> getGlobalObjectAttributes() {
        return this.globalObjectAttributes;
    }

    @Override
    public List<OcelAttribute> getGlobalLogAttributes() {
        return this.globalLogAttributes;
    }

    public List<OcelAttribute> getGlobalTraceAttributes() {
        return this.globalTraceAttributes;
    }

    public boolean accept(OcelVisitor visitor) {
        if (!visitor.precondition()) {
            return false;
        } else {
            visitor.init(this);
            visitor.visitLogPre(this);
            Iterator i$ = this.extensions.iterator();

            while(i$.hasNext()) {
                OcelExtension extension = (OcelExtension)i$.next();
                extension.accept(visitor, this);
            }

            i$ = this.classifiers.iterator();

            while(i$.hasNext()) {
                OcelEventClassifier classifier = (OcelEventClassifier)i$.next();
                classifier.accept(visitor, this);
            }

            i$ = this.attributes.values().iterator();

            while(i$.hasNext()) {
                OcelAttribute attribute = (OcelAttribute)i$.next();
                attribute.accept(visitor, this);
            }

            i$ = this.iterator();

            while(i$.hasNext()) {
                OcelEvent event = (OcelEvent)i$.next();
                event.accept(visitor, this);
            }

            visitor.visitLogPost(this);
            return true;
        }
    }

    public OcelLogInfo getInfo(OcelEventClassifier classifier) {
        return classifier.equals(this.cachedClassifier) ? this.cachedInfo : null;
    }

    public void setInfo(OcelEventClassifier classifier, OcelLogInfo info) {
        this.cachedClassifier = classifier;
        this.cachedInfo = info;
    }
}

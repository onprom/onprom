/*
 * ocel
 *
 * OcelEventLiteImpl.java
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

import com.google.common.primitives.Longs;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelSeqIDFactory;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelAttributeUtils;
import it.unibz.ocel.util.OcelUtils;

import java.util.*;

public final class OcelEventLiteImpl implements OcelEvent {
    private long id;
    private OcelAttributeMap attributes;

    public OcelEventLiteImpl() {
        this(OcelSeqIDFactory.instance().nextId(), new OcelAttributeMapLiteImpl());
    }

    public OcelEventLiteImpl(long id) {
        this(id, new OcelAttributeMapLiteImpl());
    }

    public OcelEventLiteImpl(OcelAttributeMap attributes) {
        this(OcelSeqIDFactory.instance().nextId(), attributes);
    }

    public OcelEventLiteImpl(long id, OcelAttributeMap attributes) {
        this.id = id;
        this.attributes = attributes;
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
        return OcelAttributeUtils.extractExtensions(this.attributes);
    }

    public Object clone() {
        OcelEventLiteImpl clone = new OcelEventLiteImpl();
        clone.id = OcelSeqIDFactory.instance().nextId();
        clone.attributes = (OcelAttributeMap)this.attributes.clone();
        return clone;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<OcelObject> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(OcelObject object) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends OcelObject> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends OcelObject> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    public boolean equals(Object o) {
        if (o instanceof OcelEventLiteImpl) {
            return ((OcelEventLiteImpl)o).id == this.id;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Longs.hashCode(this.id);
    }

    @Override
    public OcelObject get(int index) {
        return null;
    }

    @Override
    public OcelObject set(int index, OcelObject element) {
        return null;
    }

    @Override
    public void add(int index, OcelObject element) {

    }

    @Override
    public OcelObject remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<OcelObject> listIterator() {
        return null;
    }

    @Override
    public ListIterator<OcelObject> listIterator(int index) {
        return null;
    }

    @Override
    public List<OcelObject> subList(int fromIndex, int toIndex) {
        return null;
    }

    public OcelID getID() {
        return new OcelID(this.id, this.id);
    }

    @Override
    public int insertObject(OcelObject obj) {
        return 0;
    }

    @Override
    public void accept(OcelVisitor var1, OcelLog var2) {

    }

    public void accept(OcelVisitor visitor, OcelTrace trace) {
        visitor.visitEventPre(this, trace);

        for (OcelAttribute attribute : this.getAttributes().values()) {
            attribute.accept(visitor, this);
        }

        visitor.visitEventPost(this, trace);
    }

    public String toString() {
        String name = OcelUtils.getConceptName(this);
        return name == null ? super.toString() : name;
    }
}


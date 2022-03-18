package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.id.OcelIDFactory;
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelAttributeUtils;

import java.util.Iterator;
import java.util.Set;

public class OcelObjectImpl implements OcelObject {
    private OcelID id;
    private OcelAttributeMap attributes;

    public OcelObjectImpl() {
        this(OcelIDFactory.instance().createId(), new OcelAttributeMapImpl());
    }

    public OcelObjectImpl(OcelID id) {
        this(id, new OcelAttributeMapImpl());
    }

    public OcelObjectImpl(OcelAttributeMap attributes) {
        this(OcelIDFactory.instance().createId(), attributes);
    }

    public OcelObjectImpl(OcelID id, OcelAttributeMap attributes) {
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
        OcelObjectImpl clone;
        try {
            clone = (OcelObjectImpl)super.clone();
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            return null;
        }

        clone.id = OcelIDFactory.instance().createId();
        clone.attributes = (OcelAttributeMap)this.attributes.clone();
        return clone;
    }

    public boolean equals(Object o) {
        return o instanceof OcelObjectImpl ? ((OcelObjectImpl)o).id.equals(this.id) : false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public OcelID getID() {
        return this.id;
    }

    public void setID(OcelID id) {
        this.id = id;
    }

    public void accept(OcelVisitor visitor, OcelEvent event) {
        visitor.visitObjectPre(this, event);
        Iterator i$ = this.attributes.values().iterator();

        while(i$.hasNext()) {
            OcelAttribute attribute = (OcelAttribute)i$.next();
            attribute.accept(visitor, this);
        }

        visitor.visitObjectPost(this, event);
    }
}

package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeCollection;

import java.util.Collection;
import java.util.Iterator;

public abstract class OcelAttributeCollectionImpl extends OcelAttributeLiteralImpl implements OcelAttributeCollection {
    private static final long serialVersionUID = 4322597532345796274L;
    protected Collection<OcelAttribute> collection;

    public OcelAttributeCollectionImpl(String key) {
        super(key, "", (OcelExtension)null);
    }

    public OcelAttributeCollectionImpl(String key, OcelExtension extension) {
        super(key, "", extension);
    }

    public void addToCollection(OcelAttribute attribute) {
        if (this.collection != null) {
            this.collection.add(attribute);
        } else {
            throw new NullPointerException("Cannot add attribute to collection that is null");
        }
    }

    public void removeFromCollection(OcelAttribute attribute) {
        if (this.collection != null) {
            this.collection.remove(attribute);
        }

    }

    public Collection<OcelAttribute> getCollection() {
        return this.collection != null ? this.collection : this.getAttributes().values();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String sep = "[";
        Iterator var3 = this.getCollection().iterator();

        while(var3.hasNext()) {
            OcelAttribute attribute = (OcelAttribute)var3.next();
            buf.append(sep);
            sep = ",";
            buf.append(attribute.getKey());
            buf.append(":");
            buf.append(attribute.toString());
        }

        if (buf.length() == 0) {
            buf.append("[");
        }

        buf.append("]");
        return buf.toString();
    }

    public Object clone() {
        OcelAttributeCollectionImpl clone = (OcelAttributeCollectionImpl)super.clone();
        return clone;
    }
}

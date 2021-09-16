package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttributeList;

import java.util.ArrayList;

public class OcelAttributeListImpl extends OcelAttributeCollectionImpl implements OcelAttributeList {
    private static final long serialVersionUID = 5584421551344100844L;

    public OcelAttributeListImpl(String key) {
        super(key, (OcelExtension)null);
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


package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttributeContainer;



public class OcelAttributeContainerImpl extends OcelAttributeCollectionImpl implements OcelAttributeContainer {
    private static final long serialVersionUID = -2171609637065248221L;

    public OcelAttributeContainerImpl(String key) {
        super(key, (OcelExtension)null);
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

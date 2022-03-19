package it.unibz.ocel.model;

import java.util.Collection;

public interface OcelAttributeCollection extends OcelAttribute {
    void addToCollection(OcelAttribute var1);

    Collection<OcelAttribute> getCollection();
}


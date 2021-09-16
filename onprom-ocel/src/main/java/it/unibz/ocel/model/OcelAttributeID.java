package it.unibz.ocel.model;

import it.unibz.ocel.id.OcelID;


public interface OcelAttributeID extends OcelAttribute {
    void setValue(OcelID var1);

    OcelID getValue();
}

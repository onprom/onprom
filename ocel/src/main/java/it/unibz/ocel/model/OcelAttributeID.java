package it.unibz.ocel.model;

import it.unibz.ocel.id.OcelID;


public interface OcelAttributeID extends OcelAttribute {
    OcelID getValue();

    void setValue(OcelID var1);
}

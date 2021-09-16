package it.unibz.ocel.model;


import it.unibz.ocel.id.OcelID;

import java.util.List;

public interface OcelEvent extends OcelElement , List<OcelObject> {
    OcelID getID();
    int insertObject(OcelObject obj);

    void accept(OcelVisitor var1, OcelLog var2);
}


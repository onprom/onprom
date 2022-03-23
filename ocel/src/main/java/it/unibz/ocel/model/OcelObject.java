package it.unibz.ocel.model;

import it.unibz.ocel.id.OcelID;


public interface OcelObject extends OcelElement {
    OcelID getID();

    void accept(OcelVisitor var1, OcelEvent var2);
}

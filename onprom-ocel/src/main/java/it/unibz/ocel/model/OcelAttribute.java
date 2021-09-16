package it.unibz.ocel.model;

import it.unibz.ocel.extension.OcelExtension;

import java.io.Serializable;



public interface OcelAttribute extends OcelAttributable, Cloneable, Comparable<OcelAttribute>, Serializable {
    String getKey();

    OcelExtension getExtension();

    Object clone();

    String toString();

    void accept(OcelVisitor var1, OcelAttributable var2);
}

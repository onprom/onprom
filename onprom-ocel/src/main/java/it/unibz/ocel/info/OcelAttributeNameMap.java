package it.unibz.ocel.info;


import it.unibz.ocel.model.OcelAttribute;

public interface OcelAttributeNameMap {
    String getMappingName();

    String map(OcelAttribute var1);

    String map(String var1);
}
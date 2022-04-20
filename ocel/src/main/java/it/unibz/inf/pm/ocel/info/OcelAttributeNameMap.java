package it.unibz.inf.pm.ocel.info;


import it.unibz.inf.pm.ocel.entity.OcelAttribute;

public interface OcelAttributeNameMap {
    String getMappingName();

    String map(OcelAttribute var1);

    String map(String var1);
}
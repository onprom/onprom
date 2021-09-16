package it.unibz.ocel.model;

import java.util.Map;


public interface OcelAttributeMap extends Map<String, OcelAttribute>, Cloneable {
    Object clone();
}
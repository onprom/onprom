package it.unibz.inf.pm.ocel.entity;

import java.util.Map;


public interface OcelAttributeMap extends Map<String, OcelAttribute>, Cloneable {
    Object clone();
}
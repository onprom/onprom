package it.unibz.inf.pm.ocel.entity;


/**
 * An element is composed of a key and value(s). The key is string-based,
 * whereas the value may be string, timestamp, integer, ﬂoat, and boolean.
 */
public class OcelElement {

    private String key;

    public enum values {
        String, Timestamp, Integer, Float, Boolean
    }


}

package it.unibz.inf.pm.ocel.entity;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XVisitor;

import java.util.Set;

public class OcelAttribute implements XAttribute {
    private String key;
    private String value;
    private String type;

    public OcelAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public OcelAttribute(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }


    @Override
    public String getKey() {
        return key;
    }

    @Override
    public XExtension getExtension() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void accept(XVisitor xVisitor, XAttributable xAttributable) {

    }

    @Override
    public int compareTo(XAttribute o) {
        return 0;
    }

    @Override
    public XAttributeMap getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(XAttributeMap xAttributeMap) {

    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public Set<XExtension> getExtensions() {
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}

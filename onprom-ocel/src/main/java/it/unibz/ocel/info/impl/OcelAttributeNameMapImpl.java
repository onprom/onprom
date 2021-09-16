package it.unibz.ocel.info.impl;

import it.unibz.ocel.info.OcelAttributeNameMap;
import it.unibz.ocel.model.OcelAttribute;

import java.util.HashMap;
import java.util.Iterator;


public class OcelAttributeNameMapImpl implements OcelAttributeNameMap {
    private final String name;
    private HashMap<String, String> mapping;

    public OcelAttributeNameMapImpl(String name) {
        this.name = name;
        this.mapping = new HashMap();
    }

    public String getMappingName() {
        return this.name;
    }

    public String map(OcelAttribute attribute) {
        return this.map(attribute.getKey());
    }

    public String map(String attributeKey) {
        return (String)this.mapping.get(attributeKey);
    }

    public void registerMapping(OcelAttribute attribute, String alias) {
        this.registerMapping(attribute.getKey(), alias);
    }

    public void registerMapping(String attributeKey, String alias) {
        this.mapping.put(attributeKey, alias);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attribute name map: ");
        sb.append(this.name);
        Iterator i$ = this.mapping.keySet().iterator();

        while(i$.hasNext()) {
            String key = (String)i$.next();
            sb.append("\n");
            sb.append(key);
            sb.append(" -> ");
            sb.append((String)this.mapping.get(key));
        }

        return sb.toString();
    }
}


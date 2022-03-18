package it.unibz.ocel.model.impl;

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class OcelAttributeMapImpl extends HashMap<String, OcelAttribute> implements OcelAttributeMap {
    private static final long serialVersionUID = 2701256420845748051L;

    public OcelAttributeMapImpl() {
        this(0);
    }

    public OcelAttributeMapImpl(int size) {
        super(size);
    }

    public OcelAttributeMapImpl(Map<String, OcelAttribute> template) {
        super(template.size());
        Iterator var2 = template.keySet().iterator();

        while(var2.hasNext()) {
            String key = (String)var2.next();
            this.put(key, template.get(key));
        }

    }

    public Object clone() {
        OcelAttributeMapImpl clone = new OcelAttributeMapImpl(this.size());
        Iterator var2 = this.values().iterator();

        while(var2.hasNext()) {
            OcelAttribute value = (OcelAttribute)var2.next();
            clone.put(value.getKey(), (OcelAttribute)value.clone());
        }

        return clone;
    }
}

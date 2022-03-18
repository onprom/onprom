package it.unibz.ocel.model.impl;

import com.google.common.collect.ForwardingMap;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;


public final class OcelAttributeMapLiteImpl extends ForwardingMap<String, OcelAttribute> implements OcelAttributeMap, Serializable {
    private static final long serialVersionUID = 6928328637653669033L;
    private Map<String, OcelAttribute> attributeMap;

    public OcelAttributeMapLiteImpl() {
        this.attributeMap = this.createInternalMap();
    }

    public OcelAttributeMapLiteImpl(int initialSize) {
        this.attributeMap = this.createInternalMap(initialSize);
    }

    private Map<String, OcelAttribute> createInternalMap() {
        return new Object2ObjectOpenHashMap(4);
    }

    private Map<String, OcelAttribute> createInternalMap(int initialSize) {
        return new Object2ObjectOpenHashMap(initialSize);
    }

    protected Map<String, OcelAttribute> delegate() {
        return this.attributeMap;
    }

    public Object clone() {
        OcelAttributeMapLiteImpl clone = new OcelAttributeMapLiteImpl();
        if (this.isEmpty()) {
            return clone;
        } else {
            clone.attributeMap = this.createInternalMap(this.size());
            Iterator var2 = this.values().iterator();

            while(var2.hasNext()) {
                OcelAttribute attr = (OcelAttribute)var2.next();
                clone.attributeMap.put(attr.getKey(), (OcelAttribute)attr.clone());
            }

            return clone;
        }
    }
}

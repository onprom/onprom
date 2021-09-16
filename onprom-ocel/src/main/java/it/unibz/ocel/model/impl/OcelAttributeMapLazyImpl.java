package it.unibz.ocel.model.impl;

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeMap;

import java.util.*;


public class OcelAttributeMapLazyImpl<T extends OcelAttributeMap> implements OcelAttributeMap {
    private static final Set<Map.Entry<String, OcelAttribute>> EMPTY_ENTRYSET = Collections.unmodifiableSet(new HashSet(0));
    private static final Set<String> EMPTY_KEYSET = Collections.unmodifiableSet(new HashSet(0));
    private static final Collection<OcelAttribute> EMPTY_ENTRIES = Collections.unmodifiableCollection(new ArrayList(0));
    private Class<T> backingStoreClass;
    private T backingStore = null;

    public OcelAttributeMapLazyImpl(Class<T> implementingClass) {
        this.backingStoreClass = implementingClass;
        this.backingStore = null;
    }

    public Class<T> getBackingStoreClass() {
        return this.backingStoreClass;
    }

    public synchronized void clear() {
        this.backingStore = null;
    }

    public synchronized boolean containsKey(Object key) {
        return this.backingStore != null ? this.backingStore.containsKey(key) : false;
    }

    public synchronized boolean containsValue(Object value) {
        return this.backingStore != null ? this.backingStore.containsValue(value) : false;
    }

    public synchronized Set<Map.Entry<String, OcelAttribute>> entrySet() {
        return this.backingStore != null ? this.backingStore.entrySet() : EMPTY_ENTRYSET;
    }

    public synchronized OcelAttribute get(Object key) {
        return this.backingStore != null ? (OcelAttribute)this.backingStore.get(key) : null;
    }

    public synchronized boolean isEmpty() {
        return this.backingStore != null ? this.backingStore.isEmpty() : true;
    }

    public synchronized Set<String> keySet() {
        return this.backingStore != null ? this.backingStore.keySet() : EMPTY_KEYSET;
    }

    public synchronized OcelAttribute put(String key, OcelAttribute value) {
        if (this.backingStore == null) {
            try {
//                this.backingStore = (OcelAttributeMap)this.backingStoreClass.newInstance();
                this.backingStore = this.backingStoreClass.newInstance();
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return (OcelAttribute)this.backingStore.put(key, value);
    }

    public synchronized void putAll(Map<? extends String, ? extends OcelAttribute> t) {
        if (t.size() > 0) {
            if (this.backingStore == null) {
                try {
//                    this.backingStore = (OcelAttributeMap)this.backingStoreClass.newInstance();
                    this.backingStore = this.backingStoreClass.newInstance();
                } catch (Exception var3) {
                    var3.printStackTrace();
                }
            }

            this.backingStore.putAll(t);
        }

    }

    public synchronized OcelAttribute remove(Object key) {
        return this.backingStore != null ? (OcelAttribute)this.backingStore.remove(key) : null;
    }

    public synchronized int size() {
        return this.backingStore != null ? this.backingStore.size() : 0;
    }

    public synchronized Collection<OcelAttribute> values() {
        return this.backingStore != null ? this.backingStore.values() : EMPTY_ENTRIES;
    }

    public Object clone() {
        try {
            OcelAttributeMapLazyImpl<T> clone = (OcelAttributeMapLazyImpl)super.clone();
            if (this.backingStore != null) {
//                clone.backingStore = (OcelAttributeMap)this.backingStore.clone();
                clone.backingStore = (T) this.backingStore.clone();
            }

            return clone;
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
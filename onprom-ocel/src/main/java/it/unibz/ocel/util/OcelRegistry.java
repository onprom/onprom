package it.unibz.ocel.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public abstract class OcelRegistry<T> {
    private Set<T> registry = new HashSet();
    private T current = null;

    public OcelRegistry() {
    }

    public Set<T> getAvailable() {
        return Collections.unmodifiableSet(this.registry);
    }

    public T currentDefault() {
        return this.current;
    }

    public void register(T instance) {
        if (!this.isContained(instance)) {
            this.registry.add(instance);
            if (this.current == null) {
                this.current = instance;
            }
        }

    }

    public void setCurrentDefault(T instance) {
        this.registry.add(instance);
        this.current = instance;
    }

    protected abstract boolean areEqual(T var1, T var2);

    protected boolean isContained(T instance) {
        Iterator i$ = this.registry.iterator();

        Object ref;
        do {
            if (!i$.hasNext()) {
                return false;
            }

            ref = i$.next();
        } while(!this.areEqual(instance, (T) ref));

        return true;
    }
}

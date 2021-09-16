package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeDiscrete;

import java.util.Objects;



public class OcelAttributeDiscreteImpl extends OcelAttributeImpl implements OcelAttributeDiscrete {
    private static final long serialVersionUID = 2209799959584107671L;
    private long value;

    public OcelAttributeDiscreteImpl(String key, long value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeDiscreteImpl(String key, long value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String toString() {
        return Long.toString(this.value);
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeDiscrete)) {
            return false;
        } else {
            OcelAttributeDiscrete other = (OcelAttributeDiscrete)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getKey(), this.value});
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeDiscrete)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Long.valueOf(this.value).compareTo(((OcelAttributeDiscrete)other).getValue());
        }
    }
}


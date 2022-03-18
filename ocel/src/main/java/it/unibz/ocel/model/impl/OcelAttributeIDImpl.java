package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeID;

import java.util.Objects;


public class OcelAttributeIDImpl extends OcelAttributeImpl implements OcelAttributeID {
    private static final long serialVersionUID = -5378932771467141311L;
    private OcelID value;

    public OcelAttributeIDImpl(String key, OcelID value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeIDImpl(String key, OcelID value, OcelExtension extension) {
        super(key, extension);
        this.setValue(value);
    }

    public OcelID getValue() {
        return this.value;
    }

    public void setValue(OcelID value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in ID attribute!");
        } else {
            this.value = value;
        }
    }

    public String toString() {
        return this.value.toString();
    }

    public Object clone() {
        OcelAttributeIDImpl clone = (OcelAttributeIDImpl)super.clone();
        clone.value = (OcelID)this.value.clone();
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeID)) {
            return false;
        } else {
            OcelAttributeID other = (OcelAttributeID)obj;
            return super.equals(other) && this.value.equals(other.getValue());
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getKey(), this.value});
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeID)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : this.value.compareTo(((OcelAttributeID)other).getValue());
        }
    }
}


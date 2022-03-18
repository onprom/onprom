package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeBoolean;

import java.util.Objects;


public class OcelAttributeBooleanImpl extends OcelAttributeImpl implements OcelAttributeBoolean {
    private static final long serialVersionUID = -4696555899349337644L;
    private boolean value;

    public OcelAttributeBooleanImpl(String key, boolean value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeBooleanImpl(String key, boolean value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String toString() {
        return this.value ? "true" : "false";
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeBoolean)) {
            return false;
        } else {
            OcelAttributeBoolean other = (OcelAttributeBoolean)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getKey(), this.value});
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeBoolean)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Boolean.valueOf(this.value).compareTo(((OcelAttributeBoolean)other).getValue());
        }
    }
}


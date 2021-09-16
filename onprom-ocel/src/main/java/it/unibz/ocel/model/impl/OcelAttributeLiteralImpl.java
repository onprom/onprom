package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeLiteral;

import java.util.Objects;

public class OcelAttributeLiteralImpl extends OcelAttributeImpl implements OcelAttributeLiteral {
    private static final long serialVersionUID = -1844032762689490775L;
    private String value;

    public OcelAttributeLiteralImpl(String key, String value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeLiteralImpl(String key, String value, OcelExtension extension) {
        super(key, extension);
        this.setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in literal attribute!");
        } else {
            this.value = value;
        }
    }

    public String toString() {
        return this.value;
    }

    public Object clone() {
        OcelAttributeLiteralImpl clone = (OcelAttributeLiteralImpl)super.clone();
        clone.value = new String(this.value);
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeLiteral)) {
            return false;
        } else {
            OcelAttributeLiteral other = (OcelAttributeLiteral)obj;
            return super.equals(other) && this.value.equals(other.getValue());
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getKey(), this.value});
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeLiteral)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : this.value.compareTo(((OcelAttributeLiteral)other).getValue());
        }
    }
}

package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeContinuous;

import java.util.Objects;



public class OcelAttributeContinuousImpl extends OcelAttributeImpl implements OcelAttributeContinuous {
    private static final long serialVersionUID = -1789813595800348876L;
    private double value;

    public OcelAttributeContinuousImpl(String key, double value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeContinuousImpl(String key, double value, OcelExtension extension) {
        super(key, extension);
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String toString() {
        return Double.toString(this.value);
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OcelAttributeContinuous)) {
            return false;
        } else {
            OcelAttributeContinuous other = (OcelAttributeContinuous)obj;
            return super.equals(other) && this.value == other.getValue();
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getKey(), this.value});
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeContinuous)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Double.valueOf(this.value).compareTo(((OcelAttributeContinuous)other).getValue());
        }
    }
}

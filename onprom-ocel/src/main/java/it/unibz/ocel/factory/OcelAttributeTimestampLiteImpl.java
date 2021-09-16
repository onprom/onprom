package it.unibz.ocel.factory;

import com.google.common.primitives.Longs;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeTimestamp;
import it.unibz.ocel.model.impl.OcelAttributeImpl;

import java.util.Date;


final class OcelAttributeTimestampLiteImpl extends OcelAttributeImpl implements OcelAttributeTimestamp {
    private static final long serialVersionUID = -5883868464604814930L;
    private long value;

    public OcelAttributeTimestampLiteImpl(String key, Date value) {
        this(key, value, (OcelExtension)null);
    }

    public OcelAttributeTimestampLiteImpl(String key, Date value, OcelExtension extension) {
        this(key, value.getTime(), extension);
    }

    public OcelAttributeTimestampLiteImpl(String key, long millis) {
        this(key, millis, (OcelExtension)null);
    }

    public OcelAttributeTimestampLiteImpl(String key, long millis, OcelExtension extension) {
        super(key, extension);
        this.value = millis;
    }

    public Date getValue() {
        return new Date(this.value);
    }

    public long getValueMillis() {
        return this.value;
    }

    public void setValue(Date value) {
        if (value == null) {
            throw new NullPointerException("No null value allowed in timestamp attribute!");
        } else {
            this.value = value.getTime();
        }
    }

    public void setValueMillis(long value) {
        this.value = value;
    }

    public String toString() {
        synchronized(FORMATTER) {
            return FORMATTER.format(new Date(this.value));
        }
    }

    public Object clone() {
        OcelAttributeTimestampLiteImpl clone = (OcelAttributeTimestampLiteImpl)super.clone();
        clone.value = this.value;
        return clone;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OcelAttributeTimestamp)) {
            return false;
        } else {
            OcelAttributeTimestamp other = (OcelAttributeTimestamp)obj;
            return super.equals(other) && this.value == other.getValueMillis();
        }
    }

    public int compareTo(OcelAttribute other) {
        if (!(other instanceof OcelAttributeTimestamp)) {
            throw new ClassCastException();
        } else {
            int result = super.compareTo(other);
            return result != 0 ? result : Longs.compare(this.value, ((OcelAttributeTimestamp)other).getValueMillis());
        }
    }
}

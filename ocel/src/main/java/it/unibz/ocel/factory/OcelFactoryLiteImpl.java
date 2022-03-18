package it.unibz.ocel.factory;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.*;
import it.unibz.ocel.model.impl.*;

import java.net.URI;
import java.util.Date;


public class OcelFactoryLiteImpl implements OcelFactory {
    private final Interner<String> interner;

    public OcelFactoryLiteImpl() {
        this(true);
    }

    public OcelFactoryLiteImpl(boolean useInterner) {
        if (useInterner) {
            this.interner = Interners.newStrongInterner();
        } else {
            this.interner = new Interner<String>() {
                public String intern(String s) {
                    return s;
                }
            };
        }

    }

    public static void register() {
        OcelFactoryRegistry.instance().register(new OcelFactoryLiteImpl());
    }

    private final String intern(String s) {
        return (String)this.interner.intern(s);
    }

    public String getName() {
        return "OCELLite: Sequential IDs & Open Hash Map";
    }

    public String toString() {
        return this.getName();
    }

    public String getAuthor() {
        return "F. Mannhardt";
    }

    public URI getUri() {
        return URI.create("http://www.ocel-standard.org/");
    }

    public String getVendor() {
        return "ocel-standard.org";
    }

    public String getDescription() {
        return "A OCEL Factory that provides objects optimized for a small memory footprint. All operations are NOT synchronized!";
    }

    public OcelLog createLog() {
        return new OcelLogImpl(this.createAttributeMap());
    }

    public OcelLog createLog(OcelAttributeMap attributes) {
        return new OcelLogImpl(attributes);
    }

    public OcelTrace createTrace() {
        return new OcelTraceImpl(new OcelAttributeMapLiteImpl());
    }

    public OcelTrace createTrace(OcelAttributeMap attributes) {
        return new OcelTraceImpl(attributes);
    }
    public OcelEvent createEvent() {
        return new OcelEventLiteImpl();
    }

    public OcelEvent createEvent(OcelAttributeMap attributes) {
        return new OcelEventLiteImpl(attributes);
    }

    public OcelEvent createEvent(OcelID id, OcelAttributeMap attributes) {
        throw new UnsupportedOperationException("Cannot create an OcelEvent with a pre-defined ID");
    }

    public OcelAttributeMap createAttributeMap() {
        return new OcelAttributeMapLiteImpl();
    }

    public OcelAttributeBoolean createAttributeBoolean(String key, boolean value, OcelExtension extension) {
        return new OcelAttributeBooleanImpl(this.intern(key), value, extension);
    }

    public OcelAttributeContinuous createAttributeContinuous(String key, double value, OcelExtension extension) {
        return new OcelAttributeContinuousImpl(this.intern(key), value, extension);
    }

    public OcelAttributeDiscrete createAttributeDiscrete(String key, long value, OcelExtension extension) {
        return new OcelAttributeDiscreteImpl(this.intern(key), value, extension);
    }

    public OcelAttributeLiteral createAttributeLiteral(String key, String value, OcelExtension extension) {
        return new OcelAttributeLiteralImpl(this.intern(key), value.length() < 64 ? this.intern(value) : value, extension);
    }

    public OcelAttributeTimestamp createAttributeTimestamp(String key, Date value, OcelExtension extension) {
        return new OcelAttributeTimestampLiteImpl(this.intern(key), value, extension);
    }

    public OcelAttributeTimestamp createAttributeTimestamp(String key, long millis, OcelExtension extension) {
        return new OcelAttributeTimestampLiteImpl(this.intern(key), millis, extension);
    }

    public OcelAttributeID createAttributeID(String key, OcelID value, OcelExtension extension) {
        return new OcelAttributeIDImpl(this.intern(key), value, extension);
    }

    public OcelAttributeContainer createAttributeContainer(String key, OcelExtension extension) {
        return new OcelAttributeContainerImpl(this.intern(key), extension);
    }

    public OcelAttributeList createAttributeList(String key, OcelExtension extension) {
        return new OcelAttributeListImpl(this.intern(key), extension);
    }
}


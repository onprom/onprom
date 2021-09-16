package it.unibz.ocel.extension;

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.model.OcelVisitor;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;


public class OcelExtension implements Serializable {
    private static final long serialVersionUID = -677323212952951508L;
    protected String name;
    protected String prefix;
    protected URI uri;
    protected HashSet<OcelAttribute> allAttributes;
    protected HashSet<OcelAttribute> logAttributes;
    protected HashSet<OcelAttribute> traceAttributes;
    protected HashSet<OcelAttribute> eventAttributes;
    protected HashSet<OcelAttribute> metaAttributes;
    protected HashSet<OcelAttribute> objectAttributes;

    protected OcelExtension(String name, String prefix, URI uri) {
        this.name = name;
        this.prefix = prefix;
        this.uri = uri;
        this.allAttributes = null;
        this.logAttributes = new HashSet();
        this.traceAttributes = new HashSet();
        this.eventAttributes = new HashSet();
        this.metaAttributes = new HashSet();
        this.objectAttributes = new HashSet();
    }

    public String getName() {
        return this.name;
    }

    public URI getUri() {
        return this.uri;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Collection<OcelAttribute> getDefinedAttributes() {
        if (this.allAttributes == null) {
            this.allAttributes = new HashSet();
            this.allAttributes.addAll(this.getLogAttributes());
            this.allAttributes.addAll(this.getTraceAttributes());
            this.allAttributes.addAll(this.getEventAttributes());
            this.allAttributes.addAll(this.getMetaAttributes());
            this.allAttributes.addAll(this.getObjectAttributes());
        }

        return this.allAttributes;
    }

    public Collection<OcelAttribute> getLogAttributes() {
        return this.logAttributes;
    }

    public Collection<OcelAttribute> getTraceAttributes() {
        return this.traceAttributes;
    }

    public Collection<OcelAttribute> getEventAttributes() {
        return this.eventAttributes;
    }

    public Collection<OcelAttribute> getMetaAttributes() {
        return this.metaAttributes;
    }

    public Collection<OcelAttribute> getObjectAttributes() {
        return this.objectAttributes;
    }

    public boolean equals(Object obj) {
        return obj instanceof OcelExtension ? this.uri.equals(((OcelExtension)obj).uri) : false;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public String toString() {
        return this.name;
    }

    public void accept(OcelVisitor visitor, OcelLog log) {
        visitor.visitExtensionPre(this, log);
        visitor.visitExtensionPost(this, log);
    }
}

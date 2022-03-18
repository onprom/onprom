package it.unibz.ocel.model.impl;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelAttributeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class OcelTraceImpl extends ArrayList<OcelEvent> implements OcelTrace {
    private static final long serialVersionUID = 843122019760036963L;
    private OcelAttributeMap attributes;

    public OcelTraceImpl(OcelAttributeMap attributeMap) {
        this.attributes = attributeMap;
    }

    private OcelTraceImpl(OcelAttributeMap attributeMap, int initialCapacity) {
        super(initialCapacity);
        this.attributes = attributeMap;
    }

    public OcelAttributeMap getAttributes() {
        return this.attributes;
    }

    public void setAttributes(OcelAttributeMap attributes) {
        this.attributes = attributes;
    }

    public Set<OcelExtension> getExtensions() {
        return OcelAttributeUtils.extractExtensions(this.attributes);
    }

    public boolean hasAttributes() {
        return !this.attributes.isEmpty();
    }

    public Object clone() {
        OcelTraceImpl clone = new OcelTraceImpl((OcelAttributeMap)this.attributes.clone(), this.size());
        Iterator i$ = this.iterator();

        while(i$.hasNext()) {
            OcelEvent event = (OcelEvent)i$.next();
            clone.add((OcelEvent)event.clone());
        }

        return clone;
    }

    public synchronized int insertOrdered(OcelEvent event) {
        if (this.size() == 0) {
            this.add(event);
            return 0;
        } else {
            OcelAttribute insTsAttr = (OcelAttribute)event.getAttributes().get("timestamp");
            if (insTsAttr == null) {
                this.add(event);
                return this.size() - 1;
            } else {
                Date insTs = ((OcelAttributeTimestamp)insTsAttr).getValue();

                for(int i = this.size() - 1; i >= 0; --i) {
                    OcelAttribute refTsAttr = (OcelAttribute)((OcelEvent)this.get(i)).getAttributes().get("timestamp");
                    if (refTsAttr == null) {
                        this.add(event);
                        return this.size() - 1;
                    }

                    Date refTs = ((OcelAttributeTimestamp)refTsAttr).getValue();
                    if (!insTs.before(refTs)) {
                        this.add(i + 1, event);
                        return i + 1;
                    }
                }

                this.add(0, event);
                return 0;
            }
        }
    }

    public void accept(OcelVisitor visitor, OcelLog log) {
        visitor.visitTracePre(this, log);
        Iterator i$ = this.attributes.values().iterator();

        while(i$.hasNext()) {
            OcelAttribute attribute = (OcelAttribute)i$.next();
            attribute.accept(visitor, this);
        }

        i$ = this.iterator();

        while(i$.hasNext()) {
            OcelEvent event = (OcelEvent)i$.next();
            event.accept(visitor, log);
        }

        visitor.visitTracePost(this, log);
    }

    @Override
    public void accept(OcelVisitor visitor, OcelEvent event) {
        visitor.visitEventPre(event,this);
        Iterator i$ = this.attributes.values().iterator();

        while(i$.hasNext()) {
            OcelAttribute attribute = (OcelAttribute)i$.next();
            attribute.accept(visitor, this);
        }

        i$ = this.iterator();

        while(i$.hasNext()) {
            OcelTrace trace = (OcelTrace) i$.next();
            trace.accept(visitor, event);
        }

        visitor.visitEventPost(event,this);
    }
}


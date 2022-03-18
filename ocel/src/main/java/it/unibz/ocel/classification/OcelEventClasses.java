package it.unibz.ocel.classification;


import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.model.OcelTrace;

import java.util.*;


public class OcelEventClasses {
    protected OcelEventClassifier classifier;
    protected HashMap<String, OcelEventClass> classMap;

    public OcelEventClasses(OcelEventClassifier classifier) {
        this.classifier = classifier;
        this.classMap = new HashMap();
    }

    public static synchronized OcelEventClasses deriveEventClasses(OcelEventClassifier classifier, OcelLog log) {
        OcelEventClasses nClasses = new OcelEventClasses(classifier);
        nClasses.register(log);
        nClasses.harmonizeIndices();
        return nClasses;
    }

    public OcelEventClassifier getClassifier() {
        return this.classifier;
    }

    public Collection<OcelEventClass> getClasses() {
        return this.classMap.values();
    }

    public int size() {
        return this.classMap.size();
    }

    public OcelEventClass getClassOf(OcelEvent event) {
        return (OcelEventClass)this.classMap.get(this.classifier.getClassIdentity(event));
    }

    public OcelEventClass getByIdentity(String classIdentity) {
        return (OcelEventClass)this.classMap.get(classIdentity);
    }

    public OcelEventClass getByIndex(int index) {
        Iterator i$ = this.classMap.values().iterator();

        OcelEventClass eventClass;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            eventClass = (OcelEventClass)i$.next();
        } while(eventClass.getIndex() != index);

        return eventClass;
    }

    public void register(OcelLog log) {
        Iterator iterator = log.iterator();

        while(iterator.hasNext()) {
            OcelTrace trace = (OcelTrace)iterator.next();
            this.register(trace);
        }
    }

    public void register(OcelTrace trace) {
        Iterator i$ = trace.iterator();

        while(i$.hasNext()) {
            OcelEvent event = (OcelEvent)i$.next();
            this.register(event);
        }
    }

    public void register(OcelEvent event) {
        this.register(this.classifier.getClassIdentity(event));
    }

    public synchronized void register(String classId) {
        OcelEventClass eventClass = (OcelEventClass)this.classMap.get(classId);
        if (eventClass == null && classId != null) {
            eventClass = new OcelEventClass(classId, this.classMap.size());
            this.classMap.put(classId, eventClass);
        }

        if (eventClass != null) {
            eventClass.incrementSize();
        }

    }

    public synchronized void harmonizeIndices() {
        ArrayList<OcelEventClass> classList = new ArrayList(this.classMap.values());
        Collections.sort(classList);
        this.classMap.clear();

        for(int i = 0; i < classList.size(); ++i) {
            OcelEventClass original = (OcelEventClass)classList.get(i);
            OcelEventClass harmonized = new OcelEventClass(original.getId(), i);
            harmonized.setSize(original.size());
            this.classMap.put(harmonized.getId(), harmonized);
        }

    }

    public boolean equals(Object o) {
        return o instanceof OcelEventClasses ? ((OcelEventClasses)o).getClassifier().equals(this.classifier) : false;
    }

    public String toString() {
        return "Event classes defined by " + this.classifier.name();
    }
}

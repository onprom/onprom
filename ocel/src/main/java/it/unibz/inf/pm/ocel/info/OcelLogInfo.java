package it.unibz.inf.pm.ocel.info;


import it.unibz.inf.pm.ocel.classification.OcelEventClasses;
import it.unibz.inf.pm.ocel.classification.OcelEventClassifier;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;

import java.util.Collection;

public interface OcelLogInfo {
    OcelLog getLog();

    int getNumberOfEvents();

    int getNumberOfObjects();

    int getNumberOfTraces();

    Collection<OcelEventClassifier> getEventClassifiers();

    OcelEventClasses getEventClasses(OcelEventClassifier var1);

    OcelEventClasses getEventClasses();

    OcelEventClasses getResourceClasses();

    OcelEventClasses getNameClasses();

    OcelEventClasses getTransitionClasses();

    OcelTimeBounds getLogTimeBoundaries();

    OcelTimeBounds getEventTimeBoundaries(OcelEvent var1);

    OcelAttributeInfo getLogAttributeInfo();

    OcelAttributeInfo getTraceAttributeInfo();

    OcelAttributeInfo getEventAttributeInfo();

    OcelAttributeInfo getObjectAttributeInfo();

    OcelAttributeInfo getMetaAttributeInfo();

    String toString();
}

package it.unibz.ocel.model;

import it.unibz.ocel.classification.OcelEventClassifier;
import it.unibz.ocel.info.OcelLogInfo;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Set;

public interface OcelLog extends OcelElement {
    List<OcelEventClassifier> getClassifiers();

    List<OcelAttribute> getGlobalTraceAttributes();

    List<OcelAttribute> getGlobalEventAttributes();

    List<OcelAttribute> getGlobalObjectAttributes();

    List<OcelAttribute> getGlobalLogAttributes();

    Set<OcelEvent> getEvents();

    Set<OcelObject> getObjects();

    Multimap<OcelEvent, OcelObject> getEventObjectsMap();

    boolean accept(OcelVisitor var1);

    OcelLogInfo getInfo(OcelEventClassifier var1);

    void setInfo(OcelEventClassifier var1, OcelLogInfo var2);
}

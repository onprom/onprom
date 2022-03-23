package it.unibz.ocel.model;

import it.unibz.ocel.classification.OcelEventClassifier;
import it.unibz.ocel.info.OcelLogInfo;

import java.util.List;

public interface OcelLog extends OcelElement, List {
    List<OcelEventClassifier> getClassifiers();

    List<OcelAttribute> getGlobalTraceAttributes();

    List<OcelAttribute> getGlobalEventAttributes();

    List<OcelAttribute> getGlobalObjectAttributes();

    List<OcelAttribute> getGlobalLogAttributes();

    boolean accept(OcelVisitor var1);

    OcelLogInfo getInfo(OcelEventClassifier var1);

    void setInfo(OcelEventClassifier var1, OcelLogInfo var2);
}

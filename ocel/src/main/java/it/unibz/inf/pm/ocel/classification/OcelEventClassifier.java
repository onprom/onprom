package it.unibz.inf.pm.ocel.classification;


import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.entity.OcelVisitor;

public interface OcelEventClassifier {
    String name();

    void setName(String var1);

    boolean sameEventClass(OcelEvent var1, OcelEvent var2);

    String getClassIdentity(OcelEvent var1);

    String[] getDefiningAttributeKeys();

    void accept(OcelVisitor var1, OcelLog var2);
}

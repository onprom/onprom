package it.unibz.ocel.classification;


import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.model.OcelVisitor;

public interface OcelEventClassifier {
    String name();

    void setName(String var1);

    boolean sameEventClass(OcelEvent var1, OcelEvent var2);

    String getClassIdentity(OcelEvent var1);

    String[] getDefiningAttributeKeys();

    void accept(OcelVisitor var1, OcelLog var2);
}

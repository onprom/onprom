package it.unibz.ocel.model;

import java.util.List;


public interface OcelTrace extends OcelElement, List<OcelEvent> {
    int insertOrdered(OcelEvent var1);

    void accept(OcelVisitor var1, OcelLog var2);

    void accept(OcelVisitor var1, OcelEvent var2);
}

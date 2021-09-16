package it.unibz.ocel.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class OcelEventAndClassifier extends OcelEventAttributeClassifier {
    public OcelEventAndClassifier(OcelEventClassifier... comparators) {
        super("", new String[0]);
        Collection<String> keys = new ArrayList();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(comparators[0].name());
        keys.addAll(Arrays.asList(comparators[0].getDefiningAttributeKeys()));

        for(int i = 1; i < comparators.length; ++i) {
            sb.append(" AND ");
            sb.append(comparators[i].name());
            keys.addAll(Arrays.asList(comparators[i].getDefiningAttributeKeys()));
        }

        sb.append(")");
        this.name = sb.toString();
        this.keys = (String[])keys.toArray(new String[0]);
    }
}


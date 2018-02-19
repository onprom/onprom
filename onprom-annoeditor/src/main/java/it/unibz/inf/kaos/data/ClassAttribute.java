package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Created by T. E. Kalayci on 01-Feb-2018.
 */
public class ClassAttribute extends NavigationalAttribute {

    public ClassAttribute(UMLClass _cls) {
        this(_cls, null);
    }

    public ClassAttribute(UMLClass _cls, Set<DiagramShape> _path) {
        super(_path, _cls, null);
    }

}

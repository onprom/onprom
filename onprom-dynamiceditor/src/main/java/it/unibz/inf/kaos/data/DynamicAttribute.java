package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

public interface DynamicAttribute {

    UMLClass getRelatedClass();

    Set<DiagramShape> getPath();

    void setPath(Set<DiagramShape> path);

    boolean isPartOfURI();

    void setPartOfURI(final boolean partOfURI);

}

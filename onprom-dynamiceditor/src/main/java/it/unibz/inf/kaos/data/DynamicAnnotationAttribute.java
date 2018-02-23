package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Created by T. E. Kalayci on 25-Oct-2017.
 */
public class DynamicAnnotationAttribute implements DynamicAttribute {
    private DynamicAnnotation annotation;
    private Set<DiagramShape> path;
    private boolean partOfURI;

    DynamicAnnotationAttribute() {
    }

    public DynamicAnnotationAttribute(DynamicAnnotation annotation) {
        this.annotation = annotation;
    }

    public DynamicAnnotation getAnnotation() {
        return annotation;
    }

    @Override
    public Set<DiagramShape> getPath() {
        return path;
    }

    @Override
    public void setPath(Set<DiagramShape> path) {
        this.path = path;
    }

    @Override
    public UMLClass getRelatedClass() {
        return annotation.getRelatedClass();
    }

    @Override
    public boolean isPartOfURI() {
        return partOfURI;
    }

    @Override
    public void setPartOfURI(final boolean partOfURI) {
        this.partOfURI = partOfURI;
    }

    public String getVarName() {
        return annotation.getVarName();
    }

    public String toString() {
        return annotation.toString();
    }
}

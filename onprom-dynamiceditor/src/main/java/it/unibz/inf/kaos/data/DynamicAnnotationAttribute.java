package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Created by T. E. Kalayci on 25-Oct-2017.
 */
public class DynamicAnnotationAttribute {
    private DynamicAnnotation annotation;
    private Set<DiagramShape> path;
    private boolean partOfURI;

    DynamicAnnotationAttribute() {
    }

    public DynamicAnnotationAttribute(DynamicAnnotation annotation) {
        this.annotation = annotation;
    }

    public Set<DiagramShape> getPath() {
        return path;
    }

    public void setPath(Set<DiagramShape> path) {
        this.path = path;
    }

    public UMLClass getRelatedClass() {
        return annotation.getRelatedClass();
    }

    public String getCleanName() {
        return annotation.getCleanName();
    }

    public DynamicAnnotation getAnnotation() {
        return annotation;
    }

    public boolean isPartOfURI() {
        return partOfURI;
    }

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

package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Created by T. E. Kalayci on 14-Nov-2017.
 */
public class DynamicNavigationalAttribute implements DynamicAttribute {
    private boolean partOfURI;
    private NavigationalAttribute attribute;

    DynamicNavigationalAttribute() {

    }

    public DynamicNavigationalAttribute(String value) {
        attribute = new StringAttribute(value);
    }

    public DynamicNavigationalAttribute(NavigationalAttribute navigationalAttribute) {
        attribute = navigationalAttribute;
    }

    public NavigationalAttribute getAttribute() {
        return attribute;
    }

    @Override
    public Set<DiagramShape> getPath() {
        return attribute.getPath();
    }

    @Override
    public void setPath(Set<DiagramShape> path) {
        attribute.setPath(path);
    }

    @Override
    public UMLClass getRelatedClass() {
        return attribute.getUmlClass();
    }

    @Override
    public boolean isPartOfURI() {
        return partOfURI;
    }

    @Override
    public void setPartOfURI(final boolean partOfURI) {
        this.partOfURI = partOfURI;
    }

    public String toString() {
        return attribute.toString();
    }
}

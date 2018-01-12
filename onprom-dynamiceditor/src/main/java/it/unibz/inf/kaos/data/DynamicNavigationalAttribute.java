package it.unibz.inf.kaos.data;

/**
 * Created by T. E. Kalayci on 14-Nov-2017.
 */
public class DynamicNavigationalAttribute extends StringAttribute {
    private boolean partOfIndex;

    DynamicNavigationalAttribute() {

    }

    public DynamicNavigationalAttribute(String value) {
        super(value);
    }

    public DynamicNavigationalAttribute(NavigationalAttribute navigationalAttribute) {
        super(navigationalAttribute.getPath(), navigationalAttribute.getUmlClass(), navigationalAttribute.getAttribute());
    }

    public boolean isPartOfIndex() {
        return partOfIndex;
    }

    public void setPartOfIndex(final boolean partOfIndex) {
        this.partOfIndex = partOfIndex;
    }
}

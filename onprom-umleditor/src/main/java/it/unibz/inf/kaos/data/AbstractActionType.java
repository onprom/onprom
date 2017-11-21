package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.ActionType;

/**
 * Created by T. E. Kalayci on 19-Dec-2017.
 */
public abstract class AbstractActionType implements ActionType {

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public char getMnemonic() {
        return getTitle().charAt(0);
    }

    @Override
    public abstract String getTooltip();

    @Override
    public abstract String getTitle();
}

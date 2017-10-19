package it.unibz.inf.kaos.factory;

import it.unibz.inf.kaos.data.ActionType;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;

/**
 * Created by T. E. Kalayci on 17-Oct-2017.
 */
public abstract class AbstractAnnotationFactory implements AnnotationFactory {

    @Override
    public abstract Annotation createAnnotation(AnnotationDiagramPanel panel, ActionType currentAction, UMLClass selectedCls);

    @Override
    public boolean checkRemoval(AnnotationDiagramPanel panel, Annotation annotation) {
        return true;
    }
}

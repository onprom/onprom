package it.unibz.inf.kaos.interfaces;

import it.unibz.inf.kaos.data.ActionType;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;

/**
 * Created by T. E. Kalayci on 19-Sep-2017.
 */
public interface AnnotationFactory {
    Annotation createAnnotation(AnnotationDiagramPanel panel, ActionType currentAction, UMLClass selectedCls);

    boolean checkRemoval(AnnotationDiagramPanel panel, Annotation annotation);
}

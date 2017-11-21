package it.unibz.inf.kaos.interfaces;

import it.unibz.inf.kaos.data.Annotation;
import it.unibz.inf.kaos.data.UMLClass;

/**
 * Created by T. E. Kalayci on 19-Sep-2017.
 */
@FunctionalInterface
public interface AnnotationFactory {
    Annotation createAnnotation(AnnotationDiagram panel, ActionType currentAction, UMLClass selectedCls);

    default boolean checkRemoval(AnnotationDiagram panel, Annotation annotation) {
        return true;
    }
}

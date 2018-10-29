package it.unibz.inf.kaos.interfaces;

import it.unibz.inf.kaos.data.Annotation;

/**
 * Created by T. E. Kalayci on 19-Sep-2017.
 */
@FunctionalInterface
public interface AnnotationFactory {
    java.util.Optional<Annotation> createAnnotation(AnnotationDiagram panel, ActionType currentAction, it.unibz.inf.kaos.data.UMLClass selectedCls);

    default boolean checkRemoval(AnnotationDiagram panel, Annotation annotation) {
        return true;
    }
}

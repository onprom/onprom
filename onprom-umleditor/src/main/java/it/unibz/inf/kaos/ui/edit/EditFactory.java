package it.unibz.inf.kaos.ui.edit;

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLDiagram;

import javax.swing.undo.UndoableEdit;
import java.util.Collection;
import java.util.List;

/**
 * Created by T. E. Kalayci on 20-Nov-2017.
 */
public class EditFactory {

    public static UndoableEdit anchorCreated(Relationship relation, RelationAnchor anchor, boolean adding) {
        return new AddDeleteAnchorEdit(relation, anchor, adding);
    }

    public static UndoableEdit classCreated(UMLDiagram panel, UMLClass umlClass, boolean adding) {
        return new AddDeleteClassEdit(panel, umlClass, adding);
    }

    public static UndoableEdit relationCreated(UMLDiagram panel, Relationship relationship, AssociationClass associationClass, boolean adding) {
        return new AddDeleteRelationEdit(panel, relationship, associationClass, adding);
    }

    public static UndoableEdit shapesMoved(Collection<DiagramShape> shapes, int moveX, int moveY) {
        return new MoveShapeEdit(shapes, moveX, moveY);
    }

    public static UndoableEdit relationUpdated(Association association, String prevName, Cardinality prevFirst, Cardinality prevSecond) {
        return new UpdateRelationEdit(association, prevName, prevFirst, prevSecond);
    }

    public static UndoableEdit classUpdated(final UMLClass umlClass, final String prevName, final List<Attribute> prevAttributes) {
        return new UpdateClassEdit(umlClass, prevName, prevAttributes);
    }
}

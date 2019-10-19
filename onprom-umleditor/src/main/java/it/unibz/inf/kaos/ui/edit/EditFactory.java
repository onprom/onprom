/*
 * onprom-umleditor
 *
 * EditFactory.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static UndoableEdit anchorCreated(Relationship relation, List<RelationAnchor> anchors, boolean adding) {
        return new AddDeleteAnchorEdit(relation, anchors, adding);
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

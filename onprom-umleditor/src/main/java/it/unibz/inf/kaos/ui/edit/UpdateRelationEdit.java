/*
 * onprom-umleditor
 *
 * UpdateRelationEdit.java
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

import it.unibz.inf.kaos.data.Association;
import it.unibz.inf.kaos.data.Cardinality;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Association update undo-redo action
 * <p>
 *
 * @author T. E. Kalayci
 * Date: 29-Sep-16
 */
class UpdateRelationEdit extends AbstractUndoableEdit {
    private final Association association;
    private String relationName;
    private Cardinality oldFirst;
    private Cardinality oldSecond;

    UpdateRelationEdit(Association _association, String _name, Cardinality _first, Cardinality _second) {
        this.association = _association;
        this.relationName = _name;
        this.oldFirst = _first;
        this.oldSecond = _second;
    }

    private void swap() {
        //swap name
        String name = association.getName();
        association.setName(relationName);
        relationName = name;
        //swap first cardinality
        Cardinality first = association.getFirstMultiplicity();
        association.setFirstMultiplicity(oldFirst);
        oldFirst = first;
        //swap second cardinality
        Cardinality second = association.getSecondMultiplicity();
        association.setSecondMultiplicity(oldSecond);
        oldSecond = second;
    }

    @Override
    public void undo() {
        super.undo();
        swap();
    }

    @Override
    public void redo() {
        super.redo();
        swap();

    }

    @Override
    public String getPresentationName() {
        return String.format("UpdateRelationEdit%s_%s_%s", association
                .toString(), association.getFirstMultiplicity(), association.getSecondMultiplicity());
    }
}

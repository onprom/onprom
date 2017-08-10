/*
 * onprom-umleditor
 *
 * AddDeleteAnchorEdit.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 *  KAOS: Knowledge-Aware Operational Support project
 *  (https://kaos.inf.unibz.it).
 *
 *  Please visit https://onprom.inf.unibz.it for more information.
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

import it.unibz.inf.kaos.data.RelationAnchor;
import it.unibz.inf.kaos.data.Relationship;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Anchor addition and removal undo-redo edit
 * <p>
 * @author T. E. Kalayci
 * Date: 29-Sep-16
 */
public class AddDeleteAnchorEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = 1L;

    private final Relationship relation;
    private final RelationAnchor anchor;
    // true for adding, false for deletion
    private final boolean adding;

    public AddDeleteAnchorEdit(Relationship _rel, RelationAnchor _anchor, boolean
            _adding) {
        relation = _rel;
        anchor = _anchor;
        adding = _adding;
    }

    @Override
    public void undo() {
        super.undo();
        if (adding) {
            delete();
        } else {
            add();
        }
    }

    @Override
    public void redo() {
        super.redo();
        if (adding) {
            add();
        } else {
            delete();
        }
    }

    @Override
    public String getPresentationName() {
        return String.format("AddDeleteAnchorEdit_%s_%s", relation.toString(), anchor.toString());
    }

    private void delete() {
        relation.removeAnchor(anchor);
    }

    private void add() {
        relation.addAnchor(anchor);
    }
}

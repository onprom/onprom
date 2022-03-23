/*
 * onprom-umleditor
 *
 * AddDeleteAnchorEdit.java
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

package it.unibz.inf.onprom.ui.edit;

import it.unibz.inf.onprom.data.RelationAnchor;
import it.unibz.inf.onprom.data.Relationship;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.List;

/**
 * Anchor addition and removal undo-redo edit
 * <p>
 *
 * @author T. E. Kalayci
 * Date: 29-Sep-16
 */
class AddDeleteAnchorEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = 1L;

    private final Relationship relation;
    private final List<RelationAnchor> anchors;
    // true for adding, false for deletion
    private final boolean adding;

    AddDeleteAnchorEdit(Relationship _rel, List<RelationAnchor> _anchors, boolean
            _adding) {
        relation = _rel;
        anchors = _anchors;
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
        return String.format("AddDeleteAnchorEdit_%s_%s", relation.toString(), anchors.toString());
    }

    private void delete() {
        relation.removeAnchor(anchors);
    }

    private void add() {
        relation.addAnchors(anchors);
    }
}

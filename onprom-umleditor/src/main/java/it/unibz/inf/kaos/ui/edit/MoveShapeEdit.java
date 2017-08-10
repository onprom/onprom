/*
 * onprom-umleditor
 *
 * MoveShapeEdit.java
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

import it.unibz.inf.kaos.interfaces.DiagramShape;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.Set;

/**
 * Shape movement undo-redo action
 * <p>
 * @author T. E. Kalayci
 * Date: 29-Sep-16
 */
public class MoveShapeEdit extends AbstractUndoableEdit {
    private final int moveX;
    private final int moveY;
    private final Set<DiagramShape> shapes;

    public MoveShapeEdit(Set<DiagramShape> _cls, int _moveX, int _moveY) {
        shapes = _cls;
        moveX = _moveX;
        moveY = _moveY;
    }

    @Override
    public void undo() {
        super.undo();
        shapes.forEach(obj -> obj.translate(-moveX, -moveY));
    }

    @Override
    public void redo() {
        super.redo();
        shapes.forEach(obj -> obj.translate(moveX, moveY));
    }

    @Override
    public String getPresentationName() {
        return String.format("MoveShapeEdit_%s_%d_%d", shapes.size(), moveX, moveY);
    }
}

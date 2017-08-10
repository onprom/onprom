/*
 * onprom-umleditor
 *
 * UpdateClassEdit.java
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

import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.UMLClass;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.LinkedList;

/**
 * This class stores undo-redo information for class update operations
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class UpdateClassEdit extends AbstractUndoableEdit {
    private final UMLClass cls;
    private String oldName;
    private LinkedList<Attribute> oldAttributes;

    public UpdateClassEdit(UMLClass _cls, String _name,
                           LinkedList<Attribute> _attributes) {
        cls = _cls;
        oldName = _name;
        oldAttributes = _attributes;
    }

    private void swap() {
        String name = cls.getName();
        cls.setName(oldName);
        oldName = name;
        LinkedList<Attribute> attributes = cls.cloneAttributes();
        cls.setAttributes(oldAttributes);
        oldAttributes = attributes;
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
        return String.format("UpdateClassEdit_%s_%s_%s", cls.toString(),
                oldName, oldAttributes.toString());
    }
}

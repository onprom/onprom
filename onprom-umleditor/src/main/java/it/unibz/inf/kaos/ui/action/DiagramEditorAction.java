/*
 * onprom-umleditor
 *
 * DialogAction.java
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

package it.unibz.inf.kaos.ui.action;

import it.unibz.inf.kaos.data.UMLDiagramActions;
import it.unibz.inf.kaos.interfaces.ActionType;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;

/**
 * Opening file action of toolbar button
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class DiagramEditorAction extends ToolbarAction {

    private final DiagramEditor diagramEditor;

    public DiagramEditorAction(ActionType _actionType, DiagramEditor _editor) {
        super(_actionType);
        diagramEditor = _editor;
    }

    @Override
    public void execute() {
        if (actionType.equals(UMLDiagramActions.open)) {
            diagramEditor.open(null);
        } else if (actionType.equals(UMLDiagramActions.export)) {//export as OWL
            diagramEditor.export(false);
        } else if (actionType.equals(UMLDiagramActions.save)) {//save over the files
            diagramEditor.save();
        } else if (actionType.equals(UMLDiagramActions.saveas)) {//save as JSON file
            diagramEditor.export(true);
        } else if (actionType.equals(UMLDiagramActions.close)) {
            diagramEditor.close();

        }
    }
}

/*
 * onprom-umleditor
 *
 * DiagramPanelAction.java
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

package it.unibz.inf.onprom.ui.action;

import it.unibz.inf.onprom.data.UMLDiagramActions;
import it.unibz.inf.onprom.interfaces.ActionType;
import it.unibz.inf.onprom.ui.form.ObjectList;
import it.unibz.inf.onprom.ui.panel.UMLDiagramPanel;
import it.unibz.inf.onprom.ui.utility.DiagramUndoManager;
import it.unibz.inf.onprom.ui.utility.DrawingUtility;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.onprom.ui.utility.UMLEditorMessages;
import it.unibz.inf.onprom.uml.UMLEditor;

/**
 * Default drawing action class to transfer action to the drawing panel
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class DiagramPanelAction extends ToolbarAction {
    private final UMLEditor diagramEditor;
    private final UMLDiagramPanel diagramPanel;

    public DiagramPanelAction(ActionType _action, UMLEditor _editor, UMLDiagramPanel _diagramPanel) {
        super(_action);
        diagramEditor = _editor;
        diagramPanel = _diagramPanel;
  }

  public void execute() {
      if (actionType.equals(UMLDiagramActions.grid)) {
          diagramPanel.toggleGrid();
      } else if (actionType.equals(UMLDiagramActions.delete)) {
          diagramPanel.removeSelected();
      } else if (actionType.equals(UMLDiagramActions.newdiagram)) {
          diagramPanel.clear(UIUtility.confirm(UMLEditorMessages.CLEAR_DIAGRAM));
      } else if (actionType.equals(UMLDiagramActions.objects)) {
          diagramEditor.loadForm(new ObjectList(diagramPanel));
      } else if (actionType.equals(UMLDiagramActions.layout)) {
          DrawingUtility.relayout(diagramPanel.getClasses()::iterator, diagramPanel.getGraphics());
          diagramPanel.repaint();
      } else if (actionType.equals(UMLDiagramActions.undo)) {
        DiagramUndoManager.undo();
        diagramPanel.repaint();
      } else if (actionType.equals(UMLDiagramActions.redo)) {
        DiagramUndoManager.redo();
        diagramPanel.repaint();
      } else if (actionType.equals(UMLDiagramActions.image)) {
          UIUtility.executeInBackground(() -> DrawingUtility.exportImage(diagramPanel), diagramEditor.getProgressBar());
      } else if (actionType.equals(UMLDiagramActions.print)) {
          UIUtility.executeInBackground(() -> DrawingUtility.print(diagramPanel), diagramEditor.getProgressBar());
      } else if (actionType.equals(UMLDiagramActions.disable)) {
          diagramPanel.toggleDisabled();
      }
    diagramPanel.setCurrentAction(actionType);
  }
}

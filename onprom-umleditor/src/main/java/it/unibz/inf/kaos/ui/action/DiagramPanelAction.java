/*
 * onprom-umleditor
 *
 * DrawingPanelAction.java
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
import it.unibz.inf.kaos.ui.panel.UMLDiagramPanel;
import it.unibz.inf.kaos.ui.utility.DiagramUndoManager;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;

/**
 * Default drawing action class to transfer action to the drawing panel
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class DiagramPanelAction extends ToolbarAction {
    private final UMLDiagramPanel diagramPanel;

    public DiagramPanelAction(ActionType _action, UMLDiagramPanel _panel) {
        super(_action);
        diagramPanel = _panel;
  }

  public void execute() {
      if (actionType.equals(UMLDiagramActions.grid)) {
      diagramPanel.toggleGrid();
      } else if (actionType.equals(UMLDiagramActions.delete)) {
      diagramPanel.removeSelected();
      } else if (actionType.equals(UMLDiagramActions.newdiagram)) {
      diagramPanel.clear();
      } else if (actionType.equals(UMLDiagramActions.objects)) {
      diagramPanel.displayObjectList();
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
        DrawingUtility.exportImage(diagramPanel);
      } else if (actionType.equals(UMLDiagramActions.print)) {
        DrawingUtility.print(diagramPanel);
    }
    diagramPanel.setCurrentAction(actionType);
  }
}

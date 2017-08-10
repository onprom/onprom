/*
 * onprom-umleditor
 *
 * ZoomAction.java
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

import it.unibz.inf.kaos.data.ActionType;
import it.unibz.inf.kaos.data.UMLActionType;
import it.unibz.inf.kaos.ui.panel.UMLDiagramPanel;
import it.unibz.inf.kaos.ui.utility.ZoomUtility;

/**
 * Zoom actions of toolbar buttons
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class ZoomAction extends ToolbarAction {

    public ZoomAction(UMLDiagramPanel _panel, ActionType _action) {
        super(_panel, _action);
    }

    @Override
    public void execute() {
      if (actionType.equals(UMLActionType.zoomin)) {
        ZoomUtility.increaseZoom();
      } else if (actionType.equals(UMLActionType.zoomout)) {
        ZoomUtility.decreaseZoom();
      } else if (actionType.equals(UMLActionType.resetzoom)) {
        ZoomUtility.resetZoom();
      }
        diagramPanel.repaint();
    }
}

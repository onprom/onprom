/*
 * onprom-umleditor
 *
 * ZoomAction.java
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
import it.unibz.inf.onprom.ui.utility.ZoomUtility;

import javax.swing.*;

/**
 * Zoom actions of toolbar buttons
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Oct-16
 */
public class ZoomAction extends ToolbarAction {
    private final JPanel diagram;

    public ZoomAction(JPanel _panel, ActionType _action) {
        super(_action);
        diagram = _panel;

    }

    @Override
    public void execute() {
        if (actionType.equals(UMLDiagramActions.zoomin)) {
        ZoomUtility.increaseZoom();
        } else if (actionType.equals(UMLDiagramActions.zoomout)) {
        ZoomUtility.decreaseZoom();
        } else if (actionType.equals(UMLDiagramActions.resetzoom)) {
        ZoomUtility.resetZoom();
      }
        diagram.repaint();
    }
}

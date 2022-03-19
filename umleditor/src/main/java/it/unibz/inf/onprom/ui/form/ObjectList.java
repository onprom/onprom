/*
 * onprom-umleditor
 *
 * ObjectList.java
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

package it.unibz.inf.onprom.ui.form;

import it.unibz.inf.onprom.data.State;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.interfaces.UMLDiagram;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.onprom.ui.utility.UMLEditorButtons;

import javax.swing.*;
import java.awt.*;

/**
 * Listing all objects in the diagram
 *
 * @author T. E. Kalayci
 */
public class ObjectList extends JPanel {
    private DiagramShape previous;
    private State previousState;

    public ObjectList(final UMLDiagram drawingPanel) {

        final DefaultListModel<DiagramShape> mdlObjects = new DefaultListModel<>();
        drawingPanel.getShapes(false).forEach(mdlObjects::addElement);
        JList lstObjects = new JList<>(mdlObjects);
        lstObjects.addListSelectionListener(e -> {
            int selectedIndex = lstObjects.getSelectedIndex();
            if (selectedIndex > -1) {
                if (previous != null) {
                    previous.setState(previousState);
                }
                DiagramShape selected = mdlObjects.getElementAt(selectedIndex);
                previous = selected;
                previousState = selected.getState();
                selected.setState(State.HIGHLIGHTED);
                getParent().repaint();
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(UIUtility.createSmallButton(UMLEditorButtons.REMOVE, e -> {
            int selectedIndex = lstObjects.getSelectedIndex();
            if (selectedIndex > -1) {
                if (drawingPanel.removeShape(mdlObjects.getElementAt(selectedIndex))) {
                    mdlObjects.removeElementAt(selectedIndex);
                }
            }
        }), gridBagConstraints);

        gridBagConstraints.gridy = 1;
        add(UIUtility.createSmallButton(UMLEditorButtons.CLOSE, e -> setVisible(false)), gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;

        JScrollPane tblScroll = new JScrollPane();
        tblScroll.setViewportView(lstObjects);
        tblScroll.setPreferredSize(new Dimension(500, 100));
        add(tblScroll, gridBagConstraints);

        setVisible(true);
    }

}
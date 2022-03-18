/*
 * onprom-umleditor
 *
 * RelationForm.java
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

import it.unibz.inf.onprom.data.Association;
import it.unibz.inf.onprom.data.Cardinality;
import it.unibz.inf.onprom.data.UMLClass;
import it.unibz.inf.onprom.interfaces.UMLDiagram;
import it.unibz.inf.onprom.ui.edit.EditFactory;
import it.unibz.inf.onprom.ui.utility.DiagramUndoManager;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.onprom.ui.utility.UMLEditorButtons;
import it.unibz.inf.onprom.ui.utility.UMLEditorLabels;

import javax.swing.*;
import java.awt.*;

/**
 * Relation form
 * <p>
 * @author T. E. Kalayci
 */
public class RelationForm extends JPanel {
    private final JComboBox<Cardinality> cmbFirstCardinality;
    private final JComboBox<Cardinality> cmbSecondCardinality;
    private final JComboBox<UMLClass> cmbFirstClass;
    private final JComboBox<UMLClass> cmbSecondClass;
    private final Association association;
    private final JTextField txtRelationName;

    public RelationForm(UMLDiagram drawingPanel, Association _association) {
        association = _association;
        final boolean isUpdateAllowed = drawingPanel.isUpdateAllowed();
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        final Dimension lblDimension = new Dimension(100, 25);
        final Dimension txtDimension = new Dimension(200, 25);

        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridx = 0;
        add(UIUtility.createLabel(UMLEditorLabels.FROM_TO, lblDimension), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        cmbFirstClass = UIUtility.createWideComboBox(drawingPanel.getClasses(), txtDimension, null, isUpdateAllowed, true);
        add(cmbFirstClass, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        cmbSecondClass = UIUtility.createWideComboBox(drawingPanel.getClasses(), txtDimension, null, isUpdateAllowed, true);
        add(cmbSecondClass, gridBagConstraints);


        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridx = 0;
        add(UIUtility.createLabel(UMLEditorLabels.CARDINALITY, lblDimension), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        cmbFirstCardinality = UIUtility.createWideComboBox(Cardinality.values(), txtDimension, null, isUpdateAllowed, false);
        add(cmbFirstCardinality, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        cmbSecondCardinality = UIUtility.createWideComboBox(Cardinality.values(), txtDimension, null, isUpdateAllowed, false);
        add(cmbSecondCardinality, gridBagConstraints);


        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        add(UIUtility.createLabel(UMLEditorLabels.RELATION_NAME, lblDimension), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        txtRelationName = UIUtility.createTextField(UMLEditorLabels.RELATION_NAME.getTooltip(), new Dimension(400, 25), isUpdateAllowed);
        add(txtRelationName, gridBagConstraints);
        txtRelationName.setText(association.getName());

        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridx = 3;
        add(UIUtility.createButton(UMLEditorButtons.CANCEL, e -> setVisible(false), lblDimension), gridBagConstraints);
        if (isUpdateAllowed) {
            gridBagConstraints.gridy = 0;
            add(UIUtility.createButton(UMLEditorButtons.SAVE, e -> {
                String prevName = association.getName();
                Cardinality prevFirst = association.getFirstMultiplicity();
                Cardinality prevSecond = association.getSecondMultiplicity();
                association.setName(txtRelationName.getText());
                association.setFirstMultiplicity((Cardinality) cmbFirstCardinality.getSelectedItem());
                association.setSecondMultiplicity((Cardinality) cmbSecondCardinality.getSelectedItem());
                association.setFirstClass((UMLClass) cmbFirstClass.getSelectedItem());
                association.setSecondClass((UMLClass) cmbSecondClass.getSelectedItem());
                setVisible(false);
                //undo operation
                DiagramUndoManager.addEdit(EditFactory.relationUpdated(association, prevName, prevFirst, prevSecond));
            }, lblDimension), gridBagConstraints);
        }

        cmbFirstCardinality.setSelectedItem(association.getFirstMultiplicity());
        cmbSecondCardinality.setSelectedItem(association.getSecondMultiplicity());
        cmbFirstClass.setSelectedItem(association.getFirstClass());
        cmbSecondClass.setSelectedItem(association.getSecondClass());
    }
}

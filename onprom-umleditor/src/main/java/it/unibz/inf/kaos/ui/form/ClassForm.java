/*
 * onprom-umleditor
 *
 * ClassForm.java
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

package it.unibz.inf.kaos.ui.form;

import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.Association;
import it.unibz.inf.kaos.data.AssociationClass;
import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.component.AttributeTable;
import it.unibz.inf.kaos.ui.edit.EditFactory;
import it.unibz.inf.kaos.ui.utility.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This form is used to submit class information both for new class creation and
 * updating available classes.
 *
 * @author T. E. Kalayci
 */
public class ClassForm extends JPanel {
    private final JTextField txtName;
    private final AttributeTable tblAttributes;
    private JComboBox<Association> cmbRelations;
    private final UMLClass newClass;
    private final String prevName;
    private final List<Attribute> prevAttributes;

    public ClassForm(UMLDiagram drawingPanel, UMLClass _newClass) {
        newClass = _newClass;
        final boolean isUpdateAllowed = drawingPanel.isUpdateAllowed();
        tblAttributes = new AttributeTable(isUpdateAllowed);

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        final Dimension lblDimension = new Dimension(85, 25);
        final Dimension txtDimension = new Dimension(200, 25);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(UIUtility.createLabel(UMLEditorLabels.CLASS_NAME, lblDimension), gridBagConstraints);

        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        txtName = UIUtility.createTextField(UMLEditorLabels.CLASS_NAME.getTooltip(), txtDimension, e -> ok(), isUpdateAllowed);
        add(txtName, gridBagConstraints);

        if (newClass instanceof AssociationClass) {
            //show relation
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            add(UIUtility.createLabel(UMLEditorLabels.RELATION, lblDimension), gridBagConstraints);

            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            cmbRelations = UIUtility.createWideComboBox(drawingPanel.getAssociations(), txtDimension, null, true, false);
            cmbRelations.setEditable(isUpdateAllowed);
            cmbRelations.setEnabled(isUpdateAllowed);
            cmbRelations.setSelectedItem(((AssociationClass) newClass).getAssociation());
            add(cmbRelations, gridBagConstraints);
        }

        gridBagConstraints.gridwidth = 1;

        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridx = 4;
        add(UIUtility.createButton(UMLEditorButtons.CANCEL, e -> setVisible(false), lblDimension), gridBagConstraints);
        if (isUpdateAllowed) {
            gridBagConstraints.gridy = 0;
            add(UIUtility.createButton(UMLEditorButtons.SAVE, e -> ok(), lblDimension), gridBagConstraints);

            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            add(UIUtility.createSmallButton(UMLEditorButtons.ADD, e -> tblAttributes.addEmptyAttribute()), gridBagConstraints);

            gridBagConstraints.gridy = 1;
            add(UIUtility.createSmallButton(UMLEditorButtons.REMOVE, e -> tblAttributes.removeSelectedAttribute()), gridBagConstraints);

            gridBagConstraints.gridy = 2;
            add(UIUtility.createSmallButton(UMLEditorButtons.UP, e -> tblAttributes.moveUp()), gridBagConstraints);

            gridBagConstraints.gridy = 3;
            add(UIUtility.createSmallButton(UMLEditorButtons.DOWN, e -> tblAttributes.moveDown()), gridBagConstraints);
        }

        //attributes table
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        JScrollPane tblScroll = new JScrollPane();
        tblScroll.setPreferredSize(new Dimension(500, 125));
        tblScroll.setViewportView(tblAttributes);
        if (isUpdateAllowed) {
            tblScroll.addMouseListener(tblAttributes);
        }
        tblAttributes.setEnabled(isUpdateAllowed);
        add(tblScroll, gridBagConstraints);

        // load class form to update information
        prevName = newClass.getName();
        prevAttributes = Lists.newLinkedList();
        if (newClass.getAttributeCount() > 0) {
            prevAttributes.addAll(newClass.getAttributes().stream().map(Attribute::getClone).collect(Collectors.toList()));
        }
        txtName.setText(newClass.getName());
        tblAttributes.setAttributes(newClass.getAttributes().stream().map(Attribute::getClone).collect(Collectors.toList()));
    }

    private void ok() {
        if (txtName.getText().isEmpty()) {
            UIUtility.error(UMLEditorMessages.CLASS_NAME_ERROR);
        } else {
            String name = txtName.getText();
            if (!name.equals(newClass.getName()) && UIUtility.isNameExist(name)) {
                UIUtility.error(UMLEditorMessages.CLASS_NAME_DUPLICATE_ERROR);
            } else {
                if (!newClass.getName().isEmpty()) {
                    UIUtility.removeName(newClass.getName());
                }
                newClass.setName(name);
                //add name to the unique name set
                UIUtility.addName(newClass.getName());
                newClass.setAttributes(tblAttributes.getAttributes());
                //remove previous names of attributes
                if (prevAttributes != null && !prevAttributes.isEmpty()) {
                    prevAttributes.forEach(attr -> UIUtility.removeName(attr.getName()));
                }
                if (newClass instanceof AssociationClass) {
                    AssociationClass aClass = (AssociationClass) newClass;
                    Association association = cmbRelations.getItemAt(cmbRelations.getSelectedIndex());
                    if (!aClass.getAssociation().equals(association)) {
                        aClass.getAssociation().removeAssociationClass(aClass);
                        association.setAssociationClass(aClass);
                        aClass.setAssociation(association);
                    }
                }
                //add names of all attributes to the unique name set
                newClass.getAttributes().forEach(attr -> UIUtility.addName(attr.getName()));
                DiagramUndoManager.addEdit(EditFactory.classUpdated(newClass, prevName, prevAttributes));
                setVisible(false);
            }
        }
    }
}

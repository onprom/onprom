/*
 * onprom-annoeditor
 *
 * AttributeForm.java
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

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.AnnotationAttributeTable;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

/**
 * @author T. E. Kalayci on 8-Feb-17.
 */
class AttributeForm extends AbstractAnnotationForm {
    static final String[] NAMES = {"concept:name", "time:timestamp", "lifecycle:transition", "org:resource"};
    static final String[] TYPES = {"literal", "timestamp"};
    private final JComboBox<String> txtName;
    private final JComboBox<NavigationalAttribute> cmbValue;
    private final JComboBox<Set<DiagramShape>> cmbValuePath;
    private final AnnotationAttributeTable tblAttributes;
    private JComboBox<String> txtType;
    private JTextField txtValueFilter;

    AttributeForm(AnnotationDiagram _drawingPanel, Annotation _annotation) {
        this(_drawingPanel, _annotation, true, false, NAMES, TYPES, null);
    }

    AttributeForm(AnnotationDiagram _drawingPanel, Annotation _annotation, boolean withFilter, boolean withType, Set<NavigationalAttribute> values) {
        this(_drawingPanel, _annotation, withFilter, withType, NAMES, TYPES, values);
    }

    private AttributeForm(AnnotationDiagram _drawingPanel, Annotation _annotation, boolean withFilter, boolean withType, String[] names, String[] types, Set<NavigationalAttribute> values) {

        super(_drawingPanel, _annotation);

        tblAttributes = new AnnotationAttributeTable(withType);

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        //components in the first row
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        add(UIUtility.createLabel(AnnotationEditorLabels.NAME, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        txtName = UIUtility.createWideComboBox(names, TXT_SIZE, null, true, true);
        add(txtName, gridBagConstraints);

        //components in the second row
        gridBagConstraints.gridy += 1;
        gridBagConstraints.gridx = 0;
        add(UIUtility.createLabel(AnnotationEditorLabels.VALUE, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        cmbValue = UIUtility.createWideComboBox(values, TXT_SIZE, e -> populateValuePath(), true, true);
        add(cmbValue, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        cmbValuePath = UIUtility.createWideComboBox(TXT_SIZE, null, false, true);
        add(cmbValuePath, gridBagConstraints);

        gridBagConstraints.gridx = 3;
        add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM,
                e -> startNavigation(new UpdateListener() {
                    @Override
                    public void updateAttribute(Set<DiagramShape> navigation, UMLClass selectedClass, Attribute selectedAttribute) {
                        cmbValue.setSelectedItem(new StringAttribute(navigation, selectedClass, selectedAttribute));
                    }
                }, false)), gridBagConstraints);

        if (withFilter) {
            gridBagConstraints.gridy += 1;
            gridBagConstraints.gridx = 0;
            add(UIUtility.createLabel(AnnotationEditorLabels.FILTER, BTN_SIZE), gridBagConstraints);

            gridBagConstraints.gridx = 1;
            txtValueFilter = UIUtility.createTextField(AnnotationEditorLabels.FILTER.getTooltip(), TXT_SIZE);
            add(txtValueFilter, gridBagConstraints);
        }

        if (withType) {
            gridBagConstraints.gridy += 1;
            gridBagConstraints.gridx = 0;
            add(UIUtility.createLabel(AnnotationEditorLabels.TYPE, BTN_SIZE), gridBagConstraints);

            gridBagConstraints.gridx = 1;
            txtType = UIUtility.createWideComboBox(types, TXT_SIZE, null, true, true);
            add(txtType, gridBagConstraints);
        }

        //attribute buttons
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        JButton btnAdd = UIUtility.createSmallButton(AnnotationEditorButtons.ADD, e -> ok());
        add(btnAdd, gridBagConstraints);

        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        JButton btnRemove = UIUtility.createSmallButton(AnnotationEditorButtons.REMOVE, e -> tblAttributes.removeSelectedAttribute());
        add(btnRemove, gridBagConstraints);

        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        tblAttributes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                populateForm();
            }
        });
        JScrollPane tblScroll = new JScrollPane();
        tblScroll.setPreferredSize(new Dimension(400, 100));
        tblScroll.setViewportView(tblAttributes);
        add(tblScroll, gridBagConstraints);
    }

    @Override
    protected void populateForm() {
        if (tblAttributes.getSelectedRow() < 1) {
            txtName.setSelectedItem("");
            cmbValue.setSelectedItem("");
            cmbValuePath.setSelectedItem("");
            if (txtValueFilter != null)
                txtValueFilter.setText("");
            if (txtType != null)
                txtType.setSelectedItem("");
        }
        tblAttributes.getSelectedAttribute().ifPresent(attribute -> {
            txtName.setSelectedItem(attribute.getName());
            cmbValue.setSelectedItem(attribute.getValue());
            cmbValuePath.setSelectedItem(attribute.getValue().getPath());
            if (txtValueFilter != null)
                txtValueFilter.setText(attribute.getValue().getFilterClause());
            if (txtType != null)
                txtType.setSelectedItem(attribute.getType());
        });
    }

    private void populateValuePath() {
        if (cmbValue != null && cmbValue.getItemCount() > 0 && cmbValue.getSelectedItem() != null) {
            if (cmbValue.getSelectedItem() instanceof NavigationalAttribute) {
                NavigationalAttribute attribute = (NavigationalAttribute) cmbValue.getSelectedItem();
                UIUtility.loadItems(cmbValuePath, NavigationUtility.getFunctionalPaths(annotation.getRelatedClass(), attribute.getUmlClass()));
            }
        }
    }

    private void ok() {
        Object object = cmbValue.getSelectedItem();
        StringAttribute attributeValue = new StringAttribute();
        if (object instanceof StringAttribute) {
            attributeValue = new StringAttribute(object.toString());
        } else if (object instanceof NavigationalAttribute) {
            NavigationalAttribute attribute = (NavigationalAttribute) object;
            attributeValue = new StringAttribute(cmbValuePath.getItemAt(cmbValuePath.getSelectedIndex()), attribute.getUmlClass(), attribute.getAttribute());
        } else if (object != null) {
            attributeValue = new StringAttribute(object.toString());
        }
        if (txtValueFilter != null) attributeValue.setFilterClause(txtValueFilter.getText());
        if (txtName.getSelectedItem() != null) {
            AnnotationAttribute attribute = new AnnotationAttribute(txtName.getSelectedItem().toString(), attributeValue);
            if (txtType != null && txtType.getSelectedItem() != null) {
                attribute.setType(txtType.getSelectedItem().toString());
            }
            if (tblAttributes.getSelectedRow() > -1) {
                tblAttributes.updateAttributeAt(tblAttributes.getSelectedRow(), attribute);
            } else {
                tblAttributes.addAttribute(attribute);
            }
            tblAttributes.clearSelection();
        }
        populateForm();
    }

    List<AnnotationAttribute> getAttributes() {
        return tblAttributes.getAttributes();
    }

    void setAttributes(List<AnnotationAttribute> attributes) {
        tblAttributes.setAttributes(attributes);
    }
}
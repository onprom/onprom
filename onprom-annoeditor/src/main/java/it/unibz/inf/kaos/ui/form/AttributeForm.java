/*
 * onprom-annoeditor
 *
 * AttributeForm.java
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

package it.unibz.inf.kaos.ui.form;

import it.unibz.inf.kaos.data.AnnotationAttribute;
import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.StringAttribute;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.AnnotationAttributeTable;
import it.unibz.inf.kaos.ui.component.StringDocumentListener;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Set;

/**
 * Form to add additional attributes
 * <p>
 * @author T. E. Kalayci on 08/02/17.
 */
class AttributeForm extends AbstractAnnotationForm {

  final private JTextField txtName;
  final private JTextField txtValue;
  final private JTextField txtValueFilter;

  private final AnnotationAttributeTable tblAttributes;
  private final StringDocumentListener valueListener;
  private StringAttribute value = new StringAttribute();

  AttributeForm(AnnotationDiagram _drawingPanel, Annotation _annotation) {
    super(_drawingPanel, _annotation);

    tblAttributes = new AnnotationAttributeTable();

    setLayout(new GridBagLayout());
    GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

    final Dimension txtDimension = new Dimension(200, 25);

    //components in the first row
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridx = 0;
    add(UIUtility.createLabel(AnnotationEditorLabels.NAME, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    txtName = UIUtility.createTextField("", txtDimension);
    add(txtName, gridBagConstraints);

    //components in the second row
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridx = 0;
    add(UIUtility.createLabel(AnnotationEditorLabels.VALUE, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    txtValue = UIUtility.createTextField("Please enter name of the attribute", txtDimension);
    valueListener = new StringDocumentListener(value, txtValue);
    txtValue.getDocument().addDocumentListener(valueListener);
    add(txtValue, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM,
      e -> startNavigation(new UpdateListener() {
        @Override
        public void updateAttribute(Set<DiagramShape> navigation, UMLClass selectedClass, Attribute selectedAttribute) {
          valueListener.updateAttribute(null);
          value = new StringAttribute(navigation, selectedClass, selectedAttribute);
          txtValue.setText(value.toString());
          valueListener.updateAttribute(value);
        }
      }, false)), gridBagConstraints);

    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridx = 0;
    add(UIUtility.createLabel(AnnotationEditorLabels.FILTER, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    txtValueFilter = UIUtility.createTextField(AnnotationEditorLabels.FILTER.getTooltip(), txtDimension);
    add(txtValueFilter, gridBagConstraints);

    //attribute buttons
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    JButton btnAdd = UIUtility.createSmallButton(AnnotationEditorButtons.ADD, e -> ok());
    add(btnAdd, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    JButton btnRemove = UIUtility.createSmallButton(AnnotationEditorButtons.REMOVE, e -> tblAttributes.removeSelectedAttribute());
    add(btnRemove, gridBagConstraints);

    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    tblAttributes.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        populateForm();
      }
    });
    JScrollPane tblScroll = new JScrollPane();
    tblScroll.setPreferredSize(new Dimension(500, 100));
    tblScroll.setViewportView(tblAttributes);
    add(tblScroll, gridBagConstraints);
  }

  public void populateForm() {
    valueListener.updateAttribute(null);
    if (tblAttributes.getSelectedRow() > -1) {
      AnnotationAttribute attribute = tblAttributes.getSelectedAttribute();
      txtName.setText(attribute.getName());
      value = attribute.getValue();
      txtValue.setText(value.toString());
      txtValueFilter.setText(value.getFilterClause());
    } else {
      //clean the form
      txtName.setText("");
      value = new StringAttribute();
      txtValue.setText("");
      txtValueFilter.setText("");
    }
    valueListener.updateAttribute(value);
  }

  private void ok() {
    value.setFilterClause(txtValueFilter.getText());
    AnnotationAttribute attribute = new AnnotationAttribute(txtName.getText(), value.getClone());
    if (tblAttributes.getSelectedRow() > -1) {
      tblAttributes.updateAttributeAt(tblAttributes.getSelectedRow(), attribute);
    } else {
      tblAttributes.addAttribute(attribute);
    }
    tblAttributes.clearSelection();
    populateForm();
  }

  LinkedList<AnnotationAttribute> getAttributes() {
    return tblAttributes.getAttributes();
  }

  void setAttributes(LinkedList<AnnotationAttribute> attributes) {
    tblAttributes.setAttributes(attributes);
  }
}
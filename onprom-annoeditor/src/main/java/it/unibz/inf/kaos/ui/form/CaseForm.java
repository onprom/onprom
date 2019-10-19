/*
 * onprom-annoeditor
 *
 * CaseForm.java
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
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.*;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Form to submit/update case information
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
public class CaseForm extends AbstractAnnotationForm {

  //components
  private final JComboBox<NavigationalAttribute> cmbName;
  private final JComboBox<Set<DiagramShape>> cmbNamePath;
  private final JTextField txtNameFilter;

  private final AttributeForm attributeForm;

  public CaseForm(AnnotationDiagram _drawingPanel, CaseAnnotation _annotation) {
    super(_drawingPanel, _annotation);

    JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.NAME, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    // allow selecting available field only for the name
    cmbName = UIUtility.createWideComboBox(drawingPanel.findAttributes(annotation.getRelatedClass(), true),
            TXT_SIZE, e -> populateTimestampPath(), true, true);
    mainPanel.add(cmbName, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    cmbNamePath = UIUtility.createWideComboBox(TXT_SIZE, null, true, true);
    mainPanel.add(cmbNamePath, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    JButton btnNameAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
        cmbName.setSelectedItem(new StringAttribute(path, selectedClass, selectedAttribute));
      }
    }, false));
    mainPanel.add(btnNameAdd, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.FILTER, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    txtNameFilter = UIUtility.createTextField(TXT_SIZE);
    mainPanel.add(txtNameFilter, gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> ok(), BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 1;
      mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> setVisible(false), BTN_SIZE), gridBagConstraints);

    attributeForm = new AttributeForm(drawingPanel, annotation);
    addTabbedPane(mainPanel, attributeForm);
  }

  private void populateTimestampPath() {
    if (cmbName != null && cmbName.getItemCount() > 0 && cmbName.getSelectedItem() != null) {
      UIUtility.loadItems(cmbNamePath, NavigationUtility.getFunctionalPaths(annotation.getRelatedClass(),
              ((NavigationalAttribute) cmbName.getSelectedItem()).getUmlClass()));
    }
  }

    @Override
    public void populateForm() {
        if (annotation != null) {
            CaseAnnotation caseAnnotation = (CaseAnnotation) annotation;
            if (caseAnnotation.getCaseName() != null) {
              cmbName.setSelectedItem(caseAnnotation.getCaseName());
              cmbNamePath.setSelectedItem(caseAnnotation.getCaseName().getPath());
              txtNameFilter.setText(caseAnnotation.getCaseName().getFilterClause());
                attributeForm.setAttributes(annotation.getAttributes());
            }
        }
    }

  private void ok() {
    if (cmbName.getSelectedItem() == null) {
      UIUtility.error(AnnotationEditorMessages.CASE_NAME_ERROR);
    } else {
      CaseAnnotation caseAnnotation = (CaseAnnotation) annotation;
      NavigationalAttribute name = (NavigationalAttribute) cmbName.getSelectedItem();
      name.setPath((Set<DiagramShape>) cmbNamePath.getSelectedItem());
      name.setFilterClause(txtNameFilter.getText());
      caseAnnotation.setCaseName(name);
      caseAnnotation.setAttributes(attributeForm.getAttributes());
      setVisible(false);
    }
  }
}
/*
 * onprom-annoeditor
 *
 * ResourceForm.java
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

import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.ResourceAnnotation;
import it.unibz.inf.kaos.data.StringAttribute;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.StringDocumentListener;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Form to submit/update resource information
 * <p>
 * @author T. E. Kalayci on 09-Nov-16.
 */
public class ResourceForm extends AbstractAnnotationForm {

  //components
  private final JTextField txtLabel;
  private final JTextField txtResource;
  private final JTextField txtFilter;
  private final AttributeForm attributeForm;
  //text field listener for resource attribute
  private final StringDocumentListener resourceListener;
  //navigational resource attribute
  private StringAttribute resource = new StringAttribute();

  public ResourceForm(AnnotationDiagram _drawingPanel, ResourceAnnotation _annotation) {
    super(_drawingPanel, _annotation);

    JPanel mainPanel = new JPanel(new GridBagLayout());

    GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.LABEL, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    txtLabel = UIUtility.createTextField(TXT_SIZE);
    mainPanel.add(txtLabel, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.RESOURCE, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    txtResource = UIUtility.createTextField(TXT_SIZE);
    resourceListener = new StringDocumentListener(resource, txtResource);
    txtResource.getDocument().addDocumentListener(resourceListener);
    mainPanel.add(txtResource, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
        resourceListener.updateAttribute(null);
        resource = new StringAttribute(path, selectedClass, selectedAttribute);
        txtResource.setText(resource.toString());
        resourceListener.updateAttribute(resource);
      }
    }, false)), gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.FILTER, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    txtFilter = UIUtility.createTextField(TXT_SIZE);
    mainPanel.add(txtFilter, gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> ok(), BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> {
      setVisible(false);
      drawingPanel.resetNavigation();
    }, BTN_SIZE), gridBagConstraints);

    attributeForm = new AttributeForm(drawingPanel, annotation);
    addTabbedPane(mainPanel, attributeForm);
  }

  public void populateForm() {
    if (annotation != null) {
      ResourceAnnotation resourceAnnotation = (ResourceAnnotation) annotation;
      txtLabel.setText(resourceAnnotation.getLabel());
      if (resourceAnnotation.getResource() != null) {
        resourceListener.updateAttribute(null);
        resource = resourceAnnotation.getResource();
        txtResource.setText(resource.toString());
        txtFilter.setText(resource.getFilterClause());
        resourceListener.updateAttribute(resource);
      }
      attributeForm.setAttributes(resourceAnnotation.getAttributes());
    }
  }

  private void ok() {
    ResourceAnnotation resourceAnnotation = (ResourceAnnotation) annotation;
    if (resource == null && txtResource.getText().isEmpty()) {
      UIUtility.error(AnnotationEditorMessages.RESOURCE_MISSING_ERROR);
      return;
    }
    resource.setFilterClause(txtFilter.getText());
    resourceAnnotation.setResource(resource);
    resourceAnnotation.setLabel(txtLabel.getText());
    resourceAnnotation.setAttributes(attributeForm.getAttributes());
    setVisible(false);
  }
}
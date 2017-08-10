/*
 * onprom-annoeditor
 *
 * EventForm.java
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
import it.unibz.inf.kaos.data.DataType;
import it.unibz.inf.kaos.data.EventAnnotation;
import it.unibz.inf.kaos.data.NavigationalAttribute;
import it.unibz.inf.kaos.data.ResourceAnnotation;
import it.unibz.inf.kaos.data.StringAttribute;
import it.unibz.inf.kaos.data.TransactionalLifecycle;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.StringDocumentListener;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Form to submit/update event information
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
public class EventForm extends AbstractAnnotationForm {
  private static final Logger logger = LoggerFactory.getLogger(EventForm.class.getName());
  //components
  private final JTextField txtName;
  private final JTextField txtNameFilter;
  private final JTextField txtLabel;

  private final JComboBox<NavigationalAttribute> cmbTimestamp;
  private final JComboBox<Set<DiagramShape>> cmbTimestampPath;

  private final JComboBox<NavigationalAttribute> cmbResource;
  private final JComboBox<Set<DiagramShape>> cmbResourcePath;

  private final JComboBox<Set<DiagramShape>> cmbTracePath;

  private final JComboBox<TransactionalLifecycle> cmbLifecycle;
  private final AttributeForm attributeForm;
  private final NavigationalAttribute resource = null;
  private final StringDocumentListener nameListener;
  //variables
  private NavigationalAttribute timestamp = null;
  private NavigationalAttribute tracePath;
  private StringAttribute name = new StringAttribute();

  public EventForm(AnnotationDiagram _drawingPanel, EventAnnotation eventAnnotation) {
    super(_drawingPanel, eventAnnotation);

    JPanel mainPanel = new JPanel(new GridBagLayout());

    GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.NAME, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    txtName = UIUtility.createTextField("", TXT_SIZE, e -> ok());
    nameListener = new StringDocumentListener(name, txtName);
    txtName.getDocument().addDocumentListener(nameListener);
    mainPanel.add(txtName, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    txtNameFilter = UIUtility.createTextField("Enter name filter to this field", TXT_SIZE, e -> ok());
    mainPanel.add(txtNameFilter, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
        nameListener.updateAttribute(null);
        name = new StringAttribute(path, selectedClass, selectedAttribute);
        txtName.setText(name.toString());
        nameListener.updateAttribute(name);
      }
    }, false)), gridBagConstraints);

    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.LABEL, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    txtLabel = UIUtility.createTextField(AnnotationEditorLabels.LABEL.getTooltip(), TXT_SIZE);
    mainPanel.add(txtLabel, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.TIMESTAMP, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    cmbTimestamp = UIUtility.createWideComboBox(drawingPanel.getAttributes(eventAnnotation.getRelatedClass(), true, DataType.DATE_TIME, DataType.DATE_TIME_STAMP), TXT_SIZE, e -> populateTimestampPath(), true, true);
    mainPanel.add(cmbTimestamp, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    cmbTimestampPath = UIUtility.createWideComboBox(null, TXT_SIZE, null, true, true);
    mainPanel.add(cmbTimestampPath, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
        timestamp = new NavigationalAttribute(path, selectedClass, selectedAttribute);
        cmbTimestamp.setSelectedItem(timestamp);
      }

      @Override
      public DataType[] getDataType() {
        return UpdateListener.TIMESTAMP_TYPES;
      }
    }, true)), gridBagConstraints);

    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 1;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.LIFECYCLE, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 1;
    cmbLifecycle = UIUtility.createWideComboBoxArray(TransactionalLifecycle.values(), TXT_SIZE, true);
    mainPanel.add(cmbLifecycle, gridBagConstraints);

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.EVENT_RESOURCE, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    cmbResource = UIUtility.createWideComboBox(drawingPanel.getAnnotations(eventAnnotation.getRelatedClass(), false, ResourceAnnotation.class), TXT_SIZE, e -> populateResourcePath(), true, true);
    mainPanel.add(cmbResource, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    cmbResourcePath = UIUtility.createWideComboBox(null, TXT_SIZE, null, true, true);
    mainPanel.add(cmbResourcePath, gridBagConstraints);

    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    mainPanel.add(UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> navigation, UMLClass selectedClass, Attribute selectedAttribute) {
        //TODO should we only update path to the RESOURCE or recreate the instance?
        if (resource != null) {
          resource.setPath(navigation);
        }
      }
    }, false)), gridBagConstraints);

    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 2;
    mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.CASE_PATH, BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 2;
    cmbTracePath = UIUtility.createWideComboBox(NavigationUtility.getAllPaths(eventAnnotation.getRelatedClass(), eventAnnotation.getCase().getRelatedClass()), TXT_SIZE, null, true, false);
    mainPanel.add(cmbTracePath, gridBagConstraints);

    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 2;
    JButton btnTraceAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
      @Override
      public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
        tracePath = new NavigationalAttribute(path, selectedClass, selectedAttribute);
        cmbTracePath.setSelectedItem(tracePath);
      }
    }, false));
    mainPanel.add(btnTraceAdd, gridBagConstraints);

    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> ok(), BTN_SIZE), gridBagConstraints);

    gridBagConstraints.gridx = 6;
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
      EventAnnotation eventAnnotation = (EventAnnotation) annotation;
      txtLabel.setText(eventAnnotation.getLabel());
      if (eventAnnotation.getCasePath() != null) {
        cmbTracePath.setSelectedItem(eventAnnotation.getCasePath());
      }
      if (eventAnnotation.getEventName() != null) {
        nameListener.updateAttribute(null);
        name = eventAnnotation.getEventName();
        txtName.setText(name.toString());
        txtNameFilter.setText(name.getFilterClause());
        nameListener.updateAttribute(name);
      }
      cmbTimestamp.setSelectedItem(eventAnnotation.getTimestamp());
      if (eventAnnotation.getTimestamp() != null) {
        cmbTimestampPath.setSelectedItem(eventAnnotation.getTimestamp().getPath());
      }
      cmbResource.setSelectedItem(eventAnnotation.getResource());
      if (eventAnnotation.getResource() != null) {
        cmbResourcePath.setSelectedItem(eventAnnotation.getResource().getPath());
      }
      cmbLifecycle.setSelectedItem(eventAnnotation.getLifecycle());
      attributeForm.setAttributes(eventAnnotation.getAttributes());
    }
  }

  private void populateResourcePath() {
    try {
      NavigationalAttribute ra = (NavigationalAttribute) cmbResource.getSelectedItem();
      UIUtility.loadItems(cmbResourcePath, NavigationUtility.getAllPaths(annotation.getRelatedClass(), ra.getUmlClass()));
    } catch (Exception e) {
      logger.info(e.getMessage());
    }
  }

  private void populateTimestampPath() {
    try {
      NavigationalAttribute ts = (NavigationalAttribute) cmbTimestamp.getSelectedItem();
      UIUtility.loadItems(cmbTimestampPath, NavigationUtility.getFunctionalPaths(annotation.getRelatedClass(), ts.getUmlClass()));
    } catch (Exception e) {
      logger.info(e.getMessage());
    }
  }

  private void ok() {
    if (name == null && txtName.getText().isEmpty()) {
      UIUtility.error(AnnotationEditorMessages.EVENT_NAME_ERROR);
    } else if (timestamp == null && cmbTimestamp.getSelectedItem() == null) {
      UIUtility.error(AnnotationEditorMessages.TIMESTAMP_ERROR);
    } else {
      EventAnnotation eventAnnotation = (EventAnnotation) annotation;
      name.setFilterClause(txtNameFilter.getText());
      eventAnnotation.setEventName(name);
      NavigationalAttribute selectedTimestamp = (NavigationalAttribute) cmbTimestamp.getSelectedItem();
      selectedTimestamp.setPath((Set<DiagramShape>) cmbTimestampPath.getSelectedItem());
      eventAnnotation.setTimestamp(selectedTimestamp);
      NavigationalAttribute selectedResource = (NavigationalAttribute) cmbResource.getSelectedItem();
      eventAnnotation.setResource(selectedResource);
      if (selectedResource != null) {
        selectedResource.setPath(cmbResourcePath.getItemAt(cmbResourcePath.getSelectedIndex()));
      }
      Object tracePath = cmbTracePath.getSelectedItem();
      if (tracePath instanceof NavigationalAttribute) {
        eventAnnotation.setCasePath(((NavigationalAttribute) tracePath).getPath());
      } else if (tracePath instanceof Set) {
        eventAnnotation.setCasePath((Set<DiagramShape>) tracePath);
      }
      eventAnnotation.setLifecycle((TransactionalLifecycle) cmbLifecycle.getSelectedItem());
      eventAnnotation.setLabel(txtLabel.getText());
      eventAnnotation.setAttributes(attributeForm.getAttributes());
      setVisible(false);
    }
  }
}
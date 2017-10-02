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

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * @author T. E. Kalayci on 19-Sep-17.
 */
public class EventForm extends AbstractAnnotationForm {
    private final AttributeForm attributeForm;
    private final JComboBox<Set<DiagramShape>> cmbTracePath;
    private NavigationalAttribute tracePath;

    public EventForm(AnnotationDiagram drawingPanel, EventAnnotation eventAnnotation) {
        super(drawingPanel, eventAnnotation);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.CASE_PATH, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        cmbTracePath = UIUtility.createWideComboBox(NavigationUtility.getAllPaths(eventAnnotation.getRelatedClass(), eventAnnotation.getCase().getRelatedClass()), TXT_SIZE, null, true, false);
        mainPanel.add(cmbTracePath, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        JButton btnTraceAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
            @Override
            public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
                tracePath = new NavigationalAttribute(path, selectedClass, selectedAttribute);
                cmbTracePath.setSelectedItem(tracePath);
            }
        }, false));
        mainPanel.add(btnTraceAdd, gridBagConstraints);

        attributeForm = new AttributeForm(drawingPanel, annotation, false, true, drawingPanel.getAttributes(annotation.getRelatedClass(), false, (DataType) null));

        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> {
            Object tracePath = cmbTracePath.getSelectedItem();
            if (tracePath instanceof NavigationalAttribute) {
                eventAnnotation.setCasePath(((NavigationalAttribute) tracePath).getPath());
            } else if (tracePath instanceof Set) {
                eventAnnotation.setCasePath((Set<DiagramShape>) tracePath);
            }
            eventAnnotation.setAttributes(attributeForm.getAttributes());
            setVisible(false);
        }, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> setVisible(false), BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        mainPanel.add(attributeForm, gridBagConstraints);

        add(mainPanel);
    }

    @Override
    public void populateForm() {
        if (annotation != null) {
            attributeForm.setAttributes(annotation.getAttributes());
            cmbTracePath.setSelectedItem(((EventAnnotation) annotation).getCasePath());
        }
    }

}
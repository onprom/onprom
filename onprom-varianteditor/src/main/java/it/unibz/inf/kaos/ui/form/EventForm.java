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
    private final JComboBox<CaseAnnotation> cmbCase;
    private final JComboBox<Set<DiagramShape>> cmbCasePath;
    private final JTextField txtLabel;

    public EventForm(AnnotationDiagram drawingPanel, EventAnnotation eventAnnotation) {
        super(drawingPanel, eventAnnotation);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        mainPanel.add(UIUtility.createLabel(AnnotationEditorLabels.LABEL, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        txtLabel = UIUtility.createTextField(AnnotationEditorLabels.LABEL.getTooltip(), TXT_SIZE);
        mainPanel.add(txtLabel, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        mainPanel.add(UIUtility.createLabel("Case", TXT_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 3;
        cmbCase = UIUtility.createWideComboBox(drawingPanel.getAnnotations(eventAnnotation.getRelatedClass(), true, CaseAnnotation.class), TXT_SIZE, e -> populateCasePath(), false, true);
        mainPanel.add(cmbCase, gridBagConstraints);

        gridBagConstraints.gridx = 4;
        cmbCasePath = UIUtility.createWideComboBox(TXT_SIZE, null, false, true);
        mainPanel.add(cmbCasePath, gridBagConstraints);

        gridBagConstraints.gridx = 5;
        JButton btnTraceAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> super.startNavigation(new UpdateListener() {
            @Override
            public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
                cmbCasePath.setSelectedItem(new NavigationalAttribute(path, selectedClass, selectedAttribute));
            }
        }, false));
        mainPanel.add(btnTraceAdd, gridBagConstraints);

        attributeForm = new AttributeForm(drawingPanel, annotation, false, true, drawingPanel.getAttributes(annotation.getRelatedClass(), false, null));

        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> {
            eventAnnotation.setCase(cmbCase.getItemAt(cmbCase.getSelectedIndex()));
            Object tracePath = cmbCasePath.getSelectedItem();
            if (tracePath instanceof NavigationalAttribute) {
                eventAnnotation.setCasePath(((NavigationalAttribute) tracePath).getPath());
            } else if (tracePath instanceof Set) {
                eventAnnotation.setCasePath((Set<DiagramShape>) tracePath);
            }
            eventAnnotation.setAttributes(attributeForm.getAttributes());
            eventAnnotation.setLabel(txtLabel.getText());
            setVisible(false);
        }, BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        mainPanel.add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> setVisible(false), BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        mainPanel.add(attributeForm, gridBagConstraints);

        add(mainPanel);
    }

    private void populateCasePath() {
        if (cmbCase != null && cmbCase.getItemCount() > 0 && cmbCase.getSelectedItem() != null) {
            CaseAnnotation caseAnnotation = (CaseAnnotation) cmbCase.getSelectedItem();
            if (caseAnnotation != null)
                UIUtility.loadItems(cmbCasePath, NavigationUtility.getFunctionalPaths(annotation.getRelatedClass(), caseAnnotation.getRelatedClass()));
        }
    }

    @Override
    public void populateForm() {
        if (annotation != null) {
            EventAnnotation eventAnnotation = (EventAnnotation) annotation;
            attributeForm.setAttributes(annotation.getAttributes());
            txtLabel.setText(eventAnnotation.getLabel());
            cmbCase.setSelectedItem(eventAnnotation.getCase());
            cmbCasePath.setSelectedItem(eventAnnotation.getCasePath());
        }
    }

}
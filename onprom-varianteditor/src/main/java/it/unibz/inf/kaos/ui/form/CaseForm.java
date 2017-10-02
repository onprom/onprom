/*
 * onprom-annoeditor
 *
 * CaseForm.java
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

import it.unibz.inf.kaos.data.CaseAnnotation;
import it.unibz.inf.kaos.data.DataType;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;

/**
 * @author T. E. Kalayci on 19-Sep-17.
 */
public class CaseForm extends AbstractAnnotationForm {

    private final AttributeForm attributeForm;

    public CaseForm(AnnotationDiagram drawingPanel, CaseAnnotation annotation) {
        super(drawingPanel, annotation);

        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        attributeForm = new AttributeForm(drawingPanel, annotation, false, true, drawingPanel.getAttributes(annotation.getRelatedClass(), false, (DataType) null));
        mainPanel.add(attributeForm, gridBagConstraints);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> {
            this.annotation.setAttributes(attributeForm.getAttributes());
            setVisible(false);
        }, BTN_SIZE), BorderLayout.NORTH);
        buttonPanel.add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> setVisible(false), BTN_SIZE), BorderLayout.SOUTH);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        mainPanel.add(buttonPanel, gridBagConstraints);

        add(mainPanel);
    }

    @Override
    public void populateForm() {
        if (annotation != null) {
            attributeForm.setAttributes(annotation.getAttributes());
        }
    }

}
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

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.component.DynamicAssociationPanel;
import it.unibz.inf.kaos.ui.component.DynamicAttributePanel;
import it.unibz.inf.kaos.ui.component.UpdateListener;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author T. E. Kalayci on 2-Oct-17.
 */
public class DynamicAnnotationForm extends AbstractAnnotationForm {
    private final HashMap<String, DynamicAttributePanel> attributes;
    private final HashMap<String, DynamicAssociationPanel> associations;

    private final JTextField txtLabel;
    private final JCheckBox chkLabel;
    //private final JCheckBox chkURI;

    public DynamicAnnotationForm(AnnotationDiagram drawingPanel, DynamicAnnotation annotation) {
        super(drawingPanel, annotation);

        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();
        List<Attribute> classAttributes = annotation.getAnnotationClassAttributes();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;

        JPanel pnlLabel = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
        chkLabel = UIUtility.createCheckBox("part of the URI");
        pnlLabel.add(chkLabel);
        pnlLabel.add(UIUtility.createLabel("Label: ", BTN_SIZE, new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                chkLabel.setSelected(!chkLabel.isSelected());
            }
        }), gridBagConstraints);
        txtLabel = UIUtility.createTextField(TXT_SIZE);
        pnlLabel.add(txtLabel);
        add(pnlLabel, gridBagConstraints);

        attributes = new HashMap<>(classAttributes.size());
        for (Attribute attribute : classAttributes) {
            gridBagConstraints.gridy++;
            DynamicAttributePanel panel = new DynamicAttributePanel(this, attribute);
            add(panel, gridBagConstraints);
            attributes.put(attribute.getLongName(), panel);
        }

        Collection<Relationship> classRelationships = annotation.getAnnotationClassRelations();
        associations = new HashMap<>(classRelationships.size());
        for (Relationship relationship : classRelationships) {
            //TODO do we need to deal with inheritance relations?
            if (relationship instanceof Association) {
                Association association = (Association) relationship;
                if (association.getSecondClass().equals(annotation.getAnnotationClass())) {
                    gridBagConstraints.gridy++;
                    DynamicAssociationPanel panel = new DynamicAssociationPanel(this, association);
                    add(panel, gridBagConstraints);
                    associations.put(association.getLongName(), panel);
                }
            }
        }

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> {
            attributes.forEach((key, value) -> annotation.setAttributeValue(key, value.getValue()));
            associations.forEach((key, value) -> annotation.setRelationValue(key, value.getValue()));
            annotation.setLabel(txtLabel.getText());
            annotation.setLabelPartOfIndex(chkLabel.isSelected());
            setVisible(false);
        }, AbstractAnnotationForm.BTN_SIZE), gridBagConstraints);

        gridBagConstraints.gridy = 1;
        add(UIUtility.createButton(AnnotationEditorButtons.CANCEL, e -> setVisible(false), AbstractAnnotationForm.BTN_SIZE), gridBagConstraints);
    }

    public Collection<DynamicAnnotationAttribute> getAnnotations() {
        Collection<DynamicAnnotationAttribute> annotationAttributes = Sets.newLinkedHashSet();
        drawingPanel.findAnnotations(annotation.getRelatedClass(), false, DynamicAnnotation.class).forEach(annotation -> annotationAttributes.add(new DynamicAnnotationAttribute(annotation)));
        return annotationAttributes;
    }

    public Collection<DynamicNavigationalAttribute> getAttributes() {
        return drawingPanel.findAttributes(annotation.getRelatedClass(), false).stream().map(DynamicNavigationalAttribute::new).collect(Collectors.toSet());
    }

    public Set<Set<DiagramShape>> getPaths(UMLClass umlClass) {
        return NavigationUtility.getAllPaths(annotation.getRelatedClass(), umlClass);
    }

    @Override
    public void startNavigation(UpdateListener _updateListener, boolean functional) {
        super.startNavigation(_updateListener, functional);
    }

    @Override
    public void populateForm() {
        if (annotation != null) {
            DynamicAnnotation dynamicAnnotation = ((DynamicAnnotation) annotation);
            attributes.forEach((key, value) -> value.setValue(dynamicAnnotation.getAttributeValue(key)));
            associations.forEach((key, value) -> value.setValue(dynamicAnnotation.getRelationValue(key)));
            txtLabel.setText(dynamicAnnotation.getLabel());
            chkLabel.setSelected(dynamicAnnotation.isLabelPartOfIndex());
        }
    }

}
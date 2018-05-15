/*
 * onprom-dynamiceditor
 *
 * DynamicAttributePanel.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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

package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.form.AbstractAnnotationForm;
import it.unibz.inf.kaos.ui.form.DynamicAnnotationForm;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorLabels;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Created by T. E. Kalayci on 25-Oct-2017.
 */
public class DynamicAttributePanel extends JPanel {
    private final DynamicAnnotationForm form;
    private final JComboBox<DynamicAttribute> cmbAttributes;
    private final JComboBox<Set<DiagramShape>> cmbPath;
    private final JTextField txtFilter;
    private final JCheckBox chkIndex;

    public DynamicAttributePanel(DynamicAnnotationForm _form, Attribute attribute) {
        form = _form;
        setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

        chkIndex = UIUtility.createCheckBox(attribute.getName(), "Check the checkbox if " + attribute.getName() + " is a part of the URI");
        add(chkIndex);

        cmbAttributes = UIUtility.createWideComboBox(form.getAttributes(), AbstractAnnotationForm.TXT_SIZE, e -> populatePath(), true, true);
        add(cmbAttributes);

        cmbPath = UIUtility.createWideComboBox(AbstractAnnotationForm.TXT_SIZE, null, false, true);
        add(cmbPath);

        txtFilter = UIUtility.createTextField(AnnotationEditorLabels.FILTER.getTooltip(), AbstractAnnotationForm.TXT_SIZE);
        add(txtFilter);

        JButton btnTraceAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> form.startNavigation(new UpdateListener() {
            @Override
            public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
                cmbPath.setSelectedItem(new NavigationalAttribute(path, selectedClass, selectedAttribute));
            }
        }, false));
        add(btnTraceAdd);
    }

    private void populatePath() {
        form.populatePath(cmbAttributes, cmbPath);
    }

    public DynamicNavigationalAttribute getValue() {
        if (cmbAttributes.getSelectedItem() != null) {
            DynamicNavigationalAttribute navigationalAttribute;
            Object attributeValue = cmbAttributes.getSelectedItem();
            if (attributeValue instanceof DynamicNavigationalAttribute) {
                navigationalAttribute = (DynamicNavigationalAttribute) attributeValue;
                if (cmbPath.getSelectedIndex() > -1) {
                    navigationalAttribute.setPath(cmbPath.getItemAt(cmbPath.getSelectedIndex()));
                }
                if (!txtFilter.getText().isEmpty()) {
                    //TODO shall we support all filter expressions or only string matching like below?
                    // "regex(str(%1),\"" + txtFilter.getText() + "\",\"i\")"
                    navigationalAttribute.getAttribute().setFilterClause(txtFilter.getText());
                }
            } else {
                navigationalAttribute = new DynamicNavigationalAttribute(attributeValue.toString());
            }
            navigationalAttribute.setPartOfURI(chkIndex.isSelected());
            return navigationalAttribute;
        }
        return null;
    }

    public void setValue(DynamicNavigationalAttribute navigationalAttribute) {
        if (navigationalAttribute != null) {
            cmbAttributes.setSelectedItem(navigationalAttribute);
            cmbPath.setSelectedItem(navigationalAttribute.getPath());
            chkIndex.setSelected(navigationalAttribute.isPartOfURI());
        }
    }
}

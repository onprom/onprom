/*
 * annotationeditor
 *
 * DynamicAssociationPanel.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom.ui.component;

import com.google.common.collect.Sets;
import it.unibz.inf.onprom.data.Association;
import it.unibz.inf.onprom.data.DynamicAnnotationAttribute;
import it.unibz.inf.onprom.data.DynamicAttribute;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.ui.form.AbstractAnnotationForm;
import it.unibz.inf.onprom.ui.form.DynamicAnnotationForm;
import it.unibz.inf.onprom.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.onprom.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by T. E. Kalayci on 25-Oct-2017.
 */
public class DynamicAssociationPanel extends JPanel {
    private final DynamicAnnotationForm form;
    private final JComboBox<DynamicAttribute> cmbAnnotations;
    private final JComboBox<Set<DiagramShape>> cmbPath;
    private final JCheckBox chkIndex;
    private JComboBox<DynamicAnnotationAttribute> cmbAnnotationAttributes;
    private final boolean isManyToMany;

    public DynamicAssociationPanel(DynamicAnnotationForm _form, Association association) {
        isManyToMany = association.isManyToMany();
        form = _form;
        setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

        chkIndex = UIUtility.createCheckBox(association.getName(), "Check the checkbox if " + association.getName() + " is part of the URI", AbstractAnnotationForm.CHK_SIZE);
        add(chkIndex);

        cmbAnnotations = UIUtility.createWideComboBox(form.getAnnotations(), AbstractAnnotationForm.TXT_SIZE, e -> populatePath(), true, true);
        add(cmbAnnotations);

        cmbPath = UIUtility.createWideComboBox(AbstractAnnotationForm.TXT_SIZE, null, true, true);
        add(cmbPath);

        if (isManyToMany) {
            add(UIUtility.createSmallButton(AnnotationEditorButtons.ADD, e -> {
                if (cmbAnnotations.getSelectedItem() != null && cmbAnnotations.getSelectedItem() instanceof DynamicAttribute) {
                    DynamicAnnotationAttribute annotation = (DynamicAnnotationAttribute) cmbAnnotations.getSelectedItem();
                    if (annotation != null) {
                        if (cmbPath.getSelectedIndex() > -1) {
                            annotation.setPath(cmbPath.getItemAt(cmbPath.getSelectedIndex()));
                        }
                        annotation.setPartOfURI(chkIndex.isSelected());
                    }
                    boolean exists = false;
                    for (int i = 0; i < cmbAnnotationAttributes.getItemCount(); i++) {
                        DynamicAnnotationAttribute current = cmbAnnotationAttributes.getItemAt(i);
                        if (current.equals(annotation)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        cmbAnnotationAttributes.addItem(annotation);
                    }
                }

            }));
            cmbAnnotationAttributes = UIUtility.createWideComboBox(AbstractAnnotationForm.TXT_SIZE, null, true, false);
            add(cmbAnnotationAttributes);
            add(UIUtility.createSmallButton(AnnotationEditorButtons.REMOVE, e -> {
                if (cmbAnnotationAttributes.getSelectedIndex() > -1) {
                    cmbAnnotationAttributes.removeItemAt(cmbAnnotationAttributes.getSelectedIndex());
                }
            }));
        }
    }

    private void populatePath() {
        form.populatePath(cmbAnnotations, cmbPath);
    }

    public Set<DynamicAnnotationAttribute> getValue() {
        Set<DynamicAnnotationAttribute> selectedAttributes = Sets.newLinkedHashSet();
        if (isManyToMany) {
            selectedAttributes = IntStream.range(0, cmbAnnotationAttributes.getItemCount()).mapToObj(cmbAnnotationAttributes::getItemAt).collect(Collectors.toCollection(Sets::newLinkedHashSet));
        } else {
            if (cmbAnnotations.getSelectedItem() != null) {
                DynamicAnnotationAttribute annotation = (DynamicAnnotationAttribute) cmbAnnotations.getSelectedItem();
                if (annotation != null) {
                    if (cmbPath.getSelectedIndex() > -1) {
                        annotation.setPath(cmbPath.getItemAt(cmbPath.getSelectedIndex()));
                    }
                    annotation.setPartOfURI(chkIndex.isSelected());
                }
                selectedAttributes.add(annotation);
            }
        }
        return selectedAttributes;
    }

    public void setValue(Set<DynamicAnnotationAttribute> dynamicAnnotationAttributes) {
        if (dynamicAnnotationAttributes != null && dynamicAnnotationAttributes.size() > 0) {
            DynamicAnnotationAttribute selected = dynamicAnnotationAttributes.stream().findFirst().get();
            chkIndex.setSelected(selected.isPartOfURI());
            if (isManyToMany) {
                dynamicAnnotationAttributes.forEach(cmbAnnotationAttributes::addItem);
            } else {
                cmbAnnotations.setSelectedItem(selected);
                cmbPath.setSelectedItem(selected.getPath());
            }
        }
    }

}

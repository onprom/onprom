/*
 * onprom-annoeditor
 *
 * AnnotationAttributeTable.java
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

package it.unibz.inf.onprom.ui.component;

import it.unibz.inf.onprom.data.AnnotationAttribute;
import it.unibz.inf.onprom.ui.utility.UIUtility;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;
import java.util.Optional;

/**
 * Table for displaying and editing annotation attributes in annotation form
 *
 * @author T. E. Kalayci
 */
public class AnnotationAttributeTable extends JTable {
    private final AnnotationAttributeTableModel model;

    public AnnotationAttributeTable(boolean showType) {
        super();
        model = new AnnotationAttributeTableModel(showType);
        setModel(model);
    }

    public List<AnnotationAttribute> getAttributes() {
        return model.getAttributes();
    }

    public void setAttributes(List<AnnotationAttribute> attributes) {
        model.setAttributes(attributes);
    }

    public void removeSelectedAttribute() {
        if (UIUtility.deleteConfirm()) {
            model.removeAttribute(getSelectedRow());
        }
    }

    public void updateAttributeAt(int index, AnnotationAttribute annotationAttribute) {
        model.updateAttributeAt(index, annotationAttribute);
    }

    public void addAttribute(AnnotationAttribute annotationAttribute) {
        model.addAttribute(annotationAttribute);
    }

    @Nonnull
    public Optional<AnnotationAttribute> getSelectedAttribute() {
        return model.getAttribute(getSelectedRow());
    }
}

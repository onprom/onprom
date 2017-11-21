/*
 * onprom-annoeditor
 *
 * AnnotationAttributeTableModel.java
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

package it.unibz.inf.kaos.ui.component;

import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.AnnotationAttribute;
import it.unibz.inf.kaos.data.NavigationalAttribute;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Table model to hold and display annotation attributes in annotation forms
 *
 * @author T. E. Kalayci
 */
class AnnotationAttributeTableModel extends AbstractTableModel {

    private final String[] columnNames;
    private final Class<?>[] columnClasses;

    private List<AnnotationAttribute> attributes;

    AnnotationAttributeTableModel(boolean withType) {
        this(Lists.newLinkedList(), withType);
    }

    AnnotationAttributeTableModel(List<AnnotationAttribute> _attributes, boolean withType) {
        super();
        attributes = _attributes;
        if (withType) {
            columnNames = new String[]{"Name", "Value", "Type"};
            columnClasses = new Class[]{String.class, NavigationalAttribute.class, String.class};
        } else {
            columnNames = new String[]{"Name", "Value"};
            columnClasses = new Class[]{String.class, NavigationalAttribute.class};
        }
    }


    @Override
    public int getRowCount() {
        if (attributes == null)
            return 0;
        return attributes.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return attributes.get(rowIndex).getName();
            case 1:
                return attributes.get(rowIndex).getValue().toString();
            case 2:
                return attributes.get(rowIndex).getType();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    List<AnnotationAttribute> getAttributes() {
        return attributes;
    }

    void setAttributes(List<AnnotationAttribute> _attributes) {
        attributes = _attributes;
        fireTableDataChanged();
    }

    AnnotationAttribute getAttribute(int i) {
        if (attributes != null && attributes.size() > i)
            return attributes.get(i);
        return null;
    }

    void removeAttribute(int selectedRow) {
        attributes.remove(selectedRow);
        fireTableDataChanged();
    }

    void addAttribute(AnnotationAttribute annotationAttribute) {
        if (attributes == null) {
            attributes = Lists.newLinkedList();
        }
        attributes.add(annotationAttribute);
        fireTableDataChanged();
    }

    void updateAttributeAt(int index, AnnotationAttribute annotationAttribute) {
        attributes.set(index, annotationAttribute);
        fireTableDataChanged();
    }
}

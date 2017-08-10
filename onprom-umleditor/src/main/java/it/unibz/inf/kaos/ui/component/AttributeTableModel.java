/*
 * onprom-umleditor
 *
 * AttributeTableModel.java
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

import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.Cardinality;
import it.unibz.inf.kaos.data.DataType;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Table model to hold and display attributes in class form
 *
 * @author T. E. Kalayci
 */
class AttributeTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columnNames = {"Atrribute Name", "Attribute Type", "Attribute Cardinality"};
    private final Class<?>[] columnClass = {String.class, DataType.class, String.class};

    private LinkedList<Attribute> attributes = new LinkedList<>();

    AttributeTableModel(LinkedList<Attribute> _attributes) {
        super();
        attributes = _attributes;
    }

    @Override
    public int getRowCount() {
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
                return attributes.get(rowIndex).getType();
            case 2:
                return attributes.get(rowIndex).getMultiplicity();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClass[columnIndex];
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Attribute attribute = attributes.get(row);

        switch (col) {
            case 0:
                attribute.setName((String) value);
                break;
            case 1:
                attribute.setType((DataType) value);
                break;
            case 2:
                attribute.setMultiplicity((Cardinality) value);
                break;
        }
    }

    void addEmptyAttribute() {
        attributes.add(new Attribute("attr" + attributes.size()));
        fireTableDataChanged();
    }

    LinkedList<Attribute> getAttributes() {
        return attributes;
    }

    void setAttributes(LinkedList<Attribute> _attributes) {
        attributes = _attributes;
        fireTableDataChanged();
    }

    Attribute getAttribute(int i) {
        if (attributes != null && attributes.size() > i)
            return attributes.get(i);
        return null;
    }

    void removeAttribute(int selectedRow) {
        attributes.remove(selectedRow);
        fireTableDataChanged();
    }

    void moveUp(int selected) {
        Collections.swap(attributes, selected, selected - 1);
        fireTableDataChanged();
    }

    void moveDown(int selected) {
        Collections.swap(attributes, selected, selected + 1);
        fireTableDataChanged();
    }

}

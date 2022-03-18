/*
 * onprom-umleditor
 *
 * AttributeTable.java
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

import it.unibz.inf.onprom.data.Attribute;
import it.unibz.inf.onprom.data.Cardinality;
import it.unibz.inf.onprom.data.DataType;
import it.unibz.inf.onprom.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * Table for displaying and editing attributes in class form
 *
 * @author T. E. Kalayci
 */
public class AttributeTable extends JTable implements MouseListener {
    private final AttributeTableModel model = new AttributeTableModel();

    public AttributeTable(boolean editable) {
        super();
        setModel(model);
        //name of the attribute
        getColumnModel().getColumn(0).setCellEditor(new UniqueNameEditor());
        //data type of the attribute
        getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(DataType.values())));
        //multiplicity of the attribute
        getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(Cardinality.values())));
        if (editable) {
            this.addMouseListener(this);
        }
    }

    public List<Attribute> getAttributes() {
        return model.getAttributes();
    }

    public void setAttributes(List<Attribute> attributes) {
        model.setAttributes(attributes);
    }

    public void moveUp() {
        int selected = getSelectedRow();
        if (selected > 0) {
            model.moveUp(selected);
            setRowSelectionInterval(selected - 1, selected - 1);
        }
    }

    public void moveDown() {
        int selected = getSelectedRow();
        if (selected < model.getRowCount() - 1) {
            model.moveDown(selected);
            setRowSelectionInterval(selected + 1, selected + 1);
        }
    }

    public void addEmptyAttribute() {
        model.addEmptyAttribute();
        editCellAt(model.getRowCount() - 1, 0);
        requestFocus();
    }

    public void removeSelectedAttribute() {
        if (UIUtility.deleteConfirm()) {
            model.removeAttribute(getSelectedRow());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 || e.isPopupTrigger()) {
            if (e.isControlDown()) {
                if (getSelectedRow() > -1) {
                    removeSelectedAttribute();
                }
            } else {
                addEmptyAttribute();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    class UniqueNameEditor extends DefaultCellEditor {
        final JTextField textField;

        UniqueNameEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
        }

        public boolean stopCellEditing() {
            return isEditValid() && super.stopCellEditing();
        }

        private boolean isEditValid() {
            //check if other attributes have same name
            for (int i = 0; i < model.getRowCount(); i++) {
                if (i != getEditingRow()) {
                    if (model.getAttribute(i).filter(a -> a.getName().equals(textField.getText())).isPresent()) {
                        textField.selectAll();
                        UIUtility.error("Name already given to other attribute!");
                        return false;
                    }
                }
            }
            if (UIUtility.isNameExist(textField.getText())) {
                textField.selectAll();
                UIUtility.error("Name already exists in the diagram!");
                return false;
            }
            return true;
        }

    }
}

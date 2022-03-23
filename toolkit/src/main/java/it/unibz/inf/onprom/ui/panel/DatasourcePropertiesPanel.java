/*
 * onprom-toolkit
 *
 * DatasourcePropertiesPanel.java
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

package it.unibz.inf.onprom.ui.panel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatasourcePropertiesPanel extends JPanel {

    public DatasourcePropertiesPanel(Properties properties) {
        initUI(properties);
    }

    private void initUI(Properties properties) {
        //create the model
        PropertiesTableModel model = new PropertiesTableModel(properties);
        //create the table
        JTable table = new JTable(model);

        this.setLayout(new BorderLayout());

        this.add(new JScrollPane(table), BorderLayout.CENTER);

    }

    static class PropertiesTableModel extends AbstractTableModel {
        private final Properties properties;
        private final List<String> names;

        private final String[] columnNames = new String[]{
                "Name", "Value"
        };
        private final Class[] columnClass = new Class[]{
                String.class, String.class
        };

        public PropertiesTableModel(Properties properties) {
            this.properties = properties;
            this.names = new ArrayList<>(properties.stringPropertyNames());
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnClass[columnIndex];
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return properties.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 1) {
                properties.setProperty(names.get(row), (String) value);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return names.get(rowIndex);
            } else if (columnIndex == 1) {
                return properties.get(names.get(rowIndex));
            }
            return null;
        }
    }
}

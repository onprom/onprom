/*
 * onprom-annoeditor
 *
 * QueryEditor.java
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

package it.unibz.inf.kaos.ui.form;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Editor for displaying generated queries
 * <p>
 * @author T. E. Kalayci on 27/02/17.
 */
public class QueryEditor extends JDialog {
    private static final Dimension BOX_SIZE = new Dimension(550, 150);

    public QueryEditor(AnnotationQueries _queries) {
        setTitle("Annotation Query");
    setModal(true);
    setLayout(new GridBagLayout());
    GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

    //components in the first row
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;

    //list to display all queries
        DefaultListModel<AnnotationQuery> mdlQueries = new DefaultListModel<>();

    //add all the queries to allow editing
    _queries.getAllQueries().forEach(q -> {
      if (q != null) {
        mdlQueries.addElement(q);
          List<AnnotationQuery> attributeQueries = q.getAttributeQueries();
          if (attributeQueries != null && !attributeQueries.isEmpty()) {
          attributeQueries.forEach(aq -> {
            if (aq != null) {
              mdlQueries.addElement(aq);
            }
          });
        }
      }
    });

        JList<AnnotationQuery> lstQueries = new JList<>(mdlQueries);
    lstQueries.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    lstQueries.setLayoutOrientation(JList.VERTICAL);
    JScrollPane listScroller = new JScrollPane(lstQueries);
        listScroller.setPreferredSize(BOX_SIZE);
        add(listScroller, gridBagConstraints);

    gridBagConstraints.gridy++;
    //editor to display the query selected from the list
    JEditorPane edtQuery = new JEditorPane();
    JScrollPane scrollPane = new JScrollPane(edtQuery);
        scrollPane.setPreferredSize(BOX_SIZE);
    add(scrollPane, gridBagConstraints);

        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 1;
    //button to save the changes over the selected query
    JButton btn = UIUtility.createButton(AnnotationEditorButtons.SAVE, e -> {
      String query = edtQuery.getText();
      //first validate it using Jena
        query = SimpleQueryExporter.checkQuery(query);
      lstQueries.getSelectedValue().setQuery(query);
    }, AttributeForm.TXT_SIZE);
    add(btn, gridBagConstraints);

    gridBagConstraints.gridx = 2;
    btn = UIUtility.createButton(AnnotationEditorButtons.CONTINUE,
            e -> setVisible(false),
            AttributeForm.TXT_SIZE);
    add(btn, gridBagConstraints);

    //display selected query
    lstQueries.addListSelectionListener(e -> {
        AnnotationQuery _selected = lstQueries.getSelectedValue();
      if (_selected != null) {
        edtQuery.setText(_selected.getQuery());
      }
    });

    setMinimumSize(new Dimension(800, 600));
    setLocationRelativeTo(null);
    setVisible(true);
  }
}

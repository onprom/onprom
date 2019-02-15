/*
 * onprom-toolkit
 *
 * CustomExtractionPanel.java
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

package it.unibz.inf.kaos.ui.panel;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.SimpleXESLogExtractorWithEBDAMapping;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.kaos.onprom.OnpromToolkit;
import it.unibz.inf.kaos.ui.component.TreeNode;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorButtons;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.model.XLog;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Set;

public class CustomExtractionPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(CustomExtractionPanel.class);

    private static final Dimension CMB_DIMENSION = new Dimension(400, 25);

    public CustomExtractionPanel(OnpromToolkit toolkit) {
        initUI(toolkit);
    }

    private void initUI(OnpromToolkit toolkit) {
        Set<TreeNode<Object>> resources = toolkit.getResourceNodes();
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(UIUtility.createLabel("Domain ontology:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbDomainOntology = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof OWLOntology), CMB_DIMENSION, null, false, true);
        add(cmbDomainOntology, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("OBDA mappings:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbMappings = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof OBDAModel), CMB_DIMENSION, null, false, true);
        add(cmbMappings, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Custom event ontology:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbEventOntology = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof OWLOntology), CMB_DIMENSION, null, false, true);
        add(cmbEventOntology, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Domain to event ontology Annotations:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbEventAnnotations = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof AnnotationQueries), CMB_DIMENSION, null, false, true);
        add(cmbEventAnnotations, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Event to XES ontology annotations:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbXESAnnotations = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof AnnotationQueries), CMB_DIMENSION, null, false, true);
        add(cmbXESAnnotations, gridBagConstraints);

        JPanel buttonPanel = new JPanel();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(buttonPanel, gridBagConstraints);

        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.EXPORT, event -> UIUtility.executeInBackground(() -> {
            try {
                XLog xlog = null;
                long start = System.currentTimeMillis();
                if (cmbEventOntology.getSelectedIndex() > -1) {
                    if (cmbDomainOntology.getSelectedIndex() < -1 || cmbEventOntology.getSelectedIndex() < -1 || cmbMappings.getSelectedIndex() < -1 || cmbEventAnnotations.getSelectedIndex() < -1 || cmbXESAnnotations.getSelectedIndex() < -1) {
                        UIUtility.error("Please select domain ontology, OBDA mappings, custom event ontology, domain to event ontology annotations and event to XES ontology annotations!");
                    } else {
                        xlog = new SimpleXESLogExtractorWithEBDAMapping().extractXESLog(
                                (OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(),
                                (OBDAModel) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(),
                                (AnnotationQueries) cmbEventAnnotations.getItemAt(cmbEventAnnotations.getSelectedIndex()).getUserObject(),
                                (OWLOntology) cmbEventOntology.getItemAt(cmbEventOntology.getSelectedIndex()).getUserObject(),
                                (AnnotationQueries) cmbXESAnnotations.getItemAt(cmbXESAnnotations.getSelectedIndex()).getUserObject()
                        );
                    }
                } else if (cmbDomainOntology.getSelectedIndex() < -1 || cmbMappings.getSelectedIndex() < -1 || cmbEventAnnotations.getSelectedIndex() < -1) {
                    UIUtility.error("Please select domain ontology, OBDA mappings, custom event ontology, domain to event ontology annotations and event to XES ontology annotations!");
                } else {
                    xlog = new SimpleXESLogExtractorWithEBDAMapping().extractXESLog((OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(), (OBDAModel) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(), (AnnotationQueries) cmbEventAnnotations.getItemAt(cmbEventAnnotations.getSelectedIndex()).getUserObject());
                }
                if (xlog != null) {
                    logger.debug(String.format("EXTRACTION TOOK %s SECONDS", (System.currentTimeMillis() - start) / 1000));
                    toolkit.displayLogSummary(xlog);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                UIUtility.error(e.getMessage());
            }
        }, toolkit.getProgressBar())), gridBagConstraints);
        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.RESET, e -> {
            cmbDomainOntology.setSelectedIndex(-1);
            cmbMappings.setSelectedIndex(-1);
            cmbEventOntology.setSelectedIndex(-1);
            cmbEventAnnotations.setSelectedIndex(-1);
            cmbXESAnnotations.setSelectedIndex(-1);
            removeAll();
            initUI(toolkit);
            revalidate();
            repaint();
        }, null), gridBagConstraints);
        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.EXPORT_OBDA, event -> {
            try {
                if (cmbDomainOntology.getSelectedIndex() < -1 || cmbEventOntology.getSelectedIndex() < -1 || cmbMappings.getSelectedIndex() < -1 || cmbEventAnnotations.getSelectedIndex() < -1) {
                    UIUtility.error("Please select domain ontology, OBDA mappings, custom event ontology and domain to event ontology annotations!");
                } else {
                    OBDAMapping model = new OBDAMapper().createOBDAMapping(
                            (OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(),
                            (OWLOntology) cmbEventOntology.getItemAt(cmbEventOntology.getSelectedIndex()).getUserObject(),
                            (OBDAModel) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(),
                            (AnnotationQueries) cmbEventAnnotations.getItemAt(cmbEventAnnotations.getSelectedIndex()).getUserObject()
                    );
                    UIUtility.selectFileToOpen(FileType.MAPPING).ifPresent(file -> {
                        try {
                            new ModelIOManager(model).save(file);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                            UIUtility.error(e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                UIUtility.error(e.getMessage());
            }
        }, null), gridBagConstraints);
    }
}

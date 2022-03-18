/*
 * onprom-toolkit
 *
 * ExtractionPanel.java
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

import it.unibz.inf.onprom.OnpromToolkit;
import it.unibz.inf.onprom.data.FileType;
import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.logextractor.Extractor;
import it.unibz.inf.onprom.logextractor.ocel.OCELLogExtractor;
import it.unibz.inf.onprom.logextractor.xes.XESLogExtractor;
import it.unibz.inf.onprom.obdamapper.OBDAMapper;
import it.unibz.inf.onprom.ui.component.TreeNode;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.onprom.ui.utility.UMLEditorButtons;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class ExtractionPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ExtractionPanel.class);

    private static final Dimension CMB_DIMENSION = new Dimension(400, 25);

    public ExtractionPanel(OnpromToolkit toolkit) {
        initUI(toolkit);
    }

    private void initUI(OnpromToolkit toolkit) {
        Extractor[] finalOutputs = {
                new XESLogExtractor(),
                new OCELLogExtractor()
        };
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
                a.getUserObject() instanceof SQLPPMapping), CMB_DIMENSION, null, false, true);
        add(cmbMappings, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Datasource Properties:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbDSProperties = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof Properties), CMB_DIMENSION, null, false, true);
        add(cmbDSProperties, gridBagConstraints);


        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Domain ontology Annotations:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbDomainAnnotations = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof AnnotationQueries), CMB_DIMENSION, null, false, true);
        add(cmbDomainAnnotations, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Custom target ontology:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbTargetOntology = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof OWLOntology), CMB_DIMENSION, null, false, true);
        add(cmbTargetOntology, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Target ontology annotations:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<TreeNode<Object>> cmbTargetAnnotations = UIUtility.createWideComboBox(resources.stream().filter(a ->
                a.getUserObject() instanceof AnnotationQueries), CMB_DIMENSION, null, false, true);
        add(cmbTargetAnnotations, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(UIUtility.createLabel("Final output target:", null), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        JComboBox<Extractor> cmbFinalOutput = UIUtility.createWideComboBox(finalOutputs, CMB_DIMENSION, null, false, false);
        add(cmbFinalOutput, gridBagConstraints);

        JPanel buttonPanel = new JPanel();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        add(buttonPanel, gridBagConstraints);

        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.EXPORT, event -> UIUtility.executeInBackground(() -> {
            try {
                Object extractedLog = null;
                long start = System.currentTimeMillis();
                if (cmbTargetOntology.getSelectedIndex() > -1) {
                    if (cmbDomainOntology.getSelectedIndex() < -0 || cmbTargetOntology.getSelectedIndex() < 0 || cmbMappings.getSelectedIndex() < 0 || cmbDSProperties.getSelectedIndex() < 0 || cmbDomainAnnotations.getSelectedIndex() < 0 || cmbTargetAnnotations.getSelectedIndex() < 0) {
                        UIUtility.error("Please select domain ontology, OBDA mappings, Datasource Properties, target ontology, domain ontology annotations and target ontology annotations!");
                    } else {
                        extractedLog = cmbFinalOutput.getItemAt(cmbFinalOutput.getSelectedIndex()).extractLog(
                                (OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(),
                                (SQLPPMapping) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(),
                                (Properties) cmbDSProperties.getItemAt(cmbDSProperties.getSelectedIndex()).getUserObject(),
                                (AnnotationQueries) cmbDomainAnnotations.getItemAt(cmbDomainAnnotations.getSelectedIndex()).getUserObject(),
                                (OWLOntology) cmbTargetOntology.getItemAt(cmbTargetOntology.getSelectedIndex()).getUserObject(),
                                (AnnotationQueries) cmbTargetAnnotations.getItemAt(cmbTargetAnnotations.getSelectedIndex()).getUserObject()
                        );
                    }
                } else if (cmbDomainOntology.getSelectedIndex() < 0 || cmbMappings.getSelectedIndex() < 0 || cmbDSProperties.getSelectedIndex() < 0 || cmbDomainAnnotations.getSelectedIndex() < 0) {
                    UIUtility.error("Please select domain ontology, OBDA mappings, Datasource properties, custom event ontology, domain to event ontology annotations and event to XES ontology annotations!");
                } else {
                    extractedLog = cmbFinalOutput.getItemAt(cmbFinalOutput.getSelectedIndex()).extractLog(
                            (OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(),
                            (SQLPPMapping) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(),
                            (Properties) cmbDSProperties.getItemAt(cmbDSProperties.getSelectedIndex()).getUserObject(),
                            (AnnotationQueries) cmbDomainAnnotations.getItemAt(cmbDomainAnnotations.getSelectedIndex()).getUserObject()
                    );
                }
                if (extractedLog != null) {
                    logger.debug(String.format("EXTRACTION TOOK %s SECONDS", (System.currentTimeMillis() - start) / 1000));
                    toolkit.displayLogSummary(extractedLog);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                UIUtility.error(e.getMessage());
            }
        }, toolkit.getProgressBar())), gridBagConstraints);
        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.RESET, e -> {
            cmbDomainOntology.setSelectedIndex(-1);
            cmbMappings.setSelectedIndex(-1);
            cmbTargetOntology.setSelectedIndex(-1);
            cmbDomainAnnotations.setSelectedIndex(-1);
            cmbTargetAnnotations.setSelectedIndex(-1);
            cmbFinalOutput.setSelectedIndex(0);
            removeAll();
            initUI(toolkit);
            revalidate();
            repaint();
        }, null), gridBagConstraints);
        buttonPanel.add(UIUtility.createButton(UMLEditorButtons.EXPORT_OBDA, event -> {
            try {
                if (cmbDomainOntology.getSelectedIndex() < 0 || cmbTargetOntology.getSelectedIndex() < 0 || cmbMappings.getSelectedIndex() < 0 || cmbDSProperties.getSelectedIndex() < 0 || cmbDomainAnnotations.getSelectedIndex() < 0) {
                    UIUtility.error("Please select domain ontology, OBDA mappings, Datasource properties, target ontology and target ontology annotations!");
                } else {
                    SQLPPMapping model = new OBDAMapper(
                            (OWLOntology) cmbDomainOntology.getItemAt(cmbDomainOntology.getSelectedIndex()).getUserObject(),
                            (OWLOntology) cmbTargetOntology.getItemAt(cmbTargetOntology.getSelectedIndex()).getUserObject(),
                            (SQLPPMapping) cmbMappings.getItemAt(cmbMappings.getSelectedIndex()).getUserObject(),
                            (Properties) cmbDSProperties.getItemAt(cmbDSProperties.getSelectedIndex()).getUserObject(),
                            (AnnotationQueries) cmbDomainAnnotations.getItemAt(cmbDomainAnnotations.getSelectedIndex()).getUserObject()
                    ).getOBDAModel();
                    UIUtility.selectFileToOpen(FileType.MAPPING).ifPresent(file -> {
                        try {
                            new OntopNativeMappingSerializer().write(file, model);
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

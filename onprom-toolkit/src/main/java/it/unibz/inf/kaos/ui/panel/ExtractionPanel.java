/*
 * onprom-toolkit
 *
 * ExtractionPanel.java
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

package it.unibz.inf.kaos.ui.panel;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.ResourceConnection;
import it.unibz.inf.kaos.data.ResourceShape;
import it.unibz.inf.kaos.onprom.OnpromToolkit;
import it.unibz.inf.kaos.ui.component.TreeNode;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import static it.unibz.inf.kaos.ui.component.CustomTree.INT_ARRAY_FLAVOR;

/**
 * Extraction process panel
 * <p>
 *
 * @author T. E. Kalayci on 10-Jul-2017.
 */
public class ExtractionPanel extends UMLDiagramPanel {
    private static final Logger logger = LoggerFactory.getLogger(ExtractionPanel.class.getSimpleName());

    public ExtractionPanel(OnpromToolkit toolkit) {
        super(null);
        final ResourceShape ontology = new ResourceShape("data ontology", null);
        final ResourceShape event = new ResourceShape("event ontology", null);
        final ResourceShape mapping = new ResourceShape("mapping", null);
        final ResourceShape queries = new ResourceShape("queries", null);
        final ResourceShape extraction = new ResourceShape("extraction", null);
        final ResourceShape xesLog = new ResourceShape("XES", null);

        ontology.setStartX(50);
        ontology.setStartY(100);

        mapping.setStartX(200);
        mapping.setStartY(100);

        queries.setStartX(350);
        queries.setStartY(100);

        event.setStartX(500);
        event.setStartY(300);

        extraction.setStartX(200);
        extraction.setStartY(300);

        xesLog.setStartX(200);
        xesLog.setStartY(500);
        //connections
        shapes.add(new ResourceConnection(ontology, extraction));
        shapes.add(new ResourceConnection(mapping, extraction));
        shapes.add(new ResourceConnection(queries, extraction));
        shapes.add(new ResourceConnection(event, extraction));
        shapes.add(new ResourceConnection(extraction, xesLog));
        shapes.add(ontology);
        shapes.add(mapping);
        shapes.add(queries);
        shapes.add(event);
        shapes.add(extraction);
        shapes.add(xesLog);

        this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    Transferable transferData = dtde.getTransferable();
                    int[] selected = (int[]) transferData.getTransferData(INT_ARRAY_FLAVOR);
                    for (int i : selected) {
                        TreeNode<Object> node = toolkit.getResourceNode(i);
                        if (node.getType() == FileType.ONTOLOGY) {
                            ontology.setTreeNode(node);
                        }
                        if (node.getType() == FileType.MAPPING) {
                            mapping.setTreeNode(node);
                        }
                        if (node.getType() == FileType.QUERIES) {
                            queries.setTreeNode(node);
                        }
                        repaint();
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    UIUtility.error(e.getMessage());
                }
            }
        }));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        shapes.forEach(shape -> shape.draw(g2d));
    }
}

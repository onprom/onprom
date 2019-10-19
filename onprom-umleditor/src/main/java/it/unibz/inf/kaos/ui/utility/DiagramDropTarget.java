/*
 * onprom-umleditor
 *
 * DiagramDropTarget.java
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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Created by T. E. Kalayci on 17-Nov-2017.
 */
public class DiagramDropTarget extends DropTarget {
    private static final Logger logger = LoggerFactory.getLogger(DiagramDropTarget.class.getSimpleName());

    public DiagramDropTarget(DiagramEditor diagramEditor, JComponent diagramPanel) {

        super(diagramPanel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dropEvent) {
                if (dropEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        dropEvent.acceptDrop(dropEvent.getDropAction());
                        Object transferData = dropEvent.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData instanceof List) {
                            ((List) transferData).forEach(file -> {
                                diagramEditor.open((File) file);
                                dropEvent.dropComplete(true);
                            });
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        InformationDialog.display(e.getMessage());
                    }
                } else {
                    dropEvent.rejectDrop();
                }
            }

        });
    }

}

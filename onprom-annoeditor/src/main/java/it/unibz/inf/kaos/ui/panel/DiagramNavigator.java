/*
 * onprom-annoeditor
 *
 * DiagramNavigator.java
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

package it.unibz.inf.kaos.ui.panel;

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.Association;
import it.unibz.inf.kaos.data.DataType;
import it.unibz.inf.kaos.data.State;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.NavigationListener;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by T. E. Kalayci on 17-Nov-2017.
 */
class DiagramNavigator {
    private final Set<DiagramShape> tempNavigation = Sets.newLinkedHashSet();
    private final AnnotationDiagramPanel annotationDiagramPanel;
    private NavigationListener navigationListener;

    DiagramNavigator(AnnotationDiagramPanel _panel) {
        annotationDiagramPanel = _panel;
    }

    void navigate(final DiagramShape node, final int x, final int y, final boolean doubleClick) {
        node.setState(State.SELECTED);
        highlight(node);
        tempNavigation.add(node);
        if (doubleClick) {
            if (node instanceof UMLClass) {
                ((UMLClass) node).getClickedAttribute(x, y).ifPresent(selectedAttribute -> {
                    selectedAttribute.setState(State.SELECTED);
                    navigationListener.navigationComplete(tempNavigation, (UMLClass) node, selectedAttribute);
                });
                annotationDiagramPanel.resetNavigation();
            } else {
                UIUtility.error("Please double click on a class to finish tempNavigation");
            }
        }
    }

    void resetNavigation() {
        tempNavigation.forEach(shape -> shape.setState(State.NORMAL));
        tempNavigation.clear();
    }

    void setNavigationListener(NavigationListener _navigationListener) {
        navigationListener = _navigationListener;
    }

    private void highlight(final DiagramShape node) {
        if (node instanceof Association) {
            //highlight classes related with this association
            Association association = (Association) node;
            if (!tempNavigation.contains(association.getFirstClass()) && association.getFirstClass().notSelected()) {
                association.getFirstClass().setState(State.HIGHLIGHTED);
            }
            if (!tempNavigation.contains(association.getSecondClass()) && association.getSecondClass().notSelected()) {
                association.getSecondClass().setState(State.HIGHLIGHTED);
            }
        } else if (node instanceof UMLClass) {
            UMLClass cls = (UMLClass) node;
            //highlight relations with this class
            cls.getRelations().forEach(relation -> {
                if (relation.notSelected()) {
                    relation.setState(State.HIGHLIGHTED);
                }
            });
        }
        annotationDiagramPanel.repaint();
    }

    void updateAttributesState(UMLClass cls, State state, boolean functional, DataType... dataType) {
        cls.getAttributes().forEach(attr -> {
            if ((!functional || attr.isFunctional()) && (dataType == null || dataType.length < 1 || Arrays.asList(dataType).contains(attr.getType()))) {
                    attr.setState(state);
            }
        });
    }

    void highlightAttribute(UMLClass relatedClass, boolean functional, DataType... dataType) {
        updateAttributesState(relatedClass, State.HIGHLIGHTED, functional, dataType);
        annotationDiagramPanel.getClasses().forEach(cls -> {
            if (NavigationUtility.isConnected(relatedClass, cls, functional)) {
                updateAttributesState(cls, State.HIGHLIGHTED, functional, dataType);
            }
        });
        annotationDiagramPanel.repaint();
    }

}

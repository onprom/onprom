package it.unibz.inf.kaos.ui.panel;

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.*;
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
                UMLClass cls = (UMLClass) node;
                Attribute selectedAttribute = cls.getClickedAttribute(x, y);
                if (selectedAttribute != null) {
                    selectedAttribute.setState(State.SELECTED);
                }
                navigationListener.navigationComplete(tempNavigation, (UMLClass) node, selectedAttribute);
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

    public void setNavigationListener(NavigationListener _navigationListener) {
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
            if (!functional || attr.isFunctional()) {
                if (dataType == null || dataType.length < 1 || Arrays.asList(dataType).contains(attr.getType())) {
                    attr.setState(state);
                }
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

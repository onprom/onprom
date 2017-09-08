/*
 * onprom-annoeditor
 *
 * AnnotationDiagramPanel.java
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

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.factory.AnnotationFactory;
import it.unibz.inf.kaos.interfaces.*;
import it.unibz.inf.kaos.ui.edit.AddDeleteAnnotationEdit;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.ZoomUtility;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Drawing panel supporting annotations
 *
 * @see UMLDiagramPanel
 * <p>
 * @author T. E. Kalayci on 25/10/16.
 */
public class AnnotationDiagramPanel extends UMLDiagramPanel implements AnnotationDiagram {
  private Set<DiagramShape> tempNavigation = new LinkedHashSet<>();

  private NavigationListener navigationListener;

  public AnnotationDiagramPanel(DiagramEditor editor) {
    super(editor);
    isUpdateAllowed = false;
  }

  private void highlight(DiagramShape node) {
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
    repaint();
  }

  private void resetState() {
    shapes.forEach(shape -> shape.setState(State.NORMAL));
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //check if it is tempNavigation action
    if (currentAction == AnnotationActionType.NAVIGATE) {
      //select clicked object
      int x = ZoomUtility.get(e.getX());
      int y = ZoomUtility.get(e.getY());
      DiagramShape node = selectShape(x, y);
      //add selected element to tempNavigation list
      if (node != null) {
        node.setState(State.SELECTED);
        //highlight classes and relations which is accessible
        highlight(node);
        tempNavigation.add(node);
        if (e.getClickCount() == 2) {
          //get clicked attribute of clicked class
          if (node instanceof UMLClass) {
            UMLClass cls = (UMLClass) node;
            Attribute selectedAttribute = cls.getClickedAttribute(x, y);
            if (selectedAttribute != null)
              selectedAttribute.setState(State.SELECTED);
            //end tempNavigation
            navigationListener.navigationComplete(tempNavigation, (UMLClass) node, selectedAttribute);
            //reset states of tempNavigation nodes
            tempNavigation.forEach(shape -> shape.setState(State.NORMAL));
            resetNavigation();
          } else {
            UIUtility.error("Please double click on a class to finish tempNavigation");
          }
        }
      }
    } else {
      DiagramShape _selected = getSelected();
      if (_selected != null) {
        //create a new annotation to the selected UML class
        if (_selected instanceof UMLClass) {
          Annotation annotation = AnnotationFactory.createAnnotation(currentAction, (UMLClass) _selected, getFirstItem(CaseAnnotation.class));
          if (annotation != null) {
              annotation.setStartX(ZoomUtility.get(e.getX()));
              annotation.setStartY(ZoomUtility.get(e.getY()));
            addAnnotation(annotation);
            undoManager.addEdit(new AddDeleteAnnotationEdit(this, annotation, true));
            loadForm(annotation.getForm(this));
          }
        } else if (_selected instanceof Annotation && e.getClickCount() == 2) {
          loadForm(((Annotation) _selected).getForm(this));
        }
      }
      super.mousePressed(e);
    }
  }

  @Override
  DiagramShape getHoverShape() {
    DiagramShape _selected = getItems(Annotation.class).stream().filter(shape -> shape.over(startX, startY)).findFirst().orElse(null);
    if (_selected == null) {
      _selected = super.getHoverShape();
    }
    return _selected;
  }

  @Override
  public boolean removeShape(DiagramShape _selected) {
    if (_selected != null && _selected instanceof Annotation) {
      if (UIUtility.confirm(AnnotationEditorMessages.DELETE_CONFIRMATION)) {
        Annotation annotation = (Annotation) _selected;
          if (AnnotationFactory.checkRemoval(this, annotation)) {
          removeAnnotation(annotation);
              undoManager.addEdit(new AddDeleteAnnotationEdit(this, annotation, false));
              loadForm(null);
              repaint();
              return true;
          }
      }
    }
    //TODO should we also allow removal of relation anchors?
    return false;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    getItems(Annotation.class).forEach(shape -> shape.draw(g2d));
  }

  private DiagramShape selectShape(int x, int y) {
    return shapes.stream().filter(p -> p.over(x, y)).findFirst().orElse(null);
  }

  private void resetAllAttributesState(UMLClass relatedClass) {
      updateAttributesState(relatedClass, State.NORMAL, false, (DataType[]) null);
  }

  private void updateAttributeState(UMLClass relatedClass, State state, boolean functional, DataType... dataType) {
    updateAttributesState(relatedClass, state, functional, dataType);
    getClasses().forEach(cls -> updateAttributeState(relatedClass, cls, state, functional, dataType));
    repaint();
  }

  private void updateAttributeState(UMLClass relatedClass, UMLClass cls, State state, boolean functional, DataType... dataType) {
    if (NavigationUtility.isConnected(relatedClass, cls, functional)) {
      updateAttributesState(cls, state, functional, dataType);
    }
  }

  private void updateAttributesState(UMLClass cls, State state, boolean functional, DataType... dataType) {
    for (Attribute attr : cls.getAttributes()) {
      if (!functional || attr.isFunctional()) {
        if (dataType == null || Arrays.asList(dataType).contains(attr.getType())) {
          attr.setState(state);
        }
      }
    }
  }

    @Override
    public void addAnnotation(Annotation annotation) {
        shapes.add(annotation);
        loadForm(null);
    }

  @Override
  public void removeAnnotation(Annotation annotation) {
    //TODO check if there is any EVENT related to deleted annotation
    shapes.remove(annotation);
    loadForm(null);
  }

    public void startNavigation(NavigationListener _navigationListener) {
        navigationListener = _navigationListener;
        setCurrentAction(AnnotationActionType.NAVIGATE);
  }

  public void resetNavigation() {
    tempNavigation = new LinkedHashSet<>();
    resetState();
    currentAction = UMLActionType.select;
    clearSelection();
  }

  public void highlightAttribute(UMLClass relatedClass, boolean functional, DataType... dataType) {
    updateAttributeState(relatedClass, State.HIGHLIGHTED, functional, dataType);
  }

    public void resetAttributeStates() {
        getClasses().forEach(this::resetAllAttributesState);
  }

  @Override
  public Set<NavigationalAttribute> getAttributes(UMLClass startNode, boolean functional, DataType... types) {
    Set<NavigationalAttribute> attributes = new LinkedHashSet<>();
    for (UMLClass endNode : getClasses()) {
      for (Attribute attr : endNode.getAttributes()) {
        if (types == null || Arrays.asList(types).contains(attr.getType())) {
          if (startNode.equals(endNode)) {
            attributes.add(new NavigationalAttribute(startNode, attr));
          } else if (NavigationUtility.isConnected(startNode, endNode, functional)) {
            attributes.add(new NavigationalAttribute(endNode, attr));
          }
        }
      }
    }
    return attributes;
  }

  @Override
  public <T extends Annotation> Set<NavigationalAttribute> getAnnotations(UMLClass startNode, boolean functional, Class<T> type) {
    Set<NavigationalAttribute> annotations = new LinkedHashSet<>();
    for (Annotation annotation : getItems(type)) {
      UMLClass endNode = annotation.getRelatedClass();
      if (startNode.equals(endNode)) {
        annotations.add(new NavigationalAttribute(annotation));
      } else if (NavigationUtility.isConnected(startNode, endNode, functional)) {
        annotations.add(new NavigationalAttribute(annotation));
      }
    }
    return annotations;
  }

  private void loadForm(AnnotationForm form) {
    if (form != null) {
      form.setVisible(true);
    }
    diagramEditor.loadEditor((javax.swing.JPanel) form);
  }
}
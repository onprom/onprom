/*
 * onprom-annoeditor
 *
 * AnnotationDiagramPanel.java
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

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.NavigationListener;
import it.unibz.inf.kaos.ui.edit.AddDeleteAnnotationEdit;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.utility.*;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Drawing panel supporting annotations
 *
 * @author T. E. Kalayci on 25/10/16.
 * @see UMLDiagramPanel
 * <p>
 */
public class AnnotationDiagramPanel extends UMLDiagramPanel implements AnnotationDiagram {
    private final AnnotationFactory factory;
    private final DiagramNavigator diagramNavigator = new DiagramNavigator(this);

    public AnnotationDiagramPanel(DiagramEditor editor, AnnotationFactory _factory) {
        super(editor);
        factory = _factory;
        isUpdateAllowed = false;
        shapes = new Shapes() {
            @Override
            protected DiagramShape getFirstShapeAt(int x, int y) {
                Annotation _selected = super.getAll(Annotation.class).filter(shape -> shape.over(x, y)).findFirst().orElse(null);
                return _selected != null ? _selected : super.getFirstShapeAt(x, y);
            }
        };
        diagramMouseListener.remove();
        diagramMouseListener = new DiagramMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = ZoomUtility.get(e.getX());
                int y = ZoomUtility.get(e.getY());
                if (currentAction == AnnotationDiagramActions.NAVIGATE) {
                    DiagramShape node = shapes.over(x, y);
                    if (node != null) {
                        diagramNavigator.navigate(node, x, y, e.getClickCount() == 2);
                    }
                } else {
                    DiagramShape _selected = shapes.getSelected();
                    if (_selected != null) {
                        if (_selected instanceof UMLClass) {
                            createAnnotation((UMLClass) _selected, x, y);
                        } else if (e.getClickCount() == 2 && _selected instanceof Annotation) {
                            _selected.getForm(AnnotationDiagramPanel.this).ifPresent(form ->
                                    diagramEditor.loadForm((JPanel) form));
                            return;
                        }
                    }
                    super.mousePressed(e);
                }
            }
        };
    }

    public void startNavigation(NavigationListener navigationListener) {
        diagramNavigator.setNavigationListener(navigationListener);
        setCurrentAction(AnnotationDiagramActions.NAVIGATE);
    }

    private void createAnnotation(final UMLClass _selected, int x, int y) {
        factory.createAnnotation(AnnotationDiagramPanel.this, currentAction, _selected).ifPresent(annotation -> {
            annotation.setStartX(x);
            annotation.setStartY(y);
            addAnnotation(annotation);
            DiagramUndoManager.addEdit(new AddDeleteAnnotationEdit(AnnotationDiagramPanel.this, annotation, true));
            annotation.getForm(AnnotationDiagramPanel.this).ifPresent(diagramEditor::loadForm);
        });
    }

    @Override
    public void resetNavigation() {
        diagramNavigator.resetNavigation();
        shapes.forEach(shape -> shape.setState(State.NORMAL));
        setCurrentAction(UMLDiagramActions.select);
        shapes.clearSelection();
    }

    @Override
    public boolean removeShape(DiagramShape selected) {
        if (selected instanceof Annotation) {
            if (UIUtility.confirm(AnnotationEditorMessages.DELETE_CONFIRMATION)) {
                Annotation annotation = (Annotation) selected;
                if (factory.checkRemoval(this, annotation)) {
                    removeAnnotation(annotation);
                    DiagramUndoManager.addEdit(new AddDeleteAnnotationEdit(this, annotation, false));
                    diagramEditor.unloadForm();
                    repaint();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        shapes.getAll(Annotation.class).forEach(shape -> shape.draw((Graphics2D) g));
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        shapes.add(annotation);
        diagramEditor.unloadForm();
    }

    @Override
    public void removeAnnotation(Annotation annotation) {
        shapes.remove(annotation);
        diagramEditor.unloadForm();
    }

    public void resetAttributeStates() {
        getClasses().forEach(umlClass -> diagramNavigator.updateAttributesState(umlClass, State.NORMAL, false));
    }

    @Override
    public void highlightAttribute(final UMLClass relatedClass, final boolean functional, final DataType... dataType) {
        diagramNavigator.highlightAttribute(relatedClass, functional, dataType);
    }

    @Override
    public Set<NavigationalAttribute> findAttributes(UMLClass startNode, boolean functional, DataType... types) {
        return getClasses()
                .map(endNode -> getNavigationalAttributes(startNode, endNode, functional, types))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<NavigationalAttribute> getNavigationalAttributes(UMLClass startNode, UMLClass endNode, boolean functional, DataType[] types) {
        return endNode.getAttributes().stream()
                .filter(attribute -> ((types == null || types.length < 1 || Stream.of(types).anyMatch(type -> type == attribute.getType())) && NavigationUtility.isConnected(startNode, endNode, functional)))
                .map(attribute -> new NavigationalAttribute(endNode, attribute))
                .collect(Collectors.toSet());
    }

    @Override
    public <T extends Annotation> Set<T> findAnnotations(UMLClass startNode, boolean functional, Class<T> type) {
        return shapes.getAll(type).
                filter(annotation -> NavigationUtility.isConnected(startNode, annotation.getRelatedClass(), functional))
                .collect(Collectors.toSet());
    }
}
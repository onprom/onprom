/*
 * onprom-umleditor
 *
 * UMLDiagramPanel.java
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

import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.ActionType;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.edit.EditFactory;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.utility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

/**
 * Operations related with visualisation of the UML diagram
 *
 * @author T. E. Kalayci
 */
public class UMLDiagramPanel extends JPanel implements UMLDiagram {
    private static final Logger LOGGER = LoggerFactory.getLogger(UMLDiagramPanel.class.getSimpleName());
    final DiagramEditor diagramEditor;
    private final List<RelationAnchor> tempAnchors = Lists.newArrayList();
    private final boolean isOpenGLEnabled;
    Shapes shapes = new Shapes();
    boolean isUpdateAllowed = true;
    ActionType currentAction;
    DiagramMouseListener diagramMouseListener;
    private boolean gridVisible = true;
    private boolean logoVisible = true;
    private Rectangle selectionArea;

    public UMLDiagramPanel(DiagramEditor _editorLoader) {
        diagramEditor = _editorLoader;
        isOpenGLEnabled = Boolean.parseBoolean(System.getProperty("sun.java2d.opengl"));
        if (!isOpenGLEnabled) {
            LOGGER.warn("OpenGL is not enabled, there could be some performance and quality issues. It can be enabled using -Dsun.java2d.opengl=true runtime argument.");
        }
        diagramMouseListener = new DiagramMouseListener();
        this.setDropTarget(new DiagramDropTarget(diagramEditor, this));
        this.setBackground(Color.WHITE);
    }

    public boolean isUpdateAllowed() {
        return isUpdateAllowed;
    }

    private void createShape(int startX, int startY, boolean isControlDown) {
        DiagramShape selected = shapes.getSelected();
        if (isUpdateAllowed) {
            if (currentAction == UMLDiagramActions.umlclass) {
                if (!shapes.isClassOver(startX, startY)) {
                    UMLClass newClass = new UMLClass("Class_" + shapes.size(), startX, startY);
                    if (gridVisible) {
                        newClass.stickToGrid();
                    }
                    shapes.add(newClass);
                    DiagramUndoManager.addEdit(EditFactory.classCreated(this, newClass, true));
                    diagramEditor.loadForm(newClass.getForm(this));
                }
            } else if (currentAction == UMLDiagramActions.disjoint || currentAction == UMLDiagramActions.relation
                    || currentAction == UMLDiagramActions.isarelation) {
                if (selected instanceof UMLClass) {
                    UMLClass secondClass = shapes.getFirstClassAt(startX, startY);
                    if (secondClass != null) {
                        addRelation((UMLClass) selected, secondClass);
                    } else {
                        tempAnchors.add(new RelationAnchor(startX, startY));
                    }
                } else {
                    shapes.updateSelection(false, startX, startY);
                }
            } else if (currentAction == UMLDiagramActions.association) {
                addAssociationClass(selected, startX, startY);
            }
        }
        if (isControlDown && selected instanceof Relationship) {
            RelationAnchor anchor = ((Relationship) selected).addAnchor(startX, startY);
            if (anchor != null) {
                DiagramUndoManager.addEdit(EditFactory.anchorCreated((Relationship) selected, singletonList(anchor), true));
            }
        }
    }

    private void addAssociationClass(DiagramShape selected, int x, int y) {
        if (selected instanceof Association) {
            Association association = (Association) selected;
            AssociationClass aClass = new AssociationClass(association, x, y);
            if (gridVisible) {
                aClass.stickToGrid();
            }
            association.setAssociationClass(aClass);
            shapes.add(aClass);
            DiagramUndoManager.addEdit(EditFactory.classCreated(this, aClass, true));
        }
    }

    private void addRelation(final UMLClass firstClass, final UMLClass secondClass) {
        Relationship relationship;
        if (currentAction == UMLDiagramActions.disjoint) {
            if (firstClass.equals(secondClass)) {
                UIUtility.error("Class cannot be disjoint of itself");
                tempAnchors.clear();
                return;
            }
            if (firstClass.isRelationExist(secondClass, Disjoint.class)) {
                UIUtility.error("Classes are already disjoint");
                tempAnchors.clear();
                return;
            }
            relationship = new Disjoint(firstClass, secondClass);
        } else if (currentAction == UMLDiagramActions.isarelation) {
            if (firstClass.equals(secondClass)) {
                UIUtility.error("Class cannot have an IS-A relationship with itself");
                tempAnchors.clear();
                return;
            }
            if (firstClass.isRelationExist(secondClass, Inheritance.class)) {
                UIUtility.error("Classes already have an IS-A relationship");
                tempAnchors.clear();
                return;
            }
            relationship = new Inheritance(secondClass, firstClass);
        } else {
            if (firstClass.equals(secondClass) && tempAnchors.size() < 1) {
                tempAnchors.add(new RelationAnchor(firstClass.getEndX() + 50, firstClass.getCenterY()));
                tempAnchors.add(new RelationAnchor(secondClass.getEndX() + 50, secondClass.getEndY() + 50));
                tempAnchors.add(new RelationAnchor(secondClass.getCenterX(), secondClass.getEndY() + 50));
            }
            relationship = new Association("relation_" + shapes.size(), firstClass, secondClass);
        }
        relationship.addAnchors(tempAnchors);
        shapes.add(relationship);
        if (relationship instanceof Association) {
            UIUtility.addName(relationship.getName());
            diagramEditor.loadForm(relationship.getForm(this));
        }
        relationship.getFirstClass().addRelation(relationship);
        relationship.getSecondClass().addRelation(relationship);
        DiagramUndoManager.addEdit(EditFactory.relationCreated(this, relationship, null, true));
        tempAnchors.clear();
        shapes.clearSelection();
    }

    @Override
    public void addClass(UMLClass cls) {
        UIUtility.addName(cls.getName());
        shapes.add(cls);
        repaint();
    }

    @Override
    public void addRelation(Relationship relation) {
        if (relation instanceof Association) {
            UIUtility.addName(relation.getName());
        }
        relation.getFirstClass().addRelation(relation);
        relation.getSecondClass().addRelation(relation);
        shapes.add(relation);
    }

    @Override
    public void removeClass(UMLClass cls) {
        shapes.remove(cls);
        UIUtility.removeName(cls.getName());
        if (cls instanceof AssociationClass) {
            AssociationClass aClass = (AssociationClass) cls;
            aClass.getAssociation().removeAssociationClass(aClass);
        }
        cls.getRelations().forEach(shapes::remove);
        repaint();
    }

    @Override
    public void removeRelation(Relationship relation) {
        if (relation instanceof Association) {
            UIUtility.removeName(relation.getName());
        }
        //remove relations from classes
        relation.getFirstClass().removeRelation(relation);
        relation.getSecondClass().removeRelation(relation);
        shapes.remove(relation);
    }

    @Override
    public Stream<UMLClass> getClasses() {
        return shapes.getClasses();
    }

    @Override
    public Stream<Association> getAssociations() {
        return shapes.getAssociations();
    }

    @Override
    public Set<DiagramShape> getShapes(final boolean forJSON) {
        return shapes.getShapes(forJSON);
    }

    @Override
    public boolean removeShape(DiagramShape selected) {
        if (UIUtility.deleteConfirm()) {
            if (selected instanceof UMLClass) {
                UMLClass cls = (UMLClass) selected;
                DiagramUndoManager.addEdit(EditFactory.classCreated(this, cls, false));
                removeClass(cls);
                return true;
            }
            if (selected instanceof Relationship) {
                Relationship rel = (Relationship) selected;
                List<RelationAnchor> deleted = rel.deleteAnchor();
                if (deleted == null || deleted.isEmpty()) {
                    if (rel instanceof Association) {
                        Association association = (Association) rel;
                        DiagramUndoManager.addEdit(EditFactory.relationCreated(this, association, association.getAssociationClass(), false));
                        if (association.hasAssociation()) {
                            removeClass(association.getAssociationClass());
                        }
                    } else {
                        DiagramUndoManager.addEdit(EditFactory.relationCreated(this, rel, null, false));
                    }
                    removeRelation(rel);
                    return true;
                } else {
                    DiagramUndoManager.addEdit(EditFactory.anchorCreated(rel, deleted, false));
                    return true;
                }
            }
        }
        return false;
    }

    public void setCurrentAction(ActionType newAction) {
        tempAnchors.clear();
        this.setCursor(Cursor.getDefaultCursor());
        this.currentAction = newAction;
    }

    public void clear(boolean confirmed) {
        if (confirmed) {
            shapes.clear();
            DiagramUndoManager.discardAllEdits();
            UIUtility.clearNames();
            diagramEditor.loadForm(null);
        }
    }

    public void removeSelected() {
        DiagramShape _selected = shapes.getSelected();
        if (_selected != null) {
            removeShape(_selected);
        }
    }

    public void toggleGrid() {
        gridVisible = !gridVisible;
    }

    public void paintDiagram(Graphics g2d, int x, int y) {
        boolean gridStatus = gridVisible;
        boolean logoStatus = logoVisible;
        //remove logo and grid from panel
        if (gridStatus) {
            toggleGrid();
        }
        if (logoStatus) {
            logoVisible = !logoVisible;
        }
        // remove selected shapes
        shapes.clearSelection();
        //translate to find drawing area
        g2d.translate(-x, -y);
        //paint diagram to the graphics object
        print(g2d);
        //reset grid and logo status
        if (gridStatus) {
            toggleGrid();
        }
        if (logoStatus) {
            logoVisible = !logoVisible;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (isOpenGLEnabled) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            if (logoVisible) {
                DrawingUtility.drawLogo(g2d, getVisibleRect());
            }
        }
        g2d.scale(ZoomUtility.ZOOMING_SCALE, ZoomUtility.ZOOMING_SCALE);
        g2d.setStroke(DrawingUtility.NORMAL_STROKE);
        if (gridVisible) {
            DrawingUtility.drawGrid(g2d, getMaximumSize());
        }
        shapes.getClasses().forEach(p -> p.calculateEndCoordinates(g2d));
        shapes.getAll(Relationship.class).forEach(p -> p.draw(g2d));
        shapes.getClasses().forEach(p -> p.draw(g2d));
        if (selectionArea != null) {
            DrawingUtility.drawSelectionRectangle(g2d, selectionArea);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ZoomUtility.getWidth(), ZoomUtility.getHeight());
    }

    public void load(Set<DiagramShape> _shapes) {
        if (_shapes != null) {
            DiagramUndoManager.discardAllEdits();
            UIUtility.clearNames();
            this.shapes.load(_shapes);
            this.shapes.forEach(shape -> UIUtility.addName(shape.getName()));
        }
        if (diagramEditor != null) {
            diagramEditor.loadForm(null);
        }
        repaint();
    }

    private Set<DiagramShape> getShapesToDraw() {
        if (shapes.isShapeSelected())
            return shapes.getSelectedShapes();
        return shapes.getShapesAndAnchors();
    }

    public Rectangle getDrawingArea() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (DiagramShape shape : getShapesToDraw()) {
            if (shape.getStartX() < minX) {
                minX = shape.getStartX();
            }
            if (shape.getStartY() < minY) {
                minY = shape.getStartY();
            }
            if (shape.getEndX() > maxX) {
                maxX = shape.getEndX();
            }
            if (shape.getEndY() > maxY) {
                maxY = shape.getEndY();
            }
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public <T extends DiagramShape> T findFirst(Class<T> type) {
        return shapes.findFirst(type);
    }

    @Override
    public <T extends DiagramShape> long count(Class<T> type) {
        return shapes.count(type);
    }

    @Override
    public <T extends DiagramShape> Stream<T> getAll(Class<T> type) {
        return shapes.getAll(type);
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    class DiagramMouseListener extends MouseAdapter {
        private int prevX;
        private int prevY;
        private boolean shapeMoved;
        private int startX;
        private int startY;

        DiagramMouseListener() {
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            prevX = startX = ZoomUtility.get(e.getX());
            prevY = startY = ZoomUtility.get(e.getY());
            createShape(startX, startY, e.isControlDown());
            if (e.getClickCount() == 2) {
                if (e.isPopupTrigger()) {
                    removeSelected();
                    return;
                } else if (shapes.getSelected() != null) {
                    diagramEditor.loadForm(shapes.getSelected().getForm(UMLDiagramPanel.this));
                }
            }
            shapes.updateSelection(e.isControlDown(), startX, startY);
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
            if (shapeMoved && shapes.isShapeSelected()) {
                DiagramUndoManager.addEdit(EditFactory.shapesMoved(shapes.getSelectedShapes(), ZoomUtility.get(e.getX()) - startX, ZoomUtility.get(e.getY()) - startY));
                if (gridVisible) {
                    shapes.getSelectedShapes().forEach(DiagramShape::stickToGrid);
                }
                shapeMoved = false;
            }
            selectionArea = null;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            int diffX = ZoomUtility.get(e.getX()) - prevX;
            int diffY = ZoomUtility.get(e.getY()) - prevY;
            if (SwingUtilities.isRightMouseButton(e)) {
                Rectangle view = getVisibleRect();
                view.x -= diffX;
                view.y -= diffY;
                scrollRectToVisible(view);
            } else {
                if (selectionArea == null && shapes.isShapeSelected()) {
                    shapeMoved = shapes.moveSelectedShapes(diffX, diffY);
                    prevX = ZoomUtility.get(e.getX());
                    prevY = ZoomUtility.get(e.getY());
                } else {
                    int x = startX;
                    int y = startY;
                    int width = diffX;
                    int height = diffY;
                    if (width < 0) {
                        width = -width;
                        x = x - width;
                    }
                    if (height < 0) {
                        height = -height;
                        y = y - height;
                    }
                    selectionArea = new Rectangle(x, y, width, height);
                    shapes.selectShapes(selectionArea);
                    repaint();
                }
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = ZoomUtility.get(e.getX());
            int y = ZoomUtility.get(e.getY());
            shapes.forEach(shape -> {
                if (shape != null && shape.over(x, y)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                ZoomUtility.changeZoom(e.getPreciseWheelRotation());
                Rectangle view = getVisibleRect();
                view.x = ZoomUtility.get(e.getX());
                view.y = ZoomUtility.get(e.getY());
                scrollRectToVisible(view);
                revalidate();
            } else if (getParent() != null) {
                getParent().dispatchEvent(e);
            }
            repaint();
        }

        void remove() {
            removeMouseListener(this);
            removeMouseMotionListener(this);
            removeMouseWheelListener(this);
        }
    }
}
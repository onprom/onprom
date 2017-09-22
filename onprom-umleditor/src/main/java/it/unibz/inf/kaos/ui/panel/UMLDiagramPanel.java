/*
 * onprom-umleditor
 *
 * UMLDiagramPanel.java
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
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.edit.AddDeleteAnchorEdit;
import it.unibz.inf.kaos.ui.edit.AddDeleteClassEdit;
import it.unibz.inf.kaos.ui.edit.AddDeleteRelationEdit;
import it.unibz.inf.kaos.ui.edit.MoveShapeEdit;
import it.unibz.inf.kaos.ui.form.ClassForm;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.form.ObjectList;
import it.unibz.inf.kaos.ui.form.RelationForm;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.utility.*;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Operations related with visualisation of the diagram
 *
 * @author T. E. Kalayci
 */
public class UMLDiagramPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, UMLDiagram {
  private static final Logger logger = LoggerFactory.getLogger(UMLDiagramPanel.class.getSimpleName());
  final UndoManager undoManager = new UndoManager();
  final DiagramEditor diagramEditor;
  private final ArrayList<RelationAnchor> tempAnchors = new ArrayList<>();
  boolean isUpdateAllowed = true;
  ActionType currentAction;
  Set<DiagramShape> shapes = new LinkedHashSet<>();
  int startX;
  int startY;
  private Set<DiagramShape> selecteds = new LinkedHashSet<>();
  private int prevX;
  private int prevY;
  private boolean shapeMoved = false;
  private boolean showGrid = true;
  private boolean showLogo = true;
  private boolean isOpenGLEnabled = false;

  private Rectangle selectionArea;

  public UMLDiagramPanel(DiagramEditor _editorLoader) {
    diagramEditor = _editorLoader;
    isOpenGLEnabled = Boolean.parseBoolean(System.getProperty("sun.java2d.opengl"));
    if (!isOpenGLEnabled) {
      logger.warn("OpenGL is not enabled, there could be some performance and quality issues. It can be enabled using -Dsun.java2d.opengl=true runtime argument.");
    }
    // we are going to listen mouse clicks and movements for various operations
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.addMouseWheelListener(this);
    this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
      @Override
      public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          try {
            dtde.acceptDrop(dtde.getDropAction());
            Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (transferData != null && transferData instanceof List) {
              List files = (List) transferData;
              if (files.size() > 0) {
                diagramEditor.open((File) files.get(0));
                dtde.dropComplete(true);
              }
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            InformationDialog.display(e.getMessage());
          }
        } else {
          dtde.rejectDrop();
        }
      }
    }));
    this.setBackground(Color.WHITE);
  }

  DiagramShape getSelected() {
    return selecteds.stream().findFirst().orElse(null);
  }

  public <T extends DiagramShape> Set<T> getItems(Class<T> type) {
    return shapes.stream()
      .filter(type::isInstance)
      .map(type::cast)
      .collect(Collectors.toSet());
  }

  public <T extends DiagramShape> long getItemCount(Class<T> type) {
    return shapes.stream()
      .filter(type::isInstance)
      .count();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    prevX = startX = ZoomUtility.get(e.getX());
    prevY = startY = ZoomUtility.get(e.getY());
    if (isUpdateAllowed && currentAction == UMLActionType.umlclass && getHoverClass() == null) {
      diagramEditor.loadEditor(new ClassForm(this, null, true));
    }
    if (isUpdateAllowed && currentAction == UMLActionType.association) {
      DiagramShape _selected = getSelected();
      if (_selected != null && _selected instanceof Association) {
        addAssociationClass((Association) _selected);
      }
    }
    if (e.isControlDown()) {
      final DiagramShape _selected = getSelected();
      if (_selected != null && _selected instanceof Relationship) {
        addAnchor((Relationship) _selected, ZoomUtility.get(e.getX()), ZoomUtility.get(e.getY()));
      }
    }
    if (isUpdateAllowed && currentAction == UMLActionType.disjoint || currentAction == UMLActionType.relation
      || currentAction == UMLActionType.isarelation) {
      addRelation(ZoomUtility.get(e.getX()), ZoomUtility.get(e.getY()));
      return;
    }
    if (e.getClickCount() == 2) {
      if (e.isPopupTrigger()) {
        removeSelected();
        return;
      } else {
        final DiagramShape _selected = getSelected();
        if (_selected != null) {
          if (_selected instanceof UMLClass) {
            diagramEditor.loadEditor(new ClassForm(this, (UMLClass) _selected, isUpdateAllowed));
          } else if (_selected instanceof Relationship) {
            Relationship rlt = (Relationship) _selected;
            if (rlt instanceof Association) {
              diagramEditor.loadEditor(new RelationForm(this, (Association) rlt, isUpdateAllowed));
            }

          }
        }
      }
    }
    updateSelection(e.isControlDown());
    repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    defaultCursor();
    //all selected items are moved
    if (shapeMoved && selecteds.size() > 0) {
      MoveShapeEdit moveShapeEdit = new MoveShapeEdit(selecteds, ZoomUtility.get(e.getX()) - startX, ZoomUtility.get(e.getY()) - startY);
      undoManager.addEdit(moveShapeEdit);
      if (showGrid) {
        selecteds.forEach(DiagramShape::stickToGrid);
      }
      shapeMoved = false;
    }
    selectionArea = null;
    repaint();
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  private void addAssociationClass(Relationship _relation) {
    if (_relation instanceof Association) {
      Association association = (Association) _relation;
      AssociationClass aClass = new AssociationClass(association);
      aClass.setStartX(startX);
      aClass.setStartY(startY);
      if (showGrid) {
        aClass.stickToGrid();
      }
      association.setAssociationClass(aClass);
      shapes.add(aClass);
      undoManager.addEdit(new AddDeleteClassEdit(this, aClass, true));
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    moveCursor();
    int diffX = ZoomUtility.get(e.getX()) - prevX;
    int diffY = ZoomUtility.get(e.getY()) - prevY;
    // if right mouse is used for drag, scroll the drawing area
    if (SwingUtilities.isRightMouseButton(e)) {
      JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
      if (viewPort != null) {
        Rectangle view = viewPort.getViewRect();
        view.x -= diffX;
        view.y -= diffY;
        this.scrollRectToVisible(view);
      }
    } else {
      //move all selected objects
      if (selectionArea == null && selecteds.size() > 0) {
        selecteds.forEach(_selected -> {
          if (_selected != null) {
            _selected.translate(diffX, diffY);
            prevX = ZoomUtility.get(e.getX());
            prevY = ZoomUtility.get(e.getY());
            shapeMoved = true;
            repaint();
          }
        });
      } else {
        selectArea(startX, startY, diffX, diffY);
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    int x = ZoomUtility.get(e.getX());
    int y = ZoomUtility.get(e.getY());
    shapes.forEach(shape -> {
      if (shape != null && shape.over(x, y)) {
        handCursor();
      } else {
        defaultCursor();
      }
    });
    repaint();
  }

  private void selectArea(int x, int y, int width, int height) {
    if (width < 0) {
      width = -width;
      x = x - width;
    }
    if (height < 0) {
      height = -height;
      y = y - height;
    }
    selectionArea = new Rectangle(x, y, width, height);
    selecteds.forEach(shape -> shape.setState(State.NORMAL));
    selecteds = shapes.stream()
      .filter(shape -> shape.inside(selectionArea))
      .collect(Collectors.toSet());
    selecteds.forEach(shape -> shape.setState(State.SELECTED));
    repaint();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.isControlDown()) {
      ZoomUtility.changeZoom(e.getPreciseWheelRotation());
      //TODO position according mouse location
      JViewport viewPort = (JViewport) SwingUtilities
        .getAncestorOfClass(JViewport.class, this);
      if (viewPort != null) {
        Rectangle view = viewPort.getViewRect();
        view.x = ZoomUtility.get(e.getX());
        view.y = ZoomUtility.get(e.getY());
        this.scrollRectToVisible(view);
      }
    } else if (getParent() != null) {
      getParent().dispatchEvent(e);
    }
    repaint();
  }

  private void moveCursor() {
    super.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  private void defaultCursor() {
    super.setCursor(Cursor.getDefaultCursor());
  }

  private void handCursor() {
    super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  private void addAnchor(Relationship rlt, int x, int y) {
    RelationAnchor anchor = rlt.addAnchor(x, y);
    if (anchor != null) {
      undoManager.addEdit(new AddDeleteAnchorEdit(rlt, anchor, true));
    }
  }

  private void addRelation(int x, int y) {
    final DiagramShape _selected = getSelected();
    if (_selected != null && _selected instanceof UMLClass) {
      UMLClass secondClass = getHoverClass();
      //if second class is selected
      if (secondClass != null) {
        UMLClass firstClass = (UMLClass) _selected;
        Relationship relationship;
        if (currentAction.equals(UMLActionType.disjoint)) {
          if (firstClass.equals(secondClass)) {
            UIUtility.error("Class cannot be disjoint of itself");
            resetTemporaryAnchors();
            return;
          }
          if (firstClass.isRelationExist(secondClass, Disjoint.class)) {
            UIUtility.error("Classes are already disjoint");
            resetTemporaryAnchors();
            return;
          }
          relationship = new Disjoint(firstClass, secondClass);

        } else if (currentAction.equals(UMLActionType.isarelation)) {
          if (firstClass.equals(secondClass)) {
            UIUtility.error("Class cannot have an IS-A relationship with itself");
            resetTemporaryAnchors();
            return;
          }
          if (firstClass.isRelationExist(secondClass, Inheritance.class)) {
            UIUtility.error("Classes already have an IS-A relationship");
            resetTemporaryAnchors();
            return;
          }
          relationship = new Inheritance(secondClass, firstClass);
        } else {
          if (firstClass.equals(secondClass) && tempAnchors.size() < 1) {
            tempAnchors.add(new RelationAnchor(firstClass.getEndX() + 50, firstClass.getCenterY()));
            tempAnchors.add(new RelationAnchor(secondClass.getEndX() + 50, secondClass.getEndY() + 50));
            tempAnchors.add(new RelationAnchor(secondClass.getCenterX(), secondClass.getEndY() + 50));
          }
          relationship = new Association("rel" + shapes.size(), firstClass, secondClass);
        }
        relationship.addAnchors(tempAnchors);
        shapes.add(relationship);
        if (relationship instanceof Association) {
          UIUtility.addName(relationship.getName());
          diagramEditor.loadEditor(new RelationForm(this, (Association) relationship, isUpdateAllowed));
        }
        relationship.getFirstClass().addRelation(relationship);
        relationship.getSecondClass().addRelation(relationship);
        undoManager.addEdit(new AddDeleteRelationEdit(this, relationship, null, true));
        resetTemporaryAnchors();
        clearSelection();
      } else {
        tempAnchors.add(new RelationAnchor(x, y));
      }
    } else {
      updateSelection(false);
    }
  }

  private void resetTemporaryAnchors() {
    tempAnchors.clear();
  }

  void clearSelection() {
    selecteds.forEach(obj -> obj.setState(State.NORMAL));
    selecteds.clear();
  }

  private void updateSelection(boolean isCtrlDown) {
    DiagramShape _selected = getHoverShape();
    if (!isCtrlDown) {
      if (_selected == null || (!selecteds.contains(_selected))) {
        selecteds.forEach(obj -> obj.setState(State.NORMAL));
        selecteds.clear();
      }
    }
    if (_selected != null) {
      _selected.setState(State.SELECTED);
      selecteds.add(_selected);
    }
  }

  private UMLClass getHoverClass() {
    return getClasses().stream()
      .filter(shape -> shape.over(startX, startY))
      .findFirst().orElse(null);
  }

  DiagramShape getHoverShape() {
    DiagramShape _selected = getHoverClass();
    if (_selected == null) {
      _selected = shapes.stream()
        .filter(shape -> shape.over(startX, startY))
        .findFirst().orElse(null);
      if (_selected instanceof Relationship) {
        ((Relationship) _selected).selectAnchor(startX, startY);
      }
    }
    return _selected;
  }

  @Override
  public void addClass(UMLClass cls) {
    shapes.add(cls);
    repaint();
  }

  @Override
  public void addRelation(Relationship relation) {
    if (relation instanceof Association) {
      UIUtility.addName(relation.getName());
    }
    //add relation to the classes
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

  public void addEdit(UndoableEdit edit) {
    undoManager.addEdit(edit);
  }

  @Override
  public void createClass(UMLClass newClass) {
    //stick to grid: use remainder of dividing by grid size
    newClass.setStartX(startX);
    newClass.setStartY(startY);
    //if grid is enabled, stick it to the grid
    if (showGrid)
      newClass.stickToGrid();
    // add class to the classes list
    shapes.add(newClass);
    undoManager.addEdit(new AddDeleteClassEdit(this, newClass, true));
  }

  @Override
  public Set<Association> getRelations() {
    return shapes.stream()
      .filter(Association.class::isInstance)
      .map(Association.class::cast)
      .collect(Collectors.toSet());
  }

  @Override
  public Set<DiagramShape> getAllShapes(boolean forJSON) {
    if (forJSON) {
      LinkedHashSet<DiagramShape> all = shapes.stream().filter(UMLClass.class::isInstance).collect(Collectors.toCollection(LinkedHashSet::new));
      all.addAll(shapes.stream().filter(Association.class::isInstance).collect(Collectors.toCollection(LinkedHashSet::new)));
      all.addAll(shapes.stream().filter(shape -> !(shape instanceof UMLClass) && !(shape instanceof Association)).collect(Collectors.toCollection(LinkedHashSet::new)));
      return all;
    }
    return shapes;
  }

  @Override
  public boolean removeShape(DiagramShape _selected) {
    int choice = JOptionPane.showConfirmDialog(null,
      "Are you sure you want to delete selected object?",
      "Delete Confirmation", JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      if (_selected instanceof UMLClass) {
        UMLClass cls = (UMLClass) _selected;
        undoManager.addEdit(new AddDeleteClassEdit(this, cls, false));
        removeClass(cls);
        return true;
      }
      if (_selected instanceof Association) {
        Association rel = (Association) _selected;
        RelationAnchor deleted = rel.deleteAnchor();
        if (deleted == null) {
          undoManager.addEdit(new AddDeleteRelationEdit(this, rel, rel.getAssociationClass(), false));
          if (rel.hasAssociation()) {
            removeClass(rel.getAssociationClass());
          }
          removeRelation(rel);
          return true;
        } else {
          //add undo edit to the manager
          undoManager.addEdit(new AddDeleteAnchorEdit(rel, deleted, false));
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Set<UMLClass> getClasses() {
    return getItems(UMLClass.class);
  }

  private void drawGrid(Graphics2D g2d) {
    Dimension size = getMaximumSize();
    Color oldColor = g2d.getColor();
    g2d.setColor(new Color(0, 0, 0, 0.1f));
    for (int i = 0; i < size.width; i += DrawingConstants.GRID_SIZE) {
      g2d.drawLine(i, 0, i, size.height);
      g2d.drawLine(0, i, size.width, i);
    }
    g2d.setColor(oldColor);
  }

  public void setCurrentAction(ActionType newAction) {
    resetTemporaryAnchors();
    defaultCursor();
    this.currentAction = newAction;
  }

  public void clear() {
    if (UIUtility.confirm(UMLEditorMessages.CLEAR_DIAGRAM)) {
      shapes.clear();
      //after clearing it won't be possible to undo-redo operations
      undoManager.discardAllEdits();
      UIUtility.clearNames();
      diagramEditor.loadEditor(null);
    }
  }

  /**
   * Method for deleting currently selected object from diagram.
   */
  public void removeSelected() {
    // it requires a selected object
    DiagramShape _selected = getSelected();
    if (_selected != null) {
      removeShape(_selected);
    }
  }

  public void undo() {
    if (undoManager.canUndo()) {
      undoManager.undo();
      repaint();
    } else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  public void redo() {
    if (undoManager.canRedo()) {
      undoManager.redo();
      repaint();
    } else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  public void toggleGrid() {
    showGrid = !showGrid;
    repaint();
  }

  private void paintDrawing(Graphics g2d, int x, int y) {
    //first remove logo and grid from panel
    boolean gridStatus = showGrid;
    boolean logoStatus = showLogo;
    if (gridStatus) {
      toggleGrid();
    }
    if (logoStatus) {
      toggleLogo();
    }
    //translate according drawing area
    g2d.translate(-x, -y);
    //paint diagram to the graphics object
    print(g2d);
    //take back grid and logo status
    if (gridStatus) {
      toggleGrid();
    }
    if (logoStatus) {
      toggleLogo();
    }
  }

  public void exportImage() {
    try {
      File file = IOUtility.selectFileToSave(FileType.IMAGE);
      if (file != null) {
        Rectangle drawingArea = getDrawingArea();
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.equals("svg")) {
          SVGGraphics2D svgGenerator = IOUtility.getSVGGraphics(drawingArea.getSize());
          paintDrawing(svgGenerator, drawingArea.x, drawingArea.y);
          svgGenerator.stream(new FileWriter(file));
          svgGenerator.dispose();
        } else {
          BufferedImage bi = new BufferedImage(drawingArea.width, drawingArea.height, BufferedImage.TYPE_INT_RGB);
          Graphics g = bi.createGraphics();
          paintDrawing(g, drawingArea.x, drawingArea.y);
          ImageIO.write(bi, extension, file);
          g.dispose();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable((g, format, page) -> {
      if (page > 0) {
        return Printable.NO_SUCH_PAGE;
      }
      // get the bounds of the component
      Rectangle drawingArea = getDrawingArea();
      double cHeight = drawingArea.getSize().getHeight();
      double cWidth = drawingArea.getSize().getWidth();
      // get the bounds of the printable area
      double pHeight = format.getImageableHeight();
      double pWidth = format.getImageableWidth();
      double pXStart = format.getImageableX();
      double pYStart = format.getImageableY();
      //find ratio
      double xRatio = pWidth / cWidth;
      double yRatio = pHeight / cHeight;
      Graphics2D g2d = (Graphics2D) g;
      //translate and scale accordingly
      g2d.translate(pXStart, pYStart);
      g2d.scale(xRatio, yRatio);
      paintDrawing(g2d, drawingArea.x, drawingArea.y);
      return Printable.PAGE_EXISTS;
    });
    if (printJob.printDialog()) {
      try {
        printJob.print();
      } catch (PrinterException e) {
        UIUtility.error(e.getMessage());
      }
    }
  }

  private void toggleLogo() {
    showLogo = !showLogo;
  }

  private void drawLogo(Graphics2D g2d, JViewport viewport) {
    if (viewport != null) {
      Rectangle viewportRectangle = viewport.getViewRect();
      BufferedImage logo = UIUtility.getLogo();
      if (logo != null) {
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        g2d.drawImage(logo, (int) viewportRectangle.getX() + 150, (int)
          viewportRectangle.getY() + 300, logo.getWidth(), logo.getHeight(), null);
        g2d.setComposite(oldComposite);
      }
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
      if (showLogo) {
        drawLogo(g2d, (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this));
      }
    }
    g2d.scale(ZoomUtility.ZOOMING_SCALE, ZoomUtility.ZOOMING_SCALE);
    g2d.setStroke(DrawingConstants.NORMAL_STROKE);
    if (showGrid) drawGrid(g2d);
    shapes.stream().filter(UMLClass.class::isInstance).map(UMLClass.class::cast).forEach(p -> p.calculateEndCoordinates(g2d));
    shapes.stream().filter(Relationship.class::isInstance).forEach(p -> p.draw(g2d));
    shapes.stream().filter(UMLClass.class::isInstance).forEach(p -> p.draw(g2d));
    if (selectionArea != null) {
      drawSelectionRectangle(g2d);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(ZoomUtility.getWidth(), ZoomUtility.getHeight());
  }

  private void drawSelectionRectangle(Graphics2D g2d) {
    Stroke oldStroke = g2d.getStroke();
    Color oldColor = g2d.getColor();
    g2d.setStroke(DrawingConstants.DISJOINT_STROKE);
    g2d.setColor(State.SELECTED.getColor());
    g2d.draw(selectionArea);
    g2d.setColor(oldColor);
    g2d.setStroke(oldStroke);
  }

  public void load(Set<DiagramShape> _shapes) {
    if (_shapes != null) {
      undoManager.discardAllEdits();
      UIUtility.clearNames();
      this.shapes = _shapes.stream().filter(Objects::nonNull).collect(Collectors.toSet());
      this.shapes.forEach(shape -> UIUtility.addName(shape.getName()));
    }
    if (diagramEditor != null) {
      diagramEditor.loadEditor(null);
    }
    repaint();
  }

  public void displayObjectList() {
    diagramEditor.loadEditor(new ObjectList(this));
  }

  private Set<RelationAnchor> getAllAnchors() {
    Set<RelationAnchor> relationAnchors = new LinkedHashSet<>();
    shapes.stream().filter(Association.class::isInstance).map(Association.class::cast).filter(r -> r.getAnchorCount() > 0).forEach(r -> relationAnchors.addAll(r.getAnchors()));
    return relationAnchors;
  }

  private Rectangle getDrawingArea() {
    int minX = getHeight(), minY = getWidth(), maxX = 0, maxY = 0;
    for (DiagramShape shape : shapes) {
      if (shape.getStartX() < minX)
        minX = shape.getStartX();
      if (shape.getStartY() < minY)
        minY = shape.getStartY();
      if (shape.getEndX() > maxX)
        maxX = shape.getEndX();
      if (shape.getEndY() > maxY)
        maxY = shape.getEndY();
    }
    for (RelationAnchor anchor : getAllAnchors()) {
      if (anchor.getX() < minX)
        minX = anchor.getX();
      if (anchor.getY() < minY)
        minY = anchor.getY();
      if (anchor.getX() > maxX)
        maxX = anchor.getX();
      if (anchor.getY() > maxY)
        maxY = anchor.getY();
    }
    return new Rectangle(minX - DrawingConstants.GAP, minY - DrawingConstants.GAP, maxX - minX + 2 * DrawingConstants.GAP, maxY - minY + 2 * DrawingConstants.GAP);
  }

  public void layoutDiagram() {
    if (getItemCount(UMLClass.class) > 0) {
      if (UIUtility.confirm(UMLEditorMessages.LAYOUT_DIAGRAM)) {
        Graphics2D g2d = (Graphics2D) getGraphics();
        try {
          final int columnCount = Integer.parseInt(UIUtility.input("Enter number of columns for grid layout", "5"));
          final int padding = 20;
          int currentX = padding;
          int currentY = padding;
          int i = 0;
          int bestY = 0;
          for (UMLClass cls : getClasses()) {
            cls.setStartX(currentX);
            cls.setStartY(currentY);
            cls.calculateEndCoordinates(g2d);
            currentX = cls.getEndX() + padding;
            if (bestY < cls.getEndY())
              bestY = cls.getEndY();
            i++;
            if (i % columnCount == 0) {
              currentY = bestY + padding;
              currentX = padding;
              bestY = currentY;
            }
          }
        } catch (NumberFormatException e) {
          UIUtility.error("You didn't enter a correct integer number. Please try again.");
        }
        repaint();
      }
    }
  }
}
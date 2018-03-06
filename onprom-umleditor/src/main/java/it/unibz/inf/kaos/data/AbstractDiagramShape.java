/*
 * onprom-umleditor
 *
 * AbstractDiagramShape.java
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

package it.unibz.inf.kaos.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unibz.inf.kaos.interfaces.Diagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;
import it.unibz.inf.kaos.ui.utility.ZoomUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * This class provides a skeletal implementation of the DiagramShape interface,
 * to minimize the effort required to implement this interface.
 * @author T. E. Kalayci
 */
public abstract class AbstractDiagramShape<T extends Diagram> implements DiagramShape<T>, Cloneable {
  private int startX;
  private int startY;
  private String name;
  private String longName;
  @JsonIgnore
  private State state = State.NORMAL;
  @JsonIgnore
  private State previous = State.NORMAL;
  @JsonIgnore
  private int endX;
  @JsonIgnore
  private int endY;

  AbstractDiagramShape() {
  }

  AbstractDiagramShape(String _name, String _longName) {
    name = _name;
    longName = _longName;
  }

  AbstractDiagramShape(int x, int y) {
    startX = x;
    startY = y;
  }

    AbstractDiagramShape(String n, int x, int y) {
        name = n;
        startX = x;
        startY = y;
    }


  @Override
  public String getName() {
    return name;
  }

  public void setName(String _name) {
    name = _name;
  }

  @Override
  public String getCleanName() {
    return getName().replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  @Override
  public String getLongName() {
    if (longName == null || longName.isEmpty())
      return getName();
    return longName;
  }

  @Override
  public int getStartX() {
    return startX;
  }

  @Override
  public void setStartX(int startX) {
    this.startX = startX;
  }

  @Override
  public int getStartY() {
    return startY;
  }

  @Override
  public void setStartY(int startY) {
    this.startY = startY;
  }

  @Override
  public int getCenterX() {
    return (startX + endX) / 2;
  }

  @Override
  public int getCenterY() {
    return (startY + endY) / 2;
  }

  @Override
  public int getEndX() {
    return endX;
  }

  void setEndX(int endX) {
    this.endX = endX;
  }

  @Override
  public int getEndY() {
    return endY;
  }

  void setEndY(int endY) {
    this.endY = endY;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void setState(State state) {
    this.state = state;
    this.previous = state;
  }

  @Override
  public void translate(int diffX, int diffY) {
      startX += diffX;
      startY += diffY;
    checkBoundaries();
  }

  @Override
  public boolean over(int x, int y) {
    if (contains(x, y)) {
        if (state != State.HIGHLIGHTED) {
        previous = state;
      }
      state = State.HIGHLIGHTED;
    } else {
      state = previous;
    }
      return state == State.HIGHLIGHTED;
  }

  @Override
  public boolean notSelected() {
      return state != State.SELECTED;
  }

  @Override
  public void stickToGrid() {
      this.startX = this.startX - (getCenterX() % DrawingUtility.GRID_SIZE);
      this.startY = this.startY - (getCenterY() % DrawingUtility.GRID_SIZE);
    checkBoundaries();
  }

  @Override
  public boolean inside(Rectangle selectionArea) {
    return selectionArea.contains(getCenterX(), getCenterY());
  }

  private boolean contains(int x, int y) {
    Shape shape = getShape();
    if (shape instanceof Line2D) {
      return shape.intersects(x, y, DrawingUtility.MARGIN, DrawingUtility.MARGIN);
    } else {
      return shape.contains(x, y);
    }
  }

  Shape getShape() {
    return new Rectangle(getStartX(), getStartY(), getEndX() - getStartX(), getEndY() - getStartY());
  }

  private void checkBoundaries() {
    if (getStartX() < 0)
      setStartX(0);
    if (getStartY() < 0)
      setStartY(0);

    if (getStartX() > ZoomUtility.INITIAL_SIZE.width) {
      setStartX(ZoomUtility.INITIAL_SIZE.width);
    }
    if (getStartY() > ZoomUtility.INITIAL_SIZE.height) {
      setStartY(ZoomUtility.INITIAL_SIZE.height);
    }
  }

  void drawLabel(Graphics2D g2d, String string, int x, int y, boolean background) {
    if (background) {
      Color oldColor = g2d.getColor();
      g2d.setColor(Color.WHITE);
      g2d.fill(getStringBounds(g2d, string, x, y));
      g2d.setColor(oldColor);
    }
    g2d.drawString(string, x, y);
  }

  Rectangle getStringBounds(Graphics2D g2d, String string, int x, int y) {
    FontMetrics fm = g2d.getFontMetrics();
    Rectangle2D rect = fm.getStringBounds(string, g2d);
    return new Rectangle(x, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
  }

    @Override
    public JPanel getForm(final T panel) {
        return null;
    }
}

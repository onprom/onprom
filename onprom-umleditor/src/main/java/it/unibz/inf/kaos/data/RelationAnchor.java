/*
 * onprom-umleditor
 *
 * RelationAnchor.java
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

package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.ui.utility.DrawingUtility;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author T. E. Kalayci
 */
public class RelationAnchor extends AbstractDiagramShape {

  public RelationAnchor() {
  }

  public RelationAnchor(int x, int y) {
    super(x, y);
  }

  public int getX() {
    return getStartX();
  }

  public int getY() {
    return getStartY();
  }

  public String getName() {
    return "Anchor[" + getX() + "," + getY() + "]";
  }

  @Override
  Shape getShape() {
      return new Rectangle2D.Double((double) getX() - DrawingUtility.HALF_ANCHOR_RADIUS,
              (double) getY() - DrawingUtility.HALF_ANCHOR_RADIUS,
              DrawingUtility.ANCHOR_RADIUS, DrawingUtility.ANCHOR_RADIUS);
  }

  public void draw(Graphics2D g2d) {
      final Color oldColor = g2d.getColor();
      if (getState() == State.SELECTED) {
          g2d.setColor(getState().getColor());
      }
    g2d.fill(getShape());
    g2d.setColor(oldColor);
  }

  int[] getPosition() {
    return new int[]{getX(), getY()};
  }

  public String toString() {
    return String.format("%d,%d", getX(), getY());
  }

}
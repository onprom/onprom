/*
 * onprom-toolkit
 *
 * ResourceShape.java
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

import it.unibz.inf.kaos.ui.component.TreeNode;

import java.awt.*;

/**
 * Resource shape to draw in extraction panel
 * <p>
 * @author T. E. Kalayci on 10-Jul-2017.
 */
public class ResourceShape extends AbstractDiagramShape {
  private final static int EDGE = 100;
  private final static Color LOADED = new Color(232, 241, 250);
  private final static Color NOT_LOADED = new Color(255, 182, 176);
  private TreeNode<Object> treeNode;

  public ResourceShape(String _name, TreeNode<Object> _node) {
    super(_name, "");
    treeNode = _node;
  }

  public void setTreeNode(TreeNode<Object> _node) {
    treeNode = _node;
  }

  @Override
  public void draw(Graphics2D g2d) {
    Color oldColor = g2d.getColor();
    Stroke oldStroke = g2d.getStroke();
    Font oldFont = g2d.getFont();
    int startX = getStartX();
    int startY = getStartY();
    String label = getName();
    //draw shapes, string
    //rectangle background color
    if (treeNode != null)
      g2d.setColor(LOADED);
    else
      g2d.setColor(NOT_LOADED);
    //filled rectangle background of annotation
    g2d.fillRect(startX, startY, EDGE, EDGE);
    //set color of rectangle outline according to annotation state
    g2d.setColor(getState().getColor());
    //draw rectangle outline
    g2d.drawRect(startX, startY, EDGE, EDGE);
    //draw label of annotation if it exists
    int fontWidth = g2d.getFontMetrics().stringWidth(label);
    int typeCoord = startX + (EDGE - fontWidth) / 2;
    g2d.drawString(label, typeCoord, startY + (EDGE / 2));
    //set end coordinates for annotation
    setEndX(startX + EDGE);
    setEndY(startY + EDGE);
    //load previous properties again
    g2d.setColor(oldColor);
    g2d.setStroke(oldStroke);
    g2d.setFont(oldFont);
  }
}

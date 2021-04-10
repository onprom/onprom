/*
 * onprom-umleditor
 *
 * UMLClass.java
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.form.ClassForm;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author T. E. Kalayci
 */
public class UMLClass extends AbstractDiagramShape<UMLDiagram> {
    private final Set<Relationship> relations = Sets.newLinkedHashSet();
    private List<Attribute> attributes = Lists.newLinkedList();

  public UMLClass() {
    this("Class");
  }

  public UMLClass(String _name) {
    this(_name, null);
  }

  public UMLClass(String _name, String _longName) {
    super(_name, _longName);
  }

    public UMLClass(String n, int x, int y) {
        super(n, x, y);
    }

  public void addRelation(Relationship relation) {
    relations.add(relation);
  }

  public void removeRelation(Relationship relation) {
    relations.remove(relation);
  }

  public Set<Relationship> getRelations() {
    return relations;
  }

  public int getRelationCount() {
    return relations.size();
  }

  public void addAttribute(Attribute attr) {
    attributes.add(attr);
  }

  public boolean isRelationExist(UMLClass secondClass, Class type) {
    for (Relationship relation : relations) {
      if (relation.getFirstClass().equals(secondClass) || relation.getSecondClass().equals(secondClass)) {
        if (relation.getClass().equals(type)) {
          return true;
        }
      }
    }
    return false;
  }

    public List<Attribute> getAttributes() {
    return attributes;
  }

    public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public int getAttributeCount() {
    return attributes != null ? attributes.size() : 0;
  }

  public boolean equals(Object object) {
      return object instanceof UMLClass && getName().equals(((UMLClass) object).getName());
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    UMLClass clone = (UMLClass) super.clone();
    clone.setAttributes(cloneAttributes());
    return clone;
  }

  public String toString() {
    return getName();
  }

  public String getCoordinates() {
    return String.format("%d,%d", getStartX(), getStartY());
  }

  public void setCoordinates(String coordinates) {
    if (coordinates == null || coordinates.isEmpty())
      return;
    String[] results = coordinates.split(",");
    setStartX(Integer.parseInt(results[0]));
    setStartY(Integer.parseInt(results[1]));
  }

    @Nonnull
    public Optional<Attribute> getClickedAttribute(int x, int y) {
        return attributes.stream().filter(attribute -> attribute.contains(x, y)).findFirst();
  }

    @Nonnull
    public List<Attribute> cloneAttributes() {
        List<Attribute> newList = Lists.newLinkedList();
        attributes.forEach(attribute -> newList.add(attribute.getClone()));
        return newList;
    }

    public void calculateEndCoordinates(Graphics g2d) {
    Font oldFont = g2d.getFont();
        g2d.setFont(DrawingUtility.CLASS_NAME_FONT);
    int fontWidth = g2d.getFontMetrics().stringWidth(getName());
    int fontHeight = g2d.getFontMetrics().getHeight();
        g2d.setFont(DrawingUtility.ATTRIBUTE_NAME_FONT);
    int attrHeight = g2d.getFontMetrics().getHeight();
    for (Attribute attr : attributes) {
      int attrWidth = g2d.getFontMetrics().stringWidth(attr.toString());
      if (attrWidth > fontWidth) {
        fontWidth = attrWidth;
      }
    }
    int boxHeight = fontHeight + attrHeight * (attributes.size() + 1);
      setEndX(getStartX() + fontWidth + DrawingUtility.MARGIN * 2);
      setEndY(getStartY() + boxHeight);
    g2d.setFont(oldFont);
  }

    public boolean equalsOrInherits(UMLClass secondClass) {
        return equals(secondClass) || isRelationExist(secondClass, Inheritance.class);
    }

  public void draw(Graphics2D g2d) {
    //store previous font, stroke and color
      final Font oldFont = g2d.getFont();
      final Stroke oldStroke = g2d.getStroke();
      final Color oldColor = g2d.getColor();
    //set default color
      g2d.setColor(DrawingUtility.BACKGROUND);
    final int width = getEndX() - getStartX();
    final int height = getEndY() - getStartY();
    // draw class box first
    g2d.fill(new Rectangle2D.Double(getStartX(), getStartY(), width, height));
    g2d.setColor(oldColor);
    // draw class box
    g2d.setColor(getState().getColor());
    g2d.draw(new Rectangle2D.Double(getStartX(), getStartY(), width, height));
    // draw class name
      g2d.setFont(DrawingUtility.CLASS_NAME_FONT);
      final int fontHeight = g2d.getFontMetrics().getHeight();
      final int classNameWidth = g2d.getFontMetrics().stringWidth(getName());
    drawLabel(g2d, getName(), getStartX() + ((width - classNameWidth) / 2), getStartY() + (fontHeight / 2) + DrawingUtility.MARGIN, false);
    // draw line after class name
    g2d.draw(new Line2D.Float(getStartX(), getStartY() + fontHeight, getEndX(), getStartY() + fontHeight));
    int currentY = getStartY() + fontHeight;
    // draw class attributes
      g2d.setFont(DrawingUtility.ATTRIBUTE_NAME_FONT);
      final int attrHeight = g2d.getFontMetrics().getHeight();
    for (Attribute attr : attributes) {
      currentY = currentY + attrHeight;
      g2d.setColor(attr.getState().getColor());
      drawLabel(g2d, attr.toString(), getStartX() + DrawingUtility.MARGIN, currentY, false);
      attr.setBounds(getStringBounds(g2d, attr.toString(), getStartX() + DrawingUtility.MARGIN, currentY));
    }
    // draw line after attributes
    g2d.setColor(getState().getColor());
    g2d.draw(new Line2D.Float(getStartX(), currentY + DrawingUtility.MARGIN, getEndX(), currentY + DrawingUtility.MARGIN));
    g2d.setFont(oldFont);
    g2d.setColor(oldColor);
    g2d.setStroke(oldStroke);
  }

    @Override
    public Optional<JPanel> getForm(UMLDiagram panel) {
        return Optional.of(new ClassForm(panel, this));
    }
}
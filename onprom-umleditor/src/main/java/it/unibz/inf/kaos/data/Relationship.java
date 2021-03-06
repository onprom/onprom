/*
 * onprom-umleditor
 *
 * Relationship.java
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

package it.unibz.inf.kaos.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Relationship class
 * <p>
 * @author T. E. Kalayci
 */
public abstract class Relationship extends AbstractDiagramShape<UMLDiagram> {
    private UMLClass firstClass;
    private UMLClass secondClass;
    private List<RelationAnchor> anchors;

    @JsonIgnore
    private List<RelationAnchor> selectedAnchors;

    public Relationship() {
    }

    public Relationship(String name, String longName) {
        this(name, longName, null, null);
    }

    public Relationship(String name, UMLClass firstClass, UMLClass secondClass) {
        this(name, null, firstClass, secondClass);
    }

    public Relationship(String name, String longName, UMLClass firstClass, UMLClass secondClass) {
        super(name, longName);
        setFirstClass(firstClass);
        setSecondClass(secondClass);
    }

    public String toString() {
        return getName();
    }

    protected abstract String getDisplayString();

    Stroke getStroke() {
        return DrawingUtility.RELATION_STROKE;
    }

    public int getStartX() {
        return getFirstClass().getCenterX();
    }

    public int getStartY() {
        return getFirstClass().getCenterY();
    }

    @Override
    public int getCenterX() {
        return (getStartX() + getEndX()) / 2;
    }

    @Override
    public int getCenterY() {
        return (getStartY() + getEndY()) / 2;
    }

    public int getEndX() {
        return getSecondClass().getCenterX();
    }

    public int getEndY() {
        return getSecondClass().getCenterY();
    }

    public void translate(int diffX, int diffY) {
        if (selectedAnchors != null && !selectedAnchors.isEmpty()) {
            selectedAnchors.forEach(anchor -> anchor.translate(diffX, diffY));
        } else {
            if (anchors != null && !anchors.isEmpty()) {
                anchors.forEach(anchor -> anchor.translate(diffX, diffY));
            }
        }
    }

    @Override
    Shape getShape() {
        if (getAnchorCount() > 0) {
            GeneralPath relationLine = new GeneralPath();
            relationLine.moveTo(getStartX(), getStartY());
            for (RelationAnchor anchor : getAnchors()) {
                relationLine.lineTo(anchor.getX(), anchor.getY());
            }
            relationLine.lineTo(getEndX(), getEndY());
            return relationLine;
        } else {
            return new Line2D.Float(getStartX(), getStartY(), getEndX(), getEndY());
        }
    }

    public void draw(Graphics2D g2d) {
        // store current stroke and color
        final Font oldFont = g2d.getFont();
        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g2d.getColor();
        g2d.setFont(DrawingUtility.RELATION_FONT);
        // color according to state
        g2d.setColor(getState().getColor());
        g2d.setStroke(getStroke());
        g2d.draw(getShape());
        drawAnchors(g2d);
        if (!getDisplayString().isEmpty()) {
            final int[] namePosition = getNamePosition();
            final Rectangle2D nameRectangle = g2d.getFontMetrics().getStringBounds(getDisplayString(), g2d);
            drawLabel(g2d, getDisplayString(), namePosition[0] - (int) (nameRectangle.getWidth() / 2), namePosition[1] - DrawingUtility.MARGIN, true);
        }
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
        g2d.setFont(oldFont);
    }

    public UMLClass getFirstClass() {
        return firstClass;
    }

    public void setFirstClass(UMLClass cls) {
        if (this.firstClass != null)
            this.firstClass.removeRelation(this);
        this.firstClass = cls;
        if (this.firstClass != null)
            this.firstClass.addRelation(this);
    }

    public UMLClass getSecondClass() {
        return secondClass;
    }

    public void setSecondClass(UMLClass cls) {
        if (this.secondClass != null)
            this.secondClass.removeRelation(this);
        this.secondClass = cls;
        if (this.secondClass != null)
            this.secondClass.addRelation(this);
    }

    @Nonnull
    public List<RelationAnchor> deleteAnchor() {
        if (selectedAnchors != null) {
            selectedAnchors.forEach(anchor -> anchor.setState(State.NORMAL));
            anchors.removeAll(selectedAnchors);
            return selectedAnchors;
        }
        return Lists.newArrayList();
    }

    public int getAnchorCount() {
        if (anchors == null) {
            return 0;
        }
        return anchors.size();
    }

    void selectAnchor(int x, int y) {
        if (anchors != null) {
            selectedAnchors = anchors.stream().filter(anchor -> anchor.over(x, y)).collect(Collectors.toList());
            anchors.forEach(anchor -> anchor.setState(State.NORMAL));
            if (selectedAnchors != null && !selectedAnchors.isEmpty())
                selectedAnchors.forEach(anchor -> anchor.setState(State.SELECTED));
        }
    }

    RelationAnchor getFirstAnchor() {
        return anchors.get(0);
    }

    RelationAnchor getLastAnchor() {
        return anchors.get(anchors.size() - 1);
    }

    public RelationAnchor addAnchor(int x, int y) {
        return addAnchor((new RelationAnchor(x, y)));
    }

    public void removeAnchor(List<RelationAnchor> anchor) {
        if (anchors != null) {
            anchors.removeAll(anchor);
        }
    }

    private RelationAnchor addAnchor(RelationAnchor anchor) {
        if (anchors == null) {
            anchors = Lists.newArrayList();
        }
        if (!anchors.contains(anchor)) {
            anchors.add(anchor);
            return anchor;
        }
        return null;
    }

    public void addAnchors(Iterable<RelationAnchor> _anchors) {
        for (RelationAnchor anchor : _anchors) {
            addAnchor(new RelationAnchor(anchor.getX(), anchor.getY()));
        }
    }

    List<RelationAnchor> getAnchors() {
        return anchors;
    }

    public String getAnchorsString() {
        if (getAnchorCount() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            anchors.forEach(anchor -> {
                strBuilder.append(anchor);
                strBuilder.append(";");
            });
            String result = strBuilder.toString();
            return result.substring(0, result.length() - 1);
        }
        return "";
    }

    public void setAnchorCoordinates(String anchorString) {
        if (anchorString == null || anchorString.isEmpty())
            return;
        String[] anchors = anchorString.split(";");
        for (String anchor : anchors) {
            String[] coordinates = anchor.split(",");
            addAnchor(new RelationAnchor(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
        }
    }

    int[] getNamePosition() {
        UMLClass firstClass = getFirstClass();
        UMLClass secondClass = getSecondClass();
        if (getAnchorCount() < 1) {
            return new int[]{(firstClass.getCenterX() + secondClass
                    .getCenterX()) / 2, (firstClass.getCenterY() + secondClass
                    .getCenterY()) / 2};
        }
        //return middle anchor's position
        int[] position = getAnchors().get(getAnchorCount() / 2).getPosition();
        position[0] += DrawingUtility.MARGIN;
        position[1] -= DrawingUtility.MARGIN;
        return position;
    }

    private void drawAnchors(Graphics2D g2d) {
        if (getAnchorCount() > 0) {
            anchors.forEach(p -> p.draw(g2d));
        }
    }

    public boolean isFirstFunctional() {
        return true;
    }

    public boolean isSecondFunctional() {
        return true;
    }
}

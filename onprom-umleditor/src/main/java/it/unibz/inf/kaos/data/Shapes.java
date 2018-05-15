/*
 * onprom-umleditor
 *
 * Shapes.java
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

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.interfaces.DiagramShape;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by T. E. Kalayci on 17-Nov-2017.
 */
public class Shapes implements Iterable<DiagramShape> {
    private Set<DiagramShape> shapes = Sets.newLinkedHashSet();
    private Set<DiagramShape> selectedShapes = Sets.newLinkedHashSet();

    public DiagramShape getSelected() {
        DiagramShape shape = selectedShapes.stream().filter(UMLClass.class::isInstance).findFirst().orElse(null);
        if (shape == null) {
            return selectedShapes.stream().findFirst().orElse(null);
        }
        return shape;
    }


    public boolean moveSelectedShapes(int x, int y) {
        selectedShapes.forEach(selected -> selected.translate(x, y));
        return !selectedShapes.isEmpty();
    }

    public Set<DiagramShape> getSelectedShapes() {
        return selectedShapes;
    }

    public boolean isShapeSelected() {
        return !selectedShapes.isEmpty();
    }

    public void add(DiagramShape shape) {
        shapes.add(shape);
    }

    public void remove(DiagramShape shape) {
        shapes.remove(shape);
    }

    public Stream<Association> getAssociations() {
        return shapes.stream()
                .filter(Association.class::isInstance)
                .map(Association.class::cast);
    }

    public void clear() {
        shapes.clear();
    }

    public Stream<UMLClass> getClasses() {
        return getAll(UMLClass.class);
    }

    public Set<DiagramShape> getShapes(boolean forJSON) {
        if (forJSON) {
            LinkedHashSet<DiagramShape> all = shapes.stream().filter(UMLClass.class::isInstance).collect(Collectors.toCollection(LinkedHashSet::new));
            all.addAll(shapes.stream().filter(Association.class::isInstance).collect(Collectors.toCollection(LinkedHashSet::new)));
            all.addAll(shapes.stream().filter(shape -> !(shape instanceof UMLClass) && !(shape instanceof Association)).collect(Collectors.toCollection(LinkedHashSet::new)));
            return all;
        }
        return shapes;
    }

    public Set<DiagramShape> getShapesAndAnchors() {
        Set<DiagramShape> allShapes = getShapes(false);
        allShapes.addAll(getAll(Relationship.class)
                .filter(r -> r.getAnchorCount() > 0)
                .map(Relationship::getAnchors)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
        );
        return allShapes;
    }

    public void selectShapes(Rectangle selectionArea) {
        selectedShapes.forEach(shape -> shape.setState(State.NORMAL));
        selectedShapes = shapes.stream()
                .filter(shape -> shape.inside(selectionArea))
                .collect(Collectors.toSet());
        selectedShapes.forEach(shape -> shape.setState(State.SELECTED));
    }

    public boolean isEmpty() {
        return shapes.size() == 0;
    }

    public int size() {
        return shapes.size();
    }

    public void clearSelection() {
        selectedShapes.forEach(obj -> obj.setState(State.NORMAL));
        selectedShapes.clear();
    }

    public UMLClass getFirstClassAt(int x, int y) {
        return getClasses().filter(shape -> shape.over(x, y))
                .findFirst().orElse(null);
    }

    public boolean isClassOver(int x, int y) {
        return getClasses().anyMatch(shape -> shape.over(x, y));
    }

    protected DiagramShape getFirstShapeAt(int x, int y) {
        DiagramShape _selected = getFirstClassAt(x, y);
        if (_selected == null) {
            _selected = shapes.stream()
                    .filter(shape -> shape.over(x, y))
                    .findFirst().orElse(null);
            if (_selected instanceof Relationship) {
                ((Relationship) _selected).selectAnchor(x, y);
            }
        }
        return _selected;
    }

    public void updateSelection(boolean isCtrlDown, int startX, int startY) {
        DiagramShape _selected = getFirstShapeAt(startX, startY);
        if (!isCtrlDown) {
            if (_selected == null || (!selectedShapes.contains(_selected))) {
                selectedShapes.forEach(obj -> obj.setState(State.NORMAL));
                selectedShapes.clear();
            }
        }
        if (_selected != null) {
            _selected.setState(State.SELECTED);
            selectedShapes.add(_selected);
        }
    }

    public <T extends DiagramShape> Stream<T> getAll(Class<T> type) {
        return shapes.stream()
                .filter(type::isInstance)
                .map(type::cast);
    }

    public <T extends DiagramShape> long count(Class<T> type) {
        return shapes.stream()
                .filter(type::isInstance)
                .count();
    }

    public <T extends DiagramShape> T findFirst(Class<T> type) {
        return shapes.stream()
                .filter(type::isInstance)
                .findFirst()
                .map(type::cast)
                .orElse(null);
    }

    public DiagramShape over(int x, int y) {
        return shapes.stream()
                .filter(p -> p.over(x, y))
                .findFirst()
                .orElse(null);
    }


    public void load(final Set<DiagramShape> _shapes) {
        shapes = _shapes.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Iterator<DiagramShape> iterator() {
        return shapes.iterator();
    }
}

/*
 * onprom-umleditor
 *
 * UMLDiagram.java
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

package it.unibz.inf.onprom.interfaces;

import it.unibz.inf.onprom.data.Association;
import it.unibz.inf.onprom.data.Relationship;
import it.unibz.inf.onprom.data.UMLClass;

import java.util.stream.Stream;

/**
 * Interface for classes to use UML Diagram Panel
 * <p>
 * @author T. E. Kalayci
 * 04-Apr-2017
 */
public interface UMLDiagram extends Diagram {
    void addClass(UMLClass cls);
    void addRelation(Relationship relationship);
    void removeClass(UMLClass cls);
    void removeRelation(Relationship relationship);

    boolean removeShape(DiagramShape<? extends Diagram> shape);

    Stream<Association> getAssociations();

    Stream<UMLClass> getClasses();
}

/*
 * onprom-annoeditor
 *
 * Annotation.java
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

package it.unibz.inf.kaos.interfaces;

import it.unibz.inf.kaos.data.AnnotationAttribute;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;

import java.util.List;

/**
 * Interface of annotations supported by editor
 *
 * @author T. E. Kalayci on 19-Jun-2017.
 */
public interface Annotation extends DiagramShape {
    List<AnnotationAttribute> getAttributes();

    void setAttributes(List<AnnotationAttribute> _attributes);

    UMLClass getRelatedClass();
    List<AnnotationQuery> getQuery();

    AnnotationForm getForm(AnnotationDiagramPanel panel);
}

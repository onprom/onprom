/*
 * onprom-annoeditor
 *
 * NavigationalAttribute.java
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

import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * Attribute supporting navigation
 * <p>
 * @author T. E. Kalayci on 17/11/16.
 */
public class NavigationalAttribute {
  private Set<DiagramShape> path;
  private UMLClass umlClass;
  private Attribute attribute;
  private String filterClause;
  private Annotation annotation;

  NavigationalAttribute() {
  }

  public NavigationalAttribute(Annotation _annotation) {
    this(null, _annotation.getRelatedClass(), null);
    annotation = _annotation;
  }

  public NavigationalAttribute(UMLClass _cls, Attribute _attr) {
    this(null, _cls, _attr);
  }

  public NavigationalAttribute(Set<DiagramShape> _path, UMLClass _cls, Attribute _attr) {
    path = _path;
    umlClass = _cls;
    attribute = _attr;
  }

  void reset() {
    path = null;
    umlClass = null;
    attribute = null;
  }

  public Set<DiagramShape> getPath() {
    return path;
  }

  public void setPath(Set<DiagramShape> path) {
    this.path = path;
  }

  public UMLClass getUmlClass() {
    return umlClass;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        if (attribute != null)
            stringBuilder.append(attribute.getName()).append(" in ");
        if (umlClass != null)
            stringBuilder.append(umlClass.toString());
        if (path != null && path.size() > 0) {
            stringBuilder.append(" [ ");
            for (DiagramShape node : path) {
                stringBuilder.append(node).append("\u25b7");
            }
            //remove last character
            stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("]");
        }
        return stringBuilder.toString();
    }

  public String getFilterClause() {
    return filterClause;
  }

  public void setFilterClause(String _regex) {
    filterClause = _regex;
  }
}
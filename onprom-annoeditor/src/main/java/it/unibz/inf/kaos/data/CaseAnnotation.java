/*
 * onprom-annoeditor
 *
 * CaseAnnotation.java
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

import it.unibz.inf.kaos.data.query.old.V2.CaseAnnotationQueryV2;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.CaseForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;

/**
 * Case annotation class
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(type = "Case", color = "#E5BABA", action = AnnotationActionType.CASE)
public class CaseAnnotation extends AbstractAnnotation {

  private StringAttribute caseName;

  private CaseAnnotation() {
  }

  public CaseAnnotation(UMLClass _relatedClass) {
    super(_relatedClass);
  }

  public String toString() {
    if (caseName != null)
      return caseName.toString() + " [" + relatedClass.toString() + "]";
    return relatedClass.toString();
  }

  @Override
  public CaseAnnotationQueryV2 getQuery() {
    return new CaseAnnotationQueryV2(SimpleQueryExporter.getStringAttributeQuery(getCaseName(), getRelatedClass(), null), getRelatedClass().getCleanName(), "n", SimpleQueryExporter.getAttributeQueries(getAttributes()));
  }

  @Override
  public CaseForm getForm(AnnotationDiagramPanel panel) {
    return new CaseForm(panel, this);
  }

  public StringAttribute getCaseName() {
    return caseName;
  }

  public void setCaseName(StringAttribute name) {
    this.caseName = name;
  }
}

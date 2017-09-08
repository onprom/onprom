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

import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.CaseForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import org.apache.jena.arq.querybuilder.SelectBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Case annotation class
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(label = "Case", color = "#E5BABA", mnemonic = 'c', tooltip = "Create <u>C</u>", title = "<u>C</u>ase")
public class CaseAnnotation extends AbstractAnnotation {

  private StringAttribute caseName;

  private CaseAnnotation() {
  }

  public CaseAnnotation(UMLClass _relatedClass) {
    super(_relatedClass);
  }

  public String toString() {
      if (caseName != null) {
      return caseName.toString() + " [" + relatedClass.toString() + "]";
      }
      return super.toString();
  }

  @Override
  public List<AnnotationQuery> getQuery() {
      List<AnnotationQuery> queries = new LinkedList<>();
      try {
          //case name attribute query
          SelectBuilder builder = SimpleQueryExporter.getStringAttributeQueryBuilder(getCaseName(), getRelatedClass(), null);
          builder.addVar(XESConstants.literalExpr, XESConstants.attTypeVar);
          builder.addVar(XESConstants.nameExpr, XESConstants.attKeyVar);
          String query = builder.toString();
          queries.add(new BinaryAnnotationQuery(query, XESConstants.traceAttributeURI, new String[]{getRelatedClass().getCleanName()}, XESConstants.attArray));
          //attType query
          queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
          //attKey query
          queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
          //attValue query
          queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));
      } catch (Exception e) {
          e.printStackTrace();
      }
      return queries;
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

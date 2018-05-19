/*
 * onprom-annoeditor
 *
 * CaseAnnotation.java
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

import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.CaseForm;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Case annotation class
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(title = "Case", color = "#E5BABA", mnemonic = 'c', tooltip = "Create <u>C</u>ase")
public class CaseAnnotation extends Annotation {
    private static final Logger logger = LoggerFactory.getLogger(CaseAnnotation.class.getName());

    private NavigationalAttribute caseName;

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
      List<AnnotationQuery> queries = Lists.newLinkedList();
      try {
          //case name attribute query
          SelectBuilder builder = SimpleQueryExporter.getStringAttributeQueryBuilder(getCaseName(), this, null, XESConstants.attValueVar);
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
          logger.error(e.getMessage(), e);
          UIUtility.error(e.getMessage());
      }
      return queries;
  }

  @Override
  public CaseForm getForm(AnnotationDiagram panel) {
    return new CaseForm(panel, this);
  }

    public NavigationalAttribute getCaseName() {
    return caseName;
  }

    public void setCaseName(NavigationalAttribute name) {
    this.caseName = name;
  }
}

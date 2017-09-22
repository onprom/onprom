/*
 * onprom-annoeditor
 *
 * AnnotationEditorLabels.java
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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.ui.interfaces.Labels;

/**
 * Labels used in annotation editor
 * <p>
 * @author T. E. Kalayci on 16/02/17.
 */
public enum AnnotationEditorLabels implements Labels {
  NAME("Name", "Please enter or select the name", false),
  VALUE("Value", "Please enter or select the value", false),
  LABEL("Label", "Please enter label of the annotation", false),
    TYPE("Type", "Please enter type", false),
  FILTER("Filter", "Please enter <em>FILTER</em> clause for filtering. You should use %1 for the variable to filter. It must be a correct <em>FILTER</em> clause, " +
    "so generated query will be a valid query. You don't need to write FILTER part, only what will be inside it. Such as: " +
    "<ul>" +
    "<li>regex(%1, \"regex\")</li>" +
    "<li>regex(str(%1),\"regex\",\"i\")</li>" +
    "<li>%1 > 5</li>" +
    "<li>isIRI(%1)</li>" +
    "</ul>" +
    "<a href=\"https://www.w3.org/TR/rdf-sparql-query/#tests\" target=_blank>SPARQL supports the following operators</a> in <em>FILTER</em>:" +
    "<ul>" +
    "<li>XQuery Unary Operators (!, +, -)</li> EXAMPLES: " +
    "<li>SPARQL Accessors (str, lang, datatype) EXAMPLES: ( datatype(?shoeSize) = xsd:integer ), ( lang(?name) = \"ES\" )</li>" +
    "<li>SPARQL Tests (bound, isIRI, isBlank, isLiteral, regex) EXAMPLES: regex(str(?mbox), \"@work.example\"), (isLiteral(?mbox)) </li>" +
    "<li>Data Types (xsd:integer, xsd:decimal, xsd:float, xsd:double, xsd:string, xsd:boolean, xsd:dateTime, etc.) EXAMPLE: ( xsd:dateTime(?attribute) < xsd:dateTime(\"2005-01-01T00:00:00Z\")</li>" +
    "</ul>" +
    "You can visit <a href=\"https://www.w3.org/2001/sw/DataAccess/rq23/examples.html\" target=_blank>this</a> and <a href=\"http://rdf.myexperiment.org/howtosparql?page=FILTER\" target=_blank>this</a> for some examples.", true),
  RESOURCE("Resource", "Please enter or select RESOURCE, group or role", false),
  LIFECYCLE("Lifecycle", "Please select lifecycle of the EVENT", false),
  CASE_PATH("Case Path", "Please select path to reach case", false),
  TIMESTAMP("Timestamp", "Please select timestamp of the EVENT", false),
  EVENT_RESOURCE("Resource", "Please select RESOURCE for the EVENT", false);

  final private String label;
  final private String tooltip;
  final private boolean clickable;

  AnnotationEditorLabels(String _label, String _tooltip, boolean _clickable) {
    this.label = _label;
    this.tooltip = _tooltip;
    this.clickable = _clickable;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getTooltip() {
    return tooltip;
  }

  @Override
  public boolean isClickable() {
    return clickable;
  }
}

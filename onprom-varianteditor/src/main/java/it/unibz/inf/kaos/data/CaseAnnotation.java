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
 * @author T. E. Kalayci on 19-Sep-17.
 */
@AnnotationProperties(label = "Case", color = "#E5BABA", mnemonic = 'c', tooltip = "Create <u>C</u>ase", title = "<u>C</u>ase")
public class CaseAnnotation extends AbstractAnnotation {

    private CaseAnnotation() {
    }

    public CaseAnnotation(UMLClass _relatedClass) {
        super(_relatedClass);
    }

    @Override
    public List<AnnotationQuery> getQuery() {
        List<AnnotationQuery> queries = new LinkedList<>();
        getAttributes().forEach(attribute -> {
            try {
                SelectBuilder builder = SimpleQueryExporter.getStringAttributeQueryBuilder(attribute.getValue(), relatedClass, null);
                builder.addVar("\"" + attribute.getType() + "\"", XESConstants.attTypeVar);
                builder.addVar("\"" + attribute.getName() + "\"", XESConstants.attKeyVar);
                builder.addVar("\"" + relatedClass.getLongName() + "\"", XESConstants.labelVar);
                String query = builder.toString();
                queries.add(new BinaryAnnotationQuery(query, XESConstants.traceAttributeURI, new String[]{relatedClass.getCleanName()}, XESConstants.attArray));
                queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
                queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
                queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return queries;
    }

    @Override
    public CaseForm getForm(AnnotationDiagramPanel panel) {
        return new CaseForm(panel, this);
    }
}

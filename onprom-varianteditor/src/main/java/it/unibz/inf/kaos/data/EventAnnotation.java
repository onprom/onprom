/*
 * onprom-annoeditor
 *
 * EventAnnotation.java
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
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.EventForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;

import java.util.LinkedList;
import java.util.Set;

/**
 * @author T. E. Kalayci on 19-Sep-17.
 */
@AnnotationProperties(label = "Event", color = "#F2C78F", mnemonic = 'e', tooltip = "Create <u>E</u>vent", title = "<u>E</u>vent")
public class EventAnnotation extends AbstractAnnotation {
    private CaseAnnotation caseAnnotation;
    private Set<DiagramShape> casePath;

    private EventAnnotation() {

    }

    public EventAnnotation(CaseAnnotation _caseAnnotation, UMLClass _relatedClass) {
        super(_relatedClass);
        caseAnnotation = _caseAnnotation;
    }

    @Override
    public LinkedList<AnnotationQuery> getQuery() {
        LinkedList<AnnotationQuery> queries = new LinkedList<>();
        try {
            String eventClassName = relatedClass.getCleanName();
            Var eventVar = Var.alloc(eventClassName);
            String eventIRI = "<" + getRelatedClass().getLongName() + ">";
            String caseClassName = getCase().getRelatedClass().getCleanName();
            Var caseVar = Var.alloc(caseClassName);
            boolean inheritanceWithCase = relatedClass.isRelationExist(getCase().getRelatedClass(), Inheritance.class);
            if (inheritanceWithCase) {
                caseVar = eventVar;
            }
            SelectBuilder builder;
            final Var classVar = Var.alloc(relatedClass.getCleanName());
            final String classIRI = "<" + relatedClass.getLongName() + ">";
            final Var nameVar = XESConstants.attValueVar;
            // add class variable
            builder = new SelectBuilder();
            builder.addVar(classVar);
            if (!inheritanceWithCase && casePath != null) {
                SimpleQueryExporter.addJoin(builder, casePath);
            } else {
                builder.addWhere(classVar, "a", classIRI);
            }
            builder.addVar("\"" + eventClassName + "\"", nameVar);
            builder.addVar("\"" + classVar + "\"", XESConstants.labelVar);

            //t-contains-e query
            SelectBuilder caseBuilder = builder.clone();
            caseBuilder.addVar(caseVar);
            final String[] eventAnswerVariable = {XESConstants.label, eventClassName};
            queries.add(new BinaryAnnotationQuery(caseBuilder.toString(), XESConstants.traceEventURI, new String[]{caseClassName}, eventAnswerVariable));

            if (getAttributeCount() > 0) {
                for (AnnotationAttribute attribute : getAttributes()) {
                    builder = SimpleQueryExporter.getStringAttributeQueryBuilder(attribute.getValue(), getRelatedClass(), getCasePath());
                    builder.addVar("\"" + attribute.getType() + "\"", XESConstants.attTypeVar);
                    builder.addVar("\"" + attribute.getName() + "\"", XESConstants.attKeyVar);
                    String query = builder.toString();
                    queries.add(new BinaryAnnotationQuery(query, XESConstants.eventAttributeURI,
                            eventAnswerVariable, XESConstants.attArray)
                    );
                    queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
                    queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
                    queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queries;
    }

    @Override
    public EventForm getForm(AnnotationDiagramPanel panel) {
        return new EventForm(panel, this);
    }

    public CaseAnnotation getCase() {
        return caseAnnotation;
    }

    public Set<DiagramShape> getCasePath() {
        return casePath;
    }

    public void setCasePath(Set<DiagramShape> tracePath) {
        this.casePath = tracePath;
    }

}
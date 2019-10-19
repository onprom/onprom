/*
 * onprom-annoeditor
 *
 * EventAnnotation.java
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
package it.unibz.inf.kaos.data;

import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.EventForm;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Event annotation class
 * <p>
 *
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(title = "Event", color = "#F2C78F", mnemonic = 'e', tooltip = "Create <u>E</u>vent")
public class EventAnnotation extends Annotation {
    private static final Logger logger = LoggerFactory.getLogger(EventAnnotation.class.getName());
    private StringAttribute name;
    private NavigationalAttribute timestamp;
    private TransactionalLifecycle lifecycle;
    private CaseAnnotation caseAnnotation;
    private Set<DiagramShape> casePath;

    private EventAnnotation() {

    }

    public EventAnnotation(String _label, CaseAnnotation _caseAnnotation, UMLClass _relatedClass) {
        this(_caseAnnotation, _relatedClass);
        setLabel(_label);
    }

    public EventAnnotation(CaseAnnotation _caseAnnotation, UMLClass _relatedClass) {
        super(_relatedClass);
        caseAnnotation = _caseAnnotation;
    }

    public String toString() {
        return String.format("%s %s %s", name.toString(), caseAnnotation == null ? "" : caseAnnotation.toString(), timestamp.toString());
    }

    @Override
    public List<AnnotationQuery> getQuery() {
        List<AnnotationQuery> queries = Lists.newLinkedList();
        try {
            //concept name attribute
            String eventClassName = relatedClass.getCleanName();
            Var eventVar = Var.alloc(eventClassName);
            String eventIRI = "<" + getRelatedClass().getLongName() + ">";
            Var caseVar = Var.alloc(getCase().getRelatedClass().getCleanName());
            boolean inheritanceWithCase = getCase().getRelatedClass().equalsOrInherits(relatedClass);
            if (inheritanceWithCase) {
                caseVar = eventVar;
            }
            SelectBuilder builder;
            if (!inheritanceWithCase) {
                builder = SimpleQueryExporter.getStringAttributeQueryBuilder(getEventName(), this, getCasePath(), XESConstants.attValueVar);
            } else {
                builder = SimpleQueryExporter.getStringAttributeQueryBuilder(getEventName(), this, null, XESConstants.attValueVar);
            }
            builder.addVar("\"" + getLabel() + "\"", XESConstants.labelVar);

            //t-contains-e query
            SelectBuilder caseBuilder = builder.clone();
            caseBuilder.addVar(caseVar);
            final String[] eventAnswerVariable = {XESConstants.label, eventClassName};
            queries.add(new BinaryAnnotationQuery(caseBuilder.toString(), XESConstants.traceEventURI, new String[]{caseVar.getVarName()}, eventAnswerVariable));

            //other event queries
            builder.addVar(XESConstants.literalExpr, XESConstants.attTypeVar);
            builder.addVar(XESConstants.nameExpr, XESConstants.attKeyVar);
            String query = builder.toString();
            queries.add(new BinaryAnnotationQuery(query, XESConstants.eventAttributeURI,
                    eventAnswerVariable, XESConstants.attArray)
            );
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));

            //timestamp attribute
            builder = new SelectBuilder();
            builder.addVar(eventVar).addVar(XESConstants.attValue);
            //add timestamp path
            SimpleQueryExporter.addJoin(builder, getTimestamp().getPath());
            //add timestamp property
            if (getTimestamp().getUmlClass().equalsOrInherits(relatedClass)) {
                builder.addWhere("?" + eventClassName, "<" + getTimestamp().getAttribute().getLongName() + ">", XESConstants.attValueVar);
            } else {
                builder.addWhere("?" + getTimestamp().getUmlClass().getCleanName(), "<" + getTimestamp().getAttribute().getLongName() + ">", XESConstants.attValueVar);
            }
            //add case path
            if (!inheritanceWithCase) {
                SimpleQueryExporter.addJoin(builder, getCasePath());
            } else {
                builder.addWhere(eventVar, "a", eventIRI);
            }
            builder.addVar("\"" + getLabel() + "\"", XESConstants.labelVar);
            builder.addVar(XESConstants.timestampTypeExpr, XESConstants.attType);
            builder.addVar(XESConstants.timestampExpr, XESConstants.attKey);
            query = builder.toString();
            queries.add(new BinaryAnnotationQuery(query, XESConstants.eventAttributeURI,
                    eventAnswerVariable, XESConstants.attArray)
            );
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));

            //lifecycle attribute
            builder = new SelectBuilder();
            builder.addVar(eventVar).addVar("\"" + getLifecycle().toString() + "\"", XESConstants.attValueVar);
            //add case path
            if (!inheritanceWithCase) {
                SimpleQueryExporter.addJoin(builder, getCasePath());
            } else {
                builder.addWhere(eventVar, "a", eventIRI);
            }
            builder.addVar("\"" + getLabel() + "\"", XESConstants.label);
            builder.addVar(XESConstants.literalExpr, XESConstants.attType);
            builder.addVar(XESConstants.lifecycleExpr, XESConstants.attKey);
            query = builder.toString();
            queries.add(new BinaryAnnotationQuery(query, XESConstants.eventAttributeURI,
                    eventAnswerVariable, XESConstants.attArray)
            );
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, XESConstants.attArray, XESConstants.attTypeArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, XESConstants.attArray, XESConstants.attKeyArr));
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, XESConstants.attArray, XESConstants.attValueArr));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            UIUtility.error(e.getMessage());
        }
        return queries;
    }

    @Override
    public java.util.Optional<EventForm> getForm(AnnotationDiagram panel) {
        return java.util.Optional.of(new EventForm(panel, this));
    }

    public StringAttribute getEventName() {
        return name;
    }

    public void setEventName(StringAttribute name) {
        this.name = name;
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

    public NavigationalAttribute getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(NavigationalAttribute timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionalLifecycle getLifecycle() {
        if (lifecycle == null || lifecycle.toString().isEmpty())
            return TransactionalLifecycle.COMPLETE;
        return lifecycle;
    }

    public void setLifecycle(TransactionalLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

}
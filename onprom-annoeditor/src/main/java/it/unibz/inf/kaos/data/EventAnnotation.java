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

import it.unibz.inf.kaos.data.query.old.V2.EventAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventLifecycleAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventResourceAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventTimestampAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventTraceAnnotationQueryV2;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.EventForm;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Event annotation class
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(type = "Event", color = "#F2C78F", action = AnnotationActionType.EVENT)
public class EventAnnotation extends AbstractAnnotation {
  private static final Logger logger = LoggerFactory.getLogger(EventAnnotation.class.getName());
  private StringAttribute name;
  private NavigationalAttribute timestamp;
  private NavigationalAttribute resource;
  private TransactionalLifecycle lifecycle;
  private CaseAnnotation caseAnnotation;
  private Set<DiagramShape> casePath;

  private EventAnnotation() {

  }

  public EventAnnotation(CaseAnnotation _caseAnnotation, UMLClass _relatedClass) {
    super(_relatedClass);
    caseAnnotation = _caseAnnotation;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder(name.toString());
    if (caseAnnotation != null)
      builder.append(caseAnnotation.toString());
    if (resource != null)
      builder.append(resource.toString());
    if (timestamp != null)
      builder.append(timestamp.toString());
    return builder.toString();
  }

  @Override
  public EventAnnotationQueryV2 getQuery() {
    //name
    Var eventVar = Var.alloc(getRelatedClass().getCleanName());
    String eventIRI = "<" + getRelatedClass().getLongName() + ">";
    Var caseVar = Var.alloc(getCase().getRelatedClass().getCleanName());
    boolean inheritanceWithCase = getRelatedClass().isRelationExist(getCase().getRelatedClass(), Inheritance.class);
    if (inheritanceWithCase) {
      caseVar = eventVar;
    }
    EventAnnotationQueryV2 query;
    if (!inheritanceWithCase) {
      query = new EventAnnotationQueryV2(SimpleQueryExporter.getStringAttributeQuery(getEventName(), getRelatedClass(), getCasePath()), eventVar.getVarName(), "n");
    } else {
      query = new EventAnnotationQueryV2(SimpleQueryExporter.getStringAttributeQuery(getEventName(), getRelatedClass(), null), eventVar.getVarName(), "n");
    }
    //EVENT with case
    SelectBuilder builder = new SelectBuilder();
    //builder.setDistinct(true);
    builder.addVar(eventVar).addVar(caseVar);
    //add case path
    if (!inheritanceWithCase) {
      SimpleQueryExporter.addJoin(builder, getCasePath());
    } else {
      builder.addWhere(eventVar, "a", eventIRI);
    }
    query.setEventTrace(new EventTraceAnnotationQueryV2(builder.toString(), eventVar.getVarName(), caseVar.getVarName()));
    //timestamp
    try {
      builder = new SelectBuilder();
      //builder.setDistinct(true);
      //add variables
      final Var timestampVar = Var.alloc("t");
      builder.addVar(eventVar).addVar(timestampVar);
      //add timestamp path
      SimpleQueryExporter.addJoin(builder, getTimestamp().getPath());
      //add timestamp clause
      if (getTimestamp().getUmlClass().isRelationExist(getRelatedClass(), Inheritance.class)) {
        builder.addWhere("?" + getRelatedClass().getCleanName(), "<" + getTimestamp().getAttribute().getLongName() + ">", timestampVar);
      } else {
        builder.addWhere("?" + getTimestamp().getUmlClass().getCleanName(), "<" + getTimestamp().getAttribute().getLongName() + ">", timestampVar);
      }
      //add case path
      if (!inheritanceWithCase) {
        SimpleQueryExporter.addJoin(builder, getCasePath());
      } else {
        builder.addWhere(eventVar, "a", eventIRI);
      }
      query.setEventTimestamp(new EventTimestampAnnotationQueryV2(builder.toString(), eventVar.getVarName(), timestampVar.getVarName()));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
    }

    //lifecycle
    try {
      builder = new SelectBuilder();
      //builder.setDistinct(true);
      final Var lifecycleVar = Var.alloc("l");
      //add variables
      builder.addVar(eventVar).addVar("\"" + getLifecycle().toString() + "\"", lifecycleVar);
      //add case path
      if (!inheritanceWithCase) {
        SimpleQueryExporter.addJoin(builder, getCasePath());
      } else {
        builder.addWhere(eventVar, "a", eventIRI);
      }
      query.setEventLifecycle(new EventLifecycleAnnotationQueryV2(builder.toString(), eventVar.getVarName(), lifecycleVar.getVarName()));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
    }
    //RESOURCE query
    try {
      if (getResource() != null) {
        Var resourceVar = Var.alloc(getResource().getUmlClass().getCleanName());
        builder = new SelectBuilder();
        //builder.setDistinct(true);
        //add variables
        builder.addVar(eventVar).addVar(resourceVar);
        //add RESOURCE
        SimpleQueryExporter.addJoin(builder, getResource().getPath());
        //add case path
        if (!inheritanceWithCase) {
          SimpleQueryExporter.addJoin(builder, getCasePath());
        } else {
          builder.addWhere(eventVar, "a", eventIRI);
        }
        query.setEventResource(new EventResourceAnnotationQueryV2(builder.toString(), eventVar.getVarName(), resourceVar.getVarName()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
    }
    query.setAttributeQueries(SimpleQueryExporter.getAttributeQueries(getAttributes()));
    return query;
  }

  @Override
  public EventForm getForm(AnnotationDiagramPanel panel) {
    return new EventForm(panel, this);
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

  public void setCase(CaseAnnotation trace) {
    this.caseAnnotation = trace;
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

  public NavigationalAttribute getResource() {
    return resource;
  }

  public void setResource(NavigationalAttribute resource) {
    this.resource = resource;
  }

  public TransactionalLifecycle getLifecycle() {
    if (lifecycle == null || lifecycle.toString().isEmpty())
      return TransactionalLifecycle.COMPLETE;
    return lifecycle;
  }

  public void setLifecycle(TransactionalLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  public void removeResource(ResourceAnnotation annotation) {
    if (resource.getAnnotation().equals(annotation)) {
      resource = null;
    }
  }
}
/*
 * onprom-annoeditor
 *
 * SimpleQueryExporter.java
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

package it.unibz.inf.kaos.io;

import it.unibz.inf.kaos.data.AnnotationAttribute;
import it.unibz.inf.kaos.data.Association;
import it.unibz.inf.kaos.data.Inheritance;
import it.unibz.inf.kaos.data.Relationship;
import it.unibz.inf.kaos.data.StringAttribute;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueryV2;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationExporter;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.owl.OWLExporter;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Building annotation queries using Apache Jena query builder
 * <p>
 * In this class simple (a query for each annotation and attribute) queries are generated from annotations
 *
 * @see <a href="https://jena.apache.org/documentation/extras/querybuilder/" target="_blank>Jena Query Builder</a>
 * <p>
 * @author T. E. Kalayci on 06/12/16.
 */
public class SimpleQueryExporter implements AnnotationExporter {
  private static final Logger logger = LoggerFactory.getLogger(SimpleQueryExporter.class.getName());

  // TODO what about having spaces or some other characters that is not supported in query?

  public static LinkedList<AnnotationQueryV2> getAttributeQueries(LinkedList<AnnotationAttribute> attributes) {
    if (attributes != null && attributes.size() > 0) {
      LinkedList<AnnotationQueryV2> queries = new LinkedList<>();
      attributes.forEach(attribute -> queries.add(getAttributeQuery(attribute)));
      return queries;
    }
    return null;
  }

  public static void addJoin(SelectBuilder builder, Set<DiagramShape> path) {
    if (path == null || path.stream().filter(Relationship.class::isInstance).count() < 1) {
      return;
    }
    String isaSubject = null;
    String isaURI = null;
    Iterator<DiagramShape> shapeIterator = path.iterator();
    while (shapeIterator.hasNext()) {
      DiagramShape shape = shapeIterator.next();
      if (shape instanceof Inheritance) {
        isaSubject = ((Inheritance) shape).getSubclass().getCleanName();
        isaURI = ((Inheritance) shape).getSubclass().getLongName();
      } else if (shape instanceof Association) {
        final Association association = (Association) shape;
        if (association.hasAssociation()) {
          String subject = association.getCleanName();
          if (isaSubject != null) {
            subject = isaSubject;
            builder.addWhere("?" + isaSubject, "a", "<" + isaURI + ">");
            isaSubject = null;
          }
          DiagramShape nextNode = shapeIterator.next();
          builder.addWhere("?" + subject, "<" + association.getLongName() + OWLExporter.REIFICATION_SEPARATOR + nextNode.getCleanName() + ">", "?" + nextNode.getCleanName());
        } else {
          String subject = association.getFirstClass().getCleanName();
          if (isaSubject != null) {
            subject = isaSubject;
            isaSubject = null;
          }
          builder.addWhere("?" + subject, "<" + association.getLongName() + ">", "?" + association.getSecondClass().getCleanName());
        }
      } else if (shape instanceof UMLClass) {
        if (isaSubject != null) {
          builder.addWhere("?" + isaSubject, "a", "<" + shape.getLongName() + ">");
          isaSubject = null;
        }

      }
    }
  }

  public static String getStringAttributeQuery(StringAttribute name, UMLClass relatedClass, Set<DiagramShape> casePath) {
    try {
      //create select builder
      SelectBuilder builder = new SelectBuilder();
      //builder.setDistinct(true);
      final Var classVar = Var.alloc(relatedClass.getCleanName());
      final String classIRI = "<" + relatedClass.getLongName() + ">";
      final Var nameVar = Var.alloc("n");
      // add class variable
      builder.addVar(classVar);
      if (casePath != null) {
        addJoin(builder, casePath);
      } else {
        builder.addWhere(classVar, "a", classIRI);
      }
      if (name.getAttribute() == null) {
        //add name variable
        builder.addVar("\"" + name.getValue() + "\"", nameVar);
        return builder.toString();
      } else {
        //add name variable
        builder.addVar(nameVar);
        // add path to the name
        addJoin(builder, name.getPath());
        //add attribute
        builder.addWhere("?" + name.getUmlClass().getCleanName(), "<" + name.getAttribute().getLongName() + ">", nameVar);
        //we only add filter if it is a dynamic value
        if (name.getFilterClause() != null && !name.getFilterClause().isEmpty()) {
          //add filter clause to the query
          builder.addFilter(name.getFilterClause().replaceAll("%1", "?n"));
        }
        return builder.toString();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
      return "ERROR: QUERY IS NOT GENERATED (" + e.getMessage() + ")";
    }
  }

  private static AnnotationQueryV2 getAttributeQuery(AnnotationAttribute attribute) {
    //TODO IS-A relationships?
    try {
      SelectBuilder builder = new SelectBuilder();
      //builder.setDistinct(true);
      //definitions
      final StringAttribute value = attribute.getValue();
      final UMLClass valueClass = value.getUmlClass();
      final Var classVar = Var.alloc(valueClass.getCleanName());
      //answer variables
      final Var nameVar = Var.alloc("n");
      final Var valueVar = Var.alloc("v");
      builder.addVar("\"" + attribute.getName() + "\"", nameVar);
      builder.addVar(valueVar);
      //where clauses
      builder.addWhere(classVar, "a", "<" + valueClass.getLongName() + ">");
      // add path of the value
      addJoin(builder, value.getPath());
      //add attribute
      builder.addWhere(classVar, "<" + value.getAttribute().getLongName() + ">", valueVar);
      //we only add filter if it is a dynamic value
      if (value.getFilterClause() != null && !value.getFilterClause().isEmpty()) {
        //add filter clause to the query
        builder.addFilter(value.getFilterClause().replaceAll("%1", "?v"));
      }
      return new AnnotationQueryV2(builder.toString(), "n", "v");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
      return new AnnotationQueryV2("ERROR: QUERY IS NOT GENERATED (" + e.getMessage() + ")", "n", "v");
    }
  }

  /**
   * Alternatives to Apache Jena query builder:
   * RDF4J:           https://github.com/eclipse/rdf4j/blob/master/core/queryrender/src/main/java/org/eclipse/rdf4j/queryrender/builder/QueryBuilder.java
   * sparql-java:    https://bitbucket.org/rehei/sparql-java
   * spanqit:        https://github.com/anqit/spanqit
   * other:           https://stackoverflow.com/questions/7250189/how-to-build-sparql-queries-in-java
   * templating, SPARQL Algebra, etc.:     http://blog.mynarz.net/2016/06/on-generating-sparql.html
   * <p>
   * RDF4J currently doesn't support additional projection settings (such as adding AS)!
   * <p>
   * Example code:
   * <p>
   * final QueryBuilder<ParsedTupleQuery> select = QueryBuilderFactory.select();
   * select.addProjectionVar(relatedClass.getName()).addProjectionVar("\""+name.getValue()+"\"","as","?n");
   * select.group().atom(relatedClass.getName(), "a",relatedClass.getLongName()).closeGroup();
   * return new SPARQLQueryRenderer().render(select.query());
   */

  @Override
  public AnnotationQueriesV2 getQueries(Set<Annotation> annotations) {
    AnnotationQueriesV2 queries = new AnnotationQueriesV2();
    for (Annotation annotation : annotations) {
      queries.addQuery(annotation.getQuery());
    }
    return queries;
  }
}
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.owl.OWLExporter;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
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
public class SimpleQueryExporter {
  private static final Logger logger = LoggerFactory.getLogger(SimpleQueryExporter.class.getName());

    /*
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

  // TODO what about having spaces or some other characters that is not supported in query?

    public static String checkQuery(String queryStr) {
        try {
            // attempt to create the query from the string using Apache Jena
            // query factory and return the result if successful
            return QueryFactory.create(queryStr).toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            UIUtility.error(e.getMessage());
        }
        // in case of parsing error, return the original string
        return queryStr;
    }

    public static List<AnnotationQuery> getAttributeQueries(List<AnnotationAttribute> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            List<AnnotationQuery> queries = Lists.newLinkedList();
      attributes.forEach(attribute -> queries.add(getAttributeQuery(attribute)));
      return queries;
    }
    return null;
  }

  public static String addJoin(SelectBuilder builder, Set<DiagramShape> path){
        return addJoin(builder, path, null);
  }

  private static String getVarName(ImmutableMap<String, String> varNames, String varName){
        if(varNames!=null) {
                return varNames.getOrDefault(varName, varName);
        }
        return varName;
  }

    public static String addJoin(SelectBuilder builder, Set<DiagramShape> path, ImmutableMap<String, String> varNames) {
    if (path == null || path.stream().filter(Relationship.class::isInstance).count() < 1) {
        return null;
    }
    String isaSubject = null;
    String isaURI = null;
        String last = null;
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
              builder.addWhere("?" + getVarName(varNames, subject), "a", "<" + isaURI + ">");
            isaSubject = null;
          }
          DiagramShape nextNode = shapeIterator.next();
          builder.addWhere("?" + getVarName(varNames, subject), "<" + association.getLongName() + OWLExporter.REIFICATION_SEPARATOR + nextNode.getCleanName() + ">", "?" + getVarName(varNames, nextNode.getCleanName()));
            last = getVarName(varNames, nextNode.getCleanName());
        } else {
          String subject = association.getFirstClass().getCleanName();
          if (isaSubject != null) {
            subject = isaSubject;
            isaSubject = null;
          }
          builder.addWhere("?" + getVarName(varNames, subject), "<" + association.getLongName() + ">", "?" + getVarName(varNames, association.getSecondClass().getCleanName()));
            last = getVarName(varNames, association.getSecondClass().getCleanName());
        }
      } else if (shape instanceof UMLClass) {
        if (isaSubject != null) {
          builder.addWhere("?" + getVarName(varNames, isaSubject), "a", "<" + shape.getLongName() + ">");
          isaSubject = null;
        }
      }
    }
        return last;
  }

    public static SelectBuilder getStringAttributeQueryBuilder(NavigationalAttribute navigationalAttribute, UMLClass relatedClass, Set<DiagramShape> casePath) {
        return getStringAttributeQueryBuilder(navigationalAttribute, relatedClass, casePath, XESConstants.attValueVar);
    }

    public static SelectBuilder getStringAttributeQueryBuilder(NavigationalAttribute navigationalAttribute, Annotation relatedAnnotation, Set<DiagramShape> casePath) {
        return getStringAttributeQueryBuilder(navigationalAttribute, relatedAnnotation, casePath, XESConstants.attValueVar);
    }

    public static SelectBuilder getStringAttributeQueryBuilder(NavigationalAttribute navigationalAttribute, Annotation relatedAnnotation, Set<DiagramShape> casePath, Var nameVar) {
        final Var classVar = Var.alloc(relatedAnnotation.getVarName());
        final String classIRI = "<" + relatedAnnotation.getLongName() + ">";
        // add class variable
        SelectBuilder builder = new SelectBuilder();
        builder.addVar(classVar);
        if (casePath != null) {
            addJoin(builder, casePath);
        }
        if (navigationalAttribute instanceof StringAttribute && navigationalAttribute.getAttribute() == null) {
            try {
                if(casePath==null) {
                    builder.addWhere(classVar, "a", classIRI);
                }
                builder.addVar("\"" + ((StringAttribute) navigationalAttribute).getValue() + "\"", nameVar);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            builder.addVar(nameVar);
            addJoin(builder, navigationalAttribute.getPath());
            if (navigationalAttribute.getUmlClass().equalsOrInherits(relatedAnnotation.getRelatedClass())) {
                builder.addWhere("?" + relatedAnnotation.getVarName(), "<" + navigationalAttribute.getAttribute().getLongName() + ">", nameVar);
            } else {
                builder.addWhere("?" + navigationalAttribute.getUmlClass().getCleanName(), "<" + navigationalAttribute.getAttribute().getLongName() + ">", nameVar);
            }

            //we only add filter if it is a dynamic value
            if (navigationalAttribute.getFilterClause() != null && !navigationalAttribute.getFilterClause().isEmpty()) {
                try {
                    builder.addFilter(navigationalAttribute.getFilterClause().replaceAll("%1", "?n"));
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return builder;
    }

    public static SelectBuilder getStringAttributeQueryBuilder(NavigationalAttribute navigationalAttribute, UMLClass relatedClass, Set<DiagramShape> casePath, Var nameVar) {
        final Var classVar = Var.alloc(relatedClass.getCleanName());
        final String classIRI = "<" + relatedClass.getLongName() + ">";
        // add class variable
        SelectBuilder builder = new SelectBuilder();
        builder.addVar(classVar);
        if (casePath != null) {
            addJoin(builder, casePath);
        }
        if (navigationalAttribute instanceof StringAttribute && navigationalAttribute.getAttribute() == null) {
            try {
                if(casePath==null) {
                    builder.addWhere(classVar, "a", classIRI);
                }
                builder.addVar("\"" + ((StringAttribute) navigationalAttribute).getValue() + "\"", nameVar);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            builder.addVar(nameVar);
            addJoin(builder, navigationalAttribute.getPath());
            if (navigationalAttribute.getUmlClass().equalsOrInherits(relatedClass)) {
                builder.addWhere("?" + relatedClass.getCleanName(), "<" + navigationalAttribute.getAttribute().getLongName() + ">", nameVar);
            } else {
                builder.addWhere("?" + navigationalAttribute.getUmlClass().getCleanName(), "<" + navigationalAttribute.getAttribute().getLongName() + ">", nameVar);
            }

            //we only add filter if it is a dynamic value
            if (navigationalAttribute.getFilterClause() != null && !navigationalAttribute.getFilterClause().isEmpty()) {
                try {
                    builder.addFilter(navigationalAttribute.getFilterClause().replaceAll("%1", "?n"));
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return builder;
    }

    public static String getStringAttributeQuery(StringAttribute name, UMLClass relatedClass, Set<DiagramShape> casePath) {
        try {
            return getStringAttributeQueryBuilder(name, relatedClass, casePath).toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            InformationDialog.display(e.toString());
            return "ERROR: QUERY IS NOT GENERATED (" + e.getMessage() + ")";
        }
    }

    private static AnnotationQuery getAttributeQuery(AnnotationAttribute attribute) {
    //TODO IS-A relationships?
    try {
      SelectBuilder builder = new SelectBuilder();
      //builder.setDistinct(true);
      //definitions
      final StringAttribute value = attribute.getValue();
      final UMLClass valueClass = value.getUmlClass();
      final Var classVar = Var.alloc(valueClass.getCleanName());
      //answer variables
        builder.addVar("\"" + attribute.getName() + "\"", XESConstants.labelVar);
        builder.addVar(XESConstants.attValueVar);
      //where clauses
      builder.addWhere(classVar, "a", "<" + valueClass.getLongName() + ">");
      // add path of the value
      addJoin(builder, value.getPath());
      //add attribute
        builder.addWhere(classVar, "<" + value.getAttribute().getLongName() + ">", XESConstants.attValueVar);
      //we only add filter if it is a dynamic value
      if (value.getFilterClause() != null && !value.getFilterClause().isEmpty()) {
        //add filter clause to the query
          builder.addFilter(value.getFilterClause().replaceAll("%1", XESConstants.attValue));
      }
        //TODO unary annotation query
        //TODO adding target URI for attribute
        return new BinaryAnnotationQuery(builder.toString(), null, new String[]{XESConstants.label}, new String[]{XESConstants.attValue});
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      InformationDialog.display(e.toString());
        return new BinaryAnnotationQuery("ERROR: QUERY IS NOT GENERATED (" + e.getMessage() + ")", null, new String[]{XESConstants.label}, new String[]{XESConstants.attValue});
    }
  }
}
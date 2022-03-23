/*
 * annotationeditor
 *
 * SimpleQueryExporter.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom.io;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.onprom.data.*;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.owl.OWLExporter;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;

/**
 * Building annotation queries using Apache Jena query builder
 * <p>
 * In this class simple (a query for each annotation and attribute) queries are generated from annotations
 *
 * @author T. E. Kalayci on 06/12/16.
 * @see <a href="https://jena.apache.org/documentation/extras/querybuilder/" target="_blank>Jena Query Builder</a>
 * <p>
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
            return QueryFactory.create(queryStr).toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            UIUtility.error(e.getMessage());
        }
        return queryStr;
    }

    public static String addJoin(SelectBuilder builder, Set<DiagramShape> path) {
        return addJoin(builder, path, ImmutableMap.of());
    }

    public static String addJoin(SelectBuilder builder, Set<DiagramShape> path, @Nonnull ImmutableMap<String, String> varNames) {
        if (path == null || path.stream().filter(Relationship.class::isInstance).count() < 1) {
            return null;
        }
        String isaSubject = null;
        String subject = null;
        String isaURI = null;
        String last = null;
        Iterator<DiagramShape> shapeIterator = path.iterator();
        while (shapeIterator.hasNext()) {
            DiagramShape shape = shapeIterator.next();
            if (shape instanceof Inheritance) {
                subject = ((Inheritance) shape).getSuperclass().getCleanName();
                isaSubject = ((Inheritance) shape).getSubclass().getCleanName();
                isaURI = ((Inheritance) shape).getSubclass().getLongName();
            } else if (shape instanceof Association) {
                final Association association = (Association) shape;
                if (association.hasAssociation()) {
                    subject = association.getCleanName();
                    if (isaSubject != null) {
                        subject = isaSubject;
                        builder.addWhere("?" + varNames.getOrDefault(subject, subject), "a", "<" + isaURI + ">");
                        isaSubject = null;
                    }
                    DiagramShape nextNode = shapeIterator.next();
                    String varName1 = nextNode.getCleanName();
                    builder.addWhere("?" + varNames.getOrDefault(subject, subject), "<" + association.getLongName() + OWLExporter.REIFICATION_SEPARATOR + nextNode.getCleanName() + ">", "?" + varNames.getOrDefault(varName1, varName1));
                    String varName = nextNode.getCleanName();
                    last = varNames.getOrDefault(varName, varName);
                } else {
                    subject = association.getFirstClass().getCleanName();
                    if (isaSubject != null) {
                        subject = isaSubject;
                        isaSubject = null;
                    }
                    String varName = association.getSecondClass().getCleanName();
                    last = varNames.getOrDefault(varName, varName);
                    builder.addWhere("?" + varNames.getOrDefault(subject, subject), "<" + association.getLongName() + ">", "?" + last);
                }
            } else if (shape instanceof AssociationClass) {
                if (isaSubject != null && !shape.getCleanName().equalsIgnoreCase(subject)) {
                    String varName = shape.getCleanName();
                    builder.addWhere("?" + varNames.getOrDefault(varName, varName), "<" + shape.getLongName() + OWLExporter.REIFICATION_SEPARATOR + subject + ">", "?" + varNames.getOrDefault(isaSubject, isaSubject));
                    isaSubject = null;
                }
            } else if (shape instanceof UMLClass) {
                if (isaSubject != null) {
                    builder.addWhere("?" + varNames.getOrDefault(isaSubject, isaSubject), "a", "<" + shape.getLongName() + ">");
                    isaSubject = null;
                }
            }
        }
        return last;
    }

    public static SelectBuilder getStringAttributeQueryBuilder(NavigationalAttribute navigationalAttribute, Annotation relatedAnnotation, Set<DiagramShape> casePath, Var nameVar) {
        final Var classVar = Var.alloc(relatedAnnotation.getVarName());
        final String classIRI = "<" + relatedAnnotation.getLongName() + ">";
        SelectBuilder builder = new SelectBuilder();
        builder.addVar(classVar);
        if (casePath != null) {
            addJoin(builder, casePath);
        }
        if (navigationalAttribute instanceof ClassAttribute) {
            addClassAttribute(builder, navigationalAttribute, relatedAnnotation.getRelatedClass(), nameVar, classVar, classIRI);
        } else if (navigationalAttribute instanceof StringAttribute && navigationalAttribute.getAttribute() == null) {
            addStringAttribute(builder, (StringAttribute) navigationalAttribute, casePath, nameVar, classVar, classIRI);
        } else {
            addNavigationalAttribute(builder, navigationalAttribute, relatedAnnotation, nameVar);
        }
        return builder;
    }

    private static void addNavigationalAttribute(SelectBuilder builder, NavigationalAttribute navigationalAttribute, Annotation relatedAnnotation, Var nameVar) {
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
                builder.addFilter(navigationalAttribute.getFilterClause().replaceAll("%1", "?" + nameVar.getVarName()));
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static void addStringAttribute(SelectBuilder builder, StringAttribute navigationalAttribute, Set<DiagramShape> casePath, Var nameVar, Var classVar, String classIRI) {
        try {
            if (casePath == null) {
                builder.addWhere(classVar, "a", classIRI);
            }
            builder.addVar("\"" + navigationalAttribute.getValue() + "\"", nameVar);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void addClassAttribute(SelectBuilder builder, NavigationalAttribute navigationalAttribute, UMLClass relatedClass, Var nameVar, Var classVar, String classIRI) {
        try {
            if (navigationalAttribute.getUmlClass().equalsOrInherits(relatedClass)) {
                builder.addWhere(classVar, "a", classIRI);
                builder.addBind("?" + classVar.getVarName(), nameVar);
            } else {
                builder.addWhere("?" + navigationalAttribute.getUmlClass().getCleanName(), "a", "<" + navigationalAttribute.getUmlClass().getLongName() + ">");
                builder.addBind("?" + navigationalAttribute.getUmlClass().getCleanName(), nameVar);
            }
            builder.addVar(nameVar);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
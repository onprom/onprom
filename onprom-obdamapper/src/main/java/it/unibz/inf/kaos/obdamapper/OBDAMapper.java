/*
 * onprom-obdamapper
 *
 * OBDAMapper.java
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

package it.unibz.inf.kaos.obdamapper;

import ch.qos.logback.classic.Logger;
import com.github.jsonldjava.shaded.com.google.common.collect.Streams;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.kaos.data.query.*;
import it.unibz.inf.kaos.obdamapper.utility.OBDAMappingUtility;
import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.lang.reflect.Array;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OBDAMapper {
    protected static final Logger logger = (Logger) LoggerFactory.getLogger(OBDAMapper.class);
    private static final String objPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s <http://onprom.inf.unibz.it/%s> . "; //<[Object]> [ObjectProperty] <[Object]>
    private static final String dataPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s %s . "; //<[Object> [DataProperty] <[Value]>
    private static final String conceptTripleTemplate = " <http://onprom.inf.unibz.it/%s> a %s . "; //<[Object]> rdf:type <[Class]>

    private final OWLOntology targetOntology;
    private final OBDAModel obdaModel;
    private OntopOWLStatement statement;
    private final TargetQueryParser textParser;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    public OBDAMapper(
            OWLOntology sourceOntology, OWLOntology targetOntology, OBDAModel sourceObdaModel, Properties dataSourceProperties, AnnotationQueries annotationQueries) {
        this.targetOntology = targetOntology;
        OntopSQLOWLAPIConfiguration config = OntopUtility.getConfiguration(sourceOntology, sourceObdaModel, dataSourceProperties);
        this.obdaModel = OntopUtility.emptyOBDAModel(config);
        this.textParser = obdaModel.createTargetQueryParser();
        this.sourceQueryFactory = obdaModel.getSourceQueryFactory();

        try {
            OntopOWLReasoner reasoner = OntopOWLFactory.defaultFactory().createReasoner(config);
            this.statement = reasoner.getConnection().createStatement();
            this.startMapping(annotationQueries);
            reasoner.close();
            reasoner.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OBDAModel getOBDAModel() {
        return obdaModel;
    }

    private void startMapping(AnnotationQueries annotationQueries) {
        AnnotationQueriesProcessor mappingAdder = new AnnotationQueriesProcessor();
        for (AnnotationQuery aq : annotationQueries.getAllQueries()) {
            if (aq != null) aq.accept(mappingAdder);
        }
    }

    private void addMapping(String source, String target) {
        try {
            String newId = "ONPROM_MAPPING_" + obdaModel.getMapping(obdaModel.getDatasource().getSourceID()).size();
            logger.info("######################\nID:" + newId + "\nTARGET:" + target + "\nSOURCE:" + source + "\n######################");

            obdaModel.addTriplesMap(new OntopNativeSQLPPTriplesMap(newId,
                    sourceQueryFactory.createSourceQuery(source),
                    textParser.parse(target)), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OntopReformulationResult reformulate(String source) {
        IQ executableQuery;
        try {
            executableQuery = this.statement.getExecutableQuery(source);
        } catch (OWLException e) {
            throw new IllegalArgumentException(e);
        }
        String sqlQuery = ((NativeNode) executableQuery.getTree().getChildren().get(0)).getNativeQueryString();
        logger.info("######################\nBODY:" + sqlQuery + "\n######################");

        ConstructionNode constructionNode = (ConstructionNode) executableQuery.getTree().getRootNode();
        ImmutableSubstitution<ImmutableTerm> substitution = constructionNode.getSubstitution();

        ImmutableMap<Variable, ImmutableTerm> map = substitution.getImmutableMap();

        Stream<Entry<String, ImmutableTerm>> m1 = map
                .entrySet().stream()
                .map(e -> new SimpleEntry<>(
                        e.getKey(),
                        e.getValue().getVariableStream().collect(Collectors.toList())))
                .filter(e -> e.getValue().size() == 1)
                .map(e -> new SimpleEntry<>(
                        e.getKey().getName(),
                        e.getValue().get(0)));

        Stream<Entry<String, ImmutableTerm>> m2 = map.entrySet()
                .stream()
                .filter(e -> e.getValue() instanceof RDFLiteralConstant)
                .map(e -> new SimpleEntry<>(e.getKey().getName(), e.getValue()));

        Map<String, ImmutableTerm> m =
                Streams.concat(m1, m2).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return new OntopReformulationResult(sqlQuery,m);
    }

    private void addMapping(BinaryAnnotationQuery annoQ) {
        String[] firstComponent = annoQ.getFirstComponent();
        String[] secondComponent = annoQ.getSecondComponent();
        IRI targetURI = annoQ.getTargetIRI();
        String query = annoQ.getQuery();

        OntopReformulationResult result = reformulate(query);
        Map<String, ImmutableTerm> map = result.substitution;

        if (firstComponent == null || secondComponent == null || targetURI == null || query == null) {
            logger.error("invalid input - some inputs contain null value");
            return;
        }

        OWLEntity targetEntity;

        OWLDatatype dataType = null;
        OWLDatatype defaultDataType = new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());

        try {
            targetEntity = OBDAMappingUtility.getOWLTargetEntity(targetOntology, targetURI);

            if (targetEntity.isOWLDataProperty()) {
                if (secondComponent.length > 1) {
                    logger.error(
                            "wrong annotation - for the mapping to data property"
                                    + "the second component must contain exactly one answer variable/constant");
                    return;
                }

                dataType = OBDAMappingUtility.getDataType(this.targetOntology, targetEntity.asOWLDataProperty());

                if (dataType == null)
                    dataType = defaultDataType;
            }

            String targetQuery = "";

            String firstURITemplate = getComponentTemplate(firstComponent, map);
            StringBuilder secondURITemplate = new StringBuilder(getComponentTemplate(secondComponent, map));

            if (firstURITemplate.length() == 0 || secondURITemplate.length() == 0) {
                logger.error("something wrong with the answer variables information");
                return;
            }

            logger.info("firstURITemplate: " + firstURITemplate);
            logger.info("secondURITemplate: " + secondURITemplate);

            if (targetEntity.isOWLObjectProperty()) {
                logger.info("Add a mapping to an OBJECT PROPERTY");

                targetQuery = String.format(objPropTripleTemplate,
                        OBDAMappingUtility.cleanURI(firstURITemplate.toString()),
                        targetEntity.toString(),
                        OBDAMappingUtility.cleanURI(secondURITemplate.toString()));

            } else if (targetEntity.isOWLDataProperty()) {
                logger.info("Add a mapping to a DATA PROPERTY");

                if (OBDAMappingUtility.isConstant(secondURITemplate.toString())) {
                    secondURITemplate.insert(0, "\"");
                    secondURITemplate.append("\"");
                }

                //append data type
                secondURITemplate.append("^^");
                secondURITemplate.append(dataType);

                targetQuery = String.format(dataPropTripleTemplate,
                        OBDAMappingUtility.cleanURI(firstURITemplate.toString()),
                        targetEntity.toString(),
                        secondURITemplate);
            }
            if (!query.equals("") &&
                    targetQuery != null && !targetQuery.equals("")) {

                this.addMapping(result.sqlString, targetQuery);
            }
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }

    private String getComponentTemplate(String[] uriComponent, Map<String, ImmutableTerm> map) {

        return Arrays.stream(uriComponent)
                .map(map::get)
                .map(term -> {
                    if (term instanceof Variable) {
                        return "{" + ((Variable) term).getName() + "}";
                    } else if (term instanceof RDFConstant) {
                        return (((RDFConstant) term).getValue());
                    } else {
                        throw new IllegalArgumentException("unknown type: " + term);
                    }
                })
                .collect(Collectors.joining("/"));
    }

    private void addMapping(UnaryAnnotationQuery annoQ) {

        String[] uriComponent = annoQ.getComponent();
        IRI targetURI = annoQ.getTargetIRI();
        String query = annoQ.getQuery();

        if (uriComponent == null || targetURI == null || query == null) {
            logger.error("invalid input - some inputs contain null value");
            return;
        }

        OWLEntity targetEntity;

        try {
            targetEntity = OBDAMappingUtility.getOWLTargetEntity(targetOntology, targetURI);
            OntopReformulationResult result = reformulate(query);
            Map<String, ImmutableTerm> map = result.substitution;
            String uriTemplate = getComponentTemplate(uriComponent, map);
            if (uriTemplate.length() == 0) {
                logger.error("something wrong with the answer variables information - skip");
                return;
            }

            logger.info("uriTemplate: " + uriTemplate);
            logger.info("END OF Generating the target URI Template");
            logger.info("Add a mapping to a CONCEPT");

            String targetQuery = String.format(conceptTripleTemplate,
                    OBDAMappingUtility.cleanURI(uriTemplate.toString()), targetEntity.toString());

            if (!query.equals("") && targetQuery != null && !targetQuery.equals("")) {
                this.addMapping(result.sqlString, targetQuery);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class AnnotationQueriesProcessor implements AnnotationQueryVisitor {

        @Override
        public void visit(BinaryAnnotationQuery query) {
            addMapping(query);
        }

        @Override
        public void visit(UnaryAnnotationQuery query) {
            addMapping(query);
        }
    }

    static class OntopReformulationResult {
        public OntopReformulationResult(String sqlString, Map<String, ImmutableTerm> substitution) {
            this.sqlString = sqlString;
            this.substitution = substitution;
        }

        String sqlString;
        Map<String, ImmutableTerm> substitution;
    }
}

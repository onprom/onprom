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

package it.unibz.inf.onprom.obdamapper;

import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.onprom.data.query.*;
import it.unibz.inf.onprom.obdamapper.utility.OBDAMappingUtility;
import it.unibz.inf.onprom.obdamapper.utility.OntopUtility;
import it.unibz.inf.onprom.owl.OWLUtility;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.TemporaryDBTypeConversionToStringFunctionSymbolImpl;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.protege.core.OntologyPrefixManager;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.impl.AbstractPrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.SimplePrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.Substitution;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.rdf.rdfxml.parser.RDFConstants.RDF_TYPE;

public class OBDAMapper {
    protected static final Logger logger = (Logger) LoggerFactory.getLogger(OBDAMapper.class);
    private static final String objPropTripleTemplate = " :%s %s :%s . "; //<[Object]> [ObjectProperty] <[Object]>
    private static final String dataPropTripleTemplate = " :%s %s %s . "; //<[Object> [DataProperty] <[Value]>
    private static final String conceptTripleTemplate = " :%s a %s . "; //<[Object]> rdf:type <[Class]>

    private final OWLOntology targetOntology;
    //private final SQLPPMapping obdaModel;

    private final List<OntopNativeSQLPPTriplesMap> triplesMaps;
    private final PrefixManager prefixManager;
    private OntopOWLStatement statement;
    private final TargetQueryParser textParser;
    private final SQLPPSourceQueryFactory sourceQueryFactory;
    private final SQLPPMappingFactory ppMappingFactory;
    private final OntopSQLOWLAPIConfiguration config;

    public OBDAMapper(
            OWLOntology sourceOntology, OWLOntology targetOntology, SQLPPMapping sourceObdaModel,
            Properties dataSourceProperties, AnnotationQueries annotationQueries) {
        this(sourceOntology, targetOntology, sourceObdaModel, dataSourceProperties, annotationQueries, true);
    }

    public OBDAMapper(
            OWLOntology sourceOntology, OWLOntology targetOntology, SQLPPMapping sourceObdaModel,
            Properties dataSourceProperties, AnnotationQueries annotationQueries, boolean translate) {
        this.targetOntology = targetOntology;
        this.config = OntopUtility.getConfiguration(sourceObdaModel, dataSourceProperties);
        this.prefixManager = new OntologyExtendedPrefixManager(sourceObdaModel.getPrefixManager(), targetOntology);
        this.textParser = config.getInjector().getInstance(TargetQueryParserFactory.class).createParser(prefixManager);
        this.sourceQueryFactory = config.getInjector().getInstance(SQLPPSourceQueryFactory.class);
        this.ppMappingFactory = config.getInjector().getInstance(SQLPPMappingFactory.class);
        this.triplesMaps = new ArrayList<>();
        try {
            OntopOWLReasoner reasoner = OntopOWLFactory.defaultFactory().createReasoner(sourceOntology, config);
            this.statement = reasoner.getConnection().createStatement();
            if(translate)
                this.startMapping(annotationQueries);
            else
                this.startMappingNoTranslation(annotationQueries);
            reasoner.close();
            reasoner.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OBDAMapper(OWLOntology domainOnt, OWLOntology targetOnt, SQLPPMapping domainOBDA, Properties dsProperties,
                      SQLPPMapping targetOBDA) {
        this(targetOnt, domainOBDA, dsProperties);
        try {
            OntopOWLReasoner reasoner = OntopOWLFactory.defaultFactory().createReasoner(domainOnt, config);
            this.statement = reasoner.getConnection().createStatement();
            this.startMapping(targetOBDA);
            reasoner.close();
            reasoner.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startMapping(SQLPPMapping targetOBDA) {
        // FIXME: Finish Implementation
        targetOBDA.getTripleMaps()
                .forEach(t -> {
                    String query = t.getSourceQuery().getSQL();
                    OntopReformulationResult result = reformulate(query);
                    Map<String, List<ImmutableTerm>> map = result.substitution;
                    StringBuilder targetQuery = new StringBuilder();
                    for(TargetAtom targetAtom : t.getTargetAtoms()) {
                        IRI predicate = deriveTargetURI(targetAtom, 1);
                        if(predicate.toString().equals("<" + RDF_TYPE + ">")) {
                            String targetClass = deriveIRI(map, targetAtom.getSubstitutedTerm(0));
                            OWLEntity targetEntity = OBDAMappingUtility.getOWLTargetEntity(targetOntology,
                                    deriveTargetURI(targetAtom, 2));
                            targetQuery.append(String.format("%s a %s .", targetClass, targetEntity));
                        } else {
                            String target1, target2;
                            target1 = deriveIRI(map, targetAtom.getSubstitutedTerm(0));
                            target2 = deriveIRI(map, targetAtom.getSubstitutedTerm(2));
                            targetQuery.append(String.format("%s %s %s .", target1,
                                    OBDAMappingUtility.getOWLTargetEntity(targetOntology,predicate), target2));
                        }
                        targetQuery.append(" ");
                    }
                    this.addMapping(result.sqlString, targetQuery.toString());
                });
    }

    private String deriveIRI(Map<String, List<ImmutableTerm>> map, ImmutableTerm substitutedTerm) {
        if(substitutedTerm instanceof GroundFunctionalTerm groundTerm) {
            ImmutableList<? extends GroundTerm> terms = groundTerm.getTerms();
            if (terms.size() == 2 && terms.get(0) instanceof Constant constant && terms.get(1) instanceof RDFTermTypeConstant termTypeConstant) {
                return '"' + constant.getValue() + "\"^^" + termTypeConstant.getRDFTermType().toString();
            }
            return substitutedTerm.toString();
        }
        else if(substitutedTerm instanceof NonGroundFunctionalTerm ngTerm) {
            List<String> sb = new ArrayList<>();
            ImmutableTerm term = ngTerm.getTerm(0);
            if(term instanceof NonGroundFunctionalTerm ngTerm2) {
                FunctionSymbol functionSymbol = ngTerm2.getFunctionSymbol();
                if(functionSymbol instanceof TemporaryDBTypeConversionToStringFunctionSymbolImpl) {
                    ImmutableTerm typeTerm = ngTerm.getTerm(1), mainTerm = ngTerm2.getTerm(0);
                    if(typeTerm instanceof RDFTermTypeConstant rdfTermTypeConstant) {
                        if(mainTerm instanceof Variable variable) {
                            List<ImmutableTerm> terms = map.get(variable.getName());
                            if(terms.size() != 1)
                                throw new RuntimeException("cannot derive IRI from term " + substitutedTerm +
                                        " having non-unique or no values for " + variable.getName() + "!");
                            String formatted = OBDAMapper.formatTerms(terms);
                            String dataType = rdfTermTypeConstant.getRDFTermType().toString();
                            if(terms.getFirst() instanceof RDFConstant)
                                return "\"" + formatted + "\"" + "^^" + dataType;
                            else
                                return formatted + "^^" + dataType;
                        } else if(mainTerm instanceof RDFConstant)
                            return mainTerm.toString();
                        else
                            throw new IllegalArgumentException("Unknown term type " + mainTerm.getClass() + "!");
                    } else
                        throw new IllegalArgumentException("cannot derive IRI from term " + substitutedTerm + " of type "
                                + substitutedTerm.getClass() + "!");
                }
                String function = functionSymbol.getName();
                for(ImmutableTerm t : ngTerm2.getTerms()) {
                    if(t instanceof NonGroundFunctionalTerm ngTerm3) {
                        if(map == null)
                            sb.add(ngTerm3.getTerm(0).toString());
                        else {
                            ImmutableTerm term1 = ngTerm3.getTerm(0);
                            if(term1 instanceof Variable variable) {
                                List<ImmutableTerm> terms = map.get(variable.getName());
                                sb.add(OBDAMapper.formatTerms(terms));
                            } else
                                throw new RuntimeException("Unexpected term type " + term1.getClass() + "!");
                        }
                    } else if(t instanceof GroundTerm) {
                        sb.add(t.toString());
                    }
                }
                return "<" + MessageFormatter.arrayFormat(function, sb.toArray()).getMessage() + ">";
            }
        }
        throw new IllegalArgumentException("cannot derive IRI from term " + substitutedTerm + " of type "
                + substitutedTerm.getClass() + "!");
    }

    private IRI deriveTargetURI(TargetAtom targetAtom, int index) {
        return IRI.create(((IRIConstant) targetAtom.getSubstitutedTerm(index)).getIRI().toString());
    }

    public OBDAMapper(OWLOntology targetOnt, SQLPPMapping domainOBDA, Properties dsProperties) {
        this.targetOntology = targetOnt;
        this.config = OntopUtility.getConfiguration(domainOBDA, dsProperties);
        this.prefixManager = new OntologyExtendedPrefixManager(domainOBDA.getPrefixManager(), targetOntology);
        this.textParser = config.getInjector().getInstance(TargetQueryParserFactory.class).createParser(prefixManager);
        this.sourceQueryFactory = config.getInjector().getInstance(SQLPPSourceQueryFactory.class);
        this.ppMappingFactory = config.getInjector().getInstance(SQLPPMappingFactory.class);
        this.triplesMaps = new ArrayList<>();
    }

    public SQLPPMapping getOBDAModel() {
        ImmutableList<SQLPPTriplesMap> collect = ImmutableList.copyOf(triplesMaps);
        return ppMappingFactory.createSQLPreProcessedMapping(collect, this.prefixManager);
    }

    public SQLPPMapping getOBDAModelNoPrefix() {
        return ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.copyOf(triplesMaps),
                new EmptyPrefixManager());
    }

    private static class EmptyPrefixManager extends AbstractPrefixManager {
        @Override
        protected Optional<String> getIriDefinition(String prefix) {
            return Optional.empty();
        }

        @Override
        protected List<Entry<String, String>> getOrderedMap() {
            return List.of(new AbstractMap.SimpleEntry<>("a", RDF_TYPE));
        }

        @Override
        public Map<String, String> getPrefixMap() {
            return Map.of();
        }
    }

    private void startMapping(AnnotationQueries annotationQueries) {
        AnnotationQueriesProcessor mappingAdder = new AnnotationQueriesProcessor();
        for (AnnotationQuery aq : annotationQueries.getAllQueries()) {
            aq.accept(mappingAdder);
        }
    }

    private void startMappingNoTranslation(AnnotationQueries annotationQueries) {
        BasicAnnotationQueriesProcessor mappingAdder = new BasicAnnotationQueriesProcessor();
        for (AnnotationQuery aq : annotationQueries.getAllQueries()) {
            aq.accept(mappingAdder);
        }
    }

    private void addMapping(String source, String target) {
        this.triplesMaps.add(computeMappingFromSourceTarget(source, target));
    }

    public OntopNativeSQLPPTriplesMap computeMappingFromSourceTarget(String source, String target) {
        //String newId = "ONPROM_MAPPING_" + obdaModel.getMapping(obdaModel.getDatasource().getSourceID()).size();
        String newId = "ONPROM_MAPPING_" + (triplesMaps.size() + 1);
        logger.info("######################\nID:{}\nTARGET:{}\nSOURCE:{}\n######################", newId, target,
                source);
        try {
            return new OntopNativeSQLPPTriplesMap(newId,
                    sourceQueryFactory.createSourceQuery(source),
                    textParser.parse(target));
            //obdaModel.addTriplesMap(triplesMap, false);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private OntopReformulationResult reformulate(String source) {
        ExecutableQueryAndSubstitution exQueryAndSub = getExecutableQueryAndSubstitution(source);

        String sqlQuery = ((NativeNode) exQueryAndSub.executableQuery().getTree().getChildren().getFirst())
                .getNativeQueryString();
        logger.info("######################\nBODY:{}\n######################", sqlQuery);
        return new OntopReformulationResult(sqlQuery, exQueryAndSub.m());
    }

    private @NonNull ExecutableQueryAndSubstitution getExecutableQueryAndSubstitution(String source) {
        IQ executableQuery;
        try {
            executableQuery = this.statement.getExecutableQuery(source);
        } catch (OWLException e) {
            throw new IllegalArgumentException(e);
        }

        ConstructionNode constructionNode = (ConstructionNode) executableQuery.getTree().getRootNode();
        Substitution<ImmutableTerm> substitution = constructionNode.getSubstitution();

        ImmutableMap<Variable, ImmutableTerm> map = ImmutableMap.<Variable, ImmutableTerm>builder()
                .putAll(substitution.stream()
                        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue))).build();
        Stream<Entry<String, List<ImmutableTerm>>> m1 = map
                .entrySet().stream()
                // this is normally a function (template)
                .filter(e -> !(e.getValue() instanceof RDFLiteralConstant))
                .map(e -> new SimpleEntry<>(
                        e.getKey().getName(),
                        // TODO: we may need to consider the full IRI
                        // TODO: we might need to worry about the order
                        e.getValue().getVariableStream().collect(Collectors.toList())));
//                .filter(e -> e.getValue().size() == 1)
//                .map(e -> new SimpleEntry<>(
//                        e.getKey().getName(),
//                        e.getValue().get(0)));

        Stream<Entry<String, List<ImmutableTerm>>> m2 = map.entrySet()
                .stream()
                .filter(e -> e.getValue() instanceof RDFLiteralConstant)
                .map(e -> new SimpleEntry<>(e.getKey().getName(), ImmutableList.of(e.getValue())));

        Map<String, List<ImmutableTerm>> m = Stream.concat(m1, m2).collect(Collectors.toMap(Entry::getKey,
                Entry::getValue));
        return new ExecutableQueryAndSubstitution(executableQuery, m);
    }

    private record ExecutableQueryAndSubstitution(IQ executableQuery, Map<String, List<ImmutableTerm>> m) {
    }

    private void addMapping(BinaryAnnotationQuery annoQ) {
        addMapping(annoQ, true);
    }

    private void addMapping(BinaryAnnotationQuery annoQ, boolean translate) {
        String[] firstComponent = annoQ.getFirstComponent();
        String[] secondComponent = annoQ.getSecondComponent();
        IRI targetURI = annoQ.getTargetIRI();

        String query = annoQ.getQuery();
        String queryOrTranslated, firstURITemplate;
        StringBuilder secondURITemplate;
        if(translate) {
            OntopReformulationResult result = reformulate(query);
            queryOrTranslated = result.sqlString;
            firstURITemplate = getComponentTemplate(firstComponent, result.substitution);
            secondURITemplate = new StringBuilder(getComponentTemplate(secondComponent, result.substitution));
        } else {
            queryOrTranslated = query;
            firstURITemplate = Arrays.stream(firstComponent).map(x -> "{" + x + "}")
                    .collect(Collectors.joining("/"));
            secondURITemplate = new StringBuilder(Arrays.stream(secondComponent)
                    .map(x -> "{" + x + "}").collect(Collectors.joining("/")));
        }

        OWLEntity targetEntity = OBDAMappingUtility.getOWLTargetEntity(targetOntology, targetURI);
        String targetQuery;

        logger.info("firstURITemplate: {}", firstURITemplate);
        logger.info("secondURITemplate: {}", secondURITemplate);

        if (targetEntity.isOWLObjectProperty()) {
            logger.info("Add a mapping to an OBJECT PROPERTY");

            targetQuery = String.format(objPropTripleTemplate,
                    OBDAMappingUtility.cleanURI(firstURITemplate),
                    this.prefixManager.getShortForm(targetEntity.getIRI().toString()),
                    OBDAMappingUtility.cleanURI(secondURITemplate.toString()));

        }  else {
            String firstMappedURI = OBDAMappingUtility.cleanURI(firstURITemplate);
            String secondMappedURI = OBDAMappingUtility.cleanURI(secondURITemplate.toString());
            if (targetEntity.isOWLDataProperty()) {
                if (secondComponent.length > 1) {
                    throw new IllegalArgumentException(
                            "wrong annotation - for the mapping to data property"
                                    + "the second component must contain exactly one answer variable/constant");
                }
                logger.info("Add a mapping to a DATA PROPERTY");

                if (OBDAMappingUtility.isConstant(secondURITemplate.toString())) {
                    secondURITemplate.insert(0, "\"");
                    secondURITemplate.append("\"");
                }

                //append data type
                OWLDatatype dataType;
                OWLDatatype defaultDataType = new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());

                dataType = Objects.requireNonNullElse(OBDAMappingUtility.getDataType(
                        this.targetOntology, targetEntity.asOWLDataProperty()), defaultDataType);
                secondURITemplate.append("^^");
                secondURITemplate.append(dataType);

                targetQuery = String.format(dataPropTripleTemplate,
                        firstMappedURI,
                        this.prefixManager.getShortForm(targetEntity.getIRI().toString()),
                        secondURITemplate);
            } else if (targetEntity.isOWLClass()) {
                OWLClass targetClass = targetEntity.asOWLClass();

                logger.info("Add a mapping to an ASSOCIATION CLASS");


                OWLClass associationClass = targetEntity.asOWLClass();
                // Get all object properties whose domain is associationClass
                List<OWLObjectProperty> domainIsClass = targetOntology.objectPropertiesInSignature()
                        .filter(objectProperty ->
                                        targetOntology.getObjectPropertyDomainAxioms(objectProperty)
                                                .stream()
                                                .anyMatch(axiom ->
                                                        axiom.getDomain().equals(targetClass)))
                        .toList();
                if(domainIsClass.size() > 2)
                    throw new IllegalArgumentException("Expected association class "
                            + associationClass.getIRI().getShortForm()
                     + " to have at most two object properties with it as domain but got "
                            + domainIsClass.size() + " properties instead!");


                OWLObjectProperty[] parts = getParts(domainIsClass, associationClass);
                String associationURI = getAssociationInstanceTemplate(firstMappedURI, secondMappedURI,
                        targetEntity.getIRI());

                targetQuery =
                        String.format(conceptTripleTemplate,
                                associationURI,
                                this.prefixManager.getShortForm(targetClass.getIRI().toString()))
                                +
                                String.format(objPropTripleTemplate,
                                        associationURI,
                                        this.prefixManager.getShortForm(parts[0].getIRI().toString()),
                                        firstMappedURI)
                                +
                                String.format(objPropTripleTemplate,
                                        associationURI,
                                        this.prefixManager.getShortForm(parts[1].getIRI().toString()),
                                        secondMappedURI);
            } else
                targetQuery = "";
        }

        this.addMapping(queryOrTranslated, targetQuery);
    }

    private OWLObjectProperty[] getParts(
            List<OWLObjectProperty> properties,
            OWLClass associationClass) {

        if (properties.size() == 2) {
            OWLObjectProperty[] parts = new OWLObjectProperty[2];

            for (OWLObjectProperty property : properties) {
            int index = getAssociationPart(property, associationClass);

            if (parts[index] != null) {
                throw new IllegalArgumentException(
                        "Association class " + associationClass
                                + " has two parts with "
                                + OWLUtility.getTypeIRI()
                                + " annotation corresponding to index " + index);
            }

            parts[index] = property;
        }
            if (parts[0] == null || parts[1] == null) {
                throw new IllegalArgumentException(
                        "Association class " + associationClass
                                + " must have exactly one domain part and one range part");
            }

            return parts;
        }
        return new OWLObjectProperty[] {properties.getFirst(), properties.getFirst()};
    }

    private int getAssociationPart(OWLObjectProperty property, OWLClass associationClass) {
        // Verify that this OWL class is actually an association class
        if (!hasAssociationAnnotation(property, associationClass.getIRI().getShortForm())) {
            throw new IllegalArgumentException(
                    "OWL class used as binary relation target is not an association class, " +
                            "didn't find association property in one of its properties: "
                            + property);
        }

        List<String> options = targetOntology.annotationAssertionAxioms(property.getIRI())
                .filter(a ->
                        a.getProperty().getIRI().equals(OWLUtility.getTypeIRI()))
                .map(OWLAnnotationAssertionAxiom::getValue)
                .filter(OWLAnnotationValue::isLiteral)
                .map(v -> v.asLiteral().orElseThrow().getLiteral())
                .toList();

        if (options.size() != 1) {
            throw new IllegalArgumentException(
                    "Part of association class " + associationClass
                            + ", " + property
                            + " does not have unique "
                            + OWLUtility.getTypeIRI() + " annotation!");
        }

        String type = options.getFirst();

        if (OWLUtility.isDomain(type))
            return 0;
        if (OWLUtility.isRange(type))
            return 1;

        throw new IllegalArgumentException(
                "Unexpected value for " + OWLUtility.getTypeIRI()
                        + " annotation in " + property + ": " + type);
    }

    private String getAssociationInstanceTemplate(
            String firstURITemplate,
            String secondURITemplate,
            IRI associationIRI) {

        return associationIRI.getShortForm()
                + "/" + firstURITemplate
                + "/" + secondURITemplate;
    }

    private boolean hasAssociationAnnotation(OWLEntity subject, String targetName) {
        return targetOntology.annotationAssertionAxioms(subject.getIRI())
                .anyMatch(x ->
                {
                    OWLAnnotationProperty property = x.getProperty();
                    OWLLiteral owlLiteral = x.getValue().asLiteral().orElseThrow();
                    return property.getIRI().equals(OWLUtility.getAssociationIRI()) &&
                    owlLiteral.isLiteral() && owlLiteral.getLiteral().equals(targetName);
                });
    }

    private String getComponentTemplate(String[] uriComponent, Map<String, List<ImmutableTerm>> map) {
        return Arrays.stream(uriComponent)
                .map(key -> {
                    if(!map.containsKey(key))
                        throw new IllegalArgumentException("unknown URI component " + key + " not derived by query!");
                    return map.get(key);
                })
                .map(OBDAMapper::formatTerms)
                .collect(Collectors.joining("/"));
    }

    private static String formatTerms(List<ImmutableTerm> terms) {
        return terms.stream()
                .map(OBDAMapper::formatTerm)
                .collect(Collectors.joining("/"));
    }

    private static String formatTerm(ImmutableTerm term) {
        if (term instanceof Variable) {
            return "{" + ((Variable) term).getName() + "}";
        } else if (term instanceof RDFConstant) {
            return (((RDFConstant) term).getValue());
        } else {
            throw new IllegalArgumentException("unknown type: " + term);
        }
    }

    private void addMapping(UnaryAnnotationQuery annoQ) {
        addMapping(annoQ, true);
    }

    private void addMapping(UnaryAnnotationQuery annoQ, boolean translate) {
        // FIXME: In DyamicAnnotation.getQuery, getAnnotationInstanceQuery is used to create a UnaryAnnotationQuery
        // FIXME: that has uri can include attributes in DynamicAnnotation.attributeValues, however, the SPARQL query
        // FIXME: doesnt compute these required attributes! So here, getComponentTemplate fails!
        String[] uriComponent = annoQ.getComponent();
        IRI targetURI = annoQ.getTargetIRI();
        String query = annoQ.getQuery();


        String uriTemplate;
        String queryOrTranslated;
        if(translate) {
            OntopReformulationResult result = reformulate(query);
            try {
                uriTemplate = getComponentTemplate(uriComponent, result.substitution);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error while processing unary annotation query query\n" + query +
                        "\ntargeting " + targetURI.toString(), e);
            }
            queryOrTranslated = result.sqlString;
        } else {
            queryOrTranslated = query;
            uriTemplate = Arrays.stream(uriComponent).map(x -> "{" + x + "}").collect(Collectors.joining("/"));
        }
        if (uriTemplate.isEmpty()) {
            logger.error("something wrong with the answer variables information - skip");
            throw new IllegalStateException();
        }

        logger.info("uriTemplate: {}", uriTemplate);
        logger.info("END OF Generating the target URI Template");
        logger.info("Add a mapping to a CONCEPT");

        String targetEntity = OBDAMappingUtility.getOWLTargetEntity(targetOntology, targetURI).getIRI().toString();


        String targetQuery = String.format(conceptTripleTemplate,
                OBDAMappingUtility.cleanURI(uriTemplate), this.prefixManager.getShortForm(targetEntity));

        this.addMapping(queryOrTranslated, targetQuery);
    }

    static class OntopReformulationResult {
        String sqlString;
        Map<String, List<ImmutableTerm>> substitution;

        public OntopReformulationResult(String sqlString, Map<String, List<ImmutableTerm>> substitution) {
            this.sqlString = sqlString;
            this.substitution = substitution;
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

    private class BasicAnnotationQueriesProcessor implements AnnotationQueryVisitor {

        @Override
        public void visit(BinaryAnnotationQuery query) {
            addMapping(query, false);
        }

        @Override
        public void visit(UnaryAnnotationQuery query) {
            addMapping(query, false);
        }
    }
}

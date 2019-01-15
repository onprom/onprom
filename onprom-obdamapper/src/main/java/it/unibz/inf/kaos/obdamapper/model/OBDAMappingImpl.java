/*
 *
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */

package it.unibz.inf.kaos.obdamapper.model;

import it.unibz.inf.kaos.data.query.*;
import it.unibz.inf.kaos.obdamapper.constants.OMConstants;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.ontopext.SQLWithVarMap;
import it.unibz.inf.kaos.obdamapper.reasoner.QuestOWLReasonerExt;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import org.openrdf.query.MalformedQueryException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/*
 * Note (2017.07 - ario): the reason of making a separation among OBDAMapping interface,
 * OBDAMapping abstract class and OBDAMappingImpl class is to allow different kind of
 * OBDAMapping implementation. This implementation is just one possible implementation.
 * Another possible implementation would be the implementation that utilizes paralel
 * processing.
 */
//Note: we only support 1 data source


/**
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class OBDAMappingImpl extends OBDAMappingAbstractImpl implements OBDAMapping {

    private static final long serialVersionUID = 40062215694643168L;

    private final String objPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s <http://onprom.inf.unibz.it/%s> . "; //<[Object]> [ObjectProperty] <[Object]>
    private final String dataPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s %s . "; //<[Object> [DataProperty] <[Value]>
    private final String conceptTripleTemplate = " <http://onprom.inf.unibz.it/%s> a %s . "; //<[Object]> rdf:type <[Class]>


    protected OBDAMappingImpl(OBDADataSource obdaDataSource, OWLOntology targetOntology) {

        super(obdaDataSource, targetOntology);
        logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "An OBDA Mapping is initialized"));
    }

    protected OBDAMappingImpl(
            OWLOntology sourceOntology, OWLOntology targetOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ)
            throws InvalidDataSourcesNumberException {

        super(sourceObdaModel.getSources(), targetOntology);
        this.addMapping(sourceOntology, sourceObdaModel, annoQ);

        logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "An OBDA Mapping is initialized"));
    }

    /**
     * Add mappings based on the given annotated OBDA System
     *
     * @param sourceOntology  - the ontology of the annotated OBDA System
     * @param sourceObdaModel - the OBDA Model/Mappings of the annotated OBDA System
     * @param annoQ           - the annotations
     * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
     */
    @Override
    public void addMapping(OWLOntology sourceOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ)
            throws InvalidDataSourcesNumberException {

        //Create an instance of Extended Quest OWL Reasoner.
        QuestOWLConfiguration config = createDefaultQuestOWLConfiguration(sourceObdaModel);
        QuestOWLReasonerExt questReasoner = new QuestOWLReasonerExt(sourceOntology, config);
        //END OF Creating an instance of Extended Quest OWL Reasoner.

        try {

            //========================================================================================
            // Handling the number of data sources
            //========================================================================================

            List<OBDADataSource> newOdsList = sourceObdaModel.getSources();
            List<OBDADataSource> odsList = this.getSources();

            if (newOdsList.size() > 1) {

                //generate an error message: too many data sources
                logger.error("kebanyakan source 1 - kebanyakan yang mo ditambahin");
                return;
            }

            if (odsList.size() > 1) {

                //generate an error message: too many data sources
                logger.error("kebanyakan source 2 - udah kebanyakan");
                return;

            } else if (odsList.size() == 1) {

                if (odsList.get(0) != newOdsList.get(0)) {

                    //generate an error message: too many data sources,
                    logger.error("kebanyakan source 3 - klo ditambahin jadi kebanyakan");
                    return;
                }

            } else if (odsList.size() < 1) {

                this.addSource(newOdsList.get(0));
            }

            //========================================================================================
            // END OF Handling the number of data sources
            //========================================================================================

            AnnotationQueriesProcessor mappingAdder =
                    new AnnotationQueriesProcessor(questReasoner, this.getSources().get(0).getSourceID());

            for (AnnotationQuery aq : annoQ.getAllQueries()) {
                if (aq != null)
                    aq.accept(mappingAdder);
            }

        } finally {
            try {
                questReasoner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            questReasoner.dispose();
        }
    }

    private void addMapping(BinaryAnnotationQuery annoQ, QuestOWLReasonerExt reasoner, URI obdaDataSourceID) {

        String[] firstComponent = annoQ.getFirstComponent();
        String[] secondComponent = annoQ.getSecondComponent();
        IRI targetURI = annoQ.getTargetIRI();
        String sourceSQLQuery = annoQ.getQuery();

        if (firstComponent == null || secondComponent == null || targetURI == null || sourceSQLQuery == null) {
            //generate a message - invalid input - some inputs contain null value
            logger.error("TODO: generate a message - invalid input - some inputs contain null value");
            return;
        }

        List<SQLWithVarMap> unfoldedQueries;
        OWLEntity targetEntity = null;

        //some variables for handling the cases when the target entity is a data property
        OWLDatatype dataType = null;
        OWLDatatype defaultDataType = new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());
        //END OF some variables for handling the cases when the target entity is a data property


        //###########################################################################
        //processing the target entity - preparing the target query creation
        //###########################################################################

        try {

            targetEntity = getOWLTargetEntity(targetURI);

        } catch (Exception e) {
            //TODO: generate a better message
            logger.error(e.getMessage(), e);
        }

        //----------------------------------------------------------------------------
        // Handling the case where the targetEntity is data property
        //----------------------------------------------------------------------------

        if (targetEntity.isOWLDataProperty()) {

            if (secondComponent.length > 1) {
                //generate an error message - wrong annotation -
                //second component must contain exactly one answer variable/constant

                logger.debug(
                        "\n\nERROR!!! --- TODO: generate an error message - "
                                + "wrong annotation - for the mapping to data property"
                                + "the second component must contain exactly one answer variable/constant\n");

                return;
            }

            try {
                dataType = getDataType(targetEntity.asOWLDataProperty());

                if (dataType == null)
                    dataType = defaultDataType;

                //logger.debug("DEBUGA: dataType: "+dataType);

            } catch (Exception e) {
                //TODO: generate a more meaningful message
                logger.info(e.getMessage(), e);
            }
        }

        //----------------------------------------------------------------------------
        // END OF Handling the case where the targetEntity is data property
        //----------------------------------------------------------------------------

        //###########################################################################
        //END OF processing the target entity
        //###########################################################################


        //###########################################################################
        //Reformulate (rewrite & unfold) the Source Query
        //###########################################################################

        try {
            unfoldedQueries = reasoner.reformulateSPARQL2(sourceSQLQuery);

        } catch (OBDAException | MalformedQueryException | OWLException e) {
            //TODO: generate a message that we fail to reformulate the query annoQ, and skip this query
            logger.error(e.getMessage(), e);
            return;
        }

        //Just in case there is a failure in reformulating the source query
        if (unfoldedQueries == null) {

            //TODO: generate a message that we fail to process the query annoQ, and skip this query
            logger.debug("TODO: generate a message that we fail to process the query annoQ, and skip this query");
            return;
        }

        //logger.debug("DEBUGAGA: number of reformulated queries: " + unfoldedQueries.size());
        //logger.debug("DEBUGAGA: reformulated queries: \n\n" + unfoldedQueries.get(0).getSQL());

        //###########################################################################
        //END OF Reformulate (rewrite & unfold) the Source Query
        //###########################################################################


        //###########################################################################
        //Generate the mappings - create a mapping for each reformulated source query
        //###########################################################################

        String targetQuery = "";
        //HashMap<String, String> varMap = null;
        //StringBuilder firstURITemplate = new StringBuilder();
        //StringBuilder secondURITemplate = new StringBuilder();

        int iter = 1;

        /*
         * The following loop processes each reformulated query. Note that the target query
         * could be different depending on the varmap that is obtained from the query reformulation.
         * Thus we need to generate the target query in every loop.
         */
        for (SQLWithVarMap sqlWithVarMap : unfoldedQueries) {
            logger.debug("======================\nIteration: " + iter++ + "\n======================");


            //###########################################################################
            //Generating the target query
            //###########################################################################

            HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();

            if (varMap == null) continue;

            //===========================================================================
            //generating the target URI first and second components
            //===========================================================================

            logger.debug("\n\nDEBUGA: ------------ Generating the target URI Template ------------\n");

            StringBuilder firstURITemplate = getComponentTemplate(firstComponent, varMap);
            StringBuilder secondURITemplate = getComponentTemplate(secondComponent, varMap);

            if (firstURITemplate.length() == 0 || secondURITemplate.length() == 0) {

                //generate an error message - something wrong with the answer variables information - skip
                logger.debug("DEBUGA: something wrong with the answer variables information - skip");
                continue;
            }

            logger.debug("DEBUGA: firstURITemplate: " + firstURITemplate);
            logger.debug("DEBUGA: secondURITemplate: " + secondURITemplate);

            logger.debug("DEBUGA: ------------ END OF Generating the target URI Template ------------\n");
            //===========================================================================
            //END OF generating the target URI first and second components
            //===========================================================================


            //===========================================================================
            //generating the target Query/URI template
            //===========================================================================
            if (targetEntity.isOWLObjectProperty()) {

                //========================================================================
                //handling the case of adding a mapping that populates an OBJECT PROPERTY
                //========================================================================

                logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to an OBJECT PROPERTY"));

                targetQuery = String.format(this.objPropTripleTemplate,
                        cleanURI(firstURITemplate.toString()),
                        targetEntity.toString(),
                        cleanURI(secondURITemplate.toString()));

                //========================================================================
                //END OF handling the case of adding a mapping that populates an OBJECT PROPERTY
                //========================================================================

            } else if (targetEntity.isOWLDataProperty()) {

                //========================================================================
                //handling the case of adding a mapping that populates a DATA PROPERTY
                //========================================================================

                logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to a DATA PROPERTY"));


                if (isConstant(secondURITemplate.toString())) {
                    secondURITemplate.insert(0, "\"");
                    secondURITemplate.append("\"");
                }

                //append data type
                secondURITemplate.append("^^");
                secondURITemplate.append(dataType);

                targetQuery = String.format(this.dataPropTripleTemplate,
                        cleanURI(firstURITemplate.toString()),
                        targetEntity.toString(),
                        secondURITemplate);

                //========================================================================
                //END OF handling the case of adding a mapping that populates a DATA PROPERTY
                //========================================================================
            }
            //===========================================================================
            //END OF generating the target Query/URI template
            //===========================================================================

            //###########################################################################
            //END OF Generating the target query
            //###########################################################################

            //###########################################################################
            //get the source query
            //###########################################################################

            String sourceQuery = sqlWithVarMap.getSQL();

            logger.debug("\nDEBUGA: sourceQuery: \n" + sourceQuery);
            //###########################################################################
            //END OF getting the source query
            //###########################################################################
            //###########################################################################
            //Finally, add a new mapping to the set of OBDA Mappings
            //###########################################################################
            if (obdaDataSourceID != null &&
                    sourceQuery != null && !sourceQuery.equals("") &&
                    targetQuery != null && !targetQuery.equals("")) {

                this.addMapping(obdaDataSourceID, sourceQuery, targetQuery);
            }
            //###########################################################################
            //END OF adding a new mapping to the set of OBDA Mappings
            //###########################################################################
        }
        //###########################################################################
        //END OF Generating the mappings
        //###########################################################################
    }

    private StringBuilder getComponentTemplate(String[] uriComponent, HashMap<String, String> varMap) {
        StringBuilder uriTemplate = new StringBuilder();
        for (int i = 0; i < uriComponent.length; i++) {
            String uc = uriComponent[i];
            String uriComp = varMap.get(uc);
            logger.debug("DEBUGA: URI Comp: " + uc + " => " + uriComp);

            if (uriComp != null) {
                uriTemplate.append(uriComp);
            }
            if (i < uriComponent.length - 1) {
                uriTemplate.append("/");
            }
        }
        return uriTemplate;
    }

    private void addMapping(UnaryAnnotationQuery annoQ, QuestOWLReasonerExt reasoner, URI obdaDataSourceID) {

        String[] uriComponent = annoQ.getComponent();
        IRI targetURI = annoQ.getTargetIRI();
        String sourceSQLQuery = annoQ.getQuery();

        if (uriComponent == null || targetURI == null || sourceSQLQuery == null) {
            //generate a message - invalid input - some inputs contain null value
            logger.debug("invalid input - some inputs contain null value");
            return;
        }

        List<SQLWithVarMap> unfoldedQueries;
        OWLEntity targetEntity = null;

        //###########################################################################
        //processing the target entity - preparing the target query creation
        //###########################################################################

        try {

            targetEntity = getOWLTargetEntity(targetURI);

        } catch (Exception e) {
            //TODO: generate a better message
            logger.error(e.getMessage(), e);
        }

        //###########################################################################
        //END OF processing the target entity
        //###########################################################################

        //###########################################################################
        //Reformulate (rewrite & unfold) the Source Query
        //###########################################################################

        try {
            unfoldedQueries = reasoner.reformulateSPARQL2(sourceSQLQuery);

        } catch (OBDAException | MalformedQueryException | OWLException e) {
            //TODO: generate a message that we fail to reformulate the query annoQ, and skip this query
            logger.error(e.getMessage(), e);
            return;
        }

        //Just in case there is a failure in reformulating the source query
        if (unfoldedQueries == null) {

            //TODO: generate a message that we fail to process the query annoQ, and skip this query
            logger.debug("TODO: generate a message that we fail to process the query annoQ, and skip this query");
            return;
        }

        //###########################################################################
        //END OF Reformulate (rewrite & unfold) the Source Query
        //###########################################################################

        //###########################################################################
        //Generate the mappings - create a mapping for each reformulated source query
        //###########################################################################

        /*
         * The following loop processes each reformulated query. Note that the target query
         * could be different depending on the varmap that is obtain from the query reformulation.
         * Thus we need to generate the target query in every loop.
         */
        for (SQLWithVarMap sqlWithVarMap : unfoldedQueries) {

            //###########################################################################
            //Generating the target query
            //###########################################################################

            HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();

            if (varMap == null) continue;

            //===========================================================================
            //generating the target URI component
            //===========================================================================

            logger.debug("\n\nDEBUGA: ------------ Generating the target URI Template ------------\n");

            StringBuilder uriTemplate = getComponentTemplate(uriComponent, varMap);

            if (uriTemplate.length() == 0) {

                //generate an error message - something wrong with the answer variables information - skip
                logger.debug("DEBUGA: something wrong with the answer variables information - skip");
                continue;
            }

            logger.debug("DEBUGA: uriTemplate: " + uriTemplate);

            logger.debug("\nDEBUGA: ------------ END OF Generating the target URI Template ------------\n\n");

            //===========================================================================
            //END OF generating the target URI component
            //===========================================================================

            //===========================================================================
            //generating the target Query/URI template
            //===========================================================================

            logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to a CONCEPT"));

            String targetQuery = String.format(this.conceptTripleTemplate,
                    cleanURI(uriTemplate.toString()), targetEntity.toString());

            //===========================================================================
            //END OF generating the target Query/URI template
            //===========================================================================

            //###########################################################################
            //END OF Generating the target query
            //###########################################################################

            //###########################################################################
            //get the source query
            //###########################################################################

            String sourceQuery = sqlWithVarMap.getSQL();

            //###########################################################################
            //END OF getting the source query
            //###########################################################################

            //###########################################################################
            //Finally, add a new mapping to the set of OBDA Mappings
            //###########################################################################

            if (obdaDataSourceID != null &&
                    sourceQuery != null && !sourceQuery.equals("") &&
                    targetQuery != null && !targetQuery.equals("")) {

                this.addMapping(obdaDataSourceID, sourceQuery, targetQuery);
            }

            //###########################################################################
            //END OF adding a new mapping to the set of OBDA Mappings
            //###########################################################################
        }
        //###########################################################################
        //END OF Generating the mappings
        //###########################################################################
    }

    private class AnnotationQueriesProcessor implements AnnotationQueryVisitor {

        private final QuestOWLReasonerExt questReasoner;
        private final URI obdaDataSourceID;

        public AnnotationQueriesProcessor(QuestOWLReasonerExt questReasoner, URI obdaDataSourceID) {
            this.questReasoner = questReasoner;
            this.obdaDataSourceID = obdaDataSourceID;
        }

        ////////////////////////////////////////////////////////
        // Methods from AnnotationQueryVisitor
        ////////////////////////////////////////////////////////

        @Override
        public void visit(BinaryAnnotationQuery query) {
            addMapping(query, questReasoner, obdaDataSourceID);
        }

        @Override
        public void visit(UnaryAnnotationQuery query) {
            addMapping(query, questReasoner, obdaDataSourceID);
        }

        ////////////////////////////////////////////////////////
        // END OF Methods from AnnotationQueryVisitor
        ////////////////////////////////////////////////////////

    }


}



/*
 * onprom-logextractor
 *
 * XESConstants.java
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

package it.unibz.inf.onprom.logextractor.ocel;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.InputStream;

/**
 * This class provides some constants declaration related to the Ocel Event Ontology
 *
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class OCELConstants {

    //Event Ontology location
    static final String eventOntoPath = "/ocel-ontology.owl";

    static final String qEvtAtt_SimpleAnsVarEvent = "event";
    static final String qEvtAtt_SimpleAnsVarObject = "object";
    static final String qType_SimpleAnsVarObject = "objectType";
    static final String qEvtAtt_SimpleAnsVarActivity = "activity";
    static final String qEvtAtt_SimpleAnsVarTimestamp = "timestamp";
    static final String qAtt = "att";//att
    static final String qAttTypeKeyVal_SimpleAnsVarAttType = "attType";
    static final String qAttTypeKeyVal_SimpleAnsVarAttKey = "attKey";
    static final String qAttTypeKeyVal_SimpleAnsVarAttVal = "attValue";
    //END OF OnProm event ontology vocabularies

    //Some queries over the event ontology for retrieving the required information for generating OCEL log

    //====================================================================================================
    private static final String eventOntoPrefix = "http://onprom.inf.unibz.it/ocel/";
    //====================================================================================================
    private static final String ATTRIBUTE_CONCEPT = "<" + eventOntoPrefix + "Attribute" + ">";
    //some vocabularies for role names/object properties
    private static final String E_CONTAINS_O_ROLE = "<" + eventOntoPrefix + "e-contains-o" + ">";

    private static final String OCEL_OBJECT_IRI = "<" + eventOntoPrefix + "Object" + ">";
    private static final String OCEL_EVENT_IRI = "<" + eventOntoPrefix + "Event" + ">";
    private static final String OCEL_TIMESTAMP_IRI = "<" + eventOntoPrefix + "timestamp" + ">";
    private static final String OCEL_ACTIVITY_IRI = "<" + eventOntoPrefix + "activity" + ">";
    private static final String OCEL_OBJECTTYPE_IRI = "<" + eventOntoPrefix + "objectType" + ">";
    // PREFIX : <http://www.example.org/>
    // SELECT Distinct ?event ?obj
    // WHERE {
    //     ?event :EcontainsO ?obj .
    // }
    static final String qEvtObj_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT Distinct ?event ?obj \n" +
                    "WHERE { "
                    + "?event " + E_CONTAINS_O_ROLE + " ?obj . "
                    + "}";

    //====================================================================================================
    // Query for retrieving  the information about the association between a trace and its attributes.
    //
    // PREFIX : <http://www.example.org/>
    // SELECT distinct ?trace ?att
    // WHERE {
    //   ?trace :TcontainsA ?att .
    // }
    private static final String O_HAS_A_ROLE = "<" + eventOntoPrefix + "o-has-a" + ">";
    static final String qObjectAtt_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT Distinct ?obj ?att \n" +
                    "WHERE { "
                    + "?obj " + O_HAS_A_ROLE + " ?att . "
                    + "}";
    //====================================================================================================
    //Query for retrieving  the information about the association between an event and its attribute
    //
    // PREFIX : <http://www.example.org/>
    // SELECT Distinct ?event ?att
    // WHERE {
    //   ?event :EcontainsA ?att .
    // }
    private static final String E_HAS_A_ROLE = "<" + eventOntoPrefix + "e-has-a" + ">";
    static final String qEventAtt_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT ?event ?att \n" +
                    "WHERE { ?event " + E_HAS_A_ROLE + " ?att }";
    //some vocabularies for attribute/data properties
    private static final String ATT_TYPE_ATT = "<" + eventOntoPrefix + "attType" + ">";
    private static final String ATT_KEY_ATT = "<" + eventOntoPrefix + "attKey" + ">";
    private static final String ATT_VAL_ATT = "<" + eventOntoPrefix + "attValue" + ">";
    //====================================================================================================
    // - Query for retrieving attributes together with their type, value and key.
    //
    // PREFIX : <http://www.example.org/>
    // SELECT Distinct ?att ?attType ?attKey ?attValue
    // WHERE {
    //   ?att a :Attribute; :typeA ?attType; :keyA ?attKey; :valueA ?attValue.
    // }
    static final String qAttTypeKeyVal_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct ?att ?attType ?attKey ?attValue \n" +
                    "WHERE { ?att a " + ATTRIBUTE_CONCEPT + "; " +
                    ATT_TYPE_ATT + " ?attType; " +
                    ATT_KEY_ATT + " ?attKey; " +
                    ATT_VAL_ATT + " ?attValue  }";

    static final String qObjects =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { ?object a " + OCEL_OBJECT_IRI + " . OPTIONAL { \n" +
                    "  ?object " + O_HAS_A_ROLE + " ?att . \n" +
                    "  ?att a " + ATTRIBUTE_CONCEPT + "; \n" +
                    ATT_TYPE_ATT + " ?attType; \n" +
                    ATT_KEY_ATT + " ?attKey; \n" +
                    ATT_VAL_ATT + " ?attValue  } }";

    static final String qEvents =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { " +
                    "?event a " + OCEL_EVENT_IRI + " . \n" +
                    "OPTIONAL { \n" +
                    "  ?event " + E_HAS_A_ROLE + " ?att . \n" +
                    "  ?att a " + ATTRIBUTE_CONCEPT + "; \n" +
                    ATT_TYPE_ATT + " ?attType; \n" +
                    ATT_KEY_ATT + " ?attKey; \n" +
                    ATT_VAL_ATT + " ?attValue  } " +
                    "}";

    static final String qEventsWithObjects =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { " +
                    "?event a " + OCEL_EVENT_IRI + " . \n" +
                    "?event " + E_CONTAINS_O_ROLE + " ?object  " +
                    "}";

    static final String qEventsWithTimestamps =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { " +
                    "?event a " + OCEL_EVENT_IRI + " . \n" +
                    "?event  " + OCEL_TIMESTAMP_IRI + " ?timestamp  " +
                    "}";

    static final String qEventsWithActivities =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { " +
                    "?event a " + OCEL_EVENT_IRI + " . \n" +
                    "?event  " + OCEL_ACTIVITY_IRI + " ?activity " +
                    "}";

    static final String qObjectWithType =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT distinct * \n" +
                    "WHERE { " +
                    "?object a " + OCEL_OBJECT_IRI + " . \n" +
                    "?object  " + OCEL_OBJECTTYPE_IRI + " ?objectType " +
                    "}";


    public static synchronized OWLOntology getDefaultEventOntology() throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                getDefaultEventLogStream());
    }

    public static synchronized InputStream getDefaultEventLogStream() {
        return OCELConstants.class.getResourceAsStream(OCELConstants.eventOntoPath);
    }
}

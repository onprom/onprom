/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor;

/**
 * This class provides some constants declaration related to the XES Event Ontology
 *
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
class XESConstants {

    //Event Ontology location
    static final String eventOntoPath = "/eo-onprom.owl";
    static final String qTraceAtt_SimpleAnsVarTrace = "trace";
    static final String qTraceAtt_SimpleAnsVarAtt = "att";
    static final String qEvtAtt_SimpleAnsVarEvent = "event";
    static final String qEvtAtt_SimpleAnsVarAtt = "att";
    static final String qAttTypeKeyVal_SimpleAnsVarAtt = "att";
    static final String qAttTypeKeyVal_SimpleAnsVarAttType = "attType";
    static final String qAttTypeKeyVal_SimpleAnsVarAttKey = "attKey";
    static final String qAttTypeKeyVal_SimpleAnsVarAttVal = "attValue";
    //END OF OnProm event ontology vocabularies

    //Some queries over the event ontology for retrieving the required information for generating XES log

    //====================================================================================================
    static final String qTraceEvt_SimpleAnsVarTrace = "trace";
    static final String qTraceEvt_SimpleAnsVarEvent = "event";
    private static final String eventOntoPrefix = "http://onprom.inf.unibz.it/";
    //====================================================================================================
    private static final String ATTRIBUTE_CONCEPT = "<" + eventOntoPrefix + "Attribute" + ">";
    //some vocabularies for role names/object properties
    private static final String T_CONTAINS_E_ROLE = "<" + eventOntoPrefix + "t-contains-e" + ">";
    // PREFIX : <http://www.example.org/>
    // SELECT Distinct ?trace ?event
    // WHERE {
    //     ?trace :TcontainsE ?event .
    // }
    static final String qTraceEvt_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT Distinct ?trace ?event \n" +
                    "WHERE { "
                    + "?trace " + T_CONTAINS_E_ROLE + " ?event . "
                    + "}";
    private static final String T_CONTAINS_A_ROLE = "<" + eventOntoPrefix + "t-has-a" + ">";
    //====================================================================================================
    // Query for retrieving  the information about the association between a trace and its attributes.
    //
    // PREFIX : <http://www.example.org/>
    // SELECT distinct ?trace ?att
    // WHERE {
    //   ?trace :TcontainsA ?att .
    // }
    static final String qTraceAtt_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT Distinct ?trace ?att \n" +
                    "WHERE { "
                    + "?trace " + T_CONTAINS_A_ROLE + " ?att . "
                    + "}";
    private static final String E_CONTAINS_A_ROLE = "<" + eventOntoPrefix + "e-has-a" + ">";
    //====================================================================================================
    //Query for retrieving  the information about the association between an event and its attribute
    //
    // PREFIX : <http://www.example.org/>
    // SELECT Distinct ?event ?att
    // WHERE {
    //   ?event :EcontainsA ?att .
    // }
    static final String qEvtAtt_Simple =
            "PREFIX : <" + eventOntoPrefix + "> \n" +
                    "SELECT ?event ?att \n" +
                    "WHERE { ?event " + E_CONTAINS_A_ROLE + " ?att }";
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
}

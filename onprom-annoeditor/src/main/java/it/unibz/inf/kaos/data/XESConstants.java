/*
 * onprom-annoeditor
 *
 * XESConstants.java
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

import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.IRI;

/**
 * String constants used in query generation
 * <p>
 * Created by T. E. Kalayci on 26-Jun-2017
 */
public class XESConstants {

    public final static String label = "_label";
    public final static Var labelVar = Var.alloc(label);
    public final static String attValue = "_value";
    public final static Var attValueVar = Var.alloc(attValue);
    //TODO should we add ^^xsd:string to the expressions?
    final static String literalExpr = "\"literal\"";
    final static String lifecycleExpr = "\"lifecycle:transition\"";
    final static String timestampTypeExpr = "\"timestamp\"";
    final static String timestampExpr = "\"time:timestamp\"";
    final static String nameExpr = "\"concept:name\"";
    final static String attType = "_type";
    final static Var attTypeVar = Var.alloc(attType);
    final static String attKey = "_key";
    final static Var attKeyVar = Var.alloc(attKey);
    final static IRI traceAttributeURI = IRI.create("http://onprom.inf.unibz.it/t-has-a");
    final static IRI eventAttributeURI = IRI.create("http://onprom.inf.unibz.it/e-has-a");
    final static IRI traceEventURI = IRI.create("http://onprom.inf.unibz.it/t-contains-e");
    final static IRI attTypeURI = IRI.create("http://onprom.inf.unibz.it/attType");
    final static IRI attKeyURI = IRI.create("http://onprom.inf.unibz.it/attKey");
    final static IRI attValueURI = IRI.create("http://onprom.inf.unibz.it/attValue");
    final static String[] attArray = {attType, attKey, attValue};
    final static String[] attTypeArr = {attType};
    final static String[] attKeyArr = {attKey};
    final static String[] attValueArr = {attValue};
    //public final String resourceTypeExpr = "\"org:resource\""; //not used currently

}

/*
 * onprom-annoeditor
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

package it.unibz.inf.kaos.data;

import org.apache.jena.sparql.core.Var;

/**
 * String constants used in query generation
 * <p>
 * Created by T. E. Kalayci on 26-Jun-2017
 */
public class OCELConstants {

    public static final String label = "_label";
    public static final Var labelVar = Var.alloc(label);
    public static final String attValue = "_value";
    public static final Var attValueVar = Var.alloc(attValue);
    //TODO should we add ^^xsd:string to the expressions?
    static final String literalExpr = "\"literal\"";
    static final String lifecycleExpr = "\"lifecycle:transition\"";
    static final String timestampTypeExpr = "\"timestamp\"";
    static final String timestampExpr = "\"time:timestamp\"";
    static final String nameExpr = "\"concept:name\"";
    static final String attType = "_type";
    static final Var attTypeVar = Var.alloc(attType);
    static final String attKey = "_key";
    static final Var attKeyVar = Var.alloc(attKey);
    static final String traceAttributeURI = "http://onprom.inf.unibz.it/e-has-a";
    static final String eventAttributeURI = "http://onprom.inf.unibz.it/o-has-a";
    static final String traceEventURI = "http://onprom.inf.unibz.it/e-contains-o";
    static final String attTypeURI = "http://onprom.inf.unibz.it/attType";
    static final String attKeyURI = "http://onprom.inf.unibz.it/attKey";
    static final String attValueURI = "http://onprom.inf.unibz.it/attValue";
    static final String[] attArray = {attType, attKey, attValue};
    static final String[] attTypeArr = {attType};
    static final String[] attKeyArr = {attKey};
    static final String[] attValueArr = {attValue};
    //public final String resourceTypeExpr = "\"org:resource\""; //not used currently

}

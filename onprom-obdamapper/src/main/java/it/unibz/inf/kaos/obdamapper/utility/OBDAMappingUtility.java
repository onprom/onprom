/*
 * onprom-obdamapper
 *
 * OBDAMappingUtility.java
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

package it.unibz.inf.kaos.obdamapper.utility;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.util.Set;

public class OBDAMappingUtility {

    public static String cleanURI(String str) {
        return str.replaceAll("://", "/").replaceAll(":", "");
    }

    public static boolean isConstant(String answerVar) {
        return !(answerVar.charAt(0) == '{' && answerVar.charAt(answerVar.length() - 1) == '}');
    }

    public static OWLDatatype getDataType(OWLOntology targetOntology, OWLDataProperty dataProperty) throws Exception {

        Set<OWLDataPropertyRangeAxiom> datPropRangeAxioms = targetOntology.getDataPropertyRangeAxioms(dataProperty);
        int numOfDatPropRangeAxioms = datPropRangeAxioms.size();

        if (numOfDatPropRangeAxioms > 1) {

            throw new Exception("Invalid data type");

        } else if (numOfDatPropRangeAxioms == 1) {

            Set<OWLDatatype> datTypeSet = datPropRangeAxioms.iterator().next().getDatatypesInSignature();
            int numOfDatTypeSet = datTypeSet.size();

            if (numOfDatTypeSet > 1) {

                throw new Exception("Invalid data type");

            } else if (numOfDatTypeSet == 1) {

                return datTypeSet.iterator().next();
            }
        }

        return new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());
    }

    public static OWLEntity getOWLTargetEntity(OWLOntology targetOntology, IRI targetIRI) throws Exception {
        Set<OWLEntity> owlTargetEntities = targetOntology.getEntitiesInSignature(targetIRI);
        int numOfOWLEntities = owlTargetEntities.size();

        if (numOfOWLEntities > 1) {
            throw new Exception("ambiguous target entity - too much matching target entity for " + targetIRI.toString());
        } else if (numOfOWLEntities < 1) {
            throw new Exception("unknown target entity (possibly wrong target ontology entity) - no matching target entity for " + targetIRI.toString());
        }

        OWLEntity targetEntity = owlTargetEntities.iterator().next();

        if (targetEntity == null)
            throw new Exception("unknown target entity");

        return targetEntity;
    }
}

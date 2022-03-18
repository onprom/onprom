/*
 * onprom-umleditor
 *
 * DataType.java
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

package it.unibz.inf.onprom.data;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * Supported data types
 * <p>
 * @author T. E. Kalayci
 * 11-Oct-16
 */
public enum DataType {
    //RDF_PLAIN_LITERAL, RDF_XML_LITERAL, OWL_RATIONAL, OWL_REAL,
    //XSD_NORMALIZED_STRING, XSD_TOKEN, XSD_NAME, XSD_NCNAME, XSD_NMTOKEN,
    BOOLEAN(XSDVocabulary.BOOLEAN),
    DECIMAL(XSDVocabulary.DECIMAL),
    INTEGER(XSDVocabulary.INTEGER),
    NON_NEGATIVE_INTEGER(XSDVocabulary.NON_NEGATIVE_INTEGER),
    STRING(XSDVocabulary.STRING),
    HEX_BINARY(XSDVocabulary.HEX_BINARY),
    BASE_64_BINARY(XSDVocabulary.BASE_64_BINARY),
    ANY_URI(XSDVocabulary.ANY_URI),
    DATE(XSDVocabulary.DATE),
    TIME(XSDVocabulary.TIME),
    GYEAR(XSDVocabulary.G_YEAR),
    DATE_TIME(XSDVocabulary.DATE_TIME),
    DATE_TIME_STAMP(XSDVocabulary.DATE_TIME_STAMP),
    FLOAT(XSDVocabulary.FLOAT),
    DOUBLE(XSDVocabulary.DOUBLE),
    RDFS_LITERAL(OWLRDFVocabulary.RDFS_LITERAL);

    private final IRI iri;
    private final String prefixedName;

    DataType(final XSDVocabulary vocabulary) {
        this.prefixedName = vocabulary.getPrefixedName();
        this.iri = vocabulary.getIRI();
    }

    DataType(final OWLRDFVocabulary vocabulary) {
        this.prefixedName = vocabulary.getPrefixedName();
        this.iri = vocabulary.getIRI();
    }

    public static DataType get(String value) {
        for (DataType dataType : values()) {
            if (dataType.prefixedName.equalsIgnoreCase(value)) {
                return dataType;
            }
        }
        return STRING;
    }

    @Override
    public String toString() {
        return name();
    }

    public IRI getIRI() {
        return iri;
    }
}

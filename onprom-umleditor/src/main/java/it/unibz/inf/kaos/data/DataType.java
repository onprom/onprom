/*
 * onprom-umleditor
 *
 * DataType.java
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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supported data types
 * <p>
 * @author T. E. Kalayci
 * 11-Oct-16
 */
public enum DataType {
    //RDF_PLAIN_LITERAL, RDF_XML_LITERAL, RDFS_LITERAL, OWL_RATIONAL, OWL_REAL,
    //XSD_NORMALIZED_STRING, XSD_TOKEN, XSD_NAME, XSD_NCNAME, XSD_NMTOKEN,
    DECIMAL(XSDVocabulary.DECIMAL),
    INTEGER(XSDVocabulary.INTEGER),
    BOOLEAN(XSDVocabulary.BOOLEAN),
    NON_NEGATIVE_INTEGER(XSDVocabulary.NON_NEGATIVE_INTEGER),
    STRING(XSDVocabulary.STRING),
    HEX_BINARY(XSDVocabulary.HEX_BINARY),
    BASE_64_BINARY(XSDVocabulary.BASE_64_BINARY),
    ANY_URI(XSDVocabulary.ANY_URI),
    DATE(XSDVocabulary.DATE),
    TIME(XSDVocabulary.TIME),
    GYEAR(XSDVocabulary.G_YEAR),
    DATE_TIME(XSDVocabulary.DATE_TIME),
    DATE_TIME_STAMP(XSDVocabulary.DATE_TIME_STAMP);

    private static final Logger logger = LoggerFactory.getLogger(DataType.class.getName());

    private final XSDVocabulary vocabulary;

    DataType(final XSDVocabulary _vocabulary) {
        this.vocabulary = _vocabulary;
    }

    public static DataType get(String value) {
        try {
            XSDVocabulary vocabulary = XSDVocabulary.parseShortName(value);
            for (DataType dataType : values()) {
                if (dataType.vocabulary == vocabulary) {
                    return dataType;
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        return STRING;
    }

    @Override
    public String toString() {
        return name();
    }

    public IRI getIRI() {
        return vocabulary.getIRI();
    }
}

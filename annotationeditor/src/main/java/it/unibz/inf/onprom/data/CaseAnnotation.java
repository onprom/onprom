/*
 * annotationeditor
 *
 * CaseAnnotation.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

import com.google.common.collect.Lists;
import it.unibz.inf.onprom.data.query.AnnotationQuery;
import it.unibz.inf.onprom.data.query.BinaryAnnotationQuery;
import it.unibz.inf.onprom.interfaces.AnnotationDiagram;
import it.unibz.inf.onprom.interfaces.AnnotationProperties;
import it.unibz.inf.onprom.io.SimpleQueryExporter;
import it.unibz.inf.onprom.ui.form.CaseForm;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Case annotation class
 * <p>
 *
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(title = "Case", color = "#E5BABA", mnemonic = 'c', tooltip = "Create <u>C</u>ase")
public class CaseAnnotation extends Annotation {
    private static final Logger logger = LoggerFactory.getLogger(CaseAnnotation.class.getName());

    private NavigationalAttribute caseName;

    private CaseAnnotation() {
    }

    public CaseAnnotation(UMLClass _relatedClass) {
        super(_relatedClass);
    }

    public String toString() {
        if (caseName != null) {
            return caseName.toString() + " [" + relatedClass.toString() + "]";
        }
        return super.toString();
    }

    @Override
    public List<AnnotationQuery> getQuery() {
        List<AnnotationQuery> queries = Lists.newLinkedList();
        try {
            //case name attribute query
            SelectBuilder builder = SimpleQueryExporter.getStringAttributeQueryBuilder(getCaseName(), this, null, XESConstants.attValueVar);
            builder.addVar(XESConstants.literalExpr, XESConstants.attTypeVar);
            builder.addVar(XESConstants.nameExpr, XESConstants.attKeyVar);
            String query = builder.toString();

            String[] caseAttributeVariables = concatenate(new String[]{getRelatedClass().getCleanName()}, XESConstants.attArray);

            queries.add(new BinaryAnnotationQuery(query, XESConstants.traceAttributeURI, new String[]{getRelatedClass().getCleanName()}, caseAttributeVariables));
            //attType query
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attTypeURI, caseAttributeVariables, XESConstants.attTypeArr));
            //attKey query
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attKeyURI, caseAttributeVariables, XESConstants.attKeyArr));
            //attValue query
            queries.add(new BinaryAnnotationQuery(query, XESConstants.attValueURI, caseAttributeVariables, XESConstants.attValueArr));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            UIUtility.error(e.getMessage());
        }
        return queries;
    }

    @Override
    public java.util.Optional<JPanel> getForm(AnnotationDiagram panel) {
        return java.util.Optional.of(new CaseForm(panel, this));
    }

    public NavigationalAttribute getCaseName() {
        return caseName;
    }

    public void setCaseName(NavigationalAttribute name) {
        this.caseName = name;
    }


    public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
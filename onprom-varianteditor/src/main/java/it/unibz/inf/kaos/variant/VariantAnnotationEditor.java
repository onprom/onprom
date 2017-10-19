/*
 * onprom-annoeditor
 *
 * AnnotationEditor.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
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
package it.unibz.inf.kaos.variant;

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.factory.AbstractAnnotationFactory;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author T. E. Kalayci on 19-Sep-17.
 */
public class VariantAnnotationEditor extends AnnotationEditor {
    public VariantAnnotationEditor(OWLOntology _ontology, AnnotationEditorListener _listener) {
        super(_ontology, _listener, new VariantAnnotationFactory());
        setTitle("Variant Annotation Editor");
    }

    public static void main(String a[]) {
        new VariantAnnotationEditor(null, null).display();
    }

    @Override
    protected Collection<AnnotationProperties> getAnnotationProperties() {
        return Arrays.asList(
                CaseAnnotation.class.getAnnotation(AnnotationProperties.class),
                EventAnnotation.class.getAnnotation(AnnotationProperties.class)
        );
    }

    static class VariantAnnotationFactory extends AbstractAnnotationFactory {

        @Override
        public Annotation createAnnotation(AnnotationDiagramPanel panel, ActionType currentAction, UMLClass selectedCls) {
            if (currentAction.toString().equals(CaseAnnotation.class.getAnnotation(AnnotationProperties.class).label())) {
                return new CaseAnnotation(selectedCls);
            } else if (currentAction.toString().equals(EventAnnotation.class.getAnnotation(AnnotationProperties.class).label())) {
                return new EventAnnotation("event" + panel.getItemCount(EventAnnotation.class), selectedCls);
            }
            return null;
        }

        @Override
        public boolean checkRemoval(AnnotationDiagramPanel panel, Annotation annotation) {
            return !(annotation instanceof CaseAnnotation) || panel.getItemCount(Annotation.class) < 2 || UIUtility.confirm(AnnotationEditorMessages.CASE_DELETE_CONFIRMATION);
        }

    }

}
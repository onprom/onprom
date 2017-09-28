/*
 * onprom-annoeditor
 *
 * AnnotationFactory.java
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

package it.unibz.inf.kaos.factory;

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

/**
 * Annotation creation factory and additional methods
 * <p>
 *
 * @author T. E. Kalayci on 24-May-2017.
 */
public class DefaultAnnotationFactory implements AnnotationFactory {
    public Annotation createAnnotation(AnnotationDiagramPanel panel, ActionType currentAction, UMLClass selectedCls) {
        CaseAnnotation caseAnnotation = panel.getFirstItem(CaseAnnotation.class);

        if (currentAction.toString().equals(CaseAnnotation.class.getAnnotation(AnnotationProperties.class).label())) {
            if (caseAnnotation == null || UIUtility.confirm(AnnotationEditorMessages.CHANGE_CASE)) {
                return new CaseAnnotation(selectedCls);
            }
        } else if (currentAction.toString().equals(EventAnnotation.class.getAnnotation(AnnotationProperties.class).label())) {
            if (caseAnnotation == null) {
                UIUtility.error(AnnotationEditorMessages.SELECT_CASE);
            } else {
                if (!NavigationUtility.isConnected(selectedCls, caseAnnotation.getRelatedClass(), false)) {
                    UIUtility.error("Event class is not connected to Trace class!");
                } else {
                    return new EventAnnotation(caseAnnotation, selectedCls);
                }
            }
        }
        return null;
    }

    public boolean checkRemoval(AnnotationDiagramPanel panel, Annotation annotation) {
        return !(annotation instanceof CaseAnnotation) || panel.getItemCount(Annotation.class) < 2 || UIUtility.confirm(AnnotationEditorMessages.CASE_DELETE_CONFIRMATION);
    }

}

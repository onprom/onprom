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

import it.unibz.inf.kaos.data.ActionType;
import it.unibz.inf.kaos.data.AnnotationActionType;
import it.unibz.inf.kaos.data.CaseAnnotation;
import it.unibz.inf.kaos.data.EventAnnotation;
import it.unibz.inf.kaos.data.ResourceAnnotation;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorMessages;
import it.unibz.inf.kaos.ui.utility.NavigationUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

/**
 * Annotation creation factory
 * <p>
 * @author T. E. Kalayci on 24-May-2017.
 */
public class AnnotationFactory {
  public static Annotation createAnnotation(ActionType currentAction, UMLClass selectedCls, CaseAnnotation caseAnnotation) {
    if (currentAction == AnnotationActionType.CASE) {
      if (caseAnnotation == null || UIUtility.confirm(AnnotationEditorMessages.CHANGE_CASE)) {
        return new CaseAnnotation(selectedCls);
      }
    } else if (currentAction == AnnotationActionType.EVENT) {
      if (caseAnnotation == null) {
        UIUtility.error(AnnotationEditorMessages.SELECT_CASE);
      } else {
        if (!NavigationUtility.isConnected(selectedCls, caseAnnotation.getRelatedClass(), false)) {
          UIUtility.error("Event class is not connected to Trace class!");
        } else {
          return new EventAnnotation(caseAnnotation, selectedCls);
        }
      }
    } else if (currentAction == AnnotationActionType.RESOURCE) {
      return new ResourceAnnotation(selectedCls);
    }
    return null;
  }

}

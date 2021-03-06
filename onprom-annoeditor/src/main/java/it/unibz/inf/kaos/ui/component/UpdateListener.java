/*
 * onprom-annoeditor
 *
 * UpdateListener.java
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

package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.DataType;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.DiagramShape;

import java.util.Set;

/**
 * This abstract class is used for listening updates using navigation
 * <p>
 * @author T. E. Kalayci on 22/02/17.
 */
@FunctionalInterface
public interface UpdateListener {

    DataType[] TIMESTAMP_TYPES = {DataType.DATE_TIME, DataType.DATE_TIME_STAMP};

    void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute);

    default DataType[] getDataType() {
        return new DataType[]{DataType.STRING};
  }
}

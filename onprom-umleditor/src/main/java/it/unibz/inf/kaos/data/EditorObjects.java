/*
 * onprom-umleditor
 *
 * EditorObjects.java
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

import it.unibz.inf.kaos.interfaces.Diagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.Set;

/**
 * Loader object holding shapes and ontology
 * <p>
 * @author T. E. Kalayci
 * 29-Nov-16
 */
public class EditorObjects {
    private final File file;
    private final OWLOntology ontology;
    private final Set<DiagramShape<? extends Diagram>> shapes;

    public EditorObjects(File _file, OWLOntology _ontology, Set<DiagramShape<? extends Diagram>> _shapes) {
        this.file = _file;
        this.ontology = _ontology;
        this.shapes = _shapes;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public Set<DiagramShape<? extends Diagram>> getShapes() {
        return shapes;
    }

    public File getFile() {
        return file;
    }
}

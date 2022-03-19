/*
 * onprom-umleditor
 *
 * FileType.java
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

import java.util.Arrays;

/**
 * Various file types supported by onprom
 * <p>
 * @author T. E. Kalayci
 * Date: 03-Mar-17
 */
public enum FileType {
    ONTOLOGY("Supported Ontology Files (*.owl, *.ttl, *.rdf, *.xml)", "owl", "ttl", "rdf", "xml"),
    ANNOTATION("Annotated Domain Models (*.ann)", "ann"),
    UML("UML Data Models (*.udm)", "udm"),
    MAPPING("Ontop Mappings (*.obda)", "obda"),
    DS_PROPERTIES("Datasource Properties (*.properties)", "properties"),
    QUERIES("Annotation Queries (*.aqr)", "aqr"),
    JSON("JSON files (*.json)", "json"),
    IMAGE("Supported Image Files (*.png, *.jpg, *.jpeg, *.gif, *.svg)", "png", "jpg", "jpeg", "gif", "svg", "pdf"),
    XLOG("XES Log File (*.xes)", "xes"),
    OCEL("OCEL Log File (*.ocel)", "ocel"),
    OTHER("Unsupported Files");

    private final String[] extensions;
    private final String description;

    FileType(String _description, String... _extensions) {
        this.description = _description;
        this.extensions = _extensions;
    }

    public static FileType which(String extension) {
        return Arrays.stream(FileType.values()).filter(s -> s.contains(extension)).findFirst().orElse(OTHER);
    }

    public String getDescription() {
        return description;
    }

    public boolean contains(String extension) {
        return Arrays.stream(extensions).anyMatch(s -> s.equalsIgnoreCase(extension));
    }

    public String getDefaultExtension() {
        if (extensions != null && extensions.length > 0)
            return extensions[0];
        return "";
    }
}

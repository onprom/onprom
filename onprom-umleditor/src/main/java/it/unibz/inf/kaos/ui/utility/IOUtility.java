/*
 * onprom-umleditor
 *
 * IOUtility.java
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

package it.unibz.inf.kaos.ui.utility;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.inf.kaos.data.EditorObjects;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.owl.OWLImporter;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * Input/Output (File, Export, Import, etc.) related methods
 * <p>
 * @author T. E. Kalayci
 * Date: 24-Nov-16
 */
public class IOUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtility.class.getName());
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        //use all fields
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        //only include not null & non empty fields
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //store type of classess
        OBJECT_MAPPER.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        //ignore unknown properties
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    }

    public static File exportJSON(FileType fileType, Set<DiagramShape> allShapes) {
        File file = UIUtility.selectFileToSave(fileType);
        if (file != null)
            exportJSON(file, allShapes);
        return file;
    }

    public static void exportJSON(FileType fileType, Object object) {
        exportJSON(UIUtility.selectFileToSave(fileType), object);
    }

    public static void exportJSON(File file, Object object) {
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, object);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static <T> T readJSON(java.io.InputStream input, Class<T> cls) {
        try {
            return OBJECT_MAPPER.readValue(input, cls);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T readJSON(java.io.File file, Class<T> cls) {
        try {
            return OBJECT_MAPPER.readValue(file, cls);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static EditorObjects open(InputStream fileStream, FileType fileType) {
        switch (fileType) {
            case ONTOLOGY:
                OWLOntology ontology = OWLImporter.loadOntologyFromStream(fileStream);
                if (ontology != null) {
                    return new EditorObjects(null, ontology, OWLImporter.getShapes(ontology));
                }
                break;
            case ANNOTATION:
            case UML:
            case QUERIES:
            case JSON:
                return new EditorObjects(null, null, importJSON(fileStream));
        }
        return null;
    }

    public static EditorObjects open(File selectedFile, FileType... allowedFileType) {
        if (selectedFile == null) {
            selectedFile = UIUtility.selectFileToOpen(allowedFileType);
        }
        switch (IOUtility.getFileType(selectedFile)) {
            case ONTOLOGY:
                OWLOntology ontology = OWLImporter.loadOntologyFromFile(selectedFile);
                if (ontology != null) {
                    return new EditorObjects(selectedFile, ontology, OWLImporter.getShapes(ontology));
                }
                break;
            case ANNOTATION:
            case UML:
            case QUERIES:
            case JSON:
                return new EditorObjects(selectedFile, null, importJSON(selectedFile));
        }
        return null;
    }

    public static Set<DiagramShape> importJSON(File file) {
        try {
            return OBJECT_MAPPER.readValue(file, getType());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static Set<DiagramShape> importJSON(InputStream stream) {
        try {
            return OBJECT_MAPPER.readValue(stream, getType());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static JavaType getType() {
        return OBJECT_MAPPER.getTypeFactory().constructCollectionType(Set.class, DiagramShape.class);
    }

    public static FileType getFileType(File file) {
        if (file != null) {
            return FileType.which(getFileExtension(file));
        }
        return FileType.OTHER;
    }

    public static String getFileExtension(@Nonnull File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    public static URL getImageURL(String imageName) {
        // Look for the image.
        String imgLocation = "/images/" + imageName + ".png";
        URL imageURL = IOUtility.class.getResource(imgLocation);
        if (imageURL == null) {
            LOGGER.warn("Resource not found: " + imgLocation);
        }
        return imageURL;
    }


}
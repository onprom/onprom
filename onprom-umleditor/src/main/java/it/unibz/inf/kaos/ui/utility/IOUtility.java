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
import com.fasterxml.jackson.databind.ObjectWriter;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.LoadedObjects;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.ui.filter.FileTypeFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Input/Output (File, Export, Import, etc.) related methods
 * <p>
 * @author T. E. Kalayci
 * Date: 24-Nov-16
 */
public class IOUtility {
    private static final Logger logger = LoggerFactory.getLogger(IOUtility.class.getName());
    //file chooser object to use with dialogs
    private final static JFileChooser FILE_CHOOSER = new JFileChooser();
    //Mapper
    private static final ObjectMapper mapper;

    static {
        //initialize JSON-Object mapper
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        //use all fields
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        //only include not null & non empty fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //store type of classess also
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        //ignore unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    }

    public static File exportJSON(FileType fileType, Set<DiagramShape> allShapes) {
        File file = selectFileToSave(fileType);
        if (file != null)
            exportJSON(file, allShapes);
        return file;
    }

    public static void exportJSON(FileType fileType, Object object) {
        exportJSON(selectFileToSave(fileType), object);
    }

    public static void exportJSON(File file, Object object) {
        try {
            getWriter().writeValue(file, object);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static ObjectWriter getWriter() {
        return mapper.writerWithDefaultPrettyPrinter();
    }

    public static void exportJSON(File file, Set<DiagramShape> allShapes) {
        try {
            getWriter().forType(getType()).writeValue(file, allShapes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static <T> T readJSON(java.io.InputStream input, Class<T> cls) {
        try {
            return mapper.readValue(input, cls);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T readJSON(java.io.File file, Class<T> cls) {
        try {
            return mapper.readValue(file, cls);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static LoadedObjects open(File selectedFile, FileType... allowedFileType) {
        if (selectedFile == null) {
            selectedFile = IOUtility.selectFileToOpen(allowedFileType);
        }
        switch (IOUtility.getFileType(selectedFile)) {
            case ONTOLOGY:
                OWLOntology ontology = OWLImporter.loadOntologyFromFile(selectedFile);
                if (ontology != null) {
                    return new LoadedObjects(selectedFile, ontology, OWLImporter.getShapes(ontology));
                }
                break;
            case ANNOTATION:
            case UML:
            case QUERIES:
            case JSON:
                return new LoadedObjects(selectedFile, null, IOUtility.importJSON(selectedFile));
        }
        return null;
    }

    public static Set<DiagramShape> importJSON(File file) {
        try {
            return mapper.readValue(file, getType());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static JavaType getType() {
        return mapper.getTypeFactory().constructCollectionType(Set.class, DiagramShape.class);
    }

    public static FileType getFileType(File file) {
        if (file != null) {
            return FileType.which(FilenameUtils.getExtension(file.getName()));
        }
        return FileType.OTHER;
    }

    public static File selectOntologyFileToSave() {
        return selectFileToSave(FileType.ONTOLOGY);
    }

    public static File[] selectFiles(FileType... allowedFileType) {
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setMultiSelectionEnabled(true);
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(allowedFileType));
        int returnVal = FILE_CHOOSER.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return FILE_CHOOSER.getSelectedFiles();
        }
        return null;
    }

    private static File selectFileToOpen(FileType... allowedFileType) {
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(allowedFileType));
        FILE_CHOOSER.setMultiSelectionEnabled(false);
        int returnVal = FILE_CHOOSER.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return FILE_CHOOSER.getSelectedFile();
        }
        return null;
    }

    public static File selectFileToSave(FileType fileType) {
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(fileType));
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setSelectedFile(new File(""));
        int returnVal = FILE_CHOOSER.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = FILE_CHOOSER.getSelectedFile();
            if (FilenameUtils.getExtension(selectedFile.getName()).isEmpty()) {
                //set default extension if doesn't exist
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + fileType.getDefaultExtension());
            }
            return selectedFile;
        }
        return null;
    }

    public static URL getImageURL(String imageName) {
        // Look for the image.
        String imgLocation = "/images/" + imageName + ".png";
        URL imageURL = IOUtility.class.getResource(imgLocation);
        if (imageURL == null) {
            logger.error("Resource not found: " + imgLocation);
        }
        return imageURL;
    }

    public static SVGGraphics2D getSVGGraphics(Dimension dimension) {
        SVGGraphics2D svgGenerator = new SVGGraphics2D(GenericDOMImplementation.getDOMImplementation().createDocument("http://www.w3.org/2000/svg", "svg", null));
        //set drawing canvas as the drawing area
        svgGenerator.setSVGCanvasSize(dimension);
        svgGenerator.setClip(0, 0, dimension.width, dimension.height);
        return svgGenerator;
    }

}
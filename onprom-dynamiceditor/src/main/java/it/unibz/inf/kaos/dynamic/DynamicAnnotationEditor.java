package it.unibz.inf.kaos.dynamic;

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.AbstractActionType;
import it.unibz.inf.kaos.data.DynamicAnnotation;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.owl.OWLUtility;
import it.unibz.inf.kaos.ui.action.ToolbarAction;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by T. E. Kalayci on 16-Oct-2017.
 */
public class DynamicAnnotationEditor extends AnnotationEditor {
    private static final Logger logger = LoggerFactory.getLogger(DynamicAnnotationEditor.class.getName());
    private static final Map<String, UMLClass> annotations = new HashMap<>();

    public DynamicAnnotationEditor(OWLOntology eventOntology, OWLOntology _ontology, AnnotationEditorListener _listener) {
        super(_ontology, _listener, getAnnotationFactory());
        loadEventOntology(eventOntology);
    }

    private static AnnotationFactory getAnnotationFactory() {
        return (panel, currentAction, selectedCls) -> {
            try {
                UMLClass annotationClass = annotations.get(currentAction.toString());
                if (annotationClass != null) {
                    return new DynamicAnnotation(selectedCls, annotationClass, getAnnotationProperties(annotationClass));
                }
            } catch (RuntimeException e) {
                logger.error(String.format("A runtime exception occurred: %s", e.getMessage()));
            }
            return null;
        };
    }

    public static void main(String[] a) {
        new DynamicAnnotationEditor(null, null, null).display();
    }

    public static AnnotationProperties getAnnotationProperties(final UMLClass annotationClass) {
        return new AnnotationProperties() {
            @Override
            public String color() {
                return '#' + UIUtility.strToHexColor(annotationClass.getName());
            }

            @Override
            public char mnemonic() {
                return annotationClass.getName().charAt(0);
            }

            @Override
            public String tooltip() {
                return String.format("Create %s annotation", annotationClass.getName());
            }

            @Override
            public String title() {
                return annotationClass.getName();
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AnnotationProperties.class;
            }
        };
    }

    private void loadEventOntology(OWLOntology eventOntology) {
        annotations.clear();
        if (eventOntology == null) {
            eventOntology = OWLUtility.loadOntologyFromStream(DynamicAnnotationEditor.class.getResourceAsStream("/default-eo.owl"));
        }
        OWLImporter.getShapes(eventOntology).stream().filter(UMLClass.class::isInstance).map(UMLClass.class::cast).forEach(
                umlClass -> annotations.put(umlClass.getName(), umlClass)
        );
        initUI();
        setTitle("Annotation Editor for " + eventOntology.toString());
    }

    @Override
    protected JToolBar createToolbar() {
        JToolBar toolBar = super.createToolbar();
        toolBar.add(UIUtility.createToolbarButton(new ToolbarAction(new AbstractActionType() {
            @Override
            public String getTooltip() {
                return "Change Event Ontology";
            }

            @Override
            public String getTitle() {
                return "import";
            }
        }) {
            @Override
            public void execute() {
                loadEventOntology(OWLUtility.loadOntologyFromFile(UIUtility.selectFileToOpen(FileType.ONTOLOGY)));
            }
        }), 3);
        return toolBar;
    }

    @Override
    protected Collection<AnnotationProperties> getAnnotationProperties() {
        return annotations.values().stream().map(DynamicAnnotationEditor::getAnnotationProperties).collect(Collectors.toList());
    }

}

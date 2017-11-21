package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.data.EditorObjects;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import java.io.InputStream;

/**
 * @author T. E. Kalayci on 08-Sep-2017.
 */
@Plugin(name = "Annotation", parameterLabels = {"Filename"}, returnLabels = {"Annotation"}, returnTypes = {EditorObjects.class})
@UIImportPlugin(description = "Annotation File", extensions = {"ann"})
public class AnnotationImportPlugin extends AbstractImportPlugin {

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )

    @Override
    protected EditorObjects importFromStream(final PluginContext context, final InputStream input, final String filename,
                                             final long fileSizeInBytes) {
        try {
            context.getFutureResult(0).setLabel("Annotations (" + filename + ") " + UIUtility.getCurrentDateTime());
            return IOUtility.open(input, FileType.ANNOTATION);
        } catch (Exception e) {
            context.log(e);
        }
        return null;
    }
}

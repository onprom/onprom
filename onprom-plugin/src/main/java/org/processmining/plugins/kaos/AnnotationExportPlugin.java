package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.data.EditorObjects;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import java.io.File;

/**
 * @author T. E. Kalayci on 08-Sep-2017.
 */
@Plugin(name = "Export Annotations to a file", parameterLabels = {"Annotations", "File"}, returnLabels = {}, returnTypes = {})
@UIExportPlugin(description = "Annotations", extension = "ann")
public class AnnotationExportPlugin {
    @UITopiaVariant(
            affiliation = "Free University of Bozen-Bolzano",
            author = "onprom team",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0, 1})
    public void export(PluginContext context, EditorObjects objects, File file) {
        try {
            IOUtility.exportJSON(file, objects.getShapes());
            context.log("Exported annotations to the file: " + file.getName());
        } catch (Exception e) {
            context.log("Couldn't export annotations to the file: " + file.getName());
        }
    }
}

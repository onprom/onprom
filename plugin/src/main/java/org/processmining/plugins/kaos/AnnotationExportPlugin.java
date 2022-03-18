/*
 * onprom-plugin
 *
 * AnnotationExportPlugin.java
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

package org.processmining.plugins.kaos;

import it.unibz.inf.onprom.data.EditorObjects;
import it.unibz.inf.onprom.ui.utility.IOUtility;
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

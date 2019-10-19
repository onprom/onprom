/*
 * onprom-plugin
 *
 * AnnotationQueriesImportPlugin.java
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

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import java.io.InputStream;

/**
 * @author T. E. Kalayci
 */
@Plugin(name = "Annotation Queries", parameterLabels = {"Filename"}, returnLabels = {"Imported Annotation Queries"}, returnTypes = {AnnotationQueries.class})
@UIImportPlugin(description = "Annotation Queries File", extensions = {"aqr", "json"})
public class AnnotationQueriesImportPlugin extends AbstractImportPlugin {
    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @Override
    protected AnnotationQueries importFromStream(final PluginContext context, final InputStream input, final String filename,
                                                 final long fileSizeInBytes) {
        try {
            context.getFutureResult(0).setLabel("Queries (" + filename + ") " + UIUtility.getCurrentDateTime());
            return IOUtility.readJSON(input, AnnotationQueries.class).orElse(null);
        } catch (Exception e) {
            context.log(e);
        }
        return null;
    }
}
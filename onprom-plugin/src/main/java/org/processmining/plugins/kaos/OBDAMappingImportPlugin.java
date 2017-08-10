/*
 * onprom-plugin
 *
 * OBDAMappingImportPlugin.java
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

package org.processmining.plugins.kaos;

import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger;

import java.io.InputStream;

@Plugin(name = "OBDA Mapping File", parameterLabels = {"Filename"}, returnLabels = {"OBDA mapping imported from file"}, returnTypes = {OBDAModel.class})
@UIImportPlugin(description = "Mapping (OBDA)", extensions = {"obda"})
public class OBDAMappingImportPlugin extends AbstractImportPlugin {
  @UITopiaVariant(
    author = "onprom team",
    affiliation = "Free University of Bozen-Bolzano",
    email = "onprom@inf.unibz.it",
    website = "http://onprom.inf.unibz.it"
  )
  @Override
  protected OBDAModel importFromStream(final PluginContext context, final InputStream input, final String filename, final long fileSizeInBytes) {
    try {
      OBDAModel obdaModel = OBDADataFactoryImpl.getInstance().getOBDAModel();
      ModelIOManager ioManager = new ModelIOManager(obdaModel);
      ioManager.load(input);
      context.getFutureResult(0).setLabel("OBDA mapping imported from " + filename);
      return obdaModel;
    } catch (Exception e) {
      context.log("Couldn't load OBDA model: " + e.getMessage(), Logger.MessageLevel.ERROR);
    }
    return null;
  }
}
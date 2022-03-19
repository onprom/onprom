/*
 * ocel
 *
 * OcelLifecycleExtension.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.ocel.extension.std;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.info.OcelGlobalAttributeNameMap;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeLiteral;
import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;

import java.net.URI;


public class OcelLifecycleExtension extends OcelExtension {
    public static final URI EXTENSION_URI = URI.create("http://www.ocel-standard.org/");
    public static final String KEY_MODEL = "lifecycle:model";
    public static final String KEY_TRANSITION = "lifecycle:transition";
    public static final String VALUE_MODEL_STANDARD = "standard";
    private static final long serialVersionUID = 7368474477345685085L;
    public static OcelAttributeLiteral ATTR_MODEL;
    public static OcelAttributeLiteral ATTR_TRANSITION;
    private static OcelLifecycleExtension singleton = new OcelLifecycleExtension();

    private OcelLifecycleExtension() {
        super("Lifecycle", "lifecycle", EXTENSION_URI);
        OcelFactory factory = OcelFactoryRegistry.instance().currentDefault();
        ATTR_MODEL = factory.createAttributeLiteral("lifecycle:model", "standard", this);
        ATTR_TRANSITION = factory.createAttributeLiteral("lifecycle:transition", OcelLifecycleExtension.StandardModel.COMPLETE.getEncoding(), this);
        this.logAttributes.add((OcelAttributeLiteral)ATTR_MODEL.clone());
        this.eventAttributes.add((OcelAttributeLiteral)ATTR_TRANSITION.clone());
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "lifecycle:model", "Lifecycle Model");
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "lifecycle:transition", "Lifecycle Transition");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "lifecycle:model", "Lebenszyklus-Model");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "lifecycle:transition", "Lebenszyklus-Transition");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "lifecycle:model", "Modèle du Cycle Vital");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "lifecycle:transition", "Transition en Cycle Vital");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "lifecycle:model", "Modelo de Ciclo de Vida");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "lifecycle:transition", "Transición en Ciclo de Vida");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "lifecycle:model", "Modelo do Ciclo de Vida");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "lifecycle:transition", "Transição do Ciclo de Vida");
    }

    public static OcelLifecycleExtension instance() {
        return singleton;
    }

    private Object readResolve() {
        return singleton;
    }

    public String extractModel(OcelLog log) {
        OcelAttribute attribute = log.getAttributes().get("lifecycle:model");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignModel(OcelLog log, String model) {
        if (model != null && model.trim().length() > 0) {
            OcelAttributeLiteral modelAttr = (OcelAttributeLiteral)ATTR_MODEL.clone();
            modelAttr.setValue(model.trim());
            log.getAttributes().put("lifecycle:model", modelAttr);
        }

    }

    public boolean usesStandardModel(OcelLog log) {
        String model = this.extractModel(log);
        if (model == null) {
            return false;
        } else {
            return model.trim().equals("standard");
        }
    }

    public String extractTransition(OcelEvent event) {
        OcelExtension attribute = (OcelExtension)event.getAttributes().get("lifecycle:transition");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public OcelLifecycleExtension.StandardModel extractStandardTransition(OcelEvent event) {
        String transition = this.extractTransition(event);
        return transition != null ? OcelLifecycleExtension.StandardModel.decode(transition) : null;
    }

    public void assignTransition(OcelEvent event, String transition) {
        if (transition != null && transition.trim().length() > 0) {
            OcelAttributeLiteral transAttr = (OcelAttributeLiteral)ATTR_TRANSITION.clone();
            transAttr.setValue(transition.trim());
            event.getAttributes().put("lifecycle:transition", transAttr);
        }

    }

    public void assignStandardTransition(OcelEvent event, OcelLifecycleExtension.StandardModel transition) {
        this.assignTransition(event, transition.getEncoding());
    }

    public enum StandardModel {
        SCHEDULE("schedule"),
        ASSIGN("assign"),
        WITHDRAW("withdraw"),
        REASSIGN("reassign"),
        START("start"),
        SUSPEND("suspend"),
        RESUME("resume"),
        PI_ABORT("pi_abort"),
        ATE_ABORT("ate_abort"),
        COMPLETE("complete"),
        AUTOSKIP("autoskip"),
        MANUALSKIP("manualskip"),
        UNKNOWN("unknown");

        private final String encoding;

        StandardModel(String encoding) {
            this.encoding = encoding;
        }

        public static OcelLifecycleExtension.StandardModel decode(String encoding) {
            encoding = encoding.trim().toLowerCase();
            OcelLifecycleExtension.StandardModel[] arr$ = values();

            for (StandardModel transition : arr$) {
                if (transition.encoding.equals(encoding)) {
                    return transition;
                }
            }

            return UNKNOWN;
        }

        public String getEncoding() {
            return this.encoding;
        }
    }
}


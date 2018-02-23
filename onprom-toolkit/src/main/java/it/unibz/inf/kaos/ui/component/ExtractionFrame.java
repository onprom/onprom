/*
 * onprom-toolkit
 *
 * ExtractionFrame.java
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

package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.onprom.OnpromToolkit;
import it.unibz.inf.kaos.ui.panel.CustomExtractionPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author T. E. Kalayci on 10-Jul-2017.
 */
public class ExtractionFrame extends JInternalFrame {

    public ExtractionFrame(OnpromToolkit toolkit) {
        super("Log Extraction Diagram", true, true, true, true);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(new JScrollPane(new CustomExtractionPanel(toolkit)), BorderLayout.CENTER);
        this.setSize(new Dimension(1024, 768));
        this.setVisible(true);
    }

}

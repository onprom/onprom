/*
 * onprom-umleditor
 *
 * DiagramEditor.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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

package it.unibz.inf.kaos.ui.interfaces;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import java.io.File;

/**
 * Editor dialog interface to use with actions
 * <p>
 * @author T. E. Kalayci
 * 27-Oct-16
 */
public interface DiagramEditor {
    void open(File selectedFile);

    void export(boolean asFile);

    void close();

    void save();

    void loadForm(@Nonnull JPanel panel);

    void unloadForm();
}
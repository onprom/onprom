/*
 * onprom-toolkit
 *
 * ToolkitMessages.java
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

package it.unibz.inf.kaos.utility;

import it.unibz.inf.kaos.ui.interfaces.Messages;

public enum ToolkitMessages implements Messages {
    ABOUT("About onprom", "<table><tbody><tr><td><p><span style='color:#173155'>on</span><span style='color:#e46c3b'>prom</span>" + VersionUtility.getVersion() + "</p><p>Tool chain is developed under <a href='http://kaos.inf.unibz.it' target='_blank' rel='noopener'>Euregio KAOS</a> project by <a href='http://www.inf.unibz.it/krdb/' target='_blank' rel='noopener'>KRDB research center</a> in <a href='http://www.unibz.it' target='_blank' rel='noopener'>Free </a></p><p><a href='http://www.unibz.it' target='_blank' rel='noopener'>University of Bozen-Bolzano</a>.</p></td><td><h3><a href='http://onprom.inf.unibz.it' target='_blank' rel='noopener'><img src='http://onprom.inf.unibz.it/wp-content/uploads/2017/02/cropped-onprom-4-w300.png' align='right' border='0' /></a></h3></td></tr><tr><td><h3>You can visit project website for more information: <a href='http://onprom.inf.unibz.it' target='_blank' rel='noopener'>http://onprom.inf.unibz.it</a>.</h3><p>The development of the tool suite still in progress, it may contain bugs and/or errors.</p><p>Please <a href='http://onprom.inf.unibz.it/index.php/contact/' target=''>let us know</a> any errors or problems.</p><p>Please consider that this software and examples are distributed on an AS IS BASIS,</p><p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.</p></td><td style='width: 298px; height: 18px;'><a href='http://www.unibz.it' target='_blank' rel='noopener'><img src='http://kaos.inf.unibz.it/wp-content/uploads/2016/10/fub_logo.png' align='right' border='0' /></a></td></tr></tbody></table>\"");

    private final String title;
    private final String message;

    ToolkitMessages(String _title, String _message) {
        this.title = _title;
        this.message = _message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}

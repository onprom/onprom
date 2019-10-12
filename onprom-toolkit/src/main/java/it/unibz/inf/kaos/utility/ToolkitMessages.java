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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.onprom.interfaces.Diagram;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.ui.utility.IOUtility;
import org.junit.Test;

import java.io.File;
import java.util.Set;

public class IOUtilityTest {

    @Test public void test () {
        Set<DiagramShape<? extends Diagram>> shapes = IOUtility.importJSON(new File("src/test/resources/conference.ann"));
        IOUtility.exportYAML(new File("src/test/resources/conference.ann.yml"), shapes);
        System.out.println(shapes);
        
    }
}

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.interfaces.Diagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
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

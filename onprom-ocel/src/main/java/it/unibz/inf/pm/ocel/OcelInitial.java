package it.unibz.inf.pm.ocel;
import it.unibz.inf.pm.ocel.exporter.Exporter;
import it.unibz.inf.pm.ocel.importer.Importer;
import it.unibz.inf.pm.ocel.validation.Validate;
import org.dom4j.DocumentException;

public class OcelInitial {

    private static final String __version__ = "0.0.3.1";
    private static final String __doc__ = "OCEL (object-centric event log) support for Python";
    private static final String __author__ = "PADS";
    private static final String __author_email__ = "a.berti@pads.rwth-aachen.de";
    private static final String __maintainer__ = "PADS";
    private static final String _maintainer_email__ = "a.berti@pads.rwth-aachen.de";


    public static Object import_log(String log_path,String ... parameters) throws Exception {

        if (parameters.length > 0)
        {
            return Importer.apply(log_path,parameters);
        }else {
            return Importer.apply(log_path);
        }
    }

    public static void export_log(Object log, String log_path) throws DocumentException {
        Exporter.apply(log, log_path);
    }

    public static boolean validate(String log_path, String schema_path) throws Exception {
        return Validate.apply(log_path, schema_path,"");
    }

}

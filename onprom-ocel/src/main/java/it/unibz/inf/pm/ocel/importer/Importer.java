package it.unibz.inf.pm.ocel.importer;

public class Importer {
    public static Object apply(String input_path, String ... parameters) throws Exception {
        if (input_path.indexOf(".json") != -1) {
            if (parameters.length > 0) {
                return Oceljson.apply(input_path, parameters);
            } else {
                return Oceljson.apply(input_path);
            }
        } else {
            return Ocelxml.apply(input_path);
        }
    }
}

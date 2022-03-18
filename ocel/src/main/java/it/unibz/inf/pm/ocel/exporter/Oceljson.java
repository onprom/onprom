package it.unibz.inf.pm.ocel.exporter;

import it.unibz.inf.pm.ocel.util.JsonUtil;

public class Oceljson {
    public static void apply(Object log, String output_path, String ... parameters)
    {
        JsonUtil.saveJson(log,output_path);

    }
}

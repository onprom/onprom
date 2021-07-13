package it.unibz.inf.pm.ocel.importer;

import com.alibaba.fastjson.JSONObject;
import it.unibz.inf.pm.ocel.util.JsonUtil;

import java.io.IOException;

public class Oceljson {

//    def apply(input_path, parameters=None):
//            if parameters is None:
//    parameters = {}
//    log_obj = json.load(open(input_path, "rb"))
//            for ek in log_obj["ocel:events"]:
//    eve = log_obj["ocel:events"][ek]
//    eve["ocel:timestamp"] = datetime.fromisoformat(eve["ocel:timestamp"])
//            return log_obj
//
    public static Object apply(String input_path, String ... parameters)
    {
        JSONObject jsonObject = null;
        try {
            jsonObject = JsonUtil.readJsonfileToObject(input_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

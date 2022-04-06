package it.unibz.inf.pm.ocel.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class OcelObject {
    @JSONField(name = "ocel:id")
    private String id;

    @JSONField(name = "ocel:type")
    private String type;

    @JSONField(name = "ocel:ovmap")
    private Map<String, String> ovmap; //map with its child elements having a string value type. not required


}

package it.unibz.inf.pm.ocel.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class OcelObject {
    @JSONField(name = "ocel:activity")
    private String type;

    @JSONField(name = "ocel:type")
    private Object ovmap;
}

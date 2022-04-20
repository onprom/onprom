package it.unibz.inf.pm.ocel.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OcelObject {
    @JSONField(name = "ocel:id")
    private String id;

    @JSONField(name = "ocel:type")
    private String type;

    
//    private Map<String, Object> ovmap; //map with its child elements having a string value type. not required

    public OcelObject(String id) {
        this.id = id;
        this.ovmap = new HashMap<>();
    }

    @Deprecated
    public OcelObject() {
        this.ovmap = new HashMap<>();
    }

    @JSONField(name = "ocel:ovmap")
    private Map<String, OcelAttribute> ovmap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, OcelAttribute> getOvmap() {
        return ovmap;
    }
}

package it.unibz.inf.pm.ocel.entity;

import java.util.Date;

public class OcelAttribute  {
    private long millis;
    private String key;
    private String value;
    private String type;
    private Date date;
    private OcelExtension extension;

    public OcelAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public OcelAttribute (String key, String value, OcelExtension extension)
    {
        this.key = key;
        this.value = value;
        this.extension = extension;
    }

    public OcelAttribute(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public OcelAttribute(String key, Date date, OcelExtension extension) {
        this.key = key;
        this.date = date;
        this.extension = extension;
    }

    public OcelAttribute(String key, long millis, OcelExtension extension) {
        this.key = key;
        this.millis = millis;
        this.extension = extension;
    }


    public String getKey() {
        return key;
    }



}

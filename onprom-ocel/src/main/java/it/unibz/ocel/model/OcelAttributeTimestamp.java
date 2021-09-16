package it.unibz.ocel.model;

import it.unibz.ocel.model.impl.OcelDateTimeFormat;

import java.util.Date;

public interface OcelAttributeTimestamp extends OcelAttribute {
    OcelDateTimeFormat FORMATTER = new OcelDateTimeFormat();

    void setValue(Date var1);

    void setValueMillis(long var1);

    Date getValue();

    long getValueMillis();
}


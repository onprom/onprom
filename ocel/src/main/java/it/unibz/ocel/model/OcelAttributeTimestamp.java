package it.unibz.ocel.model;

import it.unibz.ocel.model.impl.OcelDateTimeFormat;

import java.util.Date;

public interface OcelAttributeTimestamp extends OcelAttribute {
    OcelDateTimeFormat FORMATTER = new OcelDateTimeFormat();

    Date getValue();

    void setValue(Date var1);

    long getValueMillis();

    void setValueMillis(long var1);
}


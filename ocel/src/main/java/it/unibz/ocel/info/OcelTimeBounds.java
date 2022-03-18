package it.unibz.ocel.info;

import java.util.Date;

public interface OcelTimeBounds {
    Date getStartDate();

    Date getEndDate();

    boolean isWithin(Date var1);

    String toString();
}

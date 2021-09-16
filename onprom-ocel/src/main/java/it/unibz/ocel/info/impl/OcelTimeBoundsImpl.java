package it.unibz.ocel.info.impl;


import it.unibz.ocel.extension.std.OcelTimeExtension;
import it.unibz.ocel.info.OcelTimeBounds;
import it.unibz.ocel.model.OcelEvent;

import java.util.Date;

public class OcelTimeBoundsImpl implements OcelTimeBounds {
    protected Date first = null;
    protected Date last = null;

    public OcelTimeBoundsImpl() {
    }

    public Date getStartDate() {
        return this.first;
    }

    public Date getEndDate() {
        return this.last;
    }

    public boolean isWithin(Date date) {
        if (this.first == null) {
            return false;
        } else if (date.equals(this.first)) {
            return true;
        } else if (date.equals(this.last)) {
            return true;
        } else {
            return date.after(this.first) && date.before(this.last);
        }
    }

    public void register(OcelEvent event) {
        Date date = OcelTimeExtension.instance().extractTimestamp(event);
        if (date != null) {
            this.register(date);
        }

    }

    public void register(Date date) {
        if (date != null) {
            if (this.first == null) {
                this.first = date;
                this.last = date;
            } else if (date.before(this.first)) {
                this.first = date;
            } else if (date.after(this.last)) {
                this.last = date;
            }
        }

    }

    public void register(OcelTimeBounds boundary) {
        this.register(boundary.getStartDate());
        this.register(boundary.getEndDate());
    }

    public String toString() {
        return this.first.toString() + " -- " + this.last.toString();
    }
}

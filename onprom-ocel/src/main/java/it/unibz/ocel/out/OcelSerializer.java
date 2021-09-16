package it.unibz.ocel.out;

import it.unibz.ocel.model.OcelLog;

import java.io.IOException;
import java.io.OutputStream;

public interface OcelSerializer {

    String getName();

    String getDescription();

    String getAuthor();

    String[] getSuffices();

    void serialize(OcelLog var1, OutputStream var2) throws IOException;
}

package it.unibz.ocel.out;

import it.unibz.ocel.model.OcelLog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class OcelXmlGZIPSerializer extends OcelXmlSerializer {
    public OcelXmlGZIPSerializer() {
    }

    public String getDescription() {
        return "Ocel XML Compressed Serialization";
    }

    public String getName() {
        return "Ocel XML Compressed";
    }

    public String getAuthor() {
        return "Process and Data Science Group (PADS)";
    }

    public String[] getSuffices() {
        return new String[]{"ocel", "ocel.gz"};
    }

    public void serialize(OcelLog log, OutputStream out) throws IOException {
        GZIPOutputStream gzos = new GZIPOutputStream(out);
        BufferedOutputStream bos = new BufferedOutputStream(gzos);
        super.serialize(log, bos);
        bos.flush();
        gzos.flush();
        bos.close();
        gzos.close();
    }

    public String toString() {
        return this.getName();
    }
}

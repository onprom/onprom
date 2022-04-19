package it.unibz.inf.pm.ocel.exporter;

import it.unibz.inf.pm.ocel.entity.OcelLog;
import org.deckfour.xes.model.XLog;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class OcelXmlGZIPSerializer extends OcelXmlSerializer{

    public String getDescription() {
        return "OCEL XML Compressed Serialization";
    }

    public String getName() {
        return "OCEL XML Compressed";
    }

    public String getAuthor() {
        return "Unibz";
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

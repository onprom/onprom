package it.unibz.inf.pm.ocel.exporter;

import it.unibz.inf.pm.ocel.entity.OcelLog;
import org.dom4j.DocumentException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class OcelXmlGZIPSerializer extends OcelXmlSerializer {

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

    public void serialize(OcelLog log, OutputStream out) throws IOException, DocumentException {
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

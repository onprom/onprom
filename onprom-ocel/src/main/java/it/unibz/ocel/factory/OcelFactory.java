package it.unibz.ocel.factory;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.*;

import java.net.URI;
import java.util.Date;

public interface OcelFactory {
    String getName();

    String getAuthor();

    String getVendor();

    String getDescription();

    URI getUri();

    OcelLog createLog();

    OcelLog createLog(OcelAttributeMap var1);

//    XTrace createTrace();
//
//    XTrace createTrace(XAttributeMap var1);

    OcelEvent createEvent();

    OcelEvent createEvent(OcelAttributeMap var1);

    OcelEvent createEvent(OcelID var1, OcelAttributeMap var2);

    OcelAttributeMap createAttributeMap();

    OcelAttributeBoolean createAttributeBoolean(String var1, boolean var2, OcelExtension var3);

    OcelAttributeContinuous createAttributeContinuous(String var1, double var2, OcelExtension var4);

    OcelAttributeDiscrete createAttributeDiscrete(String var1, long var2, OcelExtension var4);

    OcelAttributeLiteral createAttributeLiteral(String var1, String var2, OcelExtension var3);

    OcelAttributeTimestamp createAttributeTimestamp(String var1, Date var2, OcelExtension var3);

    OcelAttributeTimestamp createAttributeTimestamp(String var1, long var2, OcelExtension var4);

    OcelAttributeID createAttributeID(String var1, OcelID var2, OcelExtension var3);

    OcelAttributeList createAttributeList(String var1, OcelExtension var2);

    OcelAttributeContainer createAttributeContainer(String var1, OcelExtension var2);
}

package it.unibz.inf.kaos.logextractor.util;

import org.deckfour.xes.model.XTrace;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;

public class ToolUtil {
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static Collection<XTrace> sortTrace(Collection<XTrace> collection, String field) {
        collection.forEach(
                trace -> trace.sort(Comparator.comparing(e -> e.getAttributes().get(field)))
        );
        return collection;
    }
}

package it.unibz.ocel.util;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OcelDateTimeConversionJava7 extends OcelDateTimeConversion {
    public static final boolean SUPPORTS_JAVA7_DATE_FORMAT;
    private static final ThreadLocal<SoftReference<DateFormat>> THREAD_LOCAL_DF_WITH_MILLIS;
    private static final ThreadLocal<SoftReference<DateFormat>> THREAD_LOCAL_DF_WITHOUT_MILLIS;

    static {
        boolean biggerEqualJava7 = false;
        String[] splittedVersion = System.getProperty("java.version").split("\\.");
        if (splittedVersion.length > 1) {
            try {
                biggerEqualJava7 = Integer.parseInt(splittedVersion[1]) > 6;
            } catch (NumberFormatException var3) {
                biggerEqualJava7 = false;
            }
        }

        SUPPORTS_JAVA7_DATE_FORMAT = biggerEqualJava7;
        THREAD_LOCAL_DF_WITH_MILLIS = new ThreadLocal();
        THREAD_LOCAL_DF_WITHOUT_MILLIS = new ThreadLocal();
    }

    public OcelDateTimeConversionJava7() {
    }

    private static DateFormat getDateFormatWithMillis() {
        return getThreadLocaleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", THREAD_LOCAL_DF_WITH_MILLIS);
    }

    private static DateFormat getDateFormatWithoutMillis() {
        return getThreadLocaleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", THREAD_LOCAL_DF_WITHOUT_MILLIS);
    }

    private static DateFormat getThreadLocaleDateFormat(String formatString, ThreadLocal<SoftReference<DateFormat>> threadLocal) {
        if (SUPPORTS_JAVA7_DATE_FORMAT) {
            SoftReference<DateFormat> softReference = (SoftReference)threadLocal.get();
            if (softReference != null) {
                DateFormat dateFormat = (DateFormat)softReference.get();
                if (dateFormat != null) {
                    return dateFormat;
                }
            }

            DateFormat result = new SimpleDateFormat(formatString, Locale.US);
            softReference = new SoftReference(result);
            threadLocal.set(softReference);
            return result;
        } else {
            throw new RuntimeException("Error parsing OCEL log. This method should not be called unless running on Java 7!");
        }
    }

    public Date parseXsDateTime(String xsDateTime) {
        if (SUPPORTS_JAVA7_DATE_FORMAT) {
            ParsePosition position = new ParsePosition(0);
            Date parsedDate = getDateFormatWithMillis().parse(xsDateTime, position);
            if (parsedDate == null) {
                position.setIndex(0);
                position.setErrorIndex(0);
                parsedDate = getDateFormatWithoutMillis().parse(xsDateTime, position);
                return parsedDate == null ? super.parseXsDateTime(xsDateTime) : parsedDate;
            } else {
                return parsedDate;
            }
        } else {
            return super.parseXsDateTime(xsDateTime);
        }
    }

    public String format(Date date) {
        return SUPPORTS_JAVA7_DATE_FORMAT ? getDateFormatWithMillis().format(date) : super.format(date);
    }
}


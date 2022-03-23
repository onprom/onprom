package it.unibz.inf.pm.ocel.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtil {
    /**
     * DateFormat From yyyy-MM-dd'T'HH:mm:ss.SSSXXX  (yyyy-MM-dd'T'HH:mm:ss.SSSZ) TO  yyyy-MM-dd HH:mm:ss
     * 2020-04-09T23:00:00.000+08:00 TO 2020-04-09 23:00:00
     * @throws ParseException
     */
    public static String dealDateFormat(String oldDateStr) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  //yyyy-MM-dd'T'HH:mm:ss.SSSZ
        Date date = df.parse(oldDateStr);
        SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.ITALY);
        Date date1 =  df1.parse(date.toString());
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df2.format(date1);
    }

    /**
     * DateFormat From yyyy-MM-dd HH:mm:ss  TO yyyy-MM-dd'T'HH:mm:ss.SSSXXX  (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
     * 2020-04-09 23:00:00 TO 2020-04-09T23:00:00.000+08:00
     * @throws ParseException
     */
    public static String dealDateFormatReverse(String oldDateStr) throws ParseException{
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 =  df.parse(oldDateStr);
        return df.format(date1);
    }
}

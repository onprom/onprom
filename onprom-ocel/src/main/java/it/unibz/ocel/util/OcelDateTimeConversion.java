package it.unibz.ocel.util;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OcelDateTimeConversion {
    protected static final String XSDATETIME_FORMAT_STRING_MILLIS_TZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    protected final SimpleDateFormat dfMillisTZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    protected final Pattern xsDtPattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?(.+)?");
    protected GregorianCalendar cal = new GregorianCalendar();

    public OcelDateTimeConversion() {
    }

    public Date parseXsDateTime(String xsDateTime) {
        try {
            Calendar.getInstance().setLenient(true);
            Calendar cal = DatatypeConverter.parseDateTime(xsDateTime);
            return cal.getTime();
        } catch (IllegalArgumentException var3) {
            return this.parseXsDateTimeUsingPattern(xsDateTime);
        }
    }

    private Date parseXsDateTimeUsingPattern(String xsDateTime) {
        if (xsDateTime.length() >= 6 && xsDateTime.charAt(xsDateTime.length() - 6) == '+' && xsDateTime.charAt(xsDateTime.length() - 3) == ':') {
            String modified = xsDateTime.substring(0, xsDateTime.length() - 3) + xsDateTime.substring(xsDateTime.length() - 2);

            try {
                synchronized(this.dfMillisTZone) {
                    return this.dfMillisTZone.parse(modified);
                }
            } catch (ParseException var12) {
                var12.printStackTrace();
            }
        }

        Matcher matcher = this.xsDtPattern.matcher(xsDateTime);
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2)) - 1;
            int day = Integer.parseInt(matcher.group(3));
            int hour = Integer.parseInt(matcher.group(4));
            int minute = Integer.parseInt(matcher.group(5));
            int second = Integer.parseInt(matcher.group(6));
            int millis = 0;
            if (matcher.group(7) != null) {
                millis = Integer.parseInt(matcher.group(8));
            }

            this.cal.set(year, month, day, hour, minute, second);
            this.cal.set(14, millis);
            String tzString = matcher.group(9);
            if (tzString != null) {
                tzString = "GMT" + tzString.replace(":", "");
                this.cal.setTimeZone(TimeZone.getTimeZone(tzString));
            } else {
                this.cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            }

            return this.cal.getTime();
        } else {
            System.err.println("\"" + xsDateTime + "\" is not a valid representation of a XES timestamp.");
            return null;
        }
    }

    public String format(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return DatatypeConverter.printDateTime(cal);
    }
}


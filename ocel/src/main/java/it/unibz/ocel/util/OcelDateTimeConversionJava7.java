/*
 * ocel
 *
 * OcelDateTimeConversionJava7.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            SoftReference<DateFormat> softReference = threadLocal.get();
            if (softReference != null) {
                DateFormat dateFormat = softReference.get();
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


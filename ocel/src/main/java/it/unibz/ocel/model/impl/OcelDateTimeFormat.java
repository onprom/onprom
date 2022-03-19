/*
 * ocel
 *
 * OcelDateTimeFormat.java
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

package it.unibz.ocel.model.impl;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class OcelDateTimeFormat extends Format {
    private static final long serialVersionUID = 3258131340871479609L;

    public OcelDateTimeFormat() {
    }

    private int parseInt(String pString, int pOffset, StringBuffer pDigits) {
        int length = pString.length();
        pDigits.setLength(0);

        while(pOffset < length) {
            char c = pString.charAt(pOffset);
            if (!Character.isDigit(c)) {
                break;
            }

            pDigits.append(c);
            ++pOffset;
        }

        return pOffset;
    }

    public Date parseObject(String pString) throws ParseException {
        return (Date)super.parseObject(pString);
    }

    public Date parseObject(String pString, ParsePosition pParsePosition) {
        if (pString == null) {
            throw new NullPointerException("The String argument must not be null.");
        } else if (pParsePosition == null) {
            throw new NullPointerException("The ParsePosition argument must not be null.");
        } else {
            int offset = pParsePosition.getIndex();
            int length = pString.length();
            boolean isMinus = false;
            StringBuffer digits = new StringBuffer();
            if (offset < length) {
                char c = pString.charAt(offset);
                if (c == '+') {
                    ++offset;
                } else if (c == '-') {
                    ++offset;
                    isMinus = true;
                }
            }

            offset = this.parseInt(pString, offset, digits);
            if (digits.length() < 4) {
                pParsePosition.setErrorIndex(offset);
                return null;
            } else {
                int year = Integer.parseInt(digits.toString());
                if (offset < length && pString.charAt(offset) == '-') {
                    ++offset;
                    offset = this.parseInt(pString, offset, digits);
                    if (digits.length() != 2) {
                        pParsePosition.setErrorIndex(offset);
                        return null;
                    } else {
                        int month = Integer.parseInt(digits.toString());
                        if (offset < length && pString.charAt(offset) == '-') {
                            ++offset;
                            offset = this.parseInt(pString, offset, digits);
                            if (digits.length() != 2) {
                                pParsePosition.setErrorIndex(offset);
                                return null;
                            } else {
                                int mday = Integer.parseInt(digits.toString());
                                if (offset < length && pString.charAt(offset) == 'T') {
                                    ++offset;
                                    offset = this.parseInt(pString, offset, digits);
                                    if (digits.length() != 2) {
                                        pParsePosition.setErrorIndex(offset);
                                        return null;
                                    } else {
                                        int hour = Integer.parseInt(digits.toString());
                                        if (offset < length && pString.charAt(offset) == ':') {
                                            ++offset;
                                            offset = this.parseInt(pString, offset, digits);
                                            if (digits.length() != 2) {
                                                pParsePosition.setErrorIndex(offset);
                                                return null;
                                            } else {
                                                int minute = Integer.parseInt(digits.toString());
                                                if (offset < length && pString.charAt(offset) == ':') {
                                                    ++offset;
                                                    offset = this.parseInt(pString, offset, digits);
                                                    if (digits.length() != 2) {
                                                        pParsePosition.setErrorIndex(offset);
                                                        return null;
                                                    } else {
                                                        int second = Integer.parseInt(digits.toString());
                                                        int millis;
                                                        if (offset < length && pString.charAt(offset) == '.') {
                                                            ++offset;
                                                            offset = this.parseInt(pString, offset, digits);
                                                            if (digits.length() > 0) {
                                                                millis = Integer.parseInt(digits.toString());
                                                                if (millis > 999) {
                                                                    pParsePosition.setErrorIndex(offset);
                                                                    return null;
                                                                }

                                                                for(int i = digits.length(); i < 3; ++i) {
                                                                    millis *= 10;
                                                                }
                                                            } else {
                                                                millis = 0;
                                                            }
                                                        } else {
                                                            millis = 0;
                                                        }

                                                        digits.setLength(0);
                                                        digits.append("GMT");
                                                        if (offset < length) {
                                                            char c = pString.charAt(offset);
                                                            if (c == 'Z') {
                                                                ++offset;
                                                            } else if (c == '+' || c == '-') {
                                                                digits.append(c);
                                                                ++offset;

                                                                for(int i = 0; i < 5; ++i) {
                                                                    if (offset >= length) {
                                                                        pParsePosition.setErrorIndex(offset);
                                                                        return null;
                                                                    }

                                                                    c = pString.charAt(offset);
                                                                    if ((i == 2 || !Character.isDigit(c)) && (i != 2 || c != ':')) {
                                                                        pParsePosition.setErrorIndex(offset);
                                                                        return null;
                                                                    }

                                                                    digits.append(c);
                                                                    ++offset;
                                                                }
                                                            }
                                                        }

                                                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(digits.toString()));
                                                        cal.set(isMinus ? -year : year, month - 1, mday, hour, minute, second);
                                                        cal.set(14, millis);
                                                        pParsePosition.setIndex(offset);
                                                        return cal.getTime();
                                                    }
                                                } else {
                                                    pParsePosition.setErrorIndex(offset);
                                                    return null;
                                                }
                                            }
                                        } else {
                                            pParsePosition.setErrorIndex(offset);
                                            return null;
                                        }
                                    }
                                } else {
                                    pParsePosition.setErrorIndex(offset);
                                    return null;
                                }
                            }
                        } else {
                            pParsePosition.setErrorIndex(offset);
                            return null;
                        }
                    }
                } else {
                    pParsePosition.setErrorIndex(offset);
                    return null;
                }
            }
        }
    }

    private void append(StringBuffer pBuffer, int pNum, int pMinLen) {
        String s = Integer.toString(pNum);

        pBuffer.append("0".repeat(Math.max(0, pMinLen - s.length())));

        pBuffer.append(s);
    }

    public StringBuffer format(Object pCalendar, StringBuffer pBuffer, FieldPosition pPos) {
        if (pCalendar == null) {
            throw new NullPointerException("The Calendar argument must not be null.");
        } else if (pBuffer == null) {
            throw new NullPointerException("The StringBuffer argument must not be null.");
        } else if (pPos == null) {
            throw new NullPointerException("The FieldPosition argument must not be null.");
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date)pCalendar);
            int year = cal.get(1);
            if (year < 0) {
                pBuffer.append('-');
                year = -year;
            }

            this.append(pBuffer, year, 4);
            pBuffer.append('-');
            this.append(pBuffer, cal.get(2) + 1, 2);
            pBuffer.append('-');
            this.append(pBuffer, cal.get(5), 2);
            pBuffer.append('T');
            this.append(pBuffer, cal.get(11), 2);
            pBuffer.append(':');
            this.append(pBuffer, cal.get(12), 2);
            pBuffer.append(':');
            this.append(pBuffer, cal.get(13), 2);
            int millis = cal.get(14);
            if (millis > 0) {
                pBuffer.append('.');
                this.append(pBuffer, millis, 3);
            }

            TimeZone tz = cal.getTimeZone();
            int offset = cal.get(15);
            if (tz.inDaylightTime(cal.getTime())) {
                offset += cal.get(16);
            }

            if (offset == 0) {
                pBuffer.append('Z');
            } else {
                if (offset < 0) {
                    pBuffer.append('-');
                    offset = -offset;
                } else {
                    pBuffer.append('+');
                }

                int minutes = offset / '\uea60';
                int hours = minutes / 60;
                minutes -= hours * 60;
                this.append(pBuffer, hours, 2);
                pBuffer.append(':');
                this.append(pBuffer, minutes, 2);
            }

            return pBuffer;
        }
    }
}

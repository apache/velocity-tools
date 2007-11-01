package org.apache.velocity.tools;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility methods for parsing or otherwise converting between types.
 *
 * @author Nathan Bubna
 */
public class ConversionUtils
{
    public static final ConversionUtils INSTANCE = new ConversionUtils();

    private static final int STYLE_NUMBER       = 0;
    private static final int STYLE_CURRENCY     = 1;
    private static final int STYLE_PERCENT      = 2;
    //NOTE: '3' belongs to a non-public "scientific" style
    private static final int STYLE_INTEGER      = 4;

    private ConversionUtils() {}

    public ConversionUtils getInstance()
    {
        return INSTANCE;
    }


    /**
     * Returns a {@link NumberFormat} instance for the specified
     * format and {@link Locale}.  If the format specified is a standard
     * style pattern, then a number instance
     * will be returned with the number style set to the
     * specified style.  If it is a custom format, then a customized
     * {@link NumberFormat} will be returned.
     *
     * @param format the custom or standard formatting pattern to be used
     * @param locale the {@link Locale} to be used
     * @return an instance of {@link NumberFormat}
     * @see NumberFormat
     */
    public static NumberFormat getNumberFormat(String format, Locale locale)
    {
        if (format == null || locale == null)
        {
            return null;
        }

        NumberFormat nf = null;
        int style = getNumberStyleAsInt(format);
        if (style < 0)
        {
            // we have a custom format
            nf = new DecimalFormat(format, new DecimalFormatSymbols(locale));
        }
        else
        {
            // we have a standard format
            nf = getNumberFormat(style, locale);
        }
        return nf;
    }

    /**
     * Returns a {@link NumberFormat} instance for the specified
     * number style and {@link Locale}.
     *
     * @param numberStyle the number style (number will be ignored if this is
     *        less than zero or the number style is not recognized)
     * @param locale the {@link Locale} to be used
     * @return an instance of {@link NumberFormat} or <code>null</code>
     *         if an instance cannot be constructed with the given
     *         parameters
     */
    public static NumberFormat getNumberFormat(int numberStyle, Locale locale)
    {
        try
        {
            NumberFormat nf;
            switch (numberStyle)
            {
                case STYLE_NUMBER:
                    nf = NumberFormat.getNumberInstance(locale);
                    break;
                case STYLE_CURRENCY:
                    nf = NumberFormat.getCurrencyInstance(locale);
                    break;
                case STYLE_PERCENT:
                    nf = NumberFormat.getPercentInstance(locale);
                    break;
                case STYLE_INTEGER:
                    nf = NumberFormat.getIntegerInstance(locale);
                    break;
                default:
                    // invalid style was specified, return null
                    nf = null;
            }
            return nf;
        }
        catch (Exception suppressed)
        {
            // let it go...
            return null;
        }
    }

    /**
     * Checks a string to see if it matches one of the standard
     * NumberFormat style patterns:
     *      number, currency, percent, integer, or default.
     * if it does it will return the integer constant for that pattern.
     * if not, it will return -1.
     *
     * @see NumberFormat
     * @param style the string to be checked
     * @return the int identifying the style pattern
     */
    public static int getNumberStyleAsInt(String style)
    {
        // avoid needlessly running through all the string comparisons
        if (style == null || style.length() < 6 || style.length() > 8) {
            return -1;
        }
        if (style.equalsIgnoreCase("default"))
        {
            //NOTE: java.text.NumberFormat returns "number" instances
            //      as the default (at least in Java 1.3 and 1.4).
            return STYLE_NUMBER;
        }
        if (style.equalsIgnoreCase("number"))
        {
            return STYLE_NUMBER;
        }
        if (style.equalsIgnoreCase("currency"))
        {
            return STYLE_CURRENCY;
        }
        if (style.equalsIgnoreCase("percent"))
        {
            return STYLE_PERCENT;
        }
        if (style.equalsIgnoreCase("integer"))
        {
            return STYLE_INTEGER;
        }
        // ok, it's not any of the standard patterns
        return -1;
    }


    // ----------------- number conversion methods ---------------

    /**
     * Attempts to convert an unidentified {@link Object} into a {@link Number},
     * just short of turning it into a string and parsing it.  In other words,
     * this will convert to {@link Number} from a {@link Number}, {@link Calendar},
     * or {@link Date}.  If it can't do that, it will return {@code null}.
     *
     * @param obj - the object to convert
     */
    public static Number toNumber(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Number)
        {
            return (Number)obj;
        }
        if (obj instanceof Date)
        {
            return Long.valueOf(((Date)obj).getTime());
        }
        if (obj instanceof Calendar)
        {
            Date date = ((Calendar)obj).getTime();
            return Long.valueOf(date.getTime());
        }
        return null;
    }

    /**
     * Converts a string to an instance of {@link Number} using the
     * specified format and {@link Locale} to parse it.
     *
     * @param value - the string to convert
     * @param format - the format the number is in
     * @param locale - the {@link Locale}
     * @return the string as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see NumberFormat#parse
     */
    public static Number toNumber(String value, String format, Locale locale)
    {
        if (value == null || format == null || locale == null)
        {
            return null;
        }
        try
        {
            NumberFormat parser = getNumberFormat(format, locale);
            return parser.parse(value);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * specified format and {@link Locale} to parse it, if necessary.
     *
     * @param value - the object to convert
     * @param format - the format the number is in
     * @param locale - the {@link Locale}
     * @return the object as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see NumberFormat#parse
     */
    public static Number toNumber(Object value, String format, Locale locale)
    {
        // first try the easy stuff
        Number number = toNumber(value);
        if (number != null)
        {
            return number;
        }

        // turn it into a string and try parsing it
        return toNumber(String.valueOf(value), format, locale);
    }


    // -------------------------- DateFormat creation methods --------------

    /**
     * Returns a {@link DateFormat} instance for the specified
     * format, {@link Locale}, and {@link TimeZone}.  If the format
     * specified is a standard style pattern, then a date-time instance
     * will be returned with both the date and time styles set to the
     * specified style.  If it is a custom format, then a customized
     * {@link SimpleDateFormat} will be returned.
     *
     * @param format the custom or standard formatting pattern to be used
     * @param locale the {@link Locale} to be used
     * @param timezone the {@link TimeZone} to be used
     * @return an instance of {@link DateFormat}
     * @see SimpleDateFormat
     * @see DateFormat
     */
    public static DateFormat getDateFormat(String format, Locale locale,
                                           TimeZone timezone)
    {
        if (format == null)
        {
            return null;
        }

        DateFormat df = null;
        // do they want a date instance
        if (format.endsWith("_date"))
        {
            String fmt = format.substring(0, format.length() - 5);
            int style = getStyleAsInt(fmt);
            df = getDateFormat(style, -1, locale, timezone);
        }
        // do they want a time instance?
        else if (format.endsWith("_time"))
        {
            String fmt = format.substring(0, format.length() - 5);
            int style = getStyleAsInt(fmt);
            df = getDateFormat(-1, style, locale, timezone);
        }
        // ok, they either want a custom or date-time instance
        else
        {
            int style = getStyleAsInt(format);
            if (style < 0)
            {
                // we have a custom format
                df = new SimpleDateFormat(format, locale);
                df.setTimeZone(timezone);
            }
            else
            {
                // they want a date-time instance
                df = getDateFormat(style, style, locale, timezone);
            }
        }
        return df;
    }

    /**
     * Returns a {@link DateFormat} instance for the specified
     * date style, time style, {@link Locale}, and {@link TimeZone}.
     *
     * @param dateStyle the date style
     * @param timeStyle the time style
     * @param locale the {@link Locale} to be used
     * @param timezone the {@link TimeZone} to be used
     * @return an instance of {@link DateFormat}
     * @see #getDateFormat(int timeStyle, int dateStyle, Locale locale, TimeZone timezone)
     */
    public static DateFormat getDateFormat(String dateStyle, String timeStyle,
                                           Locale locale, TimeZone timezone)
    {
        int ds = getStyleAsInt(dateStyle);
        int ts = getStyleAsInt(timeStyle);
        return getDateFormat(ds, ts, locale, timezone);
    }

    /**
     * Returns a {@link DateFormat} instance for the specified
     * time style, date style, {@link Locale}, and {@link TimeZone}.
     *
     * @param dateStyle the date style (date will be ignored if this is
     *        less than zero and the date style is not)
     * @param timeStyle the time style (time will be ignored if this is
     *        less than zero and the date style is not)
     * @param locale the {@link Locale} to be used
     * @param timezone the {@link TimeZone} to be used
     * @return an instance of {@link DateFormat} or <code>null</code>
     *         if an instance cannot be constructed with the given
     *         parameters
     */
    public static DateFormat getDateFormat(int dateStyle, int timeStyle,
                                           Locale locale, TimeZone timezone)
    {
        try
        {
            DateFormat df;
            if (dateStyle < 0 && timeStyle < 0)
            {
                // no style was specified, use default instance
                df = DateFormat.getInstance();
            }
            else if (timeStyle < 0)
            {
                // only a date style was specified
                df = DateFormat.getDateInstance(dateStyle, locale);
            }
            else if (dateStyle < 0)
            {
                // only a time style was specified
                df = DateFormat.getTimeInstance(timeStyle, locale);
            }
            else
            {
                df = DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                                                    locale);
            }
            df.setTimeZone(timezone);
            return df;
        }
        catch (Exception suppressed)
        {
            // let it go...
            return null;
        }
    }

    /**
     * Checks a string to see if it matches one of the standard DateFormat
     * style patterns: full, long, medium, short, or default.  If it does,
     * it will return the integer constant for that pattern.  If not, it
     * will return -1.
     *
     * @see DateFormat
     * @param style the string to be checked
     * @return the int identifying the style pattern
     */
    public static int getStyleAsInt(String style)
    {
        // avoid needlessly running through all the string comparisons
        if (style == null || style.length() < 4 || style.length() > 7) {
            return -1;
        }
        if (style.equalsIgnoreCase("full"))
        {
            return DateFormat.FULL;
        }
        if (style.equalsIgnoreCase("long"))
        {
            return DateFormat.LONG;
        }
        if (style.equalsIgnoreCase("medium"))
        {
            return DateFormat.MEDIUM;
        }
        if (style.equalsIgnoreCase("short"))
        {
            return DateFormat.SHORT;
        }
        if (style.equalsIgnoreCase("default"))
        {
            return DateFormat.DEFAULT;
        }
        // ok, it's not any of the standard patterns
        return -1;
    }


    // ----------------- date conversion methods ---------------

    /**
     * Attempts to convert an unidentified {@link Object} into a {@link Date},
     * just short of turning it into a string and parsing it.  In other words,
     * this will convert to {@link Date} from a {@link Date}, {@link Calendar},
     * or {@link Number}.  If it can't do that, it will return {@code null}.
     *
     * @param obj - the object to convert
     */
    public static Date toDate(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Date)
        {
            return (Date)obj;
        }
        if (obj instanceof Calendar)
        {
            return ((Calendar)obj).getTime();
        }
        if (obj instanceof Number)
        {
            Date d = new Date();
            d.setTime(((Number)obj).longValue());
            return d;
        }
        return null;
    }

    /**
     * Converts an object to an instance of {@link Date} using the
     * specified format, {@link Locale}, and {@link TimeZone} if the
     * object is not already an instance of Date, Calendar, or Long.
     *
     * @param obj - the date to convert
     * @param format - the format the date is in
     * @param locale - the {@link Locale}
     * @param timezone - the {@link TimeZone}
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see #getDateFormat
     * @see SimpleDateFormat#parse
     */
    public static Date toDate(Object obj, String format,
                              Locale locale, TimeZone timezone)
    {
        // first try the easy stuff
        Date date = toDate(obj);
        if (date != null)
        {
            return date;
        }

        // turn it into a string and try parsing it
        return toDate(String.valueOf(obj), format, locale, timezone);
    }

    /**
     * Converts an object to an instance of {@link Date} using the
     * specified format, {@link Locale}, and {@link TimeZone} if the
     * object is not already an instance of Date, Calendar, or Long.
     *
     * @param str - the string to parse
     * @param format - the format the date is in
     * @param locale - the {@link Locale}
     * @param timezone - the {@link TimeZone}
     * @return the string as a {@link Date} or <code>null</code> if the
     *         parsing fails
     * @see #getDateFormat
     * @see SimpleDateFormat#parse
     */
    public static Date toDate(String str, String format,
                              Locale locale, TimeZone timezone)
    {
        try
        {
            //try parsing w/a customized SimpleDateFormat
            DateFormat parser = getDateFormat(format, locale, timezone);
            return parser.parse(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Calendar toCalendar(Date date, Locale locale)
    {
        if (date == null)
        {
            return null;
        }

        Calendar cal;
        if (locale == null)
        {
            cal = Calendar.getInstance();
        }
        else
        {
            cal = Calendar.getInstance(locale);
        }
        cal.setTime(date);
        // HACK: Force all fields to update. see link for explanation of this.
        //http://java.sun.com/j2se/1.4/docs/api/java/util/Calendar.html
        cal.getTime();
        return cal;
    }

}

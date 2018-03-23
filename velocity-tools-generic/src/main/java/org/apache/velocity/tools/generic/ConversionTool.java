package org.apache.velocity.tools.generic;

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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;

/**
 * <p>Utility class for easy conversion of String values to richer types.</p>
 * <p><pre>
 * Template example(s):
 *   $convert.toNumber('12.6')   ->  12.6
 *   $convert.toInt('12.6')      ->  12
 *   $convert.toNumbers('12.6,42')  ->  [12.6, 42]
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ConversionTool"
 *              dateFormat="yyyy-MM-dd"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This comes in very handy when parsing anything.</p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date: 2007-02-26 11:24:39 -0800 (Mon, 26 Feb 2007) $
 * @since VelocityTools 2.0
 * @deprecated use NumberTool for numbers formatting/parsing, DateTool for date/time formatting/parsing,
 * or CollectionTool for toStrings().
 */

@DefaultKey("convert")
@SkipSetters
@Deprecated
public class ConversionTool extends LocaleConfig implements Serializable
{
    public static final String STRINGS_DELIMITER_FORMAT_KEY = "stringsDelimiter";
    public static final String DATE_FORMAT_KEY = "dateFormat";
    public static final String NUMBER_FORMAT_KEY = "numberFormat";

    public static final String DEFAULT_STRINGS_DELIMITER = ",";
    public static final boolean DEFAULT_STRINGS_TRIM = true;
    public static final String DEFAULT_NUMBER_FORMAT = "default";
    public static final String DEFAULT_DATE_FORMAT = "default";

    private String stringsDelimiter = DEFAULT_STRINGS_DELIMITER;
    private boolean stringsTrim = DEFAULT_STRINGS_TRIM;
    private String numberFormat = DEFAULT_NUMBER_FORMAT;
    private String dateFormat = DEFAULT_DATE_FORMAT;

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);

        String delimiter = values.getString(STRINGS_DELIMITER_FORMAT_KEY);
        if (delimiter != null)
        {
            setStringsDelimiter(delimiter);
        }

        String dateFormat = values.getString(DATE_FORMAT_KEY);
        if (dateFormat != null)
        {
            setDateFormat(dateFormat);
        }

        String numberFormat = values.getString(NUMBER_FORMAT_KEY);
        if (numberFormat != null)
        {
            setNumberFormat(numberFormat);
        }
    }

    /**
     * Sets the delimiter used for separating values in a single String value.
     * The default string delimiter is a comma.
     *
     * @see #parseStringList
     * @deprecated use {@link CollectionTool#setStringsDelimiter(String)}
     */
    protected final void setStringsDelimiter(String stringsDelimiter)
    {
        this.stringsDelimiter = stringsDelimiter;
    }

    public final String getStringsDelimiter()
    {
        return this.stringsDelimiter;
    }

    /**
     * Sets whether strings should be trimmed when separated from
     * a delimited string value.
     * The default is true.
     *
     * @see #parseStringList
     */
    protected final void setStringsTrim(boolean stringsTrim)
    {
        this.stringsTrim = stringsTrim;
    }

    /**
     * @deprecated use {@link CollectionTool#getStringsTrim()}
     * @return strings trim
     */
    public final boolean getStringsTrim()
    {
        return this.stringsTrim;
    }

    protected final void setNumberFormat(String format)
    {
        this.numberFormat = format;
    }

    /**
     * @deprecated use {@link NumberTool} format
     * @return number format
     */
    public final String getNumberFormat()
    {
        return this.numberFormat;
    }

    protected final void setDateFormat(String format)
    {
        this.dateFormat = format;
    }

    /**
     * @deprecated use {@link DateTool#getDateFormat()}
     * @return date format
     */
    public final String getDateFormat()
    {
        return this.dateFormat;
    }

    // ----------------- public parsing methods --------------------------

    /**
     * Converts objects to String in a more Tools-ish way than
     * String.valueOf(Object), especially with nulls, Arrays and Collections.
     * Null returns null, Arrays and Collections return the toString(Object)
     * of their first value, or null if they have no values.
     *
     * @param value the object to be turned into a String
     * @return the string value of the object or null if the value is null
     *         or it is an array or collection whose first value is null
     */
    public String toString(Object value)
    {
        return ConversionUtils.toString(value);
    }

    /**
     * @param value the object to be converted
     * @return a {@link Boolean} object for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     */
    public Boolean toBoolean(Object value)
    {
        if (value instanceof Boolean)
        {
            return (Boolean)value;
        }

        String s = toString(value);
        return (s != null) ? parseBoolean(s) : null;
    }

    /**
     * @param value the object to be converted
     * @return a {@link Integer} for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     */
    public Integer toInteger(Object value)
    {
        if (value == null || value instanceof Integer)
        {
            return (Integer)value;
        }
        Number num = toNumber(value);
        return Integer.valueOf(num.intValue());
    }

    /**
     * @param value the object to be converted
     * @return a {@link Double} for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     */
    public Double toDouble(Object value)
    {
        if (value == null || value instanceof Double)
        {
            return (Double)value;
        }
        Number num = toNumber(value);
        return new Double(num.doubleValue());
    }

    /**
     * @param value the object to be converted
     * @return a {@link Number} for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     */
    public Number toNumber(Object value)
    {
        // don't do string conversion yet
        Number number = ConversionUtils.toNumber(value, false);
        if (number != null)
        {
            return number;
        }

        String s = toString(value);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        return parseNumber(s);
    }

    /**
     * @param value the object to be converted
     * @return a {@link Locale} for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     * @deprecated use {@link DateTool}.toLocale(Object)
     */
    public Locale toLocale(Object value)
    {
        if (value instanceof Locale)
        {
            return (Locale)value;
        }
        String s = toString(value);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        return parseLocale(s);
    }

    /**
     * Converts an object to an instance of {@link Date}, when necessary
     * using the configured date parsing format, the configured default
     * {@link Locale}, and the system's default {@link TimeZone} to parse
     * the string value of the specified object.
     *
     * @param value the date to convert
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @deprecated use {@link DateTool#toDate(Object)}
     */
    public Date toDate(Object value)
    {
        Date d = ConversionUtils.toDate(value);
        if (d != null)
        {
            return d;
        }
        String s = toString(value);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        return parseDate(s);
    }

    /**
     * @param value
     * @return calendar
     * @deprecated use {@link DateTool#toCalendar(Object)}
     */
    public Calendar toCalendar(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Calendar)
        {
            return (Calendar)value;
        }

        Date date = toDate(value);
        if (date == null)
        {
            return null;
        }

        //convert the date to a calendar
        return ConversionUtils.toCalendar(date, getLocale());
    }


    /**
     * @param value the value to be converted
     * @return an array of String objects containing all of the values
     *         derived from the specified array, Collection, or delimited String
     * @deprecated use {@link CollectionTool#split(String)}
     */
    public String[] toStrings(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof String[])
        {
            return (String[])value;
        }

        String[] strings = null;
        if (value instanceof Collection)
        {
            Collection values = (Collection)value;
            if (!values.isEmpty())
            {
                strings = new String[values.size()];
                int index = 0;
                for (Iterator i = values.iterator(); i.hasNext(); )
                {
                    strings[index++] = toString(i.next());
                }
            }
        }
        else if (value.getClass().isArray())
        {
            strings = new String[Array.getLength(value)];
            for (int i=0; i < strings.length; i++)
            {
                strings[i] = toString(Array.get(value, i));
            }
        }
        else
        {
            strings = parseStringList(toString(value));
        }
        return strings;
    }



    /**
     * @param value the value to be converted
     * @return an array of Boolean objects derived from the specified value,
     *         or <code>null</code>.
     */
    public Boolean[] toBooleans(Object value)
    {
        if (value != null && !value.getClass().isArray())
        {
            value = toStrings(value);
        }
        if (value == null)
        {
            return null;
        }

        Boolean[] bools = new Boolean[Array.getLength(value)];
        for (int i=0; i < bools.length; i++)
        {
            bools[i] = toBoolean(Array.get(value, i));
        }
        return bools;
    }

    /**
     * @param values the collection of values to be converted
     * @return an array of Boolean objects derived from the specified values,
     *         or <code>null</code>.
     */
    public Boolean[] toBooleans(Collection values)
    {
        if (values == null || !values.isEmpty())
        {
            return null;
        }
        Boolean[] bools = new Boolean[values.size()];
        int index = 0;
        for (Object val : values)
        {
            bools[index++] = toBoolean(val);
        }
        return bools;
    }


    /**
     * @param value the value to be converted
     * @return an array of Number objects derived from the specified value,
     *         or <code>null</code>.
     */
    public Number[] toNumbers(Object value)
    {
        if (value != null && !value.getClass().isArray())
        {
            value = toStrings(value);
        }
        if (value == null)
        {
            return null;
        }

        Number[] numbers = new Number[Array.getLength(value)];
        for (int i=0; i < numbers.length; i++)
        {
            numbers[i] = toNumber(Array.get(value, i));
        }
        return numbers;
    }

    /**
     * @param values the collection of values to be converted
     * @return an array of Number objects derived from the specified values,
     *         or <code>null</code>.
     */
    public Number[] toNumbers(Collection values)
    {
        if (values == null || !values.isEmpty())
        {
            return null;
        }
        Number[] numbers = new Number[values.size()];
        int index = 0;
        for (Object val : values)
        {
            numbers[index++] = toNumber(val);
        }
        return numbers;
    }

    /**
     * @param value the value to be converted
     * @return an array of int values derived from the specified value,
     *         or <code>null</code>.
     */
    public int[] toInts(Object value)
    {
        Number[] numbers = toNumbers(value);
        if (numbers == null)
        {
            return null;
        }

        int[] ints = new int[numbers.length];
        for (int i=0; i<ints.length; i++)
        {
            if (numbers[i] != null)
            {
                ints[i] = numbers[i].intValue();
            }
        }
        return ints;
    }

    /**
     * @param value the value to be converted
     * @return an array of int values derived from the specified value,
     *         or <code>null</code>.
     */
    public int[] toIntegers(Object value)
    {
      return toInts(value);
    }

    /**
     * @param value the value to be converted
     * @return an array of double values derived from the specified value,
     *         or <code>null</code>.
     */
    public double[] toDoubles(Object value)
    {
        Number[] numbers = toNumbers(value);
        if (numbers == null)
        {
            return null;
        }

        double[] doubles = new double[numbers.length];
        for (int i=0; i<doubles.length; i++)
        {
            if (numbers[i] != null)
            {
                doubles[i] = numbers[i].doubleValue();
            }
        }
        return doubles;
    }

    /**
     * @param value the value to be converted
     * @return an array of Locale objects derived from the specified value,
     *         or <code>null</code>.
     */
    public Locale[] toLocales(Object value)
    {
        if (value != null && !value.getClass().isArray())
        {
            value = toStrings(value);
        }
        if (value == null)
        {
            return null;
        }

        Locale[] locales = new Locale[Array.getLength(value)];
        for (int i=0; i < locales.length; i++)
        {
            locales[i] = toLocale(Array.get(value, i));
        }
        return locales;
    }

    /**
     * @param values the collection of values to be converted
     * @return an array of Locale objects derived from the specified values,
     *         or <code>null</code>.
     */
    public Locale[] toLocales(Collection values)
    {
        if (values == null || !values.isEmpty())
        {
            return null;
        }
        Locale[] locales = new Locale[values.size()];
        int index = 0;
        for (Object val : values)
        {
            locales[index++] = toLocale(val);
        }
        return locales;
    }


    /**
     * @param value the value to be converted
     * @return an array of Date objects derived from the specified value,
     *         or <code>null</code>.
     */
    public Date[] toDates(Object value)
    {
        if (value != null && !value.getClass().isArray())
        {
            value = toStrings(value);
        }
        if (value == null)
        {
            return null;
        }

        Date[] dates = new Date[Array.getLength(value)];
        for (int i=0; i < dates.length; i++)
        {
            dates[i] = toDate(Array.get(value, i));
        }
        return dates;
    }

    /**
     * @param values the collection of values to be converted
     * @return an array of Date objects derived from the specified values,
     *         or <code>null</code>.
     */
    public Date[] toDates(Collection values)
    {
        if (values == null || !values.isEmpty())
        {
            return null;
        }
        Date[] dates = new Date[values.size()];
        int index = 0;
        for (Object val : values)
        {
            dates[index++] = toDate(val);
        }
        return dates;
    }

    /**
     * @param value the value to be converted
     * @return an array of Calendar objects derived from the specified value,
     *         or <code>null</code>.
     */
    public Calendar[] toCalendars(Object value)
    {
        if (value != null && !value.getClass().isArray())
        {
            value = toStrings(value);
        }
        if (value == null)
        {
            return null;
        }

        Calendar[] calendars = new Calendar[Array.getLength(value)];
        for (int i=0; i < calendars.length; i++)
        {
            calendars[i] = toCalendar(Array.get(value, i));
        }
        return calendars;
    }

    /**
     * @param values the collection of values to be converted
     * @return an array of Calendar objects derived from the specified values,
     *         or <code>null</code>.
     */
    public Calendar[] toCalendars(Collection values)
    {
        if (values == null || !values.isEmpty())
        {
            return null;
        }
        Calendar[] calendars = new Calendar[values.size()];
        int index = 0;
        for (Object val : values)
        {
            calendars[index++] = toCalendar(val);
        }
        return calendars;
    }

    /**
     * Convert a singleton or an array as List  
     * @param value Object to be converted
     * @return either the object itself if it's already a list,
     *         the object converted to a list if it's an array,
     *         or a new singleton list.
     */
    public List toList(Object value)
    {
        if (value == null) return null;
        else return ConversionUtils.asList(value);
    }
    

    // --------------------- basic string parsing methods --------------

    /**
     * Converts a parameter value into a {@link Boolean}
     * Sub-classes can override to allow for customized boolean parsing.
     * (e.g. to handle "Yes/No" or "T/F")
     *
     * @param value the string to be parsed
     * @return the value as a {@link Boolean}
     */
    protected Boolean parseBoolean(String value)
    {
        return Boolean.valueOf(value);
    }

    /**
     * Converts a single String value into an array of Strings by splitting
     * it on the tool's set stringsDelimiter.  The default stringsDelimiter is a comma,
     * and by default, all strings parsed out are trimmed before returning.
     */
    protected String[] parseStringList(String value)
    {
        String[] values;
        if (value.indexOf(this.stringsDelimiter) < 0)
        {
            values = new String[] { value };
        }
        else
        {
            values = value.split(this.stringsDelimiter);
        }
        if (this.stringsTrim)
        {
            for (int i=0,l=values.length; i < l; i++)
            {
                values[i] = values[i].trim();
            }
        }
        return values;
    }

    /**
     * Converts a String value into a Locale.
     *
     */
    protected Locale parseLocale(String value)
    {
        return ConversionUtils.toLocale(value);
    }


    // ------------------------- number parsing methods ---------------

    /**
     * Converts an object to an instance of {@link Number} using the
     * format returned by {@link #getNumberFormat()} and the default {@link Locale}
     * if the object is not already an instance of Number.
     *
     * @param value the string to parse
     * @return the string as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @deprecated use {@link NumberTool#toNumber(Object)}
     */
    public Number parseNumber(String value)
    {
        return parseNumber(value, this.numberFormat);
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * specified format and the {@link Locale} returned by
     * {@link #getLocale()}.
     *
     * @param value - the string to parse
     * @param format - the format the number is in
     * @return the string as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see #parseNumber(String value, String format, Object locale)
     * @deprecated use {@link NumberTool#toNumber(String, Object)}
     */
    public Number parseNumber(String value, String format)
    {
        return parseNumber(value, format, getLocale());
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * configured number format and the specified {@link Locale}.
     *
     * @param value - the string to parse
     * @param locale - the Locale to use
     * @return the string as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see java.text.NumberFormat#parse
     * @deprecated use {@link NumberTool#toNumber(String, Object, Locale)}
     */
    public Number parseNumber(String value, Object locale)
    {
        return parseNumber(value, this.numberFormat, locale);
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * specified format and {@link Locale}.
     *
     * @param value - the string to parse
     * @param format - the format the number is in
     * @param locale - the Locale to use
     * @return the string as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see java.text.NumberFormat#parse
     * @deprecated use {@link NumberTool#toNumber(String, Object, Locale)}
     */
    public Number parseNumber(String value, String format, Object locale)
    {
        Locale lcl = toLocale(locale);
        if (lcl == null && locale != null)
        {
            // then they gave a broken locale value; fail, to inform them
            return null;
        }
        return ConversionUtils.toNumber(value, format, lcl);
    }

    // ----------------- date parsing methods ---------------

    /**
     * Converts a string to an instance of {@link Date},
     * using the configured date parsing format, the configured default
     * {@link Locale}, and the system's default {@link TimeZone} to parse it.
     *
     * @param value the date to convert
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @deprecated use {@link DateTool#toDate(Object)}
     */
    public Date parseDate(String value)
    {
        return parseDate(value, this.dateFormat);
    }

    /**
     * Converts a string to an instance of {@link Date} using the
     * specified format,the configured default {@link Locale},
     * and the system's default {@link TimeZone} to parse it.
     *
     * @param value - the date to convert
     * @param format - the format the date is in
     * @return the string as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see ConversionUtils#toDate(String str, String format, Locale locale, TimeZone timezone)
     * @deprecated use {@link DateTool#toDate(String, Object)}
     */
    public Date parseDate(String value, String format)
    {
        return parseDate(value, format, getLocale());
    }

    /**
     * Converts a string to an instance of {@link Date} using the
     * configured date format and specified {@link Locale} to parse it.
     *
     * @param value - the date to convert
     * @param locale - the Locale to use
     * @return the string as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see java.text.SimpleDateFormat#parse
     * @deprecated use {@link DateTool#toDate(String, Object, Locale)}}
     */
    public Date parseDate(String value, Object locale)
    {
        return parseDate(value, this.dateFormat, locale);
    }

    /**
     * Converts a string to an instance of {@link Date} using the
     * specified format and {@link Locale} to parse it.
     *
     * @param value - the date to convert
     * @param format - the format the date is in
     * @param locale - the Locale to use
     * @return the string as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see java.text.SimpleDateFormat#parse
     * @deprecated use {@link DateTool#toDate(String, Object, Locale)}}
     */
    public Date parseDate(String value, String format, Object locale)
    {
        return parseDate(value, format, locale, TimeZone.getDefault());
    }

    /**
     * Converts a string to an instance of {@link Date} using the
     * specified format, {@link Locale}, and {@link TimeZone}.
     *
     * @param value - the date to convert
     * @param format - the format the date is in
     * @param locale - the Locale to use
     * @param timezone - the {@link TimeZone}
     * @return the string as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see #getDateFormat
     * @see java.text.SimpleDateFormat#parse
     * @deprecated use {@link DateTool#toDate(String, Object, Locale, TimeZone)}}
     */
    public Date parseDate(String value, String format,
                          Object locale, TimeZone timezone)
    {
        Locale lcl = toLocale(locale);
        if (lcl == null && locale != null)
        {
            // the 'locale' passed in was broken, so don't pretend it worked
            return null;
        }
        return ConversionUtils.toDate(value, format, lcl, timezone);
    }

}

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
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>Tool for working with {@link Date} and {@link Calendar}
 * in Velocity templates.  It is useful for accessing and
 * formatting the "current" date as well as for formatting
 * arbitrary {@link Date} and {@link Calendar} objects. Also
 * the tool can be used to retrieve {@link DateFormat} instances
 * or make conversions to and from various date types.</p>
 * <p>Possible formats include:
 * <ul>
 *     <li>'short', 'medium', 'long', 'full' (from {@link java.text.DateFormat}, optionally suffixed by '_date' or '_time' to get a date-only or time-only format</li>
 *     <li>'iso' for extended ISO 8601 without time zone (ex: '2016-11-24T10:27:30'), optionally suffixed by '_date' or '_time' to get a date-only or time-only format</li>
 *     <li>'iso_tz', like 'iso' with time zone offset (ex: '2016-11-24T10:27:30+01:00'), optionally suffixed by '_time' to get a time-only format ('10:27:30+01:00')</li>
 *     <li>'intl', like 'iso' but with a space separator between date and time (ex: '2016-11-24 10:27:30'), optionally suffixed by '_date' or '_time' to get a date-only or time-only format</li>
 *     <li>'intl_tz', like 'intl' but with the time zone short id suffixed after another space (ex: '2016-11-24 10:27:30 CET'), optionally suffixed by '_time' to get a time-only format ('10:27:30+01:00')</li>
 *     <li>a custom format, as specified in {@link SimpleDateFormat}</li>
 * </ul></p>
 * <p><pre>
 * Example of formatting the "current" date:
 *  $date                         -> Oct 19, 2003 9:54:50 PM
 *  $date.long                    -> October 19, 2003 9:54:50 PM PDT
 *  $date.medium_time             -> 9:54:50 PM
 *  $date.full_date               -> Sunday, October 19, 2003
 *  $date.get('default','short')  -> Oct 19, 2003 9:54 PM
 *  $date.get('yyyy-M-d H:m:s')   -> 2003-10-19 21:54:50
 *  $date.iso                     -> 2003-10-19T21:54:50-07:00
 *  $date.iso_tz_time             -> 21:54:50-07:00
 *  $date.intl_tz                 -> 2003-10-19 21:54:50 CET
 *
 * Example of formatting an arbitrary date:
 *  $myDate                        -> Tue Oct 07 03:14:50 PDT 2003
 *  $date.format('medium',$myDate) -> Oct 7, 2003 3:14:50 AM
 *
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.DateTool"
 *              format="yyyy-MM-dd"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>Should you need to use several formats, you can either use explicit formats by means of the <code>toDate(format, date)</code> method,
 * or you can declare several date tool instances with different formats.</p>
 *
 * <p>The methods of this tool are highly interconnected, and overriding
 * key methods provides an easy way to create subclasses that use
 * a non-default format, calendar, locale, or timezone.</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 1.0
 * @version $Revision$ $Date$
 */

@DefaultKey("date")
public class DateTool extends FormatConfig implements Serializable
{
    /**
     * The key used for specifying a default timezone via tool configuration.
     */
    public static final String TIMEZONE_KEY = "timezone";

    private TimeZone timezone = TimeZone.getDefault();

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);

        String tzId = values.getString(TIMEZONE_KEY);
        if (tzId != null)
        {
            setTimeZone(TimeZone.getTimeZone(tzId));
        }
    } 

    protected void setTimeZone(TimeZone timezone)
    {
        if (timezone == null)
        {
            throw new NullPointerException("timezone may not be null");
        }
        this.timezone = timezone;
    }

    // ------------------------- system date access ------------------

    /**
     * @return the system's current time as the number of milliseconds
     * elapsed since January 1, 1970, 00:00:00 GMT.
     */
    public static final long getSystemTime()
    {
        return getSystemCalendar().getTime().getTime();
    }

    /**
     * @return the system's current time as a {@link Date}
     */
    public static final Date getSystemDate()
    {
        return getSystemCalendar().getTime();
    }


    /**
     * @return the system's current time as a {@link Calendar}
     */
    public static final Calendar getSystemCalendar()
    {
        return Calendar.getInstance();
    }


    // ------------------------- default parameter access ----------------

    /**
     * Returns the configured {@link TimeZone}. Default value is
     * from {@link TimeZone#getDefault()}.
     *
     * @return the configured {@link TimeZone}
     */
    public TimeZone getTimeZone()
    {
        return timezone;
    }

    /**
     * Returns a {@link Date} derived from the result of {@link #getCalendar}
     *
     * @return a {@link Date} derived from the result of {@link #getCalendar}
     */
    public Date getDate()
    {
        return getCalendar().getTime();
    }

    /**
     * Returns a {@link Calendar} instance created using the timezone and
     * locale returned by getTimeZone() and getLocale().  This allows subclasses
     * to easily override the default locale and timezone used by this tool.
     *
     * <p>Sub-classes may override this method to return a Calendar instance
     * not based on the system date.
     * Doing so will also cause the getDate(), get(String), get(String,String),
     * and toString() methods to return dates equivalent to the Calendar
     * returned by this method, because those methods return values derived
     * from the result of this method.</p>
     *
     * @return a {@link Calendar} instance created using the results of
     *         {@link #getTimeZone()} and {@link #getLocale()}.
     * @see Calendar#getInstance(TimeZone zone, Locale aLocale)
     */
    public Calendar getCalendar()
    {
        return Calendar.getInstance(getTimeZone(), getLocale());
    }

    // ------------------------- date value access ---------------------------

    /**
     * Returns the year value of the date returned by {@link #getCalendar()}.
     *
     * @since VelocityTools 1.2
     */
    public Integer getYear()
    {
        return getYear(getCalendar());
    }

    /**
     * Returns the year value of the specified date.
     *
     * @since VelocityTools 1.2
     */
    public Integer getYear(Object date)
    {
        return getValue(Calendar.YEAR, date);
    }

    /**
     * Returns the month value of the date returned by {@link #getCalendar()}.
     *
     * @since VelocityTools 1.2
     */
    public Integer getMonth()
    {
        return getMonth(getCalendar());
    }

    /**
     * Returns the month value of the specified date.
     *
     * @since VelocityTools 1.2
     */
    public Integer getMonth(Object date)
    {
        return getValue(Calendar.MONTH, date);
    }

    /**
     * Returns the day (of the month) value of the date
     * returned by {@link #getCalendar()}.
     * <br><br>
     * NOTE: Unlike java.util.Date, this returns the day of the month.
     * It is equivalent to Date.getDate() and
     * Calendar.get(Calendar.DAY_OF_MONTH).  We could not call this method
     * getDate() because that already exists in this class with a different
     * function.
     *
     * @since VelocityTools 1.2
     */
    public Integer getDay()
    {
        return getDay(getCalendar());
    }

    /**
     * Returns the day (of the month) value for the specified date.
     * <br><br>
     * NOTE: Unlike java.util.Date, this returns the day of the month.
     * It is equivalent to Date.getDate() and
     * Calendar.get(Calendar.DAY_OF_MONTH).  We could not call this method
     * getDate() because that already exists in this class with a different
     * function.
     *
     * @since VelocityTools 1.2
     */
    public Integer getDay(Object date)
    {
        return getValue(Calendar.DAY_OF_MONTH, date);
    }

    /**
     * Return the specified value of the date returned by
     * {@link #getCalendar()} or null if the field is invalid.
     *
     * @since VelocityTools 1.2
     */
    public Integer getValue(Object field)
    {
        return getValue(field, getCalendar());
    }

    /**
     * Returns the specified value of the specified date,
     * or null if the field or date is invalid.  The field may be
     * an Integer or it may be the name of the field as a String.
     *
     * @param field the corresponding Integer value or String name of the desired value
     * @param date the date/calendar from which the field value will be taken
     * @since VelocityTools 1.2
     */
    public Integer getValue(Object field, Object date)
    {
        if (field == null)
        {
            return null;
        }

        int fieldValue;
        if (field instanceof Integer)
        {
            fieldValue = ((Integer)field).intValue();
        }
        // all the public static field names are upper case
        String fstr = field.toString().toUpperCase();
        try
        {
            Field clsf = Calendar.class.getField(fstr);
            fieldValue = clsf.getInt(Calendar.getInstance());
        }
        catch (Exception e)
        {
            return null;
        }
        return getValue(fieldValue, date);
    }

    /**
     * Returns the specified value of the specified date,
     * or null if the field or date is invalid.
     *
     * @param field the int for the desired field (e.g. Calendar.MONTH)
     * @param date the date/calendar from which the field value will be taken
     * @since VelocityTools 1.2
     */
    public Integer getValue(int field, Object date)
    {
        Calendar cal = toCalendar(date);
        if (cal == null)
        {
            return null;
        }
        return Integer.valueOf(cal.get(field));
    }


    // ------------------------- formatting methods ---------------------------

    /**
     * Returns a formatted string representing the date returned by
     * {@link #getDate()}.  In its default implementation, this method
     * allows you to retrieve the current date in standard formats by
     * simply doing things like <code>$date.medium</code> or
     * <code>$date.full</code>.  If you want only the date or time portion
     * you can specify that along with the standard formats. (e.g.
     * <code>$date.medium_date</code> or <code>$date.short_time</code>)
     * More complex or custom formats can be retrieved
     * by using the full method syntax. (e.g. $date.get('E, MMMM d'))
     *
     * @param format the formatting instructions
     * @return a formatted representation of the date returned by
     *         {@link #getDate()}
     * @see #format(String format, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String get(String format)
    {
        return format(format, getDate());
    }

    /**
     * Returns a formatted string representing the date and/or time given by
     * {@link #getDate()} in standard, localized patterns.
     *
     * @param dateStyle the style pattern for the date
     * @param timeStyle the style pattern for the time
     * @return a formatted representation of the date returned by
     *         {@link #getDate()}
     * @see DateFormat
     * @see #format(String dateStyle, String timeStyle, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String get(String dateStyle, String timeStyle)
    {
        return format(dateStyle, timeStyle, getDate(), getLocale());
    }


    /**
     * Converts the specified object to a date and formats it according to
     * the pattern or style returned by {@link #getFormat()}.
     *
     * @param obj the date object to be formatted
     * @return the specified date formatted as a string
     * @see #format(String format, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String format(Object obj)
    {
        return format(getFormat(), obj);
    }

    /**
     * Converts the specified object to a date and returns
     * a formatted string representing that date in the locale
     * returned by {@link #getLocale()}.
     *
     * @param format the formatting instructions
     * @param obj the date object to be formatted
     * @return a formatted string for this locale representing the specified
     *         date or <code>null</code> if the parameters are invalid
     * @see #format(String format, Object obj, Locale locale, TimeZone timezone)
     */
    public String format(String format, Object obj)
    {
        return format(format, obj, getLocale());
    }

    /**
     * Converts the specified object to a date and returns
     * a formatted string representing that date in the specified
     * {@link Locale}.
     *
     * @param format the formatting instructions
     * @param obj the date object to be formatted
     * @param locale the locale to be used when formatting
     * @return the given date as a formatted string
     * @see #format(String format, Object obj, Locale locale, TimeZone timezone)
     */
    public String format(String format, Object obj, Locale locale)
    {
        return format(format, obj, locale, getTimeZone());
    }

    /**
     * Returns a formatted string representing the specified date,
     * {@link Locale}, and {@link TimeZone}.
     *
     * <p>
     * The specified format may be a standard style pattern ('full', 'long',
     * 'medium', 'short', or 'default') or extended style pattern ('iso', 'iso_tz', 'intl', 'intl_tz').
     * </p>
     * <p>
     * You may also specify that you want only the date or time portion be
     * appending '_date' or '_time' respectively to the standard style pattern.
     * (e.g. 'full_date', 'long_time', 'intl_date')
     * </p>
     * <p>
     * If the format fits neither of these patterns, then the output
     * will be formatted according to the symbols defined by
     * {@link SimpleDateFormat}.
     *
     *   Examples: "E, MMMM d" will result in "Tue, July 24"
     *             "EEE, M-d (H:m)" will result in "Tuesday, 7-24 (14:12)"
     * </pre>
     * </p>
     *
     * @param format the custom or standard pattern to be used
     * @param obj the date to format
     * @param locale the {@link Locale} to format the date for
     * @param timezone the {@link TimeZone} to be used when formatting
     * @return a formatted string representing the specified date or
     *         <code>null</code> if the parameters are invalid
     * @since VelocityTools 1.1
     */
    public String format(String format, Object obj,
                         Locale locale, TimeZone timezone)
    {
        Date date = toDate(obj);
        DateFormat df = getDateFormat(format, locale, timezone);
        if (date == null || df == null)
        {
            return null;
        }
        return df.format(date);
    }


    /**
     * Returns the specified date as a string formatted according to the
     * specified date and/or time styles.
     *
     * @param dateStyle the style pattern for the date
     * @param timeStyle the style pattern for the time
     * @param obj the date to be formatted
     * @return a formatted representation of the given date
     * @see #format(String dateStyle, String timeStyle, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String format(String dateStyle, String timeStyle, Object obj)
    {
        return format(dateStyle, timeStyle, obj, getLocale());
    }

    /**
     * Returns the specified date as a string formatted according to the
     * specified {@link Locale} and date and/or time styles.
     *
     * @param dateStyle the style pattern for the date
     * @param timeStyle the style pattern for the time
     * @param obj the date to be formatted
     * @param locale the {@link Locale} to be used for formatting the date
     * @return a formatted representation of the given date
     * @see #format(String dateStyle, String timeStyle, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String format(String dateStyle, String timeStyle,
                         Object obj, Locale locale)
    {
        return format(dateStyle, timeStyle, obj, locale, getTimeZone());
    }

    /**
     * Returns the specified date as a string formatted according to the
     * specified {@link Locale} and date and/or time styles.
     *
     * @param dateStyle the style pattern for the date
     * @param timeStyle the style pattern for the time
     * @param obj the date to be formatted
     * @param locale the {@link Locale} to be used for formatting the date
     * @param timezone the {@link TimeZone} the date should be formatted for
     * @return a formatted representation of the given date
     * @see java.text.DateFormat
     * @see #format(String dateStyle, String timeStyle, Object obj, Locale locale, TimeZone timezone)
     * @since VelocityTools 1.1
     */
    public String format(String dateStyle, String timeStyle,
                         Object obj, Locale locale, TimeZone timezone)
    {
        Date date = toDate(obj);
        DateFormat df = getDateFormat(dateStyle, timeStyle, locale, timezone);
        if (date == null || df == null)
        {
            return null;
        }
        return df.format(date);
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
     * @since VelocityTools 1.1
     */
    public DateFormat getDateFormat(String format, Locale locale,
                                    TimeZone timezone)
    {
        return ConversionUtils.getDateFormat(format, locale, timezone);
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
     * @see {@link ConversionUtils#getDateFormat(String, Locale, TimeZone)}
     * @since VelocityTools 1.1
     */
    public DateFormat getDateFormat(String dateStyle, String timeStyle,
                                    Locale locale, TimeZone timezone)
    {
        return ConversionUtils.getDateFormat(dateStyle, timeStyle, locale, timezone);
    }

    // ------------------------- date conversion methods ---------------

    /**
     * Converts an object to an instance of {@link Date} using the
     * format returned by {@link #getFormat()},the {@link Locale} returned
     * by {@link #getLocale()}, and the {@link TimeZone} returned by
     * {@link #getTimeZone()} if the object is not already an instance
     * of Date, Calendar, or Long.
     *
     * @param obj the date to convert
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     */
    public Date toDate(Object obj)
    {
        return toDate(getFormat(), obj, getLocale(), getTimeZone());
    }

    /**
     * Converts an object to an instance of {@link Date} using the
     * specified format,the {@link Locale} returned by
     * {@link #getLocale()}, and the {@link TimeZone} returned by
     * {@link #getTimeZone()} if the object is not already an instance
     * of Date, Calendar, or Long.
     *
     * @param format - the format the date is in
     * @param obj - the date to convert
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see #toDate(String format, Object obj, Locale locale)
     */
    public Date toDate(String format, Object obj)
    {
        return toDate(format, obj, getLocale(), getTimeZone());
    }

    /**
     * Converts an object to an instance of {@link Date} using the
     * specified format and {@link Locale} if the object is not already
     * an instance of Date, Calendar, or Long.
     *
     * @param format - the format the date is in
     * @param obj - the date to convert
     * @param locale - the {@link Locale}
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see SimpleDateFormat#parse
     */
    public Date toDate(String format, Object obj, Locale locale)
    {
        return toDate(format, obj, locale, getTimeZone());
    }

    /**
     * Converts an object to an instance of {@link Date} using the
     * specified format, {@link Locale}, and {@link TimeZone} if the
     * object is not already an instance of Date, Calendar, or Long.
     *
     * @param format - the format the date is in
     * @param obj - the date to convert
     * @param locale - the {@link Locale}
     * @param timezone - the {@link TimeZone}
     * @return the object as a {@link Date} or <code>null</code> if no
     *         conversion is possible
     * @see #getDateFormat
     * @see SimpleDateFormat#parse
     */
    public Date toDate(String format, Object obj,
                       Locale locale, TimeZone timezone)
    {
        return ConversionUtils.toDate(obj, format, locale, timezone);
    }

    /**
     * Converts an object to an instance of {@link Calendar} using the
     * locale returned by {@link #getLocale()} if necessary.
     *
     * @param obj the date to convert
     * @return the converted date
     * @see #toCalendar(Object obj, Locale locale)
     */
    public Calendar toCalendar(Object obj)
    {
        return toCalendar(obj, getLocale());
    }

    /**
     * Converts an object to an instance of {@link Calendar} using the
     * locale returned by {@link #getLocale()} if necessary.
     *
     * @param obj the date to convert
     * @param locale the locale used
     * @return the converted date
     * @see #toDate(String format, Object obj, Locale locale)
     * @see Calendar
     */
    public Calendar toCalendar(Object obj, Locale locale)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Calendar)
        {
            return (Calendar)obj;
        }
        //try to get a date out of it
        Date date = toDate(obj);
        if (date == null)
        {
            return null;
        }

        // if the locale is null, do as the javadoc claims
        if (locale == null)
        {
            locale = getLocale();
        }

        //convert the date to a calendar
        return ConversionUtils.toCalendar(date, locale);
    }

    /**
     * Returns a localized date format pattern for the given format.
     *
     * SimpleDateFormat uses patterns that are based upon English
     * words (such as Month = M, Day = d, and Year = y). When displaying
     * a format pattern to readers of other languages, it is appropriate
     * to display these patterns using their localized expectations.
     * For instance, the date pattern yyyy-MM-dd should, for French speakers
     * appear as "aaaa-MM-jj". {@link SimpleDateFormat#toLocalizedPattern}
     * provides this functionality, and this method merely calls
     * that on an appropriately-constructed SimpleDateFormat object.
     *
     * @param format the custom or standard pattern to convert
     * @param locale the {@link Locale} to format for pattern for
     * @return a format string appropriate for the specified Locale
     * @since VelocityTools 2.0
     */
    public String toLocalizedPattern(String format,
                                     Locale locale)
    {
        DateFormat df = getDateFormat(format, locale, getTimeZone());

        // Just in case DateFormat.getInstance doesn't return SimpleDateFormat
        if(df instanceof SimpleDateFormat)
            return ((SimpleDateFormat)df).toLocalizedPattern();
        else
            return null; // Got a better idea?
    }

    // ------------------------- default toString() implementation ------------

    /**
     * @return the result of {@link #getDate()} formatted according to the result
     *         of {@link #getFormat()}.
     * @see #format(String format, Object obj)
     */
    public String toString()
    {
        return format(getFormat(), getDate());
    }


}

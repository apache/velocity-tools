/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.velocity.tools.tools;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import org.apache.velocity.tools.view.tools.ThreadSafeViewTool;


/**
 * <p>Tool for manipulating {@link Date} and {@link Calendar}
 * objects in Velocity templates.</p> 
 * 
 * <p>The tool is tread-safe and implements interface 
 * ThreadSafeViewTool. This allows a compatible toolbox 
 * manager like {@link org.apache.velocity.tools.view.servlet.ServletToolboxManager}
 * to automatically load the tool into the context and reuse
 * the same instance for the entire runtime.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.3 $
 */

public class DateTool implements ThreadSafeViewTool
{


    /**
     * Default constructor.
     */
    public DateTool()
    {}


    /**
     * Returns a Date object. The default Locale is used.
     *
     * @return a <code>java.util.Date</code> object representing the time at 
     *    which this method was invoked
     */
    public static Date getDate()
    {
        return new Date();
    }


    /**
     * Returns a Date object representing the time at which this method was
     * invoked in the specified locale.
     *
     * @param locale the {@link java.util.Locale} to use to generate the Date
     *
     * @return a {@link java.util.Date} object representing the time at which 
     *     this method was invoked in the specified locale
     */
    public static Date getDate(Locale locale)
    {
        return getCalendar(locale).getTime();
    }


    /**
     * Returns a Calendar object representing the time at which this method was
     * invoked and in the default Locale.
     *
     * @return a {@link java.util.Calendar} object representing the time at which 
     *     this method was invoked using the default Locale
     */
    public static Calendar getCalendar()
    {
        return Calendar.getInstance();
    }


    /**
     * Returns a Calendar object representing the time at which this method was
     * invoked and in the specified locale.
     *
     * @param locale the {@link java.util.Locale} to use to create a Calendar
     * @return a {@link java.util.Calendar} object representing the time at which 
     *     this method was invoked and in the specified locale
     */
    public static Calendar getCalendar(Locale locale)
    {
        return Calendar.getInstance(locale);
    }


    /**
     * Returns a formatted string representing the specified date
     * in the default locale.
     *
     * <p>
     * This method uses the same formatting instructions as 
     * {@link SimpleDateFormat}:
     * <pre>
     *   Symbol   Meaning                 Presentation        Example
     *   ------   -------                 ------------        -------
     *   G        era designator          (Text)              AD
     *   y        year                    (Number)            1996
     *   M        month in year           (Text & Number)     July & 07
     *   d        day in month            (Number)            10
     *   h        hour in am/pm (1~12)    (Number)            12
     *   H        hour in day (0~23)      (Number)            0
     *   m        minute in hour          (Number)            30
     *   s        second in minute        (Number)            55
     *   S        millisecond             (Number)            978
     *   E        day in week             (Text)              Tuesday
     *   D        day in year             (Number)            189
     *   F        day of week in month    (Number)            2 (2nd Wed in July)
     *   w        week in year            (Number)            27
     *   W        week in month           (Number)            2
     *   a        am/pm marker            (Text)              PM
     *   k        hour in day (1~24)      (Number)            24
     *   K        hour in am/pm (0~11)    (Number)            0
     *   z        time zone               (Text)              Pacific Standard Time
     *   '        escape for text         (Delimiter)
     *   ''       single quote            (Literal)           '     
     *
     *   Examples: "E, MMMM d" will result in "Tue, July 24"
     *             "EEE, M-d (H:m)" will result in "Tuesday, 7-24 (14:12)"
     * </pre>
     *
     * @param format the formatting instructions
     * @param obj the date to format
     *
     * @return a formatted string representing the specified date or
     *     <code>null</code> if the parameters are invalid
     */
    public static String format(String format, Object obj)
    {
        Date date = toDate(obj);
        if (date == null || format == null)
        {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }


    /**
     * Returns a formatted string representing the specified date and locale.
     *
     * This method uses the same formatting instructions as 
     * {@link SimpleDateFormat}:
     *
     * @param format the formatting instructions
     * @param obj the date to format
     * @param locale the {@link java.util.Locale} to format the date for
     *
     * @return a formatted string representing the specified date or
     *         <code>null</code> if the parameters are invalid
     * @see #format
     */
    public static String format(String format, Object obj, Locale locale)
    {
        Date date = toDate(obj);
        if (date == null || format == null || locale == null)
        {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
        return formatter.format(date);
    }



    /**
     * Returns a Date object representing the specified date.
     *
     * @param obj the date to convert
     *
     * @return the converted date
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
        try
        {
            //try treating obj as a string and parsing it
            DateFormat format = DateFormat.getInstance();
            return format.parse(String.valueOf(obj));
        }
        catch (Exception e)
        {
            return null;
        }
    }


    /**
     * Returns a Calendar object representing the specified date.
     *
     * @param obj the date to convert
     *
     * @return the converted date
     */
    public static Calendar toCalendar(Object obj)
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
        
        //convert the date to a calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //Force fields to update.
        cal.getTime();
        return cal;
    }


}


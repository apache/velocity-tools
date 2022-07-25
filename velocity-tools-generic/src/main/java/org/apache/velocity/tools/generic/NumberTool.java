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
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>Tool for working with {@link Number} in Velocity templates.
 * It is useful for accessing and
 * formatting arbitrary {@link Number} objects. Also
 * the tool can be used to retrieve {@link NumberFormat} instances
 * or make conversions to and from various number types.</p>
 * <pre>
 * Example uses:
 *  $myNumber                   -gt; 13.55
 *  $number.format($myNumber)   -gt; 13.6
 *  $number.currency($myNumber) -gt; $13.55
 *  $number.integer($myNumber)  -gt; 13
 *
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.MathTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>This tool is entirely threadsafe, and has no instance members.
 * It may be used in any scope (request, session, or application).
 * As such, the methods are highly interconnected, and overriding
 * key methods provides an easy way to create subclasses that use
 * a non-default format or locale.</p>
 *
 * @author Nathan Bubna
 * @author <a href="mailto:mkienenb@alaska.net">Mike Kienenberger</a>
 * @since VelocityTools 1.2
 * @version $Id$
 */

@DefaultKey("number")
public class NumberTool extends FormatConfig implements Serializable
{
    private static final long serialVersionUID = -4336524405835332577L;

    // ------------------------- formatting methods ---------------------------

    /**
     * Converts the specified object to a number and formats it according to
     * the pattern or style returned by {@link #getFormat()}.
     *
     * @param obj the number object to be formatted
     * @return the specified number formatted as a string
     * @see #format(String format, Object obj, Locale locale)
     */
    public String format(Object obj)
    {
        return format(getFormat(), obj);
    }

    /**
     * Convenience method equivalent to $number.format("currency", $foo).
     * @param obj the number object to be formatted
     * @return the specified number formatted as a currency
     * @since VelocityTools 1.3
     */
    public String currency(Object obj)
    {
        return format("currency", obj);
    }

    /**
     * Convenience method equivalent to $number.format("integer", $foo).
     * @param obj the number object to be formatted
     * @return the specified number formatted as an integer
     * @since VelocityTools 1.3
     */
    public String integer(Object obj)
    {
        return format("integer", obj);
    }

    /**
     * Convenience method equivalent to $number.format("number", $foo).
     * @param obj the number object to be formatted
     * @return the specified number formatted as a number
     * @since VelocityTools 1.3
     */
    public String number(Object obj)
    {
        return format("number", obj);
    }

    /**
     * Convenience method equivalent to $number.format("percent", $foo).
     * @param obj the number object to be formatted
     * @return the specified number formatted as a percentage
     * @since VelocityTools 1.3
     */
    public String percent(Object obj)
    {
        return format("percent", obj);
    }

    /**
     * Converts the specified object to a number and returns
     * a formatted string representing that number in the locale
     * returned by {@link #getLocale()}.
     *
     * @param format the formatting instructions
     * @param obj the number object to be formatted
     * @return a formatted string for this locale representing the specified
     *         number or <code>null</code> if the parameters are invalid
     * @see #format(String format, Object obj, Locale locale)
     */
    public String format(String format, Object obj)
    {
        return format(format, obj, getLocale());
    }

    /**
     * Converts the specified object to a number and returns
     * a formatted string representing that number in the specified
     * {@link Locale}.
     *
     * @param format the custom or standard pattern to be used
     * @param obj the number object to be formatted
     * @param locale the {@link Locale} to be used when formatting
     * @return a formatted string representing the specified number or
     *         <code>null</code> if the parameters are invalid
     */
    public String format(String format, Object obj, Locale locale)
    {
        Number number = toNumber(obj);
        NumberFormat nf = getNumberFormat(format, locale);
        if (number == null || nf == null)
        {
            return null;
        }
        return nf.format(number);
    }

    // -------------------------- NumberFormat creation methods --------------

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
    public NumberFormat getNumberFormat(String format, Locale locale)
    {
        return ConversionUtils.getNumberFormat(format, locale);
    }

    // ------------------------- number conversion methods ---------------

    /**
     * Converts an object to an instance of {@link Number} using the
     * format returned by {@link #getFormat()} and the {@link Locale}
     * returned by {@link #getLocale()} if the object is not already
     * an instance of Number.
     *
     * @param obj the number to convert
     * @return the object as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     */
    public Number toNumber(Object obj)
    {
        return toNumber(getFormat(), obj, getLocale());
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * specified format and the {@link Locale} returned by
     * {@link #getLocale()} if the object is not already an instance
     * of Number.
     *
     * @param format - the format the number is in
     * @param obj - the number to convert
     * @return the object as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see #toNumber(String format, Object obj, Locale locale)
     */
    public Number toNumber(String format, Object obj)
    {
        return toNumber(format, obj, getLocale());
    }

    /**
     * Converts an object to an instance of {@link Number} using the
     * specified format and {@link Locale} if the object is not already
     * an instance of Number.
     *
     * @param format - the format the number is in
     * @param obj - the number to convert
     * @param locale - the {@link Locale}
     * @return the object as a {@link Number} or <code>null</code> if no
     *         conversion is possible
     * @see NumberFormat#parse
     */
    public Number toNumber(String format, Object obj, Locale locale)
    {
        return ConversionUtils.toNumber(obj, format, locale);
    }

    /**
     * Converts an object to an instance of {@link Boolean} if the object
     * is not already an instance of Boolean.
     *
     * @param value the object to be converted
     * @return a {@link Boolean} object for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     */
    public Boolean toBoolean(Object value)
    {
        return ConversionUtils.toBoolean(value);
    }
}

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Tool for working with {@link Number} in Velocity templates.
 * It is useful for accessing and
 * formatting arbitrary {@link Number} objects. Also
 * the tool can be used to retrieve {@link NumberFormat} instances
 * or make conversions to and from various number types.
 * <p><pre>
 * Example uses:
 *  $myNumber                            -> 13.55
 *  $number.format($myNumber)   -> 13.6
 *  $number.currency($myNumber) -> $13.55
 *  $number.integer($myNumber)  -> 13
 *
 * Example toolbox.xml config (if you want to use this with VelocityView):
 * &lt;tool&gt;
 *   &lt;key&gt;number&lt;/key&gt;
 *   &lt;scope&gt;application&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.generic.NumberTool&lt;/class&gt;
 *   &lt;parameter name="format" value="#0.0"/&gt;
 * &lt;/tool&gt;
 * </pre></p>
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
public class NumberTool
{
    /**
     * The default format to be used when none is specified.
     */
    public static final String DEFAULT_FORMAT = "default";

    /**
     * The key used for specifying a default format via toolbox params.
     * @since VelocityTools 1.4
     */
    public static final String DEFAULT_FORMAT_KEY = "format";

    /**
     * The key used for specifying a default locale via toolbox params.
     * @since VelocityTools 1.4
     */
    public static final String DEFAULT_LOCALE_KEY = "locale";

    /**
     * The key used for specifying whether or not to prevent templates
     * from reconfiguring this tool.  The default is true.
     * @since VelocityTools 1.4
     */
    public static final String LOCK_CONFIG_KEY = "lock-config";

    private static final int STYLE_NUMBER       = 0;
    private static final int STYLE_CURRENCY     = 1;
    private static final int STYLE_PERCENT      = 2;
    //NOTE: '3' belongs to a non-public "scientific" style
    private static final int STYLE_INTEGER      = 4;

    private String format = DEFAULT_FORMAT;
    private Locale locale = Locale.getDefault();
    private boolean configLocked = false;

    /**
     * Looks for configuration values in the given params.
     * @since VelocityTools 1.4
     */
    public void configure(Map params)
    {
        if (!configLocked)
        {
            ValueParser values = new ValueParser(params);
            configure(values);

            // by default, lock down this method after use
            // to prevent templates from re-configuring this instance
            configLocked = values.getBoolean(LOCK_CONFIG_KEY, true);
        }
    }

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     * @since VelocityTools 1.4
     */
    protected void configure(ValueParser values)
    {
        String format = values.getString(DEFAULT_FORMAT_KEY);
        if (format != null)
        {
            setFormat(format);
        }
        Locale locale = values.getLocale(DEFAULT_LOCALE_KEY);
        if (locale != null)
        {
            setLocale(locale);
        }
    }

    // ------------------------- default parameter access ----------------

    /**
     * This implementation returns the configured default locale. Subclasses
     * may override this to return alternate locales. Please note that
     * doing so will affect all formatting methods where no locale is
     * specified in the parameters.
     *
     * @return the default {@link Locale}
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Sets the default locale for this instance. This is protected,
     * because templates ought not to be using it; that would not
     * be threadsafe so far as templates are concerned.
     *
     * @since VelocityTools 1.4
     */
    protected void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Return the pattern or style to be used for formatting numbers when none
     * is specified. This implementation gives a 'default' number format.
     * Subclasses may override this to provide a different default format.
     */
    public String getFormat()
    {
        return this.format;
    }

    /**
     * Sets the default format for this instance. This is protected,
     * because templates ought not to be using it; that would not
     * be threadsafe so far as templates are concerned.
     *
     * @since VelocityTools 1.4
     */
    protected void setFormat(String format)
    {
        this.format = format;
    }


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
     * @since VelocityTools 1.3
     */
    public String currency(Object obj)
    {
        return format("currency", obj);
    }

    /**
     * Convenience method equivalent to $number.format("integer", $foo).
     * @since VelocityTools 1.3
     */
    public String integer(Object obj)
    {
        return format("integer", obj);
    }

    /**
     * Convenience method equivalent to $number.format("number", $foo).
     * @since VelocityTools 1.3
     */
    public String number(Object obj)
    {
        return format("number", obj);
    }

    /**
     * Convenience method equivalent to $number.format("percent", $foo).
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
        if (format == null)
        {
            return null;
        }

        NumberFormat nf = null;
        int style = getStyleAsInt(format);
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
    protected NumberFormat getNumberFormat(int numberStyle, Locale locale)
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
                    nf = getIntegerInstance(locale);
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
     * Since we wish to continue supporting Java 1.3,
     * for the present we cannot use Java 1.4's
     * NumberFormat.getIntegerInstance(Locale) method.
     * This method mimics that method (at least as of JDK1.4.2_01).
     * It is private so that it can be removed later
     * without a deprecation period.
     */
    private NumberFormat getIntegerInstance(Locale locale)
    {
        DecimalFormat format =
            (DecimalFormat)NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(0);
        format.setDecimalSeparatorAlwaysShown(false);
        format.setParseIntegerOnly(true);
        return format;
    }

    /**
     * Checks a string to see if it matches one of the standard
     * NumberFormat style patterns:
     *      NUMBER, CURRENCY, PERCENT, INTEGER, or DEFAULT.
     * if it does it will return the integer constant for that pattern.
     * if not, it will return -1.
     *
     * @see NumberFormat
     * @param style the string to be checked
     * @return the int identifying the style pattern
     */
    protected int getStyleAsInt(String style)
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
     * specified format and {@link Locale}if the object is not already
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
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Number)
        {
            return (Number)obj;
        }
        try
        {
            NumberFormat parser = getNumberFormat(format, locale);
            return parser.parse(String.valueOf(obj));
        }
        catch (Exception e)
        {
            return null;
        }
    }

}

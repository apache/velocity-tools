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

import java.util.Collections;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <p>Tool for comparing {@link java.util.Date} and {@link Calendar} values
 * in Velocity templates.  This is a subclass of {@link DateTool}
 * and thus provides all the functionality of that tool and
 * augments it with the ability to find the relationship between
 * any date and the current date, or between any two dates.
 * This comparison can result in either a textual representation
 * of the relationship (e.g. "3 weeks, 2 days ago", "tomorrow", or
 * "3 hrs away") or the value of a specific time unit may be requested.
 * When using the textual representations, you can configure the
 * tool to use alternate resource bundles and to skip over units
 * you do not want to be included.</p>
 * <pre>
 * Example of formatting the "current" date:
 *  $date.whenIs('2005-07-04')                -&gt; 1 year ago
 *  $date.whenIs('2007-02-15').full           -&gt; 1 year 32 weeks 2 days 17 hours 38 minutes 44 seconds 178 milliseconds ago
 *  $date.whenIs('2007-02-15').days           -&gt; -730
 *  $date.whenIs($date.calendar)              -&gt; now
 *  $date.whenIs('2005-07-04', '2005-07-04')  -&gt; same time
 *  $date.difference('2005-07-04','2005-07-04')      -&gt; 0 milliseconds
 *  $date.difference('2005-07-04','2007-02-15').abbr -&gt; 1 yr
 *
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ComparisonDateTool"
 *              format="yyyy-MM-dd" depth="1" skip="month,week,millisecond"
 *              bundle="org.apache.velocity.tools.generic.times"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * @author Nathan Bubna
 * @author Chris Townsen
 * @since VelocityTools 1.4
 * @version $Revision$ $Date: 2006-04-04 12:35:17 -0700 (Tue, 04 Apr 2006) $
 */

@ValidScope(Scope.APPLICATION)
public class ComparisonDateTool extends DateTool
{
    private static final long serialVersionUID = 6847034688404674662L;

    /** The number of milliseconds in a second. */
    public static final long MILLIS_PER_SECOND = 1000l;

    /** The number of millseconds in a minute. */
    public static final long MILLIS_PER_MINUTE = 60l * MILLIS_PER_SECOND;

    /** The number of milliseconds in an hour. */
    public static final long MILLIS_PER_HOUR = 60l * MILLIS_PER_MINUTE;

    /** The number of milliseconds in a day. */
    public static final long MILLIS_PER_DAY = 24l * MILLIS_PER_HOUR;

    /** The number of milliseconds in a week. */
    public static final long MILLIS_PER_WEEK = 7l * MILLIS_PER_DAY;

    /** An approximation of the number of milliseconds in a month. */
    public static final long MILLIS_PER_MONTH = 30l * MILLIS_PER_DAY;

    /** An approximation of the number of milliseconds in a year. */
    public static final long MILLIS_PER_YEAR = 365l * MILLIS_PER_DAY;

    /** The key used for specifying a default locale via toolbox params. */
    public static final String BUNDLE_NAME_KEY = "bundle";

    /** The key used for specifying a different default depth via toolbox params. */
    public static final String DEPTH_KEY = "depth";

    /** The key used for specifying time units to be skipped over. */
    public static final String SKIPPED_UNITS_KEY = "skip";

    /** The default path of the relative format resource bundles. */
    public static final String DEFAULT_BUNDLE_NAME =
        "org.apache.velocity.tools.generic.times";


    // time unit message keys
    protected static final String MILLISECOND_KEY = "millisecond";
    protected static final String SECOND_KEY = "second";
    protected static final String MINUTE_KEY = "minute";
    protected static final String HOUR_KEY = "hour";
    protected static final String DAY_KEY = "day";
    protected static final String WEEK_KEY = "week";
    protected static final String MONTH_KEY = "month";
    protected static final String YEAR_KEY = "year";

    /** Array of all time unit message keys to their millisecond conversion factor. */
    protected static final Map TIME_UNITS;
    static
    {
        Map units = new LinkedHashMap(8);
        units.put(MILLISECOND_KEY, Long.valueOf(1));
        units.put(SECOND_KEY, Long.valueOf(MILLIS_PER_SECOND));
        units.put(MINUTE_KEY, Long.valueOf(MILLIS_PER_MINUTE));
        units.put(HOUR_KEY, Long.valueOf(MILLIS_PER_HOUR));
        units.put(DAY_KEY, Long.valueOf(MILLIS_PER_DAY));
        units.put(WEEK_KEY, Long.valueOf(MILLIS_PER_WEEK));
        units.put(MONTH_KEY, Long.valueOf(MILLIS_PER_MONTH));
        units.put(YEAR_KEY, Long.valueOf(MILLIS_PER_YEAR));
        TIME_UNITS = Collections.unmodifiableMap(units);
    }

    // special message keys/prefixes/suffixes
    protected static final String CURRENT_PREFIX = "current.";
    protected static final String AFTER_KEY = "after";
    protected static final String BEFORE_KEY = "before";
    protected static final String EQUAL_KEY = "equal";
    protected static final String ZERO_KEY = "zero";
    protected static final String ABBR_SUFFIX = ".abbr";
    protected static final String ONE_DAY_SUFFIX = ".day";
    protected static final String PLURAL_SUFFIX = "s";

    // display types
    protected static final int CURRENT_TYPE = 0;
    protected static final int RELATIVE_TYPE = 1;
    protected static final int DIFF_TYPE = 2;

    private String bundleName = DEFAULT_BUNDLE_NAME;
    private ResourceBundle defaultBundle;
    private Map timeUnits = TIME_UNITS;
    private int depth = 1;


    /**
     * Calls the superclass implementation, then looks for a bundle name
     * and any time units to be skipped.
     */
    protected void configure(ValueParser values)
    {
        // do DateTool config
        super.configure(values);

        // look for an alternate bundle
        String bundle = values.getString(BUNDLE_NAME_KEY);
        if (bundle != null)
        {
            this.bundleName = bundle;
        }

        this.depth = values.getInt(DEPTH_KEY, 1);

        // look for time units to be ignored
        String[] skip = values.getStrings(SKIPPED_UNITS_KEY);
        if (skip != null)
        {
            timeUnits = new LinkedHashMap(TIME_UNITS);
            for (int i=0; i < skip.length; i++)
            {
                timeUnits.remove(skip[i]);
            }
        }
    }

    /**
     * Retrieves the specified text resource.
     * @param key key
     * @param locale locale
     * @return text resource
     */
    protected String getText(String key, Locale locale)
    {
        Locale defaultLocale = getLocale();
        ResourceBundle bundle = null;
        // if there is no locale or the specified locale equals the tool's default
        if (locale == null || locale.equals(defaultLocale))
        {
            if (defaultBundle == null)
            {
                // load the bundle for the default locale
                try
                {
                    // and cache it
                    defaultBundle = ResourceBundle.getBundle(this.bundleName,
                                                             defaultLocale);
                }
                catch (MissingResourceException e) {}
            }

            // use the default locale's bundle
            bundle = defaultBundle;
        }
        else
        {
            // load the bundle for the specified locale
            try
            {
                bundle = ResourceBundle.getBundle(this.bundleName, locale);
            }
            catch (MissingResourceException e) {}
        }

        // if we found a bundle...
        if (bundle != null)
        {
            try
            {
                // try to return the specified key
                return bundle.getString(key);
            }
            catch (MissingResourceException e) {}
        }

        // otherwise, return the key as an error
        return "???" + key + "???";
    }


    // ------------------------- static millisecond conversions ----------------

    /**
     * Returns the number of whole Years in the specified number of milliseconds.
     * @param ms milliseconds
     * @return years
     */
    public static long toYears(long ms)
    {
        return ms / MILLIS_PER_YEAR;
    }

    /**
     * Returns the number of whole Months in the specified number of milliseconds.
     * @param ms milliseconds
     * @return months
     */
    public static long toMonths(long ms)
    {
        return ms / MILLIS_PER_MONTH;
    }

    /**
     * Returns the number of whole Weeks in the specified number of milliseconds.
     * @param ms milliseconds
     * @return weeks
     */
    public static long toWeeks(long ms)
    {
        return ms / MILLIS_PER_WEEK;
    }

    /**
     * Returns the number of whole Days in the specified number of milliseconds.
     * @param ms milliseconds
     * @return days
     */
    public static long toDays(long ms)
    {
        return ms / MILLIS_PER_DAY;
    }

    /**
     * Returns the number of whole Hours in the specified number of milliseconds.
     * @param ms milliseconds
     * @return hours
     */
    public static long toHours(long ms)
    {
        return ms / MILLIS_PER_HOUR;
    }

    /**
     * Returns the number of whole Minutes in the specified number of milliseconds.
     * @param ms milliseconds
     * @return minutes
     */
    public static long toMinutes(long ms)
    {
        return ms / MILLIS_PER_MINUTE;
    }

    /**
     * Returns the number of whole Seconds in the specified number of milliseconds.
     * @param ms milliseconds
     * @return seconds
     */
    public static long toSeconds(long ms)
    {
        return ms / MILLIS_PER_SECOND;
    }


    // ------------------------- date comparison ---------------------------

    /**
     * Returns a {@link Comparison} between the result of
     * {@link #getCalendar()} and the specified date.  The default
     * rendering of that Comparison will be the largest unit difference
     * between the dates followed by a description of their relative position.
     *
     * @param then The date in question
     * @return {@link Comparison} object
     */
    public Comparison whenIs(Object then)
    {
        return compare(getCalendar(), then, CURRENT_TYPE);
    }

    /**
     * Returns a {@link Comparison} between the second specified date
     * and the first specified date.  The default
     * rendering of that Comparison will be the largest unit difference
     * between the dates followed by a description of their relative position.
     *
     * @param now The date to use as representative of "now"
     * @param then The date in question
     * @return {@link Comparison} object
     */
    public Comparison whenIs(Object now, Object then)
    {
        return compare(now, then, RELATIVE_TYPE);
    }

    /**
     * Returns a {@link Comparison} between the result of
     * the second specified date and the first specified date.  The default
     * rendering of that Comparison will be the largest unit difference
     * between the dates.
     *
     * @param now The date to use as representative of "now"
     * @param then The secondary date
     * @return {@link Comparison} object
     */
    public Comparison difference(Object now, Object then)
    {
        return compare(now, then, DIFF_TYPE);
    }

    /**
     * Internal comparison method.
     * @param now The date to use as representative of "now"
     * @param then The secondary date
     * @param type Difference type
     * @return {@link Comparison} object
     */
    protected Comparison compare(Object now, Object then, int type)
    {
        Calendar calThen = toCalendar(then);
        Calendar calNow = toCalendar(now);
        if (calThen == null || calNow == null)
        {
            return null;
        }

        long ms = calThen.getTimeInMillis() - calNow.getTimeInMillis();
        return new Comparison(ms, type, this.depth, false, null);
    }


    /**
     * @param ms The time in milliseconds
     * @param type Whether the time should be represented as relative to "now",
     *             relative to some other time, or as a mere difference.
     * @param depth The maximum number of units deep to show
     * @param abbr Whether the units should be abbreviated or not
     * @param loc The locale to be used when looking up resources
     * @return String representation
     */
    protected String toString(long ms, int type, int depth,
                              boolean abbr, Locale loc)
    {
        // first check if there is a difference
        if (ms == 0)
        {
            String sameKey = (abbr) ? ABBR_SUFFIX : "";
            if (type == CURRENT_TYPE)
            {
                sameKey = CURRENT_PREFIX + EQUAL_KEY + sameKey;
            }
            else if (type == RELATIVE_TYPE)
            {
                sameKey = EQUAL_KEY + sameKey;
            }
            else
            {
                sameKey = ZERO_KEY + sameKey;
            }
            return getText(sameKey, loc);
        }

        boolean isBefore = false;
        if (ms < 0)
        {
            isBefore = true;
            // convert() only works with positive values
            ms *= -1;
        }

        // get the base value
        String friendly = toString(ms, depth, abbr, loc);

        // if we only want the difference...
        if (type == DIFF_TYPE)
        {
            // add the sign (if negative)
            if (isBefore)
            {
                friendly = "-" + friendly;
            }
            // then return without direction suffix
            return friendly;
        }

        // otherwise, get the appropriate direction key
        String directionKey = (isBefore) ? BEFORE_KEY : AFTER_KEY;
        if (type == CURRENT_TYPE)
        {
            directionKey = CURRENT_PREFIX + directionKey;

            if (friendly != null && friendly.startsWith("1"))
            {
                // check for the corner case of "1 day ago" or "1 day away"
                // and convert those to "yesterday" or "tomorrow"
                String dayKey = (abbr) ? DAY_KEY + ABBR_SUFFIX : DAY_KEY;
                if (friendly.equals("1 " + getText(dayKey, loc)))
                {
                    // add .day
                    directionKey += ONE_DAY_SUFFIX;
                    // and return only the value of this key
                    // (which means we throw away the friendly value
                    //  and don't bother abbreviating things)
                    return getText(directionKey, loc);
                }
            }
        }

        // in the default bundle, this doesn't change anything.
        // but in may in user-provided bundles
        if (abbr)
        {
            directionKey += ABBR_SUFFIX;
        }

        // then combine them
        return friendly +  " " + getText(directionKey, loc);
    }


    /**
     * Converts the specified positive duration of milliseconds into larger
     * units up to the specified number of positive units, beginning with the
     * largest positive unit.  e.g.
     * <code>toString(181453, 3, false, null)</code> will return
     * "3 minutes 1 second 453 milliseconds",
     * <code>toString(181453, 2, false, null)</code> will return
     * "3 minutes 1 second", and
     * <code>toString(180000, 2, true, null)</code> will return
     * "3 min".
     * @param diff milliseconds
     * @param maxUnitDepth maximum unit depth
     * @param abbreviate whether to abbreviate unit names
     * @param locale locale to use
     * @return string representation of the difference
     */
    protected String toString(long diff, int maxUnitDepth,
                              boolean abbreviate, Locale locale)
    {
        // these cases should be handled elsewhere
        if (diff <= 0)
        {
            return null;
        }
        // can't go any deeper than we have units
        if (maxUnitDepth > timeUnits.size())
        {
            maxUnitDepth = timeUnits.size();
        }

        long value = 0;
        long remainder = 0;

        // determine the largest unit and calculate the value and remainder
        Iterator i = timeUnits.keySet().iterator();
        String unitKey = (String)i.next();
        Long unit = (Long)timeUnits.get(unitKey);
        while (i.hasNext())
        {
            // get the next unit
            String nextUnitKey = (String)i.next();
            Long nextUnit = (Long)timeUnits.get(nextUnitKey);

            // e.g. if diff < <nextUnit>
            if (diff < nextUnit.longValue())
            {
                // then we're working with <unit>
                value = diff / unit.longValue();
                remainder = diff - (value * unit.longValue());
                break;
            }

            // shift to the next unit
            unitKey = nextUnitKey;
            unit = nextUnit;
        }

        // if it was years, then we haven't done the math yet
        if (unitKey.equals(YEAR_KEY))
        {
            value = diff / unit.longValue();
            remainder = diff - (value * unit.longValue());
        }

        // select proper pluralization
        if (value != 1)
        {
            unitKey += PLURAL_SUFFIX;
        }

        if (abbreviate)
        {
            unitKey += ABBR_SUFFIX;
        }

        // combine the value and the unit
        String output = value + " " + getText(unitKey, locale);

        // recurse over the remainder if it exists and more units are allowed
        if (maxUnitDepth > 1 && remainder > 0)
        {
            output += " " + toString(remainder, maxUnitDepth - 1,
                                     abbreviate, locale);
        }
        return output;
    }



    public class Comparison
    {
        private final long milliseconds;
        private final int type;
        private final int maxUnitDepth;
        private final boolean abbreviate;
        private final Locale locale;

        /**
         * Comparison object constructor
         * @param ms milliseconds
         * @param type comparison type
         * @param depth units depth
         * @param abbr whether to abbreviate units
         * @param loc locale to use
         */
        public Comparison(long ms, int type, int depth, boolean abbr, Locale loc)
        {
            this.milliseconds = ms;
            this.type = type;
            this.maxUnitDepth = depth;
            this.abbreviate = abbr;
            this.locale = loc;
        }

        /**
         * Sets whether or not this comparison is to be rendered in
         * abbreviated form or not. By default, it is not abbreviated.
         * @param abbr flag value
         * @return new Comparison object
         */
        public Comparison abbr(boolean abbr)
        {
            return new Comparison(this.milliseconds, this.type,
                                  this.maxUnitDepth, abbr, this.locale);
        }

        /**
         * Set the maximum number of units to render for this comparison.
         * By default, this is set to 1 unit.
         * @param depth max units depth
         * @return new Comparison object
         */
        public Comparison depth(int depth)
        {
            return new Comparison(this.milliseconds, this.type,
                                  depth, this.abbreviate, this.locale);
        }

        /**
         * Sets the locale used to look up the textual portions of the
         * rendering. This defaults to the Locale configured for this tool,
         * if any.  If no locale was configured, this defaults to the system
         * default.
         * @param loc locale to use
         * @return new Comparison object
         */
        public Comparison locale(Locale loc)
        {
            return new Comparison(this.milliseconds, this.type,
                                  this.maxUnitDepth, this.abbreviate, loc);
        }

        /**
         * Return the approximate number of years between the dates being compared.
         * @return years
         */
        public long getYears()
        {
            return ComparisonDateTool.toYears(this.milliseconds);
        }

        /**
         * Return the approximate number of months between the dates being compared.
         * @return months
         */
        public long getMonths()
        {
            return ComparisonDateTool.toMonths(this.milliseconds);
        }

        /**
         * Return the number of weeks between the dates being compared.
         * @return weeks
         */
        public long getWeeks()
        {
            return ComparisonDateTool.toWeeks(this.milliseconds);
        }

        /**
         * Return the number of days between the dates being compared.
         * @return days
         */
        public long getDays()
        {
            return ComparisonDateTool.toDays(this.milliseconds);
        }

        /**
         * Return the number of hours between the dates being compared.
         * @return hours
         */
        public long getHours()
        {
            return ComparisonDateTool.toHours(this.milliseconds);
        }

        /**
         * Return the number of minutes between the dates being compared.
         * @return minutes
         */
        public long getMinutes()
        {
            return ComparisonDateTool.toMinutes(this.milliseconds);
        }

        /**
         * Return the number of seconds between the dates being compared.
         * @return seconds
         */
        public long getSeconds()
        {
            return ComparisonDateTool.toSeconds(this.milliseconds);
        }

        /**
         * Return the number of milliseconds between the dates being compared.
         * @return milliseconds
         */
        public long getMilliseconds()
        {
            return this.milliseconds;
        }

        /**
         * Sets the {@link #depth(int depth)} to which this comparison is rendered
         * to the maximum number of time units available to the tool. By default,
         * there are 8 units available, but the tool may be configured to "skip"
         * any of the standard units, thus shortening the maximum depth.
         * @return new Comparison object
         */
        public Comparison getFull()
        {
            return depth(ComparisonDateTool.this.timeUnits.size());
        }

        /**
         * Sets this comparison to be rendered as a
         * {@link ComparisonDateTool#difference}. This effectively means that
         * the comparison will render as a period of time, without any suffix
         * to describe the relative position of the dates being compared (e.g. "later"
         * or "ago").
         * @return new Comparison object
         */
        public Comparison getDifference()
        {
            return new Comparison(this.milliseconds, DIFF_TYPE,
                                  this.maxUnitDepth, this.abbreviate, this.locale);
        }

        /**
         * Sets this comparison to be rendered as if it where generated using
         * the {@link ComparisonDateTool#whenIs(Object now, Object then)} method.
         * This effectively means that the comparison will render with a suffix
         * to describe the relative position of the dates being compared (e.g. "later"
         * or "ago").
         * @return new Comparison object
         */
        public Comparison getRelative()
        {
            return new Comparison(this.milliseconds, RELATIVE_TYPE,
                                  this.maxUnitDepth, this.abbreviate, this.locale);
        }

        /**
         * This is equivalent to calling {@link #abbr(boolean abbr)} with
         * {@code true} as the argument, thus setting this comparison to be
         * rendered in abbreviated form.
         * @return new Comparison object
         */
        public Comparison getAbbr()
        {
            return abbr(true);
        }

        /**
         * Renders this comparison to a String.
         * @return string representation
         */
        public String toString()
        {
            return ComparisonDateTool.this.toString(this.milliseconds,
                                                    this.type,
                                                    this.maxUnitDepth,
                                                    this.abbreviate,
                                                    this.locale);
        }
    }

}

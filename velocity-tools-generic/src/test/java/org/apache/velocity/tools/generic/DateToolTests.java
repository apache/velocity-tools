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

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

/**
 * <p>Tests for DateToolTests</p>
 *
 * @author Christopher Schultz
 * @since VelocityTools 2.0.1
 * @version $Id$
 */
public class DateToolTests
{
    private final Locale TEST_LOCALE = Locale.FRANCE;
    private final TimeZone TEST_TIME_ZONE = TimeZone.getTimeZone("Europe/Paris");

    public @Test void toLocalizedPattern_simplePattern() throws Exception
    {
        DateTool dt = new DateTool();

        assertEquals("DateTool incorrectly localizes date format pattern",
                     new SimpleDateFormat("yyyy-MM-dd", TEST_LOCALE).toLocalizedPattern(), // "aaaa-MM-jj"
                     dt.toLocalizedPattern("yyyy-MM-dd", TEST_LOCALE));
    }

    public @Test void toLocalizedPattern_predefinedDateTimePattern() throws Exception
    {
        DateTool dt = new DateTool();

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TEST_LOCALE)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short", TEST_LOCALE));
    }

    public @Test void toLocalizedPattern_predefinedDatePattern() throws Exception
    {
        DateTool dt = new DateTool();

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, TEST_LOCALE)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short_date", TEST_LOCALE));
    }

    public @Test void toLocalizedPattern_predefinedTimePattern() throws Exception
    {
        DateTool dt = new DateTool();

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, TEST_LOCALE)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short_time", TEST_LOCALE));
    }

    public @Test void toIsoFormat() throws Exception
    {
        DateTool dt = new DateTool();

        dt.setLocale(TEST_LOCALE);
        dt.setTimeZone(TEST_TIME_ZONE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TEST_TIME_ZONE);

        Date date = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat();
        format.setCalendar(cal);

        format.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("iso", date));

        format.applyPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("iso_tz",date));

        format.applyPattern("yyyy-MM-dd HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("intl",date));

        format.applyPattern("yyyy-MM-dd HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date) + " " + TEST_TIME_ZONE.getID(), dt.format("intl_tz",date));

        format.applyPattern("yyyy-MM-dd");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("iso_date",date));

        format.applyPattern("yyyy-MM-dd");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("intl_date",date));

        format.applyPattern("HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format",format.format(date), dt.format("iso_time",date));

        format.applyPattern("HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("intl_time",date));

        format.applyPattern("HH:mm:ssXXX");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date), dt.format("iso_tz_time",date));

        format.applyPattern("HH:mm:ss");
        assertEquals("DateTool incorrectly formatted iso format", format.format(date) + " " + TEST_TIME_ZONE.getID(), dt.format("intl_tz_time",date));
    }
}

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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
//import org.apache.velocity.tools.generic.DateTool;

/**
 * <p>Tests for DateToolTests</p>
 *
 * @author Christopher Schultz
 * @since VelocityTools 2.0.1
 * @version $Id$
 */
public class DateToolTests
{
    public @Test void toLocalizedPattern_simplePattern() throws Exception
    {
        DateTool dt = new DateTool();

        Locale frFR = new Locale("fr", "FR");

        assertEquals("DateTool incorrectly localizes date format pattern",
                     new SimpleDateFormat("yyyy-MM-dd", frFR).toLocalizedPattern(), // "aaaa-MM-jj"
                     dt.toLocalizedPattern("yyyy-MM-dd", frFR));
    }

    public @Test void toLocalizedPattern_predefinedDateTimePattern() throws Exception
    {
        DateTool dt = new DateTool();

        Locale frFR = new Locale("fr", "FR");

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, frFR)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short", frFR));
    }

    public @Test void toLocalizedPattern_predefinedDatePattern() throws Exception
    {
        DateTool dt = new DateTool();

        Locale frFR = new Locale("fr", "FR");

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, frFR)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short_date", frFR));
    }

    public @Test void toLocalizedPattern_predefinedTimePattern() throws Exception
    {
        DateTool dt = new DateTool();

        Locale frFR = new Locale("fr", "FR");

        assertEquals("DateTool incorrectly localizes date format pattern",
                     ((SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, frFR)).toLocalizedPattern(),
                     dt.toLocalizedPattern("short_time", frFR));
    }

    public @Test void toIsoFormat() throws Exception
    {
        DateTool dt = new DateTool();
        Date date = new Date();
        dt.setTimeZone(TimeZone.getTimeZone("CET"));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date), dt.format("iso", date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date), dt.format("iso_tz",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date), dt.format("intl",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(date), dt.format("intl_tz",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd").format(date), dt.format("iso_date",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("yyyy-MM-dd").format(date), dt.format("intl_date",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("HH:mm:ss").format(date), dt.format("iso_time",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("HH:mm:ss").format(date), dt.format("intl_time",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("HH:mm:ssXXX").format(date), dt.format("iso_tz_time",date));
        assertEquals("DateTool incorrectly formatted iso format", new SimpleDateFormat("HH:mm:ss zzz").format(date), dt.format("intl_tz_time",date));
    }
}

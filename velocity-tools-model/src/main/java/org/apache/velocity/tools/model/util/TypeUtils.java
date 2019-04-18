package org.apache.velocity.tools.model.util;

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

import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TypeUtils
{
    public static String toString(Object value)
    {
        return value == null ? null : value.toString();
    }

    public static Boolean toBoolean(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Boolean)
        {
            return (Boolean)value;
        }
        if (value instanceof String)
        {
            String str = (String)value;
            if ("true".equals(str))
            {
                return true;
            }
            if ("false".equals(str))
            {
                return false;
            }
            try
            {
                value = Long.valueOf(str);
            }
            catch (NumberFormatException nfe)
            {
                return false;
            }
        }
        if (value instanceof Number)
        {
            return ((Number)value).longValue() != 0l;
        }
        return false;
    }

    public static Short toShort(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number)value).shortValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Short.valueOf((String)value);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        return null;
    }

    public static Integer toInteger(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number)value).intValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Integer.valueOf((String)value);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        return null;
    }

    public static Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number)value).longValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Long.valueOf((String)value);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        return null;
    }

    public static Float toFloat(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number)value).floatValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Float.valueOf((String)value);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        return null;
    }

    public static Double toDouble(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number)value).doubleValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Double.valueOf((String)value);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        return null;
    }

    public static Date toDate(Object value)
    {
        if (value == null || value instanceof Date)
        {
            return (Date)value;
        }
        if (value instanceof Calendar)
        {
            return ((Calendar)value).getTime();
        }
        return null;
    }

    public static Calendar toCalendar(Object value)
    {
        if (value == null || value instanceof Calendar)
        {
            return (Calendar)value;
        }
        if (value instanceof Date)
        {
            // CB TODO - use model locale
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime((Date)value);
            return calendar;
        }
        return null;
    }

    public static byte[] toBytes(Object value)
    {
        if (value == null || value instanceof byte[])
        {
            return (byte[])value;
        }
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    public static String base64Encode(Object value)
    {
        if (value == null)
        {
            return null;
        }
        byte[] decoded = toBytes(value);
        byte[] encoded = Base64.encodeBase64URLSafe(decoded);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static byte[] base64Decode(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String encoded = toString(value);
        return Base64.decodeBase64(encoded);

    }
}

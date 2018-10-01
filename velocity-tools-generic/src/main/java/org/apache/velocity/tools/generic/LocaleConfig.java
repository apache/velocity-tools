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

import java.util.Locale;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.ToolContext;

/**
 * Implements common logic and constants for tools which allow their
 * default {@link Locale} to be configured.
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 */
public class LocaleConfig extends SafeConfig
{
    /**
     * The default {@link Locale} to be used when none is specified.
     */
    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private Locale locale = DEFAULT_LOCALE;

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        Locale locale = values.getLocale(ToolContext.LOCALE_KEY);
        if (locale != null)
        {
            setLocale(locale);
        }
    }

    /**
     * This returns the configured default {@link Locale} for this tool.
     *
     * @return the default {@link Locale}
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Sets the default locale for this instance.
     * @param locale default locale to use
     */
    protected void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @param value the object to be converted
     * @return a {@link Locale} for the specified value or
     *         <code>null</code> if the value is null or the conversion failed
     * @since VelocityTools 3.0
     */
    public Locale toLocale(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof Locale)
        {
            return (Locale)value;
        }
        return ConversionUtils.toLocale(String.valueOf(value));
    }


}

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

/**
 * Implements common logic and constants for tools which allow their
 * default format to be configured.
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 */
public class FormatConfig extends LocaleConfig
{
    /**
     * The default format to be used when none is specified.
     */
    public static final String DEFAULT_FORMAT = "default";

    /**
     * The key used for specifying a default format via tool configuration.
     */
    public static final String FORMAT_KEY = "format";

    private String format = DEFAULT_FORMAT;

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);

        String format = values.getString(FORMAT_KEY);
        if (format != null)
        {
            setFormat(format);
        }
    }

    /**
     * This returns the configured default format for this tool.
     *
     * @return the default {@link String}
     */
    public String getFormat()
    {
        return this.format;
    }

    /**
     * Sets the default format for this instance.
     */
    protected void setFormat(String format)
    {
        this.format = format;
    }

}

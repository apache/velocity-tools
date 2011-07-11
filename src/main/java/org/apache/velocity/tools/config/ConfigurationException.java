package org.apache.velocity.tools.config;

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
 * 
 *
 * @author Nathan Bubna
 * @version $Id: ConfigurationException.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ConfigurationException extends RuntimeException
{
    private final Object source;

    public ConfigurationException(Data data, Throwable cause)
    {
        super(cause);
        this.source = data;
    }

    public ConfigurationException(Data data, String message)
    {
        super(message);
        this.source = data;
    }

    public ConfigurationException(Data data, String message, Throwable cause)
    {
        super(message, cause);
        this.source = data;
    }

    public ConfigurationException(Configuration config, Throwable cause)
    {
        super(cause);
        this.source = config;
    }

    public ConfigurationException(Configuration config, String message)
    {
        super(message);
        this.source = config;
    }

    public ConfigurationException(Configuration config, String message, Throwable cause)
    {
        super(message, cause);
        this.source = config;
    }

    public Object getSource()
    {
        return source;
    }

}

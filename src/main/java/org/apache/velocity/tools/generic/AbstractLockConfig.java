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

import java.util.Map;

/**
 * Implements common logic and constants for tools which by default
 * lock down the public configure(Map) method, to keep the tool
 * thread-safe in templates and most other common uses.
 *
 * @author Nathan Bubna
 */
public abstract class AbstractLockConfig
{
    /**
     * The key used for specifying whether or not to prevent templates
     * from reconfiguring this tool.  The default is true.
     */
    public static final String LOCK_CONFIG_KEY = "lock-config";

    private boolean configLocked = false;

    /**
     * Looks for configuration values in the given params.
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
     */
    protected abstract void configure(ValueParser values);

}

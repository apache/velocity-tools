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
 * <p>Implements common logic and constants for tools which automatically
 * locks down the {@code public void configure(Map params)} method after
 * it is called once.
 * This keeps application or session scoped tools thread-safe in templates,
 * which generally have access to the tool after configuration has happened.
 * </p><p>
 * It also provides for a separate "safe mode" setting which tells
 * tools to block any functions that may pose a security threat. This,
 * of course, is set to {@code true} by default.
 * </p><p>
 * Once "locked down", the {@link #configure(Map)} may still be called,
 * however it will do nothing (unless some subclass is foolish enough to
 * override it and not check if {@link #isConfigLocked} before changing
 * configurations.  The proper method for subclasses to override is
 * {@link #configure(ValueParser)} which will only be called by 
 * {@link #configure(Map)} when the {@link #isConfigLocked} is false
 * (i.e. the first time only).
 * </p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 */
public class SafeConfig
{
    /**
     * The key used for specifying whether or not to prevent templates
     * from reconfiguring this tool.  The default is true.
     */
    public static final String LOCK_CONFIG_KEY = "lockConfig";
    @Deprecated
    public static final String OLD_LOCK_CONFIG_KEY = "lock-config";

    /**
     * Many tools interested in locking configure() also have other
     * things they wish to secure.  This key controls that property.
     * The default value is true, of course.
     */
    public static final String SAFE_MODE_KEY = "safeMode";

    private boolean configLocked = false;
    private boolean safeMode = false;

    /**
     * Only allow subclass access to this.
     */
    protected void setLockConfig(boolean lock)
    {
        this.configLocked = lock;
    }

    protected void setSafeMode(boolean safe)
    {
        this.safeMode = safe;
    }

    /**
     * Returns {@code true} if the {@link #configure(Map)} method
     * has been locked.
     */
    public boolean isConfigLocked()
    {
        return this.configLocked;
    }

    /**
     * Returns {@code true} if this tool is in "safe mode".
     */
    public boolean isSafeMode()
    {
        return this.safeMode;
    }

    /**
     * If {@link #isConfigLocked} returns {@code true}, then this method
     * does nothing; otherwise, if {@code false}, this will create a new
     * {@link ValueParser} from the specified Map of params and call
     * {@link #configure(ValueParser)} with it.  Then this will check
     * the parameters itself to find out whether or not the configuration
     * for this tool should be put into safe mode or have its config locked.
     * The safe mode value should be a boolean under the key
     * {@link #SAFE_MODE_KEY} and the lock value should be a boolean
     * under the key {@link #LOCK_CONFIG_KEY}.
     */
    public void configure(Map params)
    {
        if (!isConfigLocked())
        {
            ValueParser values = new ValueParser(params);
            configure(values);

            setSafeMode(values.getBoolean(SAFE_MODE_KEY, true));

            // check under the new key
            Boolean lock = values.getBoolean(LOCK_CONFIG_KEY);
            if (lock == null)
            {
                // now check the old key (for now)
                // by default, lock down this method after use
                // to prevent templates from re-configuring this instance
                lock = values.getBoolean(OLD_LOCK_CONFIG_KEY, Boolean.TRUE);
            }
            setLockConfig(lock.booleanValue());
        }
    }

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        // base implementation does nothing
    }

}

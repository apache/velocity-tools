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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Many tools interested in locking configure() also have other
     * things they wish to secure.  This key controls that property.
     * The default value is true, of course.
     */
    public static final String SAFE_MODE_KEY = "safeMode";

    /**
     * Key used to explicitely specify the logger name
     */
    public static final String LOGGER_NAME_KEY = "loggerName";

    /**
     * Key used to specify whether or not tools shall use loggers
     * named after the tools classes.
     */
    public static final String USE_CLASS_LOGGER_KEY = "useClassLogger";

    private AtomicBoolean configLocked = new AtomicBoolean(false);
    private boolean safeMode = false;

    protected Logger log = null;

    /**
     * Only allow subclass access to this.
     * @param lock whether to lock config
     */
    protected void setLockConfig(boolean lock)
    {
        this.configLocked.set(lock);
    }

    /**
     * Set or clear safe mode.
     * @param safe whether to set safe mode
     */
    protected void setSafeMode(boolean safe)
    {
        this.safeMode = safe;
    }

    /**
     * Returns {@code true} if the {@link #configure(Map)} method
     * has been locked.
     * @return locked status
     */
    public boolean isConfigLocked()
    {
        return this.configLocked.get();
    }

    /**
     * Returns {@code true} if this tool is in "safe mode".
     * @return safe mode status
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
     * @param params configuration values map
     */
    public void configure(Map<String, Object> params)
    {
        if (!isConfigLocked())
        {
            synchronized (this)
            {
                if (!isConfigLocked())
                {
                    ValueParser values = new ValueParser(params);

                    // set up logger
                    initLogger(values);

                    // call configure
                    configure(values);

                    setSafeMode(values.getBoolean(SAFE_MODE_KEY, true));

                    // check under the new key
                    Boolean lock = values.getBoolean(LOCK_CONFIG_KEY, Boolean.TRUE);
                    setLockConfig(lock.booleanValue());
                }
            }
        }
    }

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     * @param values configuration values
     */
    protected void configure(ValueParser values)
    {
        // base implementation does nothing
    }

    /**
     * Initialize logger. Default implementation will try to get a Velocity engine
     * from the configuration parameters, then try to use either the configured logger
     * instance, or the configured logger name suffixed by 'tools.&lt;key&gt;'
     * @param params configuration parameters
     */
    protected void initLogger(ValueParser params)
    {
        String loggerName = params.getString(LOGGER_NAME_KEY);
        if (loggerName != null)
        {
            log = LoggerFactory.getLogger(loggerName);
        }
        else
        {
            boolean useClassLogger = params.getBoolean(USE_CLASS_LOGGER_KEY, false);
            if (!useClassLogger)
            {
                VelocityEngine engine = (VelocityEngine) params.get(ToolContext.ENGINE_KEY);
                String key = (String) params.get(ToolContext.TOOLKEY_KEY);
                if (engine != null && key != null)
                {
                    log = ConfigurationUtils.getLog(engine, "tools." + key);
                }
            }
            if (log == null)
            {
                log = LoggerFactory.getLogger(getClass());
            }
        }
    }

    /**
     * Get logger
     * @return logger
     */
    protected Logger getLog()
    {
        if (log == null)
        {
            /* if logger hasn't been innitialized, fall back to class logger */
            synchronized(this)
            {
                if (log == null)
                {
                    log = LoggerFactory.getLogger(getClass());
                }
            }
        }
        return log;
    }
}

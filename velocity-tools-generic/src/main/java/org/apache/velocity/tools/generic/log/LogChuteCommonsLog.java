package org.apache.velocity.tools.generic.log;

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

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.Log;

/**
 * Redirects commons-logging messages to Velocity's configured LogChute.
 *
 * <p>To use, specify this class in your commons-logging.properties:
 * <pre><code>
 * org.apache.commons.logging.Log=org.apache.velocity.tools.generic.log.LogChuteCommonsLog
 * </code></pre>
 * </p>
 * @since VelocityTools 2.0
 * @version $Id: LogChuteCommonsLog.java 72115 2004-11-11 07:00:54Z nbubna $
 */
public class LogChuteCommonsLog implements org.apache.commons.logging.Log
{

    private static Log target = null;

    /**
     * Allow subclasses to statically access the static target.
     */
    protected static Log getVelocityLog()
    {
        return target;
    }

    /**
     * Set a VelocityEngine to handle all the log messages.
     */
    public static void setVelocityLog(Log target)
    {
        LogChuteCommonsLog.target = target;
    }


    // ********************  begin non-static stuff *******************

    private String category;

    public LogChuteCommonsLog()
    {
        this("");
    }

    public LogChuteCommonsLog(String category)
    {
        this.category = category + ": ";
    }

    protected Log getTarget()
    {
        if (target == null)
        {
            return Velocity.getLog();
        }
        else
        {
            return target;
        }
    }


    /*************** Commons Log Interface ****************/

    /**
     * Passes messages to Velocity's LogChute at "DEBUG" level.
     * (it's the lowest available. sorry.)
     */
    public void trace(Object message)
    {
        getTarget().trace(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "DEBUG" level.
     * (it's the lowest available. sorry.)
     */
    public void trace(Object message, Throwable t)
    {
        getTarget().trace(category+message, t);
    }

    /**
     * Passes messages to Velocity's LogChute at "DEBUG" level.
     */
    public void debug(Object message)
    {
        getTarget().debug(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "DEBUG" level.
     */
    public void debug(Object message, Throwable t)
    {
        getTarget().debug(category+message, t);
    }

    /**
     * Passes messages to Velocity's LogChute at "INFO" level.
     */
    public void info(Object message)
    {
        getTarget().info(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "INFO" level.
     */
    public void info(Object message, Throwable t)
    {
        getTarget().info(category+message, t);
    }

    /**
     * Passes messages to Velocity's LogChute at "WARN" level.
     */
    public void warn(Object message)
    {
        getTarget().warn(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "WARN" level.
     */
    public void warn(Object message, Throwable t)
    {
        getTarget().warn(category+message, t);
    }

    /**
     * Passes messages to Velocity's LogChute at "ERROR" level.
     */
    public void error(Object message)
    {
        getTarget().error(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "ERROR" level.
     */
    public void error(Object message, Throwable t)
    {
        getTarget().error(category+message, t);
    }

    /**
     * Passes messages to Velocity's LogChute at "ERROR" level.
     * (it's the highest available. sorry.)
     */
    public void fatal(Object message)
    {
        getTarget().error(category+message);
    }

    /**
     * Passes messages to Velocity's LogChute at "ERROR" level.
     * (it's the highest available. sorry.)
     */
    public void fatal(Object message, Throwable t)
    {
        getTarget().error(category+message, t);
    }

    /** 
     * Returns true if Velocity's LogChute returns true 
     * for isTraceEnabled().
     */
    public boolean isTraceEnabled()
    {
        return getTarget().isTraceEnabled();
    }

    /** 
     * Returns true if Velocity's LogChute returns true 
     * for isDebugEnabled().
     */
    public boolean isDebugEnabled()
    {
        return getTarget().isDebugEnabled();
    }

    /** 
     * Returns true if Velocity's LogChute returns true 
     * for isInfoEnabled().
     */
    public boolean isInfoEnabled()
    {
        return getTarget().isInfoEnabled();
    }

    /** 
     * Returns true if Velocity's LogChute returns true 
     * for isWarnEnabled().
     */
    public boolean isWarnEnabled()
    {
        return getTarget().isWarnEnabled();
    }

    /** 
     * Returns true if Velocity's LogChute returns true 
     * for isErrorEnabled().
     */
    public boolean isErrorEnabled()
    {
        return getTarget().isErrorEnabled();
    }

    /** 
     * Returns true if isErrorEnabled() returns true, since
     * Velocity's LogChute doesn't support this level.
     */
    public boolean isFatalEnabled()
    {
        return isErrorEnabled();
    }

}

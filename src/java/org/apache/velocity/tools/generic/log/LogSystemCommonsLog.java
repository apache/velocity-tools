/*
 * Copyright 2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.generic.log;

import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Redirects commons-logging messages to Velocity's LogSystem.
 *
 * <p>To use, specify this class in your commons-logging.properties:
 * <code>
 * org.apache.commons.logging.Log=org.apache.velocity.tools.generic.log.LogSystemCommonsLog
 * </code>
 * </p>
 * 
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @since VelocityTools 1.1
 * @version $Id: LogSystemCommonsLog.java,v 1.3 2004/02/18 20:10:38 nbubna Exp $
 */
public class LogSystemCommonsLog implements Log
{

    private boolean printStackTrace = false;


    /**
     * Default constructor
     */
    public LogSystemCommonsLog() {}


    /**
     * Meaningless constructor to make commons-logging happy.
     */
    public LogSystemCommonsLog(String name) {}


    /**
     * Lets you set whether or not this instance should print the
     * full stack trace of exceptions and errors passed to it.
     *
     * <p>It should be possible to create a LogFactory implementation
     * that takes advantage of this constructor.</p>
     *
     * @param pst if true, stack traces will be printed
     */
    public LogSystemCommonsLog(boolean pst)
    {
        this.printStackTrace = pst;
    }

    
    private void log(int level, Object message)
    {
        switch (level) 
        {
            case LogSystem.WARN_ID:
                Velocity.warn(message);
                break;
            case LogSystem.INFO_ID:
                Velocity.info(message);
                break;
            case LogSystem.DEBUG_ID:
                Velocity.debug(message);
                break;
            case LogSystem.ERROR_ID:
                Velocity.error(message);
                break;
            default:
                Velocity.debug(message);
                break;
        }
    }

    
    private void log(int level, Object message, Throwable t)
    {
        if (printStackTrace)
        {
            StringWriter sw = new StringWriter();
            sw.write(String.valueOf(message));
            t.printStackTrace(new PrintWriter(sw));
            log(level, sw);
        }
        else
        {
            StringBuffer buffer = new StringBuffer(String.valueOf(message));
            buffer.append(" - ");
            buffer.append(t.getMessage());
            log(level, buffer);
        }
    }


    /*************** Commons Log Interface ****************/

    /**
     * Passes messages to Velocity's LogSystem at "DEBUG" level.
     * (it's the lowest available. sorry.)
     */
    public void trace(Object message)
    {
        log(LogSystem.DEBUG_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "DEBUG" level.
     * (it's the lowest available. sorry.)
     */
    public void trace(Object message, Throwable t)
    {
        log(LogSystem.DEBUG_ID, message, t);
    }

    /**
     * Passes messages to Velocity's LogSystem at "DEBUG" level.
     */
    public void debug(Object message)
    {
        log(LogSystem.DEBUG_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "DEBUG" level.
     */
    public void debug(Object message, Throwable t)
    {
        log(LogSystem.DEBUG_ID, message, t);
    }

    /**
     * Passes messages to Velocity's LogSystem at "INFO" level.
     */
    public void info(Object message)
    {
        log(LogSystem.INFO_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "INFO" level.
     */
    public void info(Object message, Throwable t)
    {
        log(LogSystem.INFO_ID, message, t);
    }

    /**
     * Passes messages to Velocity's LogSystem at "WARN" level.
     */
    public void warn(Object message)
    {
        log(LogSystem.WARN_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "WARN" level.
     */
    public void warn(Object message, Throwable t)
    {
        log(LogSystem.WARN_ID, message, t);
    }

    /**
     * Passes messages to Velocity's LogSystem at "ERROR" level.
     */
    public void error(Object message)
    {
        log(LogSystem.ERROR_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "ERROR" level.
     */
    public void error(Object message, Throwable t)
    {
        log(LogSystem.ERROR_ID, message, t);
    }

    /**
     * Passes messages to Velocity's LogSystem at "ERROR" level.
     * (it's the highest available. sorry.)
     */
    public void fatal(Object message)
    {
        log(LogSystem.ERROR_ID, message);
    }

    /**
     * Passes messages to Velocity's LogSystem at "ERROR" level.
     * (it's the highest available. sorry.)
     */
    public void fatal(Object message, Throwable t)
    {
        log(LogSystem.ERROR_ID, message, t);
    }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isTraceEnabled() { return true; }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isDebugEnabled() { return true; }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isInfoEnabled() { return true; }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isWarnEnabled() { return true; }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isErrorEnabled() { return true; }

    /** 
     * Always returns true since Velocity's LogSystem 
     * doesn't provide this information. 
     *
     * @return true
     */
    public boolean isFatalEnabled() { return true; }

}

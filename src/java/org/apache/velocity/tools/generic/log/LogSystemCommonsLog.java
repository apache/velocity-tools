/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 *
 * @version $Id: LogSystemCommonsLog.java,v 1.1 2003/07/22 18:26:45 nbubna Exp $
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

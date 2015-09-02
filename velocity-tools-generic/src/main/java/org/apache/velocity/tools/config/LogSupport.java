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

import org.slf4j.Logger;

/**
 * This allows for a Logger to optionally be attached to
 * a subclass in order to output logging messages. This is simpler
 * than constantly checking whether we have a Log or not in each
 * usage throughout the classes which could use logging.  Methods should
 * only be added to this as necessary.  Performance considerations
 * can also be made later if deemed necessary.  This is meant for internal
 * use and should NOT be relied upon by VelocityTools users.
 *
 * @author Nathan Bubna
 * @version $Id: LogSupport.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public abstract class LogSupport
{
    private static final String DEFAULT_PREFIX = "";
    private Logger log;

    /**
     * Override this to set a class-specific prefix
     */
    protected String logPrefix()
    {
        return DEFAULT_PREFIX;
    }

    public void setLog(Logger log)
    {
        this.log = log;
    }

    protected Logger getLog()
    {
        return this.log;
    }

    protected boolean isWarnEnabled()
    {
        return (log != null && log.isWarnEnabled());
    }

    protected void warn(String msg)
    {
        if (isWarnEnabled())
        {
            log.warn(logPrefix() + msg);
        }
    }

    protected boolean isDebugEnabled()
    {
        return (log != null && log.isDebugEnabled());
    }

    protected void debug(String msg)
    {
        if (isDebugEnabled())
        {
            log.debug(logPrefix() + msg);
        }
    }

    protected boolean isTraceEnabled()
    {
        return (log != null && log.isTraceEnabled());
    }

    protected void trace(String msg)
    {
        if (isTraceEnabled())
        {
            log.trace(logPrefix() + msg);
        }
    }

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Redirects Velocity's LogSystem messages to commons-logging.
 *
 * <p>To use, first set up commons-logging, then tell Velocity to use
 * this class for logging by adding the following to your velocity.properties:
 *
 * <code>
 * runtime.log.logsystem.class = org.apache.velocity.tools.generic.log.CommonsLogLogSystem
 * </code>
 * </p>
 *
 * <p>You may also set this property to specify what log/name Velocity's
 * messages should be logged to (example below is default).
 * <code>
 * runtime.log.logsystem.commons.logging.name = org.apache.velocity
 * </code>
 * </p>
 * 
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @since VelocityTools 1.1
 * @version $Id: CommonsLogLogSystem.java,v 1.2 2003/11/06 00:26:54 nbubna Exp $
 */
public class CommonsLogLogSystem implements LogSystem
{

    /** Property key for specifying the name for the log instance */
    public static final String LOGSYSTEM_COMMONS_LOG_NAME =
        "runtime.log.logsystem.commons.logging.name";

    /** Default name for the commons-logging instance */
    public static final String DEFAULT_LOG_NAME = "org.apache.velocity";

    
    /** the commons-logging Log instance */
    protected Log log;


    /********** LogSystem methods *************/

    public void init(RuntimeServices rs) throws Exception
    {
        String name = 
            (String)rs.getProperty(LOGSYSTEM_COMMONS_LOG_NAME);
        
        if (name == null)
        {
            name = DEFAULT_LOG_NAME;
        }
        log = LogFactory.getLog(name);
        logVelocityMessage(LogSystem.DEBUG_ID, 
                           "CommonsLogLogSystem name is '" + name + "'");
    }

    /**
     * Send a log message from Velocity.
     */
    public void logVelocityMessage(int level, String message)
    {
        switch (level) 
        {
            case LogSystem.WARN_ID:
                log.warn(message);
                break;
            case LogSystem.INFO_ID:
                log.info(message);
                break;
            case LogSystem.DEBUG_ID:
                log.debug(message);
                break;
            case LogSystem.ERROR_ID:
                log.error(message);
                break;
            default:
                log.debug(message);
                break;
        }
    }

}

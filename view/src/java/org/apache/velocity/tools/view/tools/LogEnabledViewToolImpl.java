/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.velocity.tools.view.tools;


/**
 * <p>An implementation of the {@link LogEnabledViewTool} interface. </p> 
 * 
 * <p>This class is intended to be extended by view tools that need
 * logging. It implements logging to a logger object passed passed using
 * the {@link #setLogger} method. The implementation support three log 
 * levels ERROR, WARN and INFO and takes care of formatting the log message
 * appropriately.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 *
 * @version $Id: LogEnabledViewToolImpl.java,v 1.1 2002/04/15 18:30:29 sidler Exp $
 * 
 */
public abstract class LogEnabledViewToolImpl implements LogEnabledViewTool
{

    /**
     * Constant for error log messages.
     */
    public static final int ERROR = 10;  


    /**
     * Constant for warning log messages.
     */
    public static final int WARN = 20;
    

    /**
     * Constant for informational log messages.
     */
    public static final int INFO = 30;

    
    /**
     * A reference to the logger.
     */
    protected static ViewToolLogger logger;


    /**
     * <p>Sets a logger instance for this class of view tools. </p>
     *
     * <p>This logger can be used subsequently by instances to log 
     * messages to the logging infrastructor of the underlying framework.
     * If a logger is never set, the {@link #log(int level, String msg)} 
     * method will simply do nothing.</p>
     *
     * @param logger the logger instance to be set
     */
    public void setLogger(ViewToolLogger logger)
    {
        this.logger = logger;    
    }
    

    /**
     * <p>Writes a message to the log. </p>
     *
     * <p>The message is modified in the following ways before it is 
     * sent to the logger:</p>
     * <ul>
     *   <li>the class name is prepended</li>
     *   <li>one of <code>[ERROR]</code>, <code>[WARN]</code>, or 
     *     <code>[INFO]</code> is prepended</li>
     * </ul>
     * <p>For example, the log message "File not found." would be 
     * written to the log as:
     * <pre>
     *      org.apache.velocity.tools.tools.toolXY [ERROR] File not found.
     * </pre>
     * <p>If no logger has been set previously, the method will do nothing.</p>
     *
     * @param level log level, one of {@link #ERROR}, {@link #WARN} or 
     *     {@link #INFO}
     * @param msg log message
     */
    public void log(int level, String msg)
    {
        if (logger != null)
        {
            if (level == ERROR)
            {
                logger.log(this.getClass() + ": [ERROR] " + msg);
            }
            else if (level == WARN)
            {
                logger.log(this.getClass() + ": [WARN] " + msg);    
            }
            else
            {
                logger.log(this.getClass() + ": [INFO] " + msg);
            }
        }
        
    }

}

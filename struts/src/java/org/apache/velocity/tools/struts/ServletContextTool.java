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

package org.apache.velocity.tools.struts;

import javax.servlet.ServletContext;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ContextTool;

/**
 * <p>A basic context tool for use in Servlet-based applications. 
 * It implements logging to the logging facility provided by the
 * Servlet API. This is an abstract class that needs to be extended.</p>
 *
 * <p>This needs to be moved to the proper package.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ServletContextTool.java,v 1.1 2002/03/12 11:36:49 sidler Exp $
 * 
 */
public abstract class ServletContextTool implements ContextTool 
{

    // --------------------------------------------- Properties --------------

    /**
     * A reference to the servlet context. A class that 
     * extends this class must initialize this field with a reference to 
     * the servlet context.
     */ 
    protected ServletContext application;
    
    
    /**
     * A constant for informative log messages. No action is necessary.
     */ 
    public static final int INFO = 10;
    

    /**
     * A constant for warning log messages. Use WARNING log messages to
     * report non-critical errors while processing the template.
     */ 
    public static final int WARNING = 20;


    /**
     * A constant for error log messages. Use ERROR log messages to 
     * report severe errors during template processing.
     */ 
    public static final int ERROR = 30;


    // --------------------------------------------- Constructors -------------


    // --------------------------------------------- ContextTool Interface ----

    /**
     * A new tool object will be instantiated per-request by calling 
     * this method. A ContextTool is effectively a factory used to 
     * create objects for use in templates. Some tools may simply return
     * themselves from this method others may instantiate new objects
     * to hold the per-request state.
     */
    public abstract Object init(ViewContext context);
    
    
    /**
     * Perform any cleanup needed. This method is called after the template
     * has been processed.
     */
    public abstract void destroy(Object o);


    // --------------------------------------------- View Helpers -------------


    // --------------------------------------------- Logging Utils ------------

    /**
     * <p>Logs to the Servlet API's logging facility.</p>
     * 
     * @param level A log level. See properties {@link #INFO}, {@link #WARNING}
     * and {@link #ERROR}.
     * @param msg A message to log. The message should indicate where, why and
     *     what error occured.
     */
    public void log(int level, String msg)
    {
        // If we have no reference to the servlet context, logging cannot occur.
        if (application != null)
        {
            String lev;
            if (level == ERROR)
            {
                lev = "ERROR";
            }
            else if (level == WARNING)
            {
                lev = "WARNING";
            }
            else
            {
                lev = "INFO";
            }
            application.log( this.getClass().toString() + " [" + lev + "] " + msg);
        }        
    }


}

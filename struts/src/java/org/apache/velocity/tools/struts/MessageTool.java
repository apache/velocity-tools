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

import java.util.Locale;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.*;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ContextTool;


/**
 * <p>Context tool to work with the Struts message resources.
 * Extends ServletContextTool to profit from the logging
 * facilities of that class.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: MessageTool.java,v 1.1 2002/03/12 11:36:49 sidler Exp $
 * 
 */
public class MessageTool extends ServletContextTool
{

    // --------------------------------------------- Private Properties -------

    /**
     * A reference to the HttpServletRequest.
     */ 
    protected HttpServletRequest request;
    

    /**
     * A reference to the HtttpSession.
     */ 
    protected HttpSession session;


    /**
     * A reference to the Struts message resources.
     */
    protected MessageResources resources;


    /**
     * A reference to the user's locale.
     */
    protected Locale locale;


    
    // --------------------------------------------- Constructors -------------

    /**
     * Returns a factory. Use method {@link #init(ViewContext context)} to 
     * obtain instances of this class.
     */
    public MessageTool()
    {
    }
    
    
    /**
     * For internal use only! Use method {@link #init(ViewContext context)} 
     * to obtain instances of the tool.
     */
    private MessageTool(ViewContext context)
    {
        this.request = context.getRequest();
        this.session = request.getSession(false);
        this.application = context.getServletContext();    
        
        resources = StrutsUtils.getMessageResources(application);
        locale = StrutsUtils.getLocale(request, session);
    }
    


    // --------------------------------------------- ContextTool Interface ----

    /**
     * A new tool object will be instantiated per-request by calling 
     * this method. A context tool is effectively a factory used to 
     * create objects for use in templates. Some tools may simply return
     * themselves from this method others may instantiate new objects
     * to hold the per-request state.
     */
    public Object init(ViewContext context)
    {
        return new MessageTool(context);
    }


    /**
     * Perform any cleanup needed. This method is called after the template
     * has been processed.
     */
    public void destroy(Object o)
    {
    }



    // --------------------------------------------- View Helpers -------------

    /**
     * Looks up and returns the localized message for the specified key.
     * The user's locale is consulted to determine the language of the 
     * message.
     *
     * @param key message key
     * @return the localized message for the specified key or 
     * <code>null</code> if no such message exists
     */
    public String get(String key)
    {
        if (resources == null)
        {
            log(ERROR, "Message resources are not available.");
            return null;
        }
        return resources.getMessage(locale, key);
    }


    /**
     * Looks up and returns the localized message for the specified key.
     * Replacement parameters passed with <code>args</code> are 
     * inserted into the message. The user's locale is consulted to 
     * determine the language of the message.
     *
     * @param key message key
     * @param args replacement parameters for this message
     * @return the localized message for the specified key or 
     * <code>null</code> if no such message exists
     */
    public String get(String key, Object args[])
    {
        if (resources == null)
        {
            log(ERROR, "Message resources are not available.");
            return null;
        }
        
        // return the requested message
        if (args == null)
        {
            return resources.getMessage(locale, key);
        }
        else
        {
            return resources.getMessage(locale, key, args);
        }
    }


    /**
     * Same as {@link #get(String key, Object[] args)}, but takes a
     * <code>java.util.ArrayList</code> instead of an array. This is 
     * more Velocity compatible. 
     */
    public String get(String key, ArrayList args)
    {
        return get(key, args.toArray());        
    }


    /**
     * Checks if a message string for a specified message key exists
     * for the user's locale.
     *
     * @param key message key
     * @return <code>true</code> if a message strings exists, 
     * <code>false</code> otherwise
     */
    public boolean exists(String key)
    {
        if (resources == null)
        {
            log(ERROR, "Message resources are not available.");
            return false;
        }

        // Return the requested message presence indicator
        return (resources.isPresent(locale, key));
    }


    /**
     * Returns the user's locale. If a locale is not found, the default 
     * locale is returned.
     */
    public Locale getLocale()
    {
        return locale;
    }
    
}

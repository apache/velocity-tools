/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.*;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ContextTool;


/**
 * <p>Struts context tool. Its functionality covers partially the 
 * Struts bean tag lib for JSP.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: StrutsBeanTool.java,v 1.1 2002/01/03 20:21:27 geirm Exp $
 * 
 */
public class StrutsBeanTool implements ContextTool {

    // --------------------------------------------- Private Properties -------

    /**
     * A reference to the HtttpServletRequest.
     */ 
    private HttpServletRequest request;
    

    /**
     * A reference to the HtttpSession.
     */ 
    private HttpSession session;


    /**
     * A reference to the ServletContext.
     */ 
    private ServletContext application;


    
    // --------------------------------------------- View Helpers -------------

    /**
     * Return the localized message for the specified key,
     * otherwise return <code>null</code>.
     *
     * @param key Message key
     */
    public String message(String key) {

        MessageResources resources = ContextHelper.getMessageResources(application);
        if (resources == null)
            return null;
        return resources.getMessage(ContextHelper.getLocale(request, session),key);

    }


    /**
     * Look up and return a message string, based on the specified parameters.
     *
     * @param key Message key to be looked up and returned
     * @param args Replacement parameters for this message
     */
    public String message(String key, Object args[]) {

        MessageResources resources = ContextHelper.getMessageResources(application);

        if (resources == null) return null;

        // Return the requested message
        if (args == null)
            return (resources.getMessage(ContextHelper.getLocale(request, session), key));
        else
            return (resources.getMessage(ContextHelper.getLocale(request, session), key, args));

    }


    /**
     * Return true if a message string for the specified message key
     * is present for the user's Locale.
     *
     * @param key Message key
     */
    public boolean isMessage(String key) {

        // Look up the requested MessageResources
        MessageResources resources = ContextHelper.getMessageResources(application);

        if (resources == null) return false;

        // Return the requested message presence indicator
        return (resources.isPresent(ContextHelper.getLocale(request, session), key));

    }



    // --------------------------------------------- Constructors -------------

    /**
     * Constructor to obtain a factory. Use method {@link #init(ChainedContext context)} to obtain 
     * instances of the tool.
     */
    public StrutsBeanTool()
    {
    }
    
    
    /**
     * For internal use only! Use method {@link #init(ChainedContext context)} to obtain instances of 
     * the tool.
     */
    public StrutsBeanTool( ViewContext context)
    {
        this.request = context.getRequest();
        this.session = request.getSession(false);
        this.application = context.getServletContext();    
    }
    


    // --------------------------------------------- ContextTool Interface ----

   /**
     * A new tool object will be instantiated per-request by calling 
     * this method. A ContextTool is effectively a factory used to 
     * create objects for use in templates. Some tools may simply return
     * themselves from this method others may instantiate new objects
     * to hold the per-request state.
     */
   public Object init( ViewContext context)
   {
        return new StrutsBeanTool(context);
   }


   /**
     * At the end of processing this method will be called to 
     * return the object generated by init(), in case it needs
     * to be recycled or otherwise cleaned up.
     */
   public void destroy(Object o)
   {
   }

}

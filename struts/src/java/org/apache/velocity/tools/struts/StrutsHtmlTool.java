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

import java.util.Iterator;

/**
 * <p>Struts context tool. Its functionality covers partially the 
 * Struts html tag lib for JSP.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: StrutsHtmlTool.java,v 1.2 2002/01/09 11:25:44 sidler Exp $
 * 
 */
public class StrutsHtmlTool implements ContextTool 
{


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
     * Renders the reference for a HTML <base> element
     *
     * @author Luis Arias <luis@elysia.com>
     * @author Ted Husted
     */
    public String baseRef() 
    {

        if (request==null) return null;

        StringBuffer result = new StringBuffer();
        result.append(request.getScheme());
        result.append("://");
        result.append(request.getServerName());
        if ("http".equals(request.getScheme()) && (80 == request.getServerPort())) 
        {
            ;
        } 
        else if ("https".equals(request.getScheme()) && (443 == request.getServerPort())) 
        {
            ;
        } 
        else 
        {
            result.append(":");
            result.append(request.getServerPort());
        }
        result.append(request.getRequestURI());

        return result.toString();
    }


    /**
     * Return the number of error messages.
     */
    public int errorSize() 
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
            return 0;

        return actionErrors.size();
    }


    /**
     * Return true if there are no errors queued
     */
    public boolean errorsEmpty() 
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
            return false;

        return actionErrors.empty();
    }


    /**
     * Return the error messages
     */
    public Iterator errors() 
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
            return null;

        return actionErrors.get();
    }


    /**
     * Return an ActionError for a property
     *
     * @param property Property name
     */
    public Iterator errors(String property) 
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
            return null;

        return actionErrors.get(property);
    }


    /**
     * Return the number of error messages.
     *
     * @param property Property name
     */
    public int errorSize(String property) 
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
            return 0;

        return actionErrors.size(property);
    }


    /**
     * Returns the errors.header, any errors, and the errors.footer.
     *
     * @param property Property name
     */
    public String errorOutput(String property) 
    {

        ActionErrors errors = ContextHelper.getActionErrors(request);

        if ((errors==null) || (errors.empty()))
            return null;

        // Check for presence of header and footer message keys
        boolean headerPresent = isMessage("errors.header");
        boolean footerPresent = isMessage("errors.footer");

        // Render the error messages appropriately
        StringBuffer results = new StringBuffer();
        String message = null;
        if (headerPresent)
            message = getMessage("errors.header");
        Iterator reports = null;

        if (property == null)
            reports = errors.get();
        else
            reports = errors.get(property);

       // Render header if this is a global tag or there is an error for this property
       boolean propertyMsgPresent = reports.hasNext();
       if ((message != null)&&(property == null) || propertyMsgPresent) 
       {
           results.append(message);
           results.append("\r\n");
       }

        while (reports.hasNext()) 
        {
            ActionError report = (ActionError) reports.next();
            message = getMessage( report.getKey(), report.getValues());
            if (message != null) 
            {
                results.append(message);
                results.append("\r\n");
            }
        }
        message = null;
        if (footerPresent)
            message = getMessage("errors.footer");

        if ((message != null)&&(property == null) || propertyMsgPresent) 
        {
            results.append(message);
            results.append("\r\n");
        }

        // return result
        return results.toString();

    }


    /**
     * Wrapper for getErrorMarkup(null)
     */
    public String errorMarkup() 
    {
        return errorOutput((String) null);
    }


    /**
     * Return the form action converted into a server-relative URL.
     */
    public String actionMappingURL(String action) 
    {

        StringBuffer value = new StringBuffer(this.request.getContextPath());

        // Use our servlet mapping, if one is specified
        String servletMapping = ContextHelper.getServletMapping(application);

        if (servletMapping != null) 
        {
            String queryString = null;
            int question = action.indexOf("?");
            if (question >= 0)
                queryString = action.substring(question);
            String actionMapping = getActionMappingName(action);
            if (servletMapping.startsWith("*.")) 
            {
                value.append(actionMapping);
                value.append(servletMapping.substring(1));
            } 
            else if (servletMapping.endsWith("/*")) 
            {
                value.append(servletMapping.substring
                             (0, servletMapping.length() - 2));
                value.append(actionMapping);
            }
            if (queryString != null)
                value.append(queryString);
        }

        // Otherwise, assume extension mapping is in use and extension is
        // already included in the action property
        else 
        {
            if (!action.startsWith("/"))
                value.append("/");
            value.append(action);
        }

        // Return the completed value
        return (value.toString());

    }


    /**
     * Return the path for the specified forward,
     * otherwise return <code>null</code>.
     *
     * @param name Name given to local or global forward.
     */
     public String link(String name) 
     {

        ActionForward forward = ContextHelper.getActionForward(name, application);
        if (forward == null)
            return null;

       StringBuffer path = new StringBuffer(this.request.getContextPath());
       path.append(forward.getPath());

       // :TODO: What about runtime parameters?

       return path.toString();

    }




    // --------------------------------------------- Internal Utiliteis -------

    /**
     * Return the form action converted into an action mapping path.  The
     * value of the <code>action</code> property is manipulated as follows in
     * computing the name of the requested mapping:
     * <ul>
     * <li>Any filename extension is removed (on the theory that extension
     *     mapping is being used to select the controller servlet).</li>
     * <li>If the resulting value does not start with a slash, then a
     *     slash is prepended.</li>
     * </ul>
     */
    private String getActionMappingName(String action)
    {

        String value = action;
        int question = action.indexOf("?");
        if (question >= 0)
            value = value.substring(0, question);
        int slash = value.lastIndexOf("/");
        int period = value.lastIndexOf(".");
        if ((period >= 0) && (period > slash))
            value = value.substring(0, period);
        if (value.startsWith("/"))
            return (value);
        else
            return ("/" + value);

    }


    /**
     * Return the localized message for the specified key,
     * otherwise return <code>null</code>.
     *
     * @param key Message key
     */
    private String getMessage(String key) 
    {
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
    public String getMessage(String key, Object args[]) 
    {

        MessageResources resources = ContextHelper.getMessageResources(application);

        if (resources == null) 
            return null;

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
    private boolean isMessage(String key) 
    {

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
    public StrutsHtmlTool()
    {
    }
    
    
    /**
     * For internal use only! Use method {@link #init(ChainedContext context)} to obtain instances of 
     * the tool.
     */
    public StrutsHtmlTool( ViewContext context)
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
        return new StrutsHtmlTool(context);
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

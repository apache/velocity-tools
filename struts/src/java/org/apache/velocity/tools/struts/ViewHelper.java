/*
 * $Header: /home/cvs/jakarta-velocity-tools/struts/src/java/org/apache/velocity/tools/struts/Attic/ViewHelper.java,v 1.1 2002/01/03 20:21:28 geirm Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/03 20:21:28 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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
 *
 */


package org.apache.velocity.tools.struts;


import java.util.Locale;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.apache.struts.util.MessageResources;

import org.apache.struts.action.*; // expediancy


/**
 * <p>A helper object that provides a set of tools for the view.</p> 
 *
 * <p>This class is currently not used. It contains some code that
 * might be useful as example to developers of Struts-specific context 
 * tools.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>, based
 * on code by <a href="mailto:ted@husted.org">Ted Husted</a>
 *
 * @version $Revision: 1.1 $ $Date: 2002/01/03 20:21:28 $
 */
public class ViewHelper
{


// --------------------------------------------------------  Properites


    /**
     * The application associated with this instance.
     */
    private ServletContext application = null;


    /**
     * Set the application associated with this instance.
     * [servlet.getServletContext()]
     */
    public void setApplication(ServletContext application)
    {
        this.application = application;
    }


    /**
     * The session associated with this instance.
     */
    private HttpSession session = null;


    /**
     * Set the session associated with this instance.
     */
    public void setSession(HttpSession session)
    {
        this.session = session;
    }


    /**
     * The request associated with this instance.
     */
    private HttpServletRequest request = null;


    /**
     * Set the request associated with this object.
     * Session object is also set or cleared.
     */
    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
        if (this.request==null)
        {
            setSession(null);
        }
        else
        {
            setSession(this.request.getSession());
        }
    }


    /**
     * Set the application and request for this object instance.
     * The ServletContext can be set by any servlet in the application.
     * The request should be the instant request.
     * Most of the other methods retrieve their own objects
     * by reference to the application, request, or session
     * attributes.
     * Do not call other methods without setting these first!
     * This is also called by the convenience constructor.
     *
     * @param application - The associated ServletContext.
     * @param request - The associated HTTP request.
     */
    public void setResources(ServletContext application, HttpServletRequest request)
    {
        setApplication(application);
        setRequest(request);
    }



// ---------------------------------------------------- Utility Methods

    /**
     * Return true if a message string for the specified message key
     * is present for the user's Locale.
     *
     * @param key Message key
     */
    public boolean isMessage(String key)
    {

        // Look up the requested MessageResources
        MessageResources resources = ContextHelper.getMessageResources(application);

        if (resources == null)
        {
            return false;
        }

        // Return the requested message presence indicator
        return (resources.isPresent(ContextHelper.getLocale(request, session), key));

    }



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
    protected String getActionMappingName(String action)
    {

        String value = action;
        int question = action.indexOf("?");

        if (question >= 0)
        {
            value = value.substring(0, question);
        }

        int slash = value.lastIndexOf("/");
        int period = value.lastIndexOf(".");

        if ((period >= 0) && (period > slash))
        {
            value = value.substring(0, period);
        }

        if (value.startsWith("/"))
        {
            return (value);
        }
        else
        {
            return ("/" + value);
        }
    }


    /**
     * Return the form action converted into a server-relative URL.
     */
    protected String getActionMappingURL(String action)
    {
        StringBuffer value = new StringBuffer(this.request.getContextPath());

        // Use our servlet mapping, if one is specified
        String servletMapping = ContextHelper.getServletMapping(application);

        if (servletMapping != null)
        {
            String queryString = null;
            int question = action.indexOf("?");

            if (question >= 0)
            {
                queryString = action.substring(question);
            }

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
            {
                value.append(queryString);
            }
        }
        else
        {
            /*
             * Otherwise, assume extension mapping is in use and extension is
             * already included in the action property
             */

            if (!action.startsWith("/"))
            {
                value.append("/");
            }

            value.append(action);
        }

        // Return the completed value
        return (value.toString());

    }



// ------------------------------------------------ Presentation API


    /**
     * Renders the reference for a HTML <base> element
     *
     * @author Luis Arias <luis@elysia.com>
     * @author Ted Husted
     */
    public String getBaseRef()
    {
        if (request==null)
        {
           return null;
        }

        StringBuffer result = new StringBuffer();

        result.append(request.getScheme());
        result.append("://");
        result.append(request.getServerName());

        if ("http".equals(request.getScheme()) &&
            (80 == request.getServerPort()))
        {
            // do nothing
            ;
        }
        else if ("https".equals(request.getScheme()) &&
                   (443 == request.getServerPort()))
        {
            // do nothing
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
     * Return the path for the specified forward,
     * otherwise return <code>null</code>.
     *
     * @param name Name given to local or global forward.
     */
    public String getLink(String name)
    {
        ActionForward forward = ContextHelper.getActionForward(name, application);

        if (forward == null)
        {
            return null;
        }

       StringBuffer path = new StringBuffer(this.request.getContextPath());
       path.append(forward.getPath());

       // :TODO: What about runtime parameters?

       return path.toString();
    }


    /**
     * Wrapper for getLink(String)
     *
     * @param name Name given to local or global forward.
     */
    public String link(String name)
    {
        return getLink(name);
    }


    /**
     * Return the localized message for the specified key,
     * otherwise return <code>null</code>.
     *
     * @param key Message key
     */
    public String getMessage(String key)
    {
        MessageResources resources = ContextHelper.getMessageResources(application);

        if (resources == null)
        {
            return null;
        }

        return resources.getMessage(ContextHelper.getLocale(request, session),key);
    }

    /**
     * Wrapper for getMessage(String)
     *
     * @param key Message key
     */
    public String message(String key)
    {
        return getMessage(key);
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
        {
           return null;
        }

        // Return the requested message
        if (args == null)
        {
            return (resources.getMessage(ContextHelper.getLocale(request, session), key));
        }
        else
        {
            return (resources.getMessage(ContextHelper.getLocale(request, session), key, args));
        }
    }

    /**
     * Wrapper for getMessage(String,Object[])
     *
     * @param key Message key to be looked up and returned
     * @param args Replacement parameters for this message
     */
    public String message(String key, Object args[])
    {
        return getMessage(key, args);
    }


    /**
     * Return the URL for the specified ActionMapping,
     * otherwise return <code>null</code>.
     *
     * @param name Name given to local or global forward.
     */
    public String getAction(String path)
    {
        return getActionMappingURL(path);
    }

    /**
     * Wrapper for getAction(String)
     *
     * @param name Name given to local or global forward.
     */
    public String action(String path)
    {
        return getAction(path);
    }


    /**
     * Return the number of error messages.
     */
    public int getErrorSize()
    {
        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
        {
            return 0;
        }

        return actionErrors.size();
    }

    /**
     * Alias for getErrorSize()
     */
    public int errorSize()
    {
        return getErrorSize();
    }


    /**
     * Return true if there are no errors queued
     */
    public boolean getErrorsEmpty()
    {
        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
        {
            return false;
        }

        return actionErrors.empty();
    }

    /**
     * Wrapper for getErrorEmpty()
     */
    public boolean errorsEmpty()
    {
        return getErrorsEmpty();
    }


    /**
     * Return the error messages
     */
    public Iterator getErrors()
    {
        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
        {
            return null;
        }

        return actionErrors.get();
    }

    /**
     * Wrapper for getErrors()
     */
    public Iterator errors()
    {
        return errors();
    }


    /**
     * Return an ActionError for a property
     *
     * @param property Property name
     */
    public Iterator getErrors(String property)
    {

        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
        {
            return null;
        }

        return actionErrors.get(property);
    }

    /**
     * Wrapper for getErrors(String)
     */
    public Iterator errors(String property)
    {
        return getErrors(property);
    }


    /**
     * Return the number of error messages.
     *
     * @param property Property name
     */
    public int getErrorSize(String property)
    {
        ActionErrors actionErrors = ContextHelper.getActionErrors(request);

        if (actionErrors == null)
        {
            return 0;
        }

        return actionErrors.size(property);
    }

    /**
     * Wrapper for getErrorSize(String)
     *
     * @param property Property name
     */
    public int errorSize(String property)
    {
        return getErrorSize(property);
    }


    /**
     * Returns the errors.header, any errors, and the errors.footer.
     *
     * @param property Property name
     */
    public String getErrorOutput(String property)
    {
        ActionErrors errors = ContextHelper.getActionErrors(request);

        if ((errors==null) || (errors.empty()))
        {
            return null;
        }

        // Check for presence of header and footer message keys
        boolean headerPresent = isMessage("errors.header");
        boolean footerPresent = isMessage("errors.footer");

        // Render the error messages appropriately
        StringBuffer results = new StringBuffer();
        String message = null;

        if (headerPresent)
        {
            message = getMessage("errors.header");
        }

        Iterator reports = null;

        if (property == null)
        {
            reports = errors.get();
        }
        else
        {
            reports = errors.get(property);
        }

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
            message = getMessage( report.getKey(),
                report.getValues());
            if (message != null)
            {
                results.append(message);
                results.append("\r\n");
            }
        }

        message = null;

        if (footerPresent)
        {
            message = getMessage("errors.footer");
        }

        if ((message != null)&&(property == null) || propertyMsgPresent)
        {
            results.append(message);
            results.append("\r\n");
        }

        // return result
        return results.toString();

    }

    /**
     * Wrapper for getErrorMarkup(String)
     */
    public String errorOutput(String property)
    {
        return getErrorOutput(property);
    }

    /**
     * Wrapper for getErrorMarkup(null)
     */
    public String getErrorMarkup()
    {
        return getErrorOutput((String) null);
    }

    /**
     * Wrapper for getErrorMarkup()
     */
    public String errorMarkup()
    {
        return getErrorMarkup();
    }


// ------------------------------------------------------- Constructors


    public ViewHelper()
    {
        super();
    }


    public ViewHelper(ServletContext application,
            HttpServletRequest request)
    {
        super();
        setResources(application,request);
    }

}

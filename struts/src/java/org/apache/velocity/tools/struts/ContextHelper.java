/*
 * $Header: /home/cvs/jakarta-velocity-tools/struts/src/java/org/apache/velocity/tools/struts/Attic/ContextHelper.java,v 1.1 2002/01/03 20:21:27 geirm Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/03 20:21:27 $
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
 * A helper object to expose the Struts shared resources,
 * which are be stored in the application, session, or
 * request contexts, as appropriate.<br>
 *
 * This class provides all static methods. No use to 
 * instantiate an object.<br>
 *
 * Provided for use by other servlets in the application
 * so they can easily access the Struts shared resources.<br>
 *
 * NOTE: THIS CLASS IS UNDER ACTIVE DEVELOPMENT.
 * THE CURRENT CODE IS WRITTEN FOR CLARITY NOT EFFICIENCY.
 * NOT EVERY API FUNCTION HAS BEEN IMPLEMENTED YET.<br>
 *
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>, based
 * on code by <a href="mailto:ted@husted.org">Ted Husted</a>
 *
 * @version $Revision: 1.1 $ $Date: 2002/01/03 20:21:27 $
 */
public class ContextHelper
{

// ------------------------------------------------ Application Context

    /**
     * The strong>default</strong>
     * configured data source (which must implement
     * <code>javax.sql.DataSource</code>),
     * if one is configured for this application.
     */
    public static DataSource getDataSource(ServletContext application)
    {
        if (application==null)
        {
           return null;
        }

        return (DataSource)
            application.getAttribute(Action.DATA_SOURCE_KEY);
    }


/*
This method depends of features that are not available in Struts 1.0
    public static ActionMessages getActionMessages(ServletContext application) {

        if (application==null)
            return null;
        return (ActionMessages)
            application.getAttribute(Action.MESSAGE_KEY);

    }
*/

    /**
     * The <code>org.apache.struts.action.ActionFormBeans</code> collection
     * for this application.
     */
    public static ActionFormBeans getActionFormBeans(ServletContext application)
    {
        if (application==null)
        {
            return null;
        }

        return (ActionFormBeans)
            application.getAttribute(Action.FORM_BEANS_KEY);
    }


    /**
     * Return the form bean definition associated with the specified
     * logical name, if any; otherwise return <code>null</code>.
     *
     * @param name Logical name of the requested form bean definition
     */
    public static ActionFormBean getFormBean(String name, ServletContext application)
    {
        ActionFormBeans formBeans = getActionFormBeans(application);

        if (formBeans==null)
        {
            return null;
        }

        return formBeans.findFormBean(name);

    }


    /**
     * The <code>org.apache.struts.action.ActionForwards</code> collection
     * for this application.
     */
    public static ActionForwards getActionForwards(ServletContext application)
    {
        if (application==null)
        {
            return null;
        }

        return (ActionForwards)
            application.getAttribute(Action.FORWARDS_KEY);
    }


    /**
     * Return the forwarding associated with the specified logical name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Logical name of the requested forwarding
     */
    public static ActionForward getActionForward(String name, ServletContext application)
    {
        ActionForwards forwards = getActionForwards(application);

        if (forwards==null)
        {
            return null;
        }

        return forwards.findForward(name);
    }


    /**
     * The context attributes key under which our
     * <code>org.apache.struts.action.ActionMappings</code> collection
     * is normally stored, unless overridden when initializing our
     * ActionServlet.
     */
    public static ActionMappings getActionMappings(ServletContext application)
    {
        if (application==null)
        {
            return null;
        }

        return (ActionMappings)
            application.getAttribute(Action.MAPPINGS_KEY);
    }


    /**
     * Return the mapping associated with the specified request path, if any;
     * otherwise return <code>null</code>.
     *
     * @param path Request path for which a mapping is requested
     */
    public static ActionMapping getActionMapping(String path, ServletContext application)
    {
        ActionMappings mappings = getActionMappings(application);

        if (mappings==null)
        {
            return null;
        }

        return mappings.findMapping(path);

    }


    /**
     * The application resources for this application.
     */
    public static MessageResources getMessageResources(ServletContext application)
    {
        if (application==null)
        {
            return null;
        }

        return (MessageResources)
            application.getAttribute(Action.MESSAGES_KEY);
    }


    /**
     * The path-mapped pattern (<code>/action/*</code>) or
     * extension mapped pattern ((<code>*.do</code>)
     * used to determine our Action URIs in this application.
     */
    public static String getServletMapping(ServletContext application)
    {
        if (application==null)
        {
            return null;
        }

        return (String)
            application.getAttribute(Action.SERVLET_KEY);
    }


// ---------------------------------------------------- Session Context


    /**
     * The <code>java.util.Locale</code> for the user, if any.
     * If a default locale object is not in the user's session,
     * the system default locale is returned.
     * If used, the user locale is typically set during login
     * processing under the key <code>Action.LOCALE_KEY</code>.
     */
    public static Locale getLocale(HttpServletRequest request, HttpSession session)
    {
        Locale locale = null;

        if (session!=null)
        {
            locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
        }

        if ((locale==null) && (request!=null))
        {
            locale = request.getLocale();
        }

        return locale;
    }


    /**
     * The transaction token stored in this session, if it is used.
     */
    public static String getToken(HttpSession session)
    {

        if (session==null)
        {
            return null;
        }

        return (String) session.getAttribute(Action.TRANSACTION_TOKEN_KEY);

    }


// ---------------------------------------------------- Request Context


    /**
     * The <code>org.apache.struts.action.ActionErrors</code> object,
     * for this request.
     */
    public static ActionErrors getActionErrors(HttpServletRequest request)
    {
        if (request==null)
        {
            return null;
        }

        return (ActionErrors)
            request.getAttribute(Action.ERROR_KEY);
    }


    /**
     * The runtime JspException that may be been thrown by a Struts tag
     * extension, or compatible presentation extension, and placed
     * in the request.
     */
    public static Throwable getException(HttpServletRequest request)
    {
        if (request==null)
        {
            return null;
        }

        return (Throwable)
            request.getAttribute(Action.EXCEPTION_KEY);
    }


    /**
     * The multipart object for this request.
     */
    public static MultipartRequestWrapper getMultipartRequestWrapper(HttpServletRequest request)
    {
        if (request==null)
        {
            return null;
        }

        return (MultipartRequestWrapper)
            request.getAttribute(Action.MULTIPART_KEY);
    }


   /**
     * The <code>org.apache.struts.ActionMapping</code>
     * instance for this request.
     */
    public static ActionMapping getMapping(HttpServletRequest request)
    {
        if (request==null)
        {
           return null;
        }

        return (ActionMapping)
            request.getAttribute(Action.MAPPING_KEY);
    }


    /*
     * Retrieve and return the <code>ActionForm</code> bean associated with
     * this mapping, creating and stashing one if necessary.  If there is no
     * form bean associated with this mapping, return <code>null</code>.
     *
     */
     public static ActionForm getActionForm(HttpServletRequest request, HttpSession session)
    {

        // Is there a mapping associated with this request?
        ActionMapping mapping = getMapping(request);

        if (mapping == null)
        {
            return (null);
        }

        // Is there a form bean associated with this mapping?
        String attribute = mapping.getAttribute();

        if (attribute == null)
        {
            return (null);
        }

        // Look up the existing form bean, if any
        ActionForm instance = null;

        if ("request".equals(mapping.getScope()))
        {
            instance = (ActionForm) request.getAttribute(attribute);
        }
        else
        {
            instance = (ActionForm) session.getAttribute(attribute);
        }

        return instance;
    }
}

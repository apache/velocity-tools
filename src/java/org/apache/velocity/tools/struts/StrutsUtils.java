/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.struts;


import java.util.Locale;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;

/* deprecated imports */
import org.apache.struts.action.ActionFormBeans;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionForwards;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMappings;

/**
 * <p>A utility class to expose the Struts shared
 * resources. All methods are static.</p>
 *
 * <p>This class is provided for use by Velocity view tools
 * that need access to Struts resources. By having all Struts-
 * specific code in this utility class, maintenance is simplified
 * and reuse fostered.</p>
 *
 * <p>It is the aim, that sooner or later the functionality in
 * this class is integrated into Struts itself.  See
 * <a href="http://nagoya.apache.org/bugzilla/show_bug.cgi?id=16814">Bug #16814</a>
 * for more on that.</p>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * based on code by <a href="mailto:ted@husted.org">Ted Husted</a>
 *
 * @version $Id: StrutsUtils.java,v 1.16.2.1 2004/03/12 20:16:27 nbubna Exp $
 */
public class StrutsUtils
{

    /****************** Struts ServletContext Resources ****************/

    /**
     * Returns the default configured data source (which must implement
     * <code>javax.sql.DataSource</code>) or <code>null</code> if not found.
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static DataSource getDataSource(ServletContext app)
    {
        return (DataSource)app.getAttribute(Globals.DATA_SOURCE_KEY);
    }


    /**
     * Returns the <code>org.apache.struts.action.ActionFormBeans</code>
     * collection for this application or <code>null</code> if not found.
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static ActionFormBeans getActionFormBeans(ServletContext app)
    {
        return (ActionFormBeans)app.getAttribute(Globals.FORM_BEANS_KEY);
    }


    /**
     * Returns the form bean definition associated with the specified
     * logical name or <code>null</code> if not found.
     *
     * @param name logical name of the requested form bean definition
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static FormBeanConfig getFormBean(String name, ServletContext app)
    {
        ActionFormBeans formBeans = getActionFormBeans(app);
        if (formBeans == null)
        {
            return null;
        }
        return formBeans.findFormBean(name);

    }


    /**
     * Returns the <code>org.apache.struts.action.ActionForwards</code>
     * collection for this application or <code>null</code> if not found.
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static ActionForwards getActionForwards(ServletContext app)
    {
        return (ActionForwards)app.getAttribute(Globals.FORWARDS_KEY);
    }


    /**
     * Returns the forwarding associated with the specified logical name
     * or <code>null</code> if not found.
     *
     * @param name Logical name of the requested forwarding
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static ActionForward getActionForward(String name,
                                                 ServletContext app)
    {
        ActionForwards forwards = getActionForwards(app);
        if (forwards == null)
        {
            return null;
        }
        return forwards.findForward(name);
    }


    /**
     * Returns the <code>org.apache.struts.action.ActionMappings</code>
     * collection for this application or <code>null</code> if not found.
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static ActionMappings getActionMappings(ServletContext app)
    {
        return (ActionMappings)app.getAttribute(Globals.MAPPINGS_KEY);
    }


    /**
     * Returns the mapping associated with the specified request path,
     * or <code>null</code> if not found.
     *
     * @param path Request path for which a mapping is requested
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static ActionMapping getActionMapping(String path,
                                                 ServletContext app)
    {
        ActionMappings mappings = getActionMappings(app);
        if (mappings == null)
        {
            return null;
        }
        return mappings.findMapping(path);
    }


    /**
     * Returns the message resources for this application or <code>null</code>
     * if not found.
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static MessageResources getMessageResources(ServletContext app)
    {
        return (MessageResources)app.getAttribute(Globals.MESSAGES_KEY);
    }


    /**
     * Returns the message resources for this application or <code>null</code>
     * if not found.
     *
     * @param app the servlet context
     * @since VelocityTools 1.1
     */
    public static MessageResources getMessageResources(HttpServletRequest request,
                                                       ServletContext app)
    {
        /* Identify the current module */
        ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request, app);
        return (MessageResources)app.getAttribute(Globals.MESSAGES_KEY +
                                                  moduleConfig.getPrefix());
    }


    /**
     * Returns the message resources with the specified bundle name for this application
     * or <code>null</code> if not found.
     *
     * @param app the servlet context
     * @param bundle The bundle name to look for.  If this is <code>null</code>, the
     *               default bundle name is used.
     * @since VelocityTools 1.1
     */
    public static MessageResources getMessageResources(HttpServletRequest request,
                                                       ServletContext app,
                                                       String bundle)
    {
        MessageResources resources = null;

        /* Identify the current module */
        ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request, app);


        if (bundle == null) {
            bundle = Globals.MESSAGES_KEY;
        }

        // First check request scope
        resources = (MessageResources) request.getAttribute(bundle + moduleConfig.getPrefix());

        if (resources == null) {
            resources = (MessageResources) app.getAttribute(bundle + moduleConfig.getPrefix());
        }

        return resources;
    }


    /**
     * Select the module to which the specified request belongs, and
     * add return the corresponding ModuleConfig.
     *
     * @param urlPath The requested URL
     * @param app The ServletContext for this web application
     * @return The ModuleConfig for the given URL path
     * @since VelocityTools 1.1
     */
    public static ModuleConfig selectModule(String urlPath,
                                            ServletContext app)
    {
        /* Match against the list of sub-application prefixes */
        String prefix = RequestUtils.getModuleName(urlPath, app);

        /* Expose the resources for this sub-application */
        ModuleConfig config = (ModuleConfig)
            app.getAttribute(Globals.MODULE_KEY + prefix);

        return config;
    }


    /**
     * Returns the servlet mapping used for this application or
     * <code>null</code> if not found. The servlet mapping is
     * either a path-mapped pattern (<code>/action/*</code>) or an
     * extension mapped pattern (<code>*.do</code>).
     *
     * @param app the servlet context
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static String getServletMapping(ServletContext app)
    {
        return (String)app.getAttribute(Globals.SERVLET_KEY);
    }


    /********************** Struts Session Resources ******************/

    /**
     * Returns the <code>java.util.Locale</code> for the user. If a
     * locale object is not found in the user's session, the system
     * default locale is returned.
     *
     * @param request the servlet request
     * @param session the HTTP session
     */
    public static Locale getLocale(HttpServletRequest request,
                                   HttpSession session)
    {
        Locale locale = null;

        if (session != null)
        {
            locale = (Locale)session.getAttribute(Globals.LOCALE_KEY);
        }
        if (locale == null)
        {
            locale = request.getLocale();
        }
        return locale;
    }


    /**
     * Returns the transaction token stored in this session or
     * <code>null</code> if not used.
     *
     * @param session the HTTP session
     */
    public static String getToken(HttpSession session)
    {
        if (session == null)
        {
            return null;
        }
        return (String)session.getAttribute(Globals.TRANSACTION_TOKEN_KEY);
    }


    /*********************** Struts Request Resources ****************/

    /**
     * Returns the <code>org.apache.struts.action.ActionErrors</code>
     * object for this request or <code>null</code> if none exists.
     *
     * @param request the servlet request
     */
    public static ActionErrors getActionErrors(HttpServletRequest request)
    {
        return (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
    }


    /**
     * Returns the <code>org.apache.struts.action.ActionMessages</code>
     * object for this request or <code>null</code> if none exists.
     *
     * @param request the servlet request
     * @since VelocityTools 1.1
     */
    public static ActionMessages getActionMessages(HttpServletRequest request)
    {
        return (ActionMessages)request.getAttribute(Globals.MESSAGE_KEY);
    }


    /**
     * Returns the runtime Exception that may have been thrown by a
     * Struts view tool or compatible presentation extension, and
     * placed in the request. Returns <code>null</code> if none found.
     *
     * @param request the servlet request
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static Throwable getException(HttpServletRequest request)
    {
        return (Throwable)request.getAttribute(Globals.EXCEPTION_KEY);
    }


    /**
     * Returns the multipart object for this request or <code>null</code>
     * if none exists.
     *
     * @param request the servlet request
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static MultipartRequestWrapper getMultipartRequestWrapper(HttpServletRequest request)
    {
        return (MultipartRequestWrapper)request.getAttribute(Globals.MULTIPART_KEY);
    }


    /**
     * Returns the <code>ActionForm</code> bean associated with
     * this request of <code>null</code> if none exists.
     *
     * @param request the servlet request
     * @param session the HTTP session
     */
    public static ActionForm getActionForm(HttpServletRequest request,
                                           HttpSession session)
    {
        /* Is there a mapping associated with this request? */
        ActionConfig mapping =
            (ActionConfig)request.getAttribute(Globals.MAPPING_KEY);
        if (mapping == null)
        {
            return null;
        }

        /* Is there a form bean associated with this mapping? */
        String attribute = mapping.getAttribute();
        if (attribute == null)
        {
            return null;
        }

        /* Look up the existing form bean */
        if ("request".equals(mapping.getScope()))
        {
            return (ActionForm)request.getAttribute(attribute);
        }
        if (session != null)
        {
            return (ActionForm)session.getAttribute(attribute);
        }
        return null;
    }


    /********************* Important Struts Constants *****************/

    /**
     * Returns the query parameter name under which a cancel button press
     * must be reported if form validation is to be skipped.
     *
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static String getCancelName()
    {
        return org.apache.struts.taglib.html.Constants.CANCEL_PROPERTY;
    }


    /**
     * Returns the default "GLOBAL" category name that can be used with
     * messages that are not associated with a particular property.
     *
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static String getGlobalErrorName()
    {
        return org.apache.struts.action.ActionErrors.GLOBAL_ERROR;
    }


    /**
     * Returns the query parameter name under which a transaction token
     * must be reported.
     *
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static String getTokenName()
    {
        return org.apache.struts.taglib.html.Constants.TOKEN_KEY;
    }


    /*************************** Utilities *************************/

    /**
     * Returns the form action converted into an action mapping path. The
     * value of the <code>action</code> property is manipulated as follows in
     * computing the name of the requested mapping:
     * <ul>
     * <li>Any filename extension is removed (on the theory that extension
     *     mapping is being used to select the controller servlet).</li>
     * <li>If the resulting value does not start with a slash, then a
     *     slash is prepended.</li>
     * </ul>
     *
     * @param action the name of an action as per struts-config.xml
     * @deprecated This will be removed in VelocityTools 1.2
     */
    public static String getActionMappingName(String action)
    {
        return RequestUtils.getActionMappingName(action);
    }


    /**
     * Returns the form action converted into a server-relative URI
     * reference.
     *
     * @param application the servlet context
     * @param request the servlet request
     * @param action the name of an action as per struts-config.xml
     */
    public static String getActionMappingURL(ServletContext application,
                                             HttpServletRequest request,
                                             String action)
    {
        StringBuffer value = new StringBuffer(request.getContextPath());
        ModuleConfig config =
            (ModuleConfig)request.getAttribute(Globals.MODULE_KEY);
        if (config != null)
        {
            value.append(config.getPrefix());
        }

        /* Use our servlet mapping, if one is specified */
        String servletMapping =
            (String)application.getAttribute(Globals.SERVLET_KEY);

        if (servletMapping != null)
        {
            String queryString = null;
            int question = action.indexOf("?");

            if (question >= 0)
            {
                queryString = action.substring(question);
            }

            String actionMapping = RequestUtils.getActionMappingName(action);

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
            /* Otherwise, assume extension mapping is in use and extension is
             * already included in the action property */
            if (!action.startsWith("/"))
            {
                value.append("/");
            }
            value.append(action);
        }

        /* Return the completed value */
        return value.toString();
    }


    /**
     * Returns the action forward name converted into a server-relative URI
     * reference.
     *
     * @param app the servlet context
     * @param request the servlet request
     * @param forward the name of a forward as per struts-config.xml
     */
    public static String getForwardURL(HttpServletRequest request,
                                       ServletContext app,
                                       String forward)
    {
        ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request, app);
        //TODO? beware of null module config if ActionServlet isn't init'ed?
        ForwardConfig fc = moduleConfig.findForwardConfig(forward);
        if (fc == null)
        {
            return null;
        }

        StringBuffer url = new StringBuffer();
        if (fc.getPath().startsWith("/"))
        {
            url.append(request.getContextPath());
            url.append(RequestUtils.forwardURL(request, fc));
        }
        else
        {
            url.append(fc.getPath());
        }
        return url.toString();
    }


    /**
     * Returns a formatted error message. The error message is assembled from
     * the following three pieces: First, value of message resource
     * "errors.header" is prepended. Then, the list of error messages is
     * rendered. Finally, the value of message resource "errors.footer"
     * is appended.
     *
     * @param property the category of errors to markup and return
     * @param request the servlet request
     * @param session the HTTP session
     * @param application the servlet context
     *
     * @return The formatted error message. If no error messages are queued,
     * an empty string is returned.
     */
    public static String errorMarkup(String property,
                                     HttpServletRequest request,
                                     HttpSession session,
                                     ServletContext application)
    {
        return errorMarkup(property, null, request, session, application);
    }


    /**
     * Returns a formatted error message. The error message is assembled from
     * the following three pieces: First, value of message resource
     * "errors.header" is prepended. Then, the list of error messages is
     * rendered. Finally, the value of message resource "errors.footer"
     * is appended.
     *
     * @param property the category of errors to markup and return
     * @param bundle the message resource bundle to use
     * @param request the servlet request
     * @param session the HTTP session
     * @param application the servlet context
     * @since VelocityTools 1.1
     * @return The formatted error message. If no error messages are queued,
     * an empty string is returned.
     */
    public static String errorMarkup(String property,
                                     String bundle,
                                     HttpServletRequest request,
                                     HttpSession session,
                                     ServletContext application)
    {
        ActionErrors errors = getActionErrors(request);
        if (errors == null)
        {
            return "";
        }

        /* fetch the error messages */
        Iterator reports = null;
        if (property == null)
        {
            reports = errors.get();
        }
        else
        {
            reports = errors.get(property);
        }

        if (!reports.hasNext())
        {
            return "";
        }

        /* Render the error messages appropriately if errors have been queued */
        StringBuffer results = new StringBuffer();
        String header = null;
        String footer = null;
        Locale locale = getLocale(request, session);

        MessageResources resources =
            getMessageResources(request, application, bundle);
        if (resources != null)
        {
            header = resources.getMessage(locale, "errors.header");
            footer = resources.getMessage(locale, "errors.footer");
        }
        if (header == null)
        {
            header = "errors.header";
        }
        if (footer == null)
        {
            footer = "errors.footer";
        }

        results.append(header);
        results.append("\r\n");

        String message;
        while (reports.hasNext())
        {
            message = null;
            ActionMessage report = (ActionMessage)reports.next();
            if (resources != null)
            {
                message = resources.getMessage(locale,
                                               report.getKey(),
                                               report.getValues());
            }
            if (message != null)
            {
                results.append(message);
                results.append("\r\n");
            }
            else
            {
                results.append(report.getKey());
                results.append("\r\n");
            }
        }

        results.append(footer);
        results.append("\r\n");

        /* return result */
        return results.toString();
    }

}

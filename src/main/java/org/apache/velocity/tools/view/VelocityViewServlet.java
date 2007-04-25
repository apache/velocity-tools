package org.apache.velocity.tools.view;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.log.Log;

/**
 * <p>A servlet to process Velocity templates. This is comparable to the
 * the JspServlet for JSP-based applications.</p>
 *
 * <p>The servlet provides the following features:</p>
 * <ul>
 *   <li>renders Velocity templates</li>
 *   <li>provides support for an auto-loaded, configurable toolbox</li>
 *   <li>provides transparent access to the servlet request attributes,
 *       servlet session attributes and servlet context attributes by
 *       auto-searching them</li>
 *   <li>logs to the logging facility of the servlet API</li>
 * </ul>
 *
 * <p>VelocityViewServlet supports the following configuration parameters
 * in web.xml:</p>
 * <dl>
 *   <dt>org.apache.velocity.toolbox</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, the servlet will check for a toolbox file at
 *     '/WEB-INF/toolbox.xml'.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity will check for a properties file at
 *     '/WEB-INF/velocity.properties'.  If no file is found there, then
 *     Velocity is initialized with the settings in the classpath at
 *     'org.apache.velocity.tools.view.servlet.velocity.properties'.</dd>
 * </dl>
 *
 * @version $Id$
 */

public class VelocityViewServlet extends HttpServlet
{
    /** serial version id */
    private static final long serialVersionUID = -3329444102562079189L;
    private VelocityView view;

    public static final String SHARED_CONFIG_PARAM =
        "org.apache.velocity.tools.shared.config";

    /**
     * @deprecated Default path is now managed by {@link VelocityView}
     */
    protected static final String DEFAULT_TOOLBOX_PATH =
        VelocityView.DEPRECATED_USER_TOOLS_PATH;

    /**
     * <p>Initializes servlet and VelocityView used to process requests.
     * Called by the servlet container on loading.</p>
     *
     * @param config servlet configuation
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // check for an init-param telling this servlet NOT
        // to share its VelocityView with others.  by default, we
        // play nice and share the VelocityView with the other kids.
        String shared = findInitParameter(config, SHARED_CONFIG_PARAM);
        if (shared == null || shared.equals("false"))
        {
            setVelocityView(ServletUtils.getVelocityView(config));
        }
        else
        {
            // just create our own non-shared VelocityView
            setVelocityView(new VelocityView(config));
        }
    }


    /**
     * Looks up an init parameter with the specified key in either the
     * ServletConfig or, failing that, in the ServletContext.
     */
    protected String findInitParameter(ServletConfig config, String key)
    {
        // check the servlet config
        String param = config.getInitParameter(key);

        if (param == null || param.length() == 0)
        {
            // check the servlet context
            ServletContext servletContext = config.getServletContext();
            param = servletContext.getInitParameter(key);
        }
        return param;
    }

    protected void setVelocityView(VelocityView view)
    {
        if (view == null)
        {
            throw new NullPointerException("VelocityView cannot be null");
        }
        this.view = view;
    }

    protected VelocityView getVelocityView()
    {
        return this.view;
    }

    protected String getVelocityProperty(String name, String alternate)
    {
        return this.view.getProperty(name, alternate);
    }

    protected Log getLog()
    {
        return this.view.getLog();
    }

    /**
     * Handles GET - calls doRequest()
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     * Handle a POST request - calls doRequest()
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     *  Handles with both GET and POST requests
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    protected void doRequest(HttpServletRequest request, HttpServletResponse response)
    {
        Context context = null;
        try
        {
            // then get a context
            context = createContext(request, response);

            // call standard extension point
            fillContext(context, request);

            setContentType(request, response);

            // get the template
            Template template = getTemplate(request, response);

            // merge the template and context into the response
            mergeTemplate(template, context, response);
        }
        catch (Throwable e)
        {
            error(request, response, e);
        }
        finally
        {
            requestCleanup(request, response, context);
        }
    }

    protected Context createContext(HttpServletRequest request,
                                    HttpServletResponse response)
    {
        return view.getContext(request, response);
    }

    protected void fillContext(Context context, HttpServletRequest request)
    {
        // this implementation does nothing
    }


    /**
     * Sets the content type of the response.  This is available to be overriden
     * by a derived class.
     *
     * <p>The default implementation is :
     * <code>
     *    response.setContentType(view.getDefaultContentType());
     * </code>
     * where defaultContentType is set to the value of the default.contentType
     * property, or "text/html" if that was not set for the {@link VelocityView}.
     * </p>
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected void setContentType(HttpServletRequest request,
                                  HttpServletResponse response)
    {
        response.setContentType(view.getDefaultContentType());
    }

    protected Template getTemplate(HttpServletRequest request,
                                   HttpServletResponse response)
    {
        return view.getTemplate(request, response);
    }

    protected Template getTemplate(String name)
    {
        return view.getTemplate(name);
    }

    protected void mergeTemplate(Template template, Context context,
                                 HttpServletResponse response)
        throws IOException
    {
        view.merge(template, context, response.getWriter());
    }


    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param e  Exception that was thrown by some other part of process.
     */
    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         Throwable e)
    {
        try
        {
            StringBuffer html = new StringBuffer();
            html.append("<html>\n");
            html.append("<head><title>Error</title></head>\n");
            html.append("<body>\n");
            html.append("<h2>VelocityView : Error processing a template for path '");
            html.append(ServletUtils.getPath(request));
            html.append("'</h2>\n");

            Throwable cause = e;

            String why = cause.getMessage();
            if (why != null && why.trim().length() > 0)
            {
                html.append(StringEscapeUtils.escapeHtml(why));
                html.append("\n<br>\n");
            }

            //TODO: add line/column/template info for parse errors et al

            // if it's an MIE, i want the real stack trace!
            if (cause instanceof MethodInvocationException)
            {
                // get the real cause
                cause = ((MethodInvocationException)cause).getWrappedThrowable();
            }

            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));

            html.append("<pre>\n");
            html.append(StringEscapeUtils.escapeHtml(sw.toString()));
            html.append("</pre>\n");
            html.append("</body>\n");
            html.append("</html>");
            response.getWriter().write(html.toString());
        }
        catch (Exception e2)
        {
            // clearly something is quite wrong.
            // let's log the new exception then give up and
            // throw a runtime exception that wraps the first one
            String msg = "Exception while printing error screen";
            view.getLog().error(msg, e2);
            throw new RuntimeException(msg, e);
        }
    }


    /**
     * Cleanup routine called at the end of the request processing sequence
     * allows a derived class to do resource cleanup or other end of
     * process cycle tasks.  This default implementation does nothing.
     *
     * @param request servlet request from client
     * @param response servlet response
     * @param context Context that was merged with the requested template
     */
    protected void requestCleanup(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Context context)
    {
    }

}

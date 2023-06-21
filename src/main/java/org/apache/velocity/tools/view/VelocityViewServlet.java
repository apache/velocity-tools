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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
import org.apache.velocity.exception.ResourceNotFoundException;
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
 *   <dt>org.apache.velocity.tools</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, the servlet will check for a toolbox file at
 *     '/WEB-INF/tools.xml'.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity will check for a properties file at
 *     '/WEB-INF/velocity.properties'.  If no file is found there, then
 *     Velocity is initialized with the settings in the classpath at
 *     'org.apache.velocity.tools.view.velocity.properties'.</dd>
 *   <dt>org.apache.velocity.tools.shared.config</dt>
 *   <dd>By default, this is {@code true}. If set to {@code false}, then
 *     the {@link VelocityView} used by this servlet will not be shared
 *     with {@link VelocityViewFilter}s, other VelocityViewServlets or
 *     {@link org.apache.velocity.tools.view.jsp.VelocityViewTag}s in the
 *     application.</dd>
 *   <dt>org.apache.velocity.tools.loadDefaults</dt>
 *   <dd>By default, this is {@code true}. If set to {@code false}, then
 *     the default toolbox configuration will not be added to your (if any)
 *     custom configuration.  NOTE: The default configuration will also be
 *     suppressed if you are using a deprecated toolbox.xml format and do not
 *     explicitly set this to {@code true}.</dd>
 *   <dt>org.apache.velocity.tools.cleanConfiguration</dt>
 *   <dd>By default, this is {@code false}. If set to {@code true}, then
 *     then the final toolbox configuration (the combination of any custom
 *     one(s) provided by yourself and/or the default configuration(s))
 *     will have all invalid tools, properties, and/or data removed prior to
 *     configuring the ToolboxFactory for this servlet by a
 *     {@link org.apache.velocity.tools.config.ConfigurationCleaner}</dd>
 *   <dt>org.apache.velocity.tools.bufferOutput</dt>
 *   <dd>By default, the processed templates are merged directly into
 *     the {@link HttpServletResponse}'s writer.  If this parameter is
 *     set to {@code true}, then the output of the merge process will be
 *     buffered before being fed to the response. This allows the {@link #error}
 *     method to be overridden to return a "500 Internal Server Error" or
 *     at least not return any of the failed request content. Essentially,
 *     setting this to {@code true} degrades performance in order to enable
 *     a more "correct" error response"</dd>
 *  </dd>
 * </dl>
 *
 * @version $Id$
 */

public class VelocityViewServlet extends HttpServlet
{
    public static final String BUFFER_OUTPUT_PARAM =
        "org.apache.velocity.tools.bufferOutput";
    private static final long serialVersionUID = -3329444102562079189L;

    private transient VelocityView view;
    private boolean bufferOutput = false;

    /**
     * <p>Initializes servlet and VelocityView used to process requests.
     * Called by the servlet container on loading.</p>
     *
     * @param config servlet configuation
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // init the VelocityView (if it hasn't been already)
        getVelocityView();

        String buffer = findInitParameter(config, BUFFER_OUTPUT_PARAM);
        if (buffer != null && buffer.equals("true"))
        {
            this.bufferOutput = true;
            getLog().debug("VelocityViewServlet will buffer mergeTemplate output.");
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

    protected VelocityView getVelocityView()
    {
        if (this.view == null)
        {
            setVelocityView(ServletUtils.getVelocityView(getServletConfig()));
            assert (this.view != null);
        }
        return this.view;
    }

    protected void setVelocityView(VelocityView view)
    {
        this.view = view;
    }

    protected String getVelocityProperty(String name, String alternate)
    {
        return getVelocityView().getProperty(name, alternate);
    }

    protected Log getLog()
    {
        return getVelocityView().getLog();
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
        throws IOException
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
            Template template = handleRequest(request, response, context);

            // merge the template and context into the response
            mergeTemplate(template, context, response);
        } catch (IOException e) {
            error(request, response, e);
            throw e;
        }
        catch (ResourceNotFoundException e)
        {
            manageResourceNotFound(request, response, e);
        }
        catch (RuntimeException e)
        {
            error(request, response, e);
            throw e;
        }
        finally
        {
            requestCleanup(request, response, context);
        }
    }


    /**
     * <p>This was a common extension point, but now it is usually
     * simpler to override {@link #fillContext} to add custom things
     * to the {@link Context} or override a {@link #getTemplate}
     * method to change how {@link Template}s are retrieved.
     * This is only recommended for more complicated use-cases.</p>
     *
     * @param request client request
     * @param response client response
     * @param ctx  VelocityContext to fill
     * @return Velocity Template object or null
     */
    protected Template handleRequest(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Context ctx)
    {
        return getTemplate(request, response);
    }

    protected Context createContext(HttpServletRequest request,
                                    HttpServletResponse response)
    {
        return getVelocityView().createContext(request, response);
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
     *    response.setContentType(getVelocityView().getDefaultContentType());
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
        response.setContentType(getVelocityView().getDefaultContentType());
    }

    protected Template getTemplate(HttpServletRequest request,
                                   HttpServletResponse response)
    {
        return getVelocityView().getTemplate(request, response);
    }

    protected Template getTemplate(String name)
    {
        return getVelocityView().getTemplate(name);
    }

    protected void mergeTemplate(Template template, Context context,
                                 HttpServletResponse response)
        throws IOException
    {
        Writer writer;
        if (this.bufferOutput)
        {
            writer = new StringWriter();
        }
        else
        {
            writer = response.getWriter();
        }

        getVelocityView().merge(template, context, writer);

        if (this.bufferOutput)
        {
            response.getWriter().write(writer.toString());
        }
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
        String path = ServletUtils.getPath(request);
        if (response.isCommitted())
        {
            getLog().error("An error occured but the response headers have already been sent.");
            getLog().error("Error processing a template for path '" + path + "'", e);
            return;
        }

        try
        {
            getLog().error("Error processing a template for path '" + path + "'", e);
            StringBuilder html = new StringBuilder();
            html.append("<html>\n");
            html.append("<head><title>Error</title></head>\n");
            html.append("<body>\n");
            html.append("<h2>VelocityView : Error processing a template for path '");
            html.append(StringEscapeUtils.escapeHtml(ServletUtils.getPath(request)));
            html.append("'</h2>\n");

            Throwable cause = e;

            String why = cause.getMessage();
            if (why != null && why.length() > 0)
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
            getLog().error(msg, e2);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Manages the {@link ResourceNotFoundException} to send an HTTP 404 result
     * when needed.
     *
     * @param request The request object.
     * @param response The response object.
     * @param e The exception to check.
     * @throws IOException If something goes wrong when sending the HTTP error.
     */
    protected void manageResourceNotFound(HttpServletRequest request,
            HttpServletResponse response, ResourceNotFoundException e)
            throws IOException
    {
        String path = ServletUtils.getPath(request);
        if (getLog().isDebugEnabled())
        {
            getLog().debug("Resource not found for path '" + path + "'", e);
        }
        String message = e.getMessage();
        if (!response.isCommitted() && path != null &&
            message != null && message.contains("'" + path + "'"))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
        }
        else
        {
            error(request, response, e);
            throw e;
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

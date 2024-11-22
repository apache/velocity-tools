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
import java.io.StringWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;

/**
 * Extension of the VelocityViewServlet to perform "two-pass"
 * layout rendering and allow for a customized error screen.
 *
 * @author Nathan Bubna
 * @version $Id$
 */
public class VelocityLayoutServlet extends VelocityViewServlet
{

    /** serial version id */
    private static final long serialVersionUID = -4521817395157483487L;

    /**
     * The velocity.properties key for specifying the
     * servlet's error template.
     */
    public static final String PROPERTY_ERROR_TEMPLATE =
        "tools.view.servlet.error.template";

    /**
     * The velocity.properties key for specifying the
     * relative directory holding layout templates.
     */
    public static final String PROPERTY_LAYOUT_DIR =
        "tools.view.servlet.layout.directory";

    /**
     * The velocity.properties key for specifying the
     * servlet's default layout template's filename.
     */
    public static final String PROPERTY_DEFAULT_LAYOUT =
        "tools.view.servlet.layout.default.template";


    /**
     * The default error template's filename.
     */
    public static final String DEFAULT_ERROR_TEMPLATE = "Error.vm";

    /**
     * The default layout directory
     */
    public static final String DEFAULT_LAYOUT_DIR = "layout/";

    /**
     * The default filename for the servlet's default layout
     */
    public static final String DEFAULT_DEFAULT_LAYOUT = "Default.vm";


    /**
     * The context key that will hold the content of the screen.
     *
     * This key ($screen_content) must be present in the layout
     * template for the current screen to be rendered.
     */
    public static final String KEY_SCREEN_CONTENT = "screen_content";

    /**
     * The context/parameter key used to specify an alternate
     * layout to be used for a request instead of the default layout.
     */
    public static final String KEY_LAYOUT = "layout";


    /**
     * The context key that holds the {@link Throwable} that
     * broke the rendering of the requested screen.
     */
    public static final String KEY_ERROR_CAUSE = "error_cause";

    /**
     * The context key that holds the stack trace of the error that
     * broke the rendering of the requested screen.
     */
    public static final String KEY_ERROR_STACKTRACE = "stack_trace";

    /**
     * The context key that holds the {@link MethodInvocationException}
     * that broke the rendering of the requested screen.
     *
     * If this value is placed in the context, then $error_cause
     * will hold the error that this invocation exception is wrapping.
     */
    public static final String KEY_ERROR_INVOCATION_EXCEPTION = "invocation_exception";

    /**
     * The velocity.properties key for specifying
     * whether dynamic layout change is allowed
     */
    public static final String PROPERTY_DYNAMIC_LAYOUT =
        "tools.view.servlet.layout.dynamic";

    protected String errorTemplate;
    protected String layoutDir;
    protected String defaultLayout;

    /**
     * Initializes Velocity, the view servlet and checks for changes to
     * the initial layout configuration.
     *
     * @param config servlet configuration parameters
     */
    public void init(ServletConfig config) throws ServletException
    {
        // first do VVS' init()
        super.init(config);

        // check for default template path overrides
        errorTemplate =
            getVelocityProperty(PROPERTY_ERROR_TEMPLATE, DEFAULT_ERROR_TEMPLATE);
        layoutDir =
            getVelocityProperty(PROPERTY_LAYOUT_DIR, DEFAULT_LAYOUT_DIR);
        defaultLayout =
            getVelocityProperty(PROPERTY_DEFAULT_LAYOUT, DEFAULT_DEFAULT_LAYOUT);

        // preventive error checking! directory must end in /
        if (!layoutDir.endsWith("/"))
        {
            layoutDir += '/';
        }

        // log the current settings
        getLog().info("VelocityLayoutServlet: Error screen is '{}'", errorTemplate);
        getLog().info("VelocityLayoutServlet: Layout directory is '{}'", layoutDir);
        getLog().info("VelocityLayoutServlet: Default layout template is '{}'", defaultLayout);

        // for efficiency's sake, make defaultLayout a full path now
        defaultLayout = layoutDir + defaultLayout;
    }


    /**
     * Overrides VelocityViewServlet to check the request for
     * an alternate layout
     *
     * @param ctx context for this request
     * @param request client request
     */
    protected void fillContext(Context ctx, HttpServletRequest request)
    {
        String layout = findLayout(request);
        if (layout != null)
        {
            // let the template know what its new layout is
            ctx.put(KEY_LAYOUT, layout);
        }
    }

    /**
     * Searches for a non-default layout to be used for this request.
     * This implementation checks the request parameters and attributes.
     * @param request servlet request
     * @return layout name or null
     */
    protected String findLayout(HttpServletRequest request)
    {
        // look in the request attributes
        return (String)request.getAttribute(KEY_LAYOUT);
    }

    /**
     * Overrides VelocityViewServlet.mergeTemplate to do a two-pass
     * render for handling layouts
     * @param template {@link Template} object
     * @param context Velocity context
     * @param request servlet request
     * @param response servlet response
     * @throws IOException
     */
    protected void mergeTemplate(Template template, Context context, HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException
    {
        //
        // this section is based on Tim Colson's "two pass render"
        //
        // Render the screen content
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        // Add the resulting content to the context
        context.put(KEY_SCREEN_CONTENT, sw.toString());

        // Check for an alternate layout
        //
        // we check after merging the screen template so the screen
        // can overrule any layout set in the request parameters
        // by doing #set( $layout = "MyLayout.vm" )
        Object obj = context.get(KEY_LAYOUT);
        String layout = (obj == null) ? null : obj.toString();
        if (layout == null)
        {
            // no alternate, use default
            layout = defaultLayout;
        }
        else
        {
            // make it a full(er) path
            layout = layoutDir + layout;
        }

        try
        {
            //load the layout template
            template = getTemplate(layout);
        }
        catch (Exception e)
        {
            getLog().error("Can't load layout \"{}\"", layout, e);

            // if it was an alternate layout we couldn't get...
            if (!layout.equals(defaultLayout))
            {
                // try to get the default layout
                // if this also fails, let the exception go
                template = getTemplate(defaultLayout);
            }
        }

        // Render the layout template into the response
        super.mergeTemplate(template, context, request, response);
    }


    /**
     * Overrides VelocityViewServlet to display user's custom error template
     * @param request servlet request
     * @param response servlet response
     * @param e thrown error
     */
    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         Throwable e)
    {
        try
        {
            // get a velocity context
            Context ctx = createContext(request, response);

            Throwable cause = e;

            // if it's an MIE, i want the real cause and stack trace!
            if (cause instanceof MethodInvocationException)
            {
                // put the invocation exception in the context
                ctx.put(KEY_ERROR_INVOCATION_EXCEPTION, e);
                // get the real cause
                cause = cause.getCause();
            }

            // add the cause to the context
            ctx.put(KEY_ERROR_CAUSE, cause);

            // grab the cause's stack trace and put it in the context
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new java.io.PrintWriter(sw));
            ctx.put(KEY_ERROR_STACKTRACE, sw.toString());

            // retrieve and render the error template
            Template et = getTemplate(errorTemplate);
            mergeTemplate(et, ctx, request, response);

        }
        catch (Exception e2)
        {
            // d'oh! log this
            getLog().error("Error during error template rendering", e2);
            // then punt the original to a higher authority
            super.error(request, response, e);
        }
    }


}

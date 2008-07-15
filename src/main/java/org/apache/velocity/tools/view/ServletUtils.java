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

import java.lang.reflect.Constructor;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.Toolbox;

/**
 * <p>A set of utility methods for the servlet environment.</p>
 *
 * @version $Id: ServletUtils.java 471244 2006-11-04 18:34:38Z henning $
 */
public class ServletUtils
{
    public static final String VELOCITY_VIEW_KEY =
        VelocityView.class.getName();
    public static final String SHARED_CONFIG_PARAM =
        "org.apache.velocity.tools.shared.config";
    public static final String ALT_VELOCITY_VIEW_KEY =
        "org.apache.velocity.tools.view.class";

    public static final ServletUtils INSTANCE = new ServletUtils();

    protected ServletUtils() {}

    public ServletUtils getInstance()
    {
        return INSTANCE;
    }

    /**
     * Retrieves the path for the specified request regardless of
     * whether this is a direct request or an include by the
     * RequestDispatcher.
     */
    public static String getPath(HttpServletRequest request)
    {
        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following special
        // attribute holds the correct path.  See section 8.3 of the Servlet
        // 2.3 specification.
        String path = (String)request.getAttribute("javax.servlet.include.servlet_path");
        // also take into account the PathInfo stated on SRV.4.4 Request Path Elements
        String info = (String)request.getAttribute("javax.servlet.include.path_info");
        if (path == null)
        {
            path = request.getServletPath();
            info = request.getPathInfo();
        }
        if (info != null)
        {
            path += info;
        }
        return path;
    }


    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link ServletConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(ServletConfig config)
    {
        return getVelocityView(new JeeConfig(config));
    }


    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link FilterConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(FilterConfig config)
    {
        return getVelocityView(new JeeConfig(config));
    }

    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link JeeConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(JeeConfig config)
    {
        // check for an init-param telling this servlet/filter NOT
        // to share its VelocityView with others.  by default, we
        // play nice and share the VelocityView with the other kids.
        String shared = config.findInitParameter(SHARED_CONFIG_PARAM);
        if (shared != null && shared.equals("false"))
        {
            // just create a new, non-shared VelocityView
            return createView(config);
        }

        ServletContext application = config.getServletContext();

        // check for an already initialized VelocityView to use
        VelocityView view = getVelocityView(application, false);
        if (view == null)
        {
            // only create a new one if we don't already have one
            view = createView(config);

            // and store it in the application attributes, so other
            // servlets, filters, or tags can use it
            application.setAttribute(VELOCITY_VIEW_KEY, view);
        }
        return view;
    }

    private static VelocityView createView(JeeConfig config)
    {
        String cls = config.findInitParameter(ALT_VELOCITY_VIEW_KEY);
        if (cls == null)
        {
            return new VelocityView(config);
        }
        try
        {
            return createView(ClassUtils.getClass(cls), config);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IllegalArgumentException("Could not find class "+cls, cnfe);
        }
    }

    private static VelocityView createView(Class klass, JeeConfig config)
    {
        if (!VelocityView.class.isAssignableFrom(klass))
        {
            throw new IllegalArgumentException(klass+" must extend "+VelocityView.class);
        }
        try
        {
            Constructor ctor = klass.getConstructor(JeeConfig.class);
            return (VelocityView)ctor.newInstance(config);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new IllegalArgumentException(klass+" must have a constructor that takes "+JeeConfig.class, nsme);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not instantiate "+klass+" with "+config, e);
        }
    }

    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link ServletContext}. If one has not yet been created,
     * it will create one, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(ServletContext application)
    {
        return getVelocityView(new JeeConfig(application));
    }

    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link ServletContext}. If one has not yet been created and
     * the second parameter is <code>true</code>, then it will
     * create one, store it for future access, and return it.
     */
    public static VelocityView getVelocityView(ServletContext application,
                                               boolean createIfMissing) {
        VelocityView view =
            (VelocityView)application.getAttribute(VELOCITY_VIEW_KEY);
        if (view == null && createIfMissing)
        {
            return getVelocityView(application);
        }
        return view;
    }


    public static Object findTool(String key, ServletContext application)
    {
        return findTool(key, VelocityView.DEFAULT_TOOLBOX_KEY, application);
    }

    public static Object findTool(String key, String toolboxKey,
                                  ServletContext application)
    {
        Toolbox toolbox = (Toolbox)application.getAttribute(toolboxKey);
        if (toolbox != null)
        {
            return toolbox.get(key);
        }
        return null;
    }

    public static Object findTool(String key, HttpServletRequest request)
    {
        return findTool(key, request, null);
    }

    public static Object findTool(String key, String toolboxKey,
                                  HttpServletRequest request)
    {
        return findTool(key, toolboxKey, request, null);
    }

    public static Object findTool(String key, HttpServletRequest request,
                                  ServletContext application)
    {
        return findTool(key, VelocityView.DEFAULT_TOOLBOX_KEY,
                        request, application);
    }

    public static Object findTool(String key, String toolboxKey,
                                  HttpServletRequest request,
                                  ServletContext application)
    {
        String path = getPath(request);

        Toolbox toolbox = (Toolbox)request.getAttribute(toolboxKey);
        if (toolbox != null)
        {
            Object tool = toolbox.get(key, path);
            if (tool != null)
            {
                return tool;
            }
        }

        HttpSession session = request.getSession(false);
        if (session != null)
        {
            toolbox = (Toolbox)session.getAttribute(toolboxKey);
            if (toolbox != null)
            {
                Object tool = toolbox.get(key, path);
                if (tool != null)
                {
                    return tool;
                }
            }

            if (application == null)
            {
                application = session.getServletContext();
            }
        }

        if (application != null)
        {
            toolbox = (Toolbox)application.getAttribute(toolboxKey);
            if (toolbox != null)
            {
                return toolbox.get(key, path);
            }
        }

        return null;
    }

}

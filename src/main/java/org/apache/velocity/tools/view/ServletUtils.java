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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.FileFactoryConfiguration;
import org.apache.velocity.tools.config.PropertiesFactoryConfiguration;
import org.apache.velocity.tools.config.XmlFactoryConfiguration;

/**
 * <p>A set of utility methods for supporting and using
 * VelocityTools in the servlet environment.</p>
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
    /**
     * Key used to access a live {@link FactoryConfiguration} previously
     * placed in the ServletContext attributes.
     */
    public static final String CONFIGURATION_KEY =
        "org.apache.velocity.tools";

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
        return getVelocityView(new JeeServletConfig(config));
    }


    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link FilterConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(FilterConfig config)
    {
        return getVelocityView(new JeeFilterConfig(config));
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
        return getVelocityView(new JeeContextConfig(application));
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

    public static InputStream getInputStream(String path, ServletContext application)
    {
        // first, search the classpath
        InputStream inputStream = ClassUtils.getResourceAsStream(path, ServletUtils.class);
        if (inputStream == null)
        {
            // then, try the servlet context
            inputStream = application.getResourceAsStream(path);

            if (inputStream == null)
            {
                // then, try the file system directly
                File file = new File(path);
                if (file.exists())
                {
                    try
                    {
                        inputStream = new FileInputStream(file);
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        // we should not be able to get here
                        // since we already checked whether the file exists
                        throw new IllegalStateException(fnfe);
                    }
                }
            }
        }
        return inputStream;
    }

    public static FactoryConfiguration getConfiguration(ServletContext application)
    {
        Object obj = application.getAttribute(CONFIGURATION_KEY);
        if (obj instanceof FactoryConfiguration)
        {
            FactoryConfiguration injected = (FactoryConfiguration)obj;
            // make note of where we found this
            String source = injected.getSource();
            String addnote = " from ServletContext.getAttribute("+CONFIGURATION_KEY+")";
            if (!source.endsWith(addnote))
            {
                injected.setSource(source+addnote);
            }
            return injected;
        }
        return null;
    }

    public static FactoryConfiguration getConfiguration(String path,
                                                        ServletContext application)
    {
        return getConfiguration(path, application, path.endsWith("toolbox.xml"));
    }

    public static FactoryConfiguration getConfiguration(String path,
                                                        ServletContext application,
                                                        boolean deprecationSupportMode)
    {
        // first make sure we can even get such a file
        InputStream inputStream = getInputStream(path, application);
        if (inputStream == null)
        {
            return null;
        }

        // then make sure it's a file type we recognize
        FileFactoryConfiguration config = null;
        String source = "ServletUtils.getConfiguration("+path+",ServletContext[,depMode="+deprecationSupportMode+"])";
        if (path.endsWith(".xml"))
        {
            config = new XmlFactoryConfiguration(deprecationSupportMode, source);
        }
        else if (path.endsWith(".properties"))
        {
            config = new PropertiesFactoryConfiguration(source);
        }
        else
        {
            String msg = "Unknown configuration file type: " + path +
                         "\nOnly .xml and .properties configuration files are supported at this time.";
            throw new UnsupportedOperationException(msg);
        }

        // now, try to read the file
        try
        {
            config.read(inputStream);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Failed to load configuration at: "+path, ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                throw new RuntimeException("Failed to close input stream for "+path, ioe);
            }
        }
        return config;
    }

    /**
     * Returns a mutex (lock object) unique to the specified session
     * and stored under the specified key to allow for reliable
     * synchronization on the session.
     */
    public static Object getMutex(HttpSession session, String key, Object caller)
    {
        // yes, this uses double-checked locking, but it is safe here
        // since partial initialization of the lock is not an issue
        Object lock = session.getAttribute(key);
        if (lock == null)
        {
            // one thread per caller at a time
            synchronized(caller)
            {
                // in case another thread already came thru
                lock = session.getAttribute(key);
                if (lock == null)
                {
                    // use a small, serializable object
                    // that is unlikely to be unfortunately optimized
                    lock = new SessionMutex();
                    session.setAttribute(key, lock);
                }
            }
        }
        return lock;
    }

    private static class SessionMutex implements java.io.Serializable
    {
    }

}

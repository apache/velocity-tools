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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
     * @param request servlet request
     * @return request path
     */
    public static String getPath(HttpServletRequest request)
    {
        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following special
        // attribute holds the correct path.  See section 8.3 of the Servlet
        // 2.3 specification.
        String path = (String)request.getAttribute("jakarta.servlet.include.servlet_path");
        // also take into account the PathInfo stated on SRV.4.4 Request Path Elements
        String info = (String)request.getAttribute("jakarta.servlet.include.path_info");
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
     * @param config servlet config
     * @return VelocityView instance
     */
    public static VelocityView getVelocityView(ServletConfig config)
    {
        return getVelocityView(new JeeServletConfig(config));
    }


    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link FilterConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     * @param config filter config
     * @return VelocityView instance
     */
    public static VelocityView getVelocityView(FilterConfig config)
    {
        return getVelocityView(new JeeFilterConfig(config));
    }

    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link JeeConfig}'s context. If one has not yet been created, it
     * will create, store it for future access, and then return it.
     * @param config configuration parameters container (webapp, servlet or filter)
     * @return VelocityView instance
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
     * @param application servlet context
     * @return VelocityView instance
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
     * @param application servlet context
     * @param createIfMissing whether to create VelocityView if not yet built
     * @return VelocityView instance
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

    protected static boolean isWebappResource(String path)
    {
      return path != null && (path.startsWith("WEB-INF") || path.startsWith("/WEB-INF"));
    }

    public static InputStream getInputStream(final String path, final ServletContext application)
    {
        InputStream inputStream = null;
        if (!isWebappResource(path))
        {
            // search classpath except for WEB-INF/*
            inputStream = ClassUtils.getResourceAsStream(path, ServletUtils.class);
        }
        else
        {
            // then webapp only for WEB-INF/*
            if (System.getSecurityManager() != null)
            {
                inputStream = AccessController.doPrivileged(
                    new PrivilegedAction<InputStream>()
                    {
                        @Override
                        public InputStream run()
                        {

                            return application.getResourceAsStream(path);
                        }
                    });
            }
            else
            {
                inputStream = application.getResourceAsStream(path);
            }
        }
        return inputStream;
    }

    public static URL getURL(final String path, final ServletContext application)
    {
        URL url = null;
        if (!isWebappResource(path))
        {
            // search classpath except for WEB-INF/*
            url = ClassUtils.getResource(path, ServletUtils.class);
        }
        else
        {
            // then webapp only for WEB-INF/*
            if (System.getSecurityManager() != null)
            {
                url = AccessController.doPrivileged(
                    new PrivilegedAction<URL>()
                    {
                        @Override
                        public URL run()
                        {
                            try
                            {
                                return application.getResource(path);
                            }
                            catch (MalformedURLException mue)
                            {
                                return null;
                            }
                        }
                    });
            }
            else
            {
                try
                {
                    url = application.getResource(path);
                }
                catch (MalformedURLException mue) {}
            }
        }
        return url;
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

    public static FactoryConfiguration getConfiguration(final String path, final ServletContext application)
    {
        // first make sure we can even get such a file
        URL url = getURL(path, application);
        if (url == null)
        {
            return null;
        }

        // then make sure it's a file type we recognize
        FileFactoryConfiguration config = null;
        String source = "ServletUtils.getConfiguration("+path+",ServletContext)";
        if (path.endsWith(".xml"))
        {
            config = new XmlFactoryConfiguration(source);
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
            config.read(url);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load configuration at: "+path, e);
        }
        return config;
    }

    /**
     * Returns a mutex (lock object) unique to the specified session
     * and stored under the specified key to allow for reliable
     * synchronization on the session.
     * @param session HTTP session
     * @param key mutex key
     * @param caller caller object
     * @return session mutex object
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

    /**
     * If end is null, this will return start and vice versa.
     * If neither is null, this will append the end to the start,
     * making sure that there is only one '/' character between
     * the two values.
     * @param before start path
     * @param after end path
     * @return combined path
     */
    public static String combinePath(String before, String after)
    {
        // code taken from generics LinkTool.
        // TODO - it should be factorize somewhere in generics tools.
        if (after == null)
        {
            return before;
        }
        if (before == null)
        {
            return after;
        }

        // make sure we don't get // or nothing between start and end
        boolean startEnds = before.endsWith("/");
        boolean endStarts = after.startsWith("/");
        if (startEnds ^ endStarts) //one
        {
            return before + after;
        }
        else if (startEnds & endStarts) //both
        {
            return before + after.substring(1, after.length());
        }
        else //neither
        {
            return before + '/' + after;
        }
    }

    private static class SessionMutex implements java.io.Serializable
    {
    }

}

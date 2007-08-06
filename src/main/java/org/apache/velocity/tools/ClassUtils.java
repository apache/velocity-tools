package org.apache.velocity.tools;

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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Repository for common class and reflection methods.
 *
 * @author Nathan Bubna
 * @version $Id: ClassUtils.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ClassUtils
{
    // shortcuts for readability...
    private static final ClassLoader getThreadContextLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    private static final ClassLoader getClassLoader()
    {
        return ClassUtils.class.getClassLoader();
    }

    /**
     * Load a class with a given name.
     * <p/>
     * It will try to load the class in the following order:
     * <ul>
     * <li>From {@link Thread.currentThread().getContextClassLoader()}
     * <li>Using the basic {@link Class#forName(java.lang.String) }
     * <li>From {@link ClassUtils.class.getClassLoader()}
     * </ul>
     *
     * @param name Fully qualified class name to be loaded
     * @return Class object
     * @exception ClassNotFoundException if the class cannot be found
     */
    public static Class getClass(String name) throws ClassNotFoundException
    {
        try
        {
            return getThreadContextLoader().loadClass(name);
        }
        catch (ClassNotFoundException e)
        {
            try
            {
                return Class.forName(name);
            }
            catch (ClassNotFoundException ex)
            {
                return getClassLoader().loadClass(name);
            }
        }
    }

    public static Object getInstance(String classname)
         throws ClassNotFoundException, IllegalAccessException,
                InstantiationException
    {
        return getClass(classname).newInstance();
    }

    /**
     * Load all resources with the specified name. If none are found, we 
     * prepend the name with '/' and try again.
     *
     * This will attempt to load the resources from (in this order):
     * <ul>
     *  <li>the result Thread.currentThread().getContextClassLoader()</li>
     *  <li>the result of ClassUtils.class.getClassLoader()</li>
     * </ul>
     *
     * @param name The name of the resources to load
     */
    public static List<URL> getResources(String name)
    {
        Set<URL> urls = new LinkedHashSet<URL>();

        Enumeration<URL> e;
        try
        {
            e = getThreadContextLoader().getResources(name);
            while (e.hasMoreElements())
            {
                urls.add(e.nextElement());
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }

        try
        {
            e = getClassLoader().getResources(name);
            while (e.hasMoreElements())
            {
                urls.add(e.nextElement());
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }

        if (!urls.isEmpty())
        {
            List<URL> result = new ArrayList<URL>(urls.size());
            result.addAll(urls);
            return result;
        }
        else if (!name.startsWith("/"))
        {
            // try again with a / in front of the name
            return getResources("/"+name);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * Load a given resource.
     * <p/>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>From {@link Thread.currentThread().getContextClassLoader()}
     * <li>From {@link ClassUtils.class.getClassLoader()}
     * </ul>
     *
     * @param name The name of the resource to load
     */
    public static URL getResource(String name)
    {
        URL url = getThreadContextLoader().getResource(name);
        if (url == null)
        {
            url = getClassLoader().getResource(name);
        }
        return url;
    }

    /**
     * This is a convenience method to load a resource as a stream.
     * <p/>
     * The algorithm used to find the resource is given in getResource()
     *
     * @param name The name of the resource to load
     */
    public static InputStream getResourceAsStream(String name)
    {
        URL url = getResource(name);
        try
        {
            return (url != null) ? url.openStream() : null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public static Method findMethod(Class clazz, String name, Class[] params)
        throws SecurityException
    {
        try
        {
            // check for a public setup(Map) method first
            return clazz.getMethod(name, params);
        }
        catch (NoSuchMethodException nsme)
        {
            // ignore this
        }
        return findDeclaredMethod(clazz, name, params);
    }

    public static Method findDeclaredMethod(Class clazz, String name, Class[] params)
        throws SecurityException
    {
        try
        {
            // check for a protected one
            Method method = clazz.getDeclaredMethod(name, params);
            if (method != null)
            {
                // and give this class access to it
                method.setAccessible(true);
                return method;
            }
        }
        catch (NoSuchMethodException nsme)
        {
            // ignore this
        }

        // ok, didn't find it declared in this class, try the superclass
        Class supclazz = clazz.getSuperclass();
        if (supclazz != null)
        {
            // recurse upward
            return findDeclaredMethod(supclazz, name, params);
        }
        // otherwise, return null
        return null;
    }

}

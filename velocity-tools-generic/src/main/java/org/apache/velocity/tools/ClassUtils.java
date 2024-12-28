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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.util.EnumerationIterator;

/**
 * Repository for common class and reflection methods.
 *
 * @author Nathan Bubna
 * @version $Id: ClassUtils.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ClassUtils
{
    private ClassUtils() {}

    // shortcuts for readability...
    private static final ClassLoader getThreadContextLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    private static final ClassLoader getClassLoader()
    {
        return ClassUtils.class.getClassLoader();
    }

    private static final ClassLoader getCallerLoader(Object caller)
    {
        if (caller instanceof Class)
        {
            return ((Class)caller).getClassLoader();
        }
        else
        {
            return caller.getClass().getClassLoader();
        }
    }

    /**
     * Load a class with a given name.
     * It will try to load the class in the following order:
     * <ul>
     * <li>From {@link Thread}.currentThread().getContextClassLoader()
     * <li>Using the basic {@link Class#forName(java.lang.String) }
     * <li>From {@link ClassUtils}.class.getClassLoader()
     * </ul>
     *
     * @param name Fully qualified class name to be loaded
     * @return Class object
     * @exception ClassNotFoundException if the class cannot be found
     */
    public static Class getClass(String name) throws ClassNotFoundException
    {
        Class foundClass = null;
        try
        {
            ClassLoader contextLoader = getThreadContextLoader();
            if (contextLoader != null)
            {
                foundClass = contextLoader.loadClass(name);
            }
        }
        catch (ClassNotFoundException e)
        {
            //ignore
        }
        if (foundClass != null)
        {
            return foundClass;
        }

        try
        {
            foundClass = Class.forName(name);
        }
        catch (ClassNotFoundException e)
        {
            //ignore
        }
        if (foundClass != null)
        {
            return foundClass;
        }

        return getClassLoader().loadClass(name);
    }

    /**
     * Get an instance of a named class.
     * @param classname class name
     * @return new class instance
     * @throws ClassNotFoundException if class is not found
     * @throws IllegalAccessException if not granted
     * @throws InstantiationException if instance creation throwed
     */
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
     * This will attempt to load the resources from the following methods (in order):
     * <ul>
     * <li>Thread.currentThread().getContextClassLoader().getResources(name)</li>
     * <li>{@link ClassUtils}.class.getClassLoader().getResources(name)</li>
     * <li>{@link ClassUtils}.class.getResource(name)</li>
     * <li>{@link #getCallerLoader(Object caller)}.getResources(name)</li>
     * <li>caller.getClass().getResource(name)</li>
     * </ul>
     *
     * @param name The name of the resources to load
     * @param caller The instance or {@link Class} calling this method
     * @return the list of found resources
     */
    public static List<URL> getResources(String name, Object caller)
    {
        Set<String> urls = new LinkedHashSet<String>();

        // try to load all from the current thread context classloader
        addResources(name, urls, getThreadContextLoader());

        // try to load all from this class' classloader
        if (!addResources(name, urls, getClassLoader()))
        {
            // ok, try to load one directly from this class
            addResource(name, urls, ClassUtils.class);
        }

        // try to load all from the classloader of the calling class
        if (!addResources(name, urls, getCallerLoader(caller)))
        {
            // try to load one directly from the calling class
            addResource(name, urls, caller.getClass());
        }

        if (!urls.isEmpty())
        {
            List<URL> result = new ArrayList<URL>(urls.size());
            try
            {
                for (String url : urls)
                {
                    result.add(new URL(url));
                }
            }
            catch (MalformedURLException mue)
            {
                throw new IllegalStateException("A URL could not be recreated from its own toString() form", mue);
            }
            return result;
        }
        else if (!name.startsWith("/"))
        {
            // try again with a / in front of the name
            return getResources("/"+name, caller);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private static final void addResource(String name, Set<String> urls, Class c)
    {
        URL url = c.getResource(name);
        if (url != null)
        {
            urls.add(url.toString());
        }
    }

    private static final boolean addResources(String name, Set<String> urls,
                                              ClassLoader loader)
    {
        boolean foundSome = false;
        try
        {
            Enumeration<URL> e = loader.getResources(name);
            while (e.hasMoreElements())
            {
                urls.add(e.nextElement().toString());
                foundSome = true;
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }
        return foundSome;
    }

    private static URL getResourceImpl(final String name, final Object caller)
    {
        URL url = null;
        ClassLoader contextLoader = getThreadContextLoader();
        if (contextLoader != null)
        {
            url = contextLoader.getResource(name);
        }
        if (url == null)
        {
            url = getClassLoader().getResource(name);
            if (url == null)
            {
                url = ClassUtils.class.getResource(name);
                if (url == null && caller != null)
                {
                    Class callingClass = caller.getClass();
                    if (callingClass == Class.class)
                    {
                        callingClass = (Class)caller;
                    }
                    url = callingClass.getResource(name);
                }
            }
        }
        return url;
    }

    private static InputStream getResourceAsStreamImpl(final String name, final Object caller)
    {
        InputStream inputStream = null;
        ClassLoader contextLoader = getThreadContextLoader();
        if (contextLoader != null)
        {
            inputStream = contextLoader.getResourceAsStream(name);
        }
        if (inputStream == null)
        {
            inputStream = getClassLoader().getResourceAsStream(name);
            if (inputStream == null)
            {
                inputStream = ClassUtils.class.getResourceAsStream(name);
                if (inputStream == null && caller != null)
                {
                    Class callingClass = caller.getClass();
                    if (callingClass == Class.class)
                    {
                        callingClass = (Class)caller;
                    }
                    inputStream = callingClass.getResourceAsStream(name);
                }
            }
        }
        return inputStream;
    }

    /**
     * Load a given resource.
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>Thread.currentThread().getContextClassLoader().getResource(name)</li>
     * <li>{@link ClassUtils}.class.getClassLoader().getResource(name)</li>
     * <li>{@link ClassUtils}.class.getResource(name)</li>
     * <li>caller.getClass().getResource(name) or, if caller is a Class,
     *     caller.getResource(name)</li>
     * </ul>
     *
     * @param name The name of the resource to load
     * @param caller The instance or {@link Class} calling this method
     * @return the found URL, or null if not found
     */
    public static URL getResource(final String name, final Object caller)
    {
        URL url = null;
        if (System.getSecurityManager() != null)
        {
            url = AccessController.doPrivileged(
                new PrivilegedAction<URL>()
                {
                    @Override
                    public URL run()
                    {
                        return getResourceImpl(name, caller);
                    }
                });
        }
        else
        {
            url = getResourceImpl(name, caller);
        }
        return url;
    }

    /**
     * This is a convenience method to load a resource as a stream.
     * The algorithm used to find the resource is given in getResource()
     *
     * @param name The name of the resource to load
     * @param caller The instance or {@link Class} calling this method
     * @return the resource input stream or null if not found
     */
    public static InputStream getResourceAsStream(final String name, final Object caller)
    {
        InputStream inputStream = null;
        if (System.getSecurityManager() != null)
        {
            inputStream = AccessController.doPrivileged(
                new PrivilegedAction<InputStream>()
                {
                    @Override
                    public InputStream run()
                    {
                        return getResourceAsStreamImpl(name, caller);
                    }
                });
        }
        else
        {
            inputStream = getResourceAsStreamImpl(name, caller);
        }
        return inputStream;
    }

    /**
     * Find a callable method in a class
     * @param clazz target class
     * @param name method name
     * @param params method arguments classes
     * @return method object
     * @throws SecurityException if not granted
     */
    public static Method findMethod(Class clazz, String name, Class... params)
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

    /**
     * Find a declared method in a class. It will be made accessible if needed and allowed.
     * @param clazz target class
     * @param name method name
     * @param params method arguments classes
     * @return
     * @throws SecurityException if not allowed
     */
    public static Method findDeclaredMethod(Class clazz, String name, Class... params)
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

    /**
     * Given a static field path, aka <i>classname</i>.<i>field</i>, get the field value.
     * @param fieldPath field path
     * @return field value
     * @throws ClassNotFoundException if class hasn't been found
     * @throws NoSuchFieldException if field hasn't been found
     * @throws SecurityException if not granted
     * @throws IllegalAccessException if field is not accessible
     */
    public static Object getFieldValue(String fieldPath)
        throws ClassNotFoundException, NoSuchFieldException,
               SecurityException, IllegalAccessException
    {
        int lastDot = fieldPath.lastIndexOf('.');
        String classname = fieldPath.substring(0, lastDot);
        String fieldname = fieldPath.substring(lastDot + 1, fieldPath.length());

        Class clazz = getClass(classname);
        return getFieldValue(clazz, fieldname);
    }

    /**
     * Given a class and a static field name, get the field value.
     * @param clazz target class
     * @param fieldname field name
     * @return field value
     * @throws NoSuchFieldException if field hasn't been found
     * @throws SecurityException if not granted
     * @throws IllegalAccessException if field is not accessible
     */
    public static Object getFieldValue(Class clazz, String fieldname)
        throws NoSuchFieldException, SecurityException, IllegalAccessException
    {
        Field field = clazz.getField(fieldname);
        int mod = field.getModifiers();
        if (!Modifier.isStatic(mod))
        {
            throw new UnsupportedOperationException("Field "+fieldname+" in class "+clazz.getName()+" is not static.  Only static fields are supported.");
        }
        return field.get(null);
    }

    /**
     * Retrieves an Iterator from or creates and Iterator for the specified object.
     * This method is almost entirely copied from Engine's UberspectImpl class.
     * @param obj the target obj
     * @return an iterator over the content of obj, or null if not found
     * @throws NoSuchMethodException if no iterator() method
     * @throws IllegalAccessException if iterator() method not callable
     * @throws InvocationTargetException if iterator() method throwed
     */
    public static Iterator getIterator(Object obj)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        if (obj.getClass().isArray())
        {
            return new ArrayIterator(obj);
        }
        else if (obj instanceof Collection)
        {
            return ((Collection) obj).iterator();
        }
        else if (obj instanceof Map)
        {
            return ((Map) obj).values().iterator();
        }
        else if (obj instanceof Iterator)
        {
            return ((Iterator) obj);
        }
        else if (obj instanceof Iterable)
        {
            return ((Iterable)obj).iterator();
        }
        else if (obj instanceof Enumeration)
        {
            return new EnumerationIterator((Enumeration) obj);
        }
        else
        {
            // look for an iterator() method to support
            // any user tools/DTOs that want to work in
            // foreach w/o implementing the Collection interface
            Method iter = obj.getClass().getMethod("iterator");
            if (Iterator.class.isAssignableFrom(iter.getReturnType()))
            {
                return (Iterator)iter.invoke(obj);
            }
            else
            {
                return null;
            }
        }
    }

    private static String factoryMethodPrefixes[] = { "create", "new", "get" };

    /**
     * <p>Given a factory class and a target class, search for the following methods:</p>
     * <ul>
     *     <li><code>create<i>TargetClassname</i>()</code>,</li>
     *     <li><code>new<i>TargetClassname</i>()</code>, or</li>
     *     <li><code>get<i>TargetClassname</i>()</code>.</li>
     * </ul>
     * @param factory factory class
     * @param target target class
     * @return first factory method found, or null otherwise
     */
    public static Method findFactoryMethod(Class factory, Class target)
    {
        Method ret = null;
        String undecoratedName = target.getSimpleName();
        for (String prefix : factoryMethodPrefixes)
        {
            String methodName = prefix + undecoratedName;
            ret = findMethod(factory, methodName, new Class[] {});
            if (ret != null) break;
        }
        return ret;
    }

    public static Method findGetter(String getterName, Class clazz) throws NoSuchMethodException
    {
        return findGetter(getterName, clazz, true);
    }

    public static Method findGetter(String getterName, Class clazz, boolean mandatory) throws NoSuchMethodException
    {
        do
        {
            for (Method method : clazz.getDeclaredMethods())
            {
                // prefix matching: we allow a method name like setWriteAccess for a parameter like write="..."
                if (method.getParameterCount() == 0 && method.getName().startsWith(getterName))
                {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        while (clazz != Object.class);
        if (mandatory)
        {
            throw new NoSuchMethodException(clazz.getName() + "::" + getterName);
        }
        else
        {
            return null;
        }
    }

    public static Method findSetter(String setterName, Class clazz) throws NoSuchMethodException
    {
        return findSetter(setterName, clazz, x -> true);
    }

    public static Method findSetter(String setterName, Class clazz, Predicate<Class> argumentClassFilter) throws NoSuchMethodException
    {
        return findSetter(setterName, clazz, argumentClassFilter, true);
    }

    public static Method findSetter(String setterName, Class clazz, Predicate<Class> argumentClassFilter, boolean mandatory) throws NoSuchMethodException
    {
        do
        {
            for (Method method : clazz.getDeclaredMethods())
            {
                // prefix matching: we allow a method name like setWriteAccess for a parameter like write="..."
                if (method.getParameterCount() == 1 && method.getName().startsWith(setterName) && argumentClassFilter.test(method.getParameterTypes()[0]))
                {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        while (clazz != Object.class);
        if (mandatory)
        {
            throw new NoSuchMethodException(clazz.getName() + "::" + setterName);
        }
        else
        {
            return null;
        }
    }


}

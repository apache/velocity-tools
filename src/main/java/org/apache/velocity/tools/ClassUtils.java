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

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Repository for common class and reflection methods.
 *
 * @author Nathan Bubna
 * @version $Id: ClassUtils.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ClassUtils
{
    /**
     * Return the <code>Class</code> object for the specified fully qualified
     * class name, from this thread's current class loader.  If no
     * class loader is set for the current thread, then the class loader
     * that loaded this class will be used.
     *
     * @param name Fully qualified class name to be loaded
     * @return Class object
     * @exception ClassNotFoundException if the class cannot be found
     */
    public static Class getClass(String name) throws ClassNotFoundException
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null)
        {
            loader = ClassUtils.class.getClassLoader();
        }
        return loader.loadClass(name);
    }

    public static Object getInstance(String classname)
         throws ClassNotFoundException, IllegalAccessException,
                InstantiationException
    {
        return getClass(classname).newInstance();
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

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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * TODO? rename to ClassUtils if nothing else goes in here?
 *       if more does go in here, split into different classes
 *       for different concerns?  not sure i care either way...
 *
 * @author Nathan Bubna
 * @version $Id: Utils.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Utils
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
            loader = Utils.class.getClassLoader();
        }
        return loader.loadClass(name);
    }

    public static Object getInstance(String classname)
         throws ClassNotFoundException, IllegalAccessException,
                InstantiationException
    {
        return getClass(classname).newInstance();
    }

}

package org.apache.velocity.tools.test.blackbox;

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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * <p>Helper class for LinkToolTests class</p>
 *
 * @author Christopher Schultz
 * @version $Id$
 */
public class ServletContextAdaptor implements InvocationHandler
{
    private String _contextPath;

    public ServletContextAdaptor()
    {
        this(null);
    }

    public ServletContextAdaptor(String contextPath)
    {
        _contextPath = contextPath;
        if(null == _contextPath)
        {
            _contextPath = "";
        }
    }

    public Object invoke(Object proxy,
                         Method method,
                         Object[] args)
    {
        Class clazz = method.getDeclaringClass();
        if(clazz.isAssignableFrom(ServletContext.class))
        {
            return context(proxy, method, args);
        }
        else
        {
            throw new IllegalStateException("Unexpected proxy interface: "
                                            + clazz.getName());
        }
    }

    protected Object context(Object proxy,
                             Method method,
                             Object[] args)
    {
        String methodName = method.getName();

        if("getContextPath".equals(methodName))
        {
            return _contextPath;
        }
        else if ("getResource".equals(methodName))
        {
            return getResource((String)args[0]);
        }
        else if ("getResourceAsStream".equals(methodName))
        {
            return getResourceAsStream((String)args[0]);
        }
        else if ("getRequestDispatcher".equals(methodName))
        {
            return getRequestDispatcher((String)args[0]);
        }
        else if ("toString".equals(methodName))
        {
            return toString();
        }
        else
        {
            throw new IllegalStateException("Unexpected method call: "
                                            + methodName);
        }
    }

    protected URL getResource(String path)
    {
        return null;
    }

    protected InputStream getResourceAsStream(String path)
    {
        return null;
    }

    protected RequestDispatcher getRequestDispatcher(String url)
    {
        return null;
    }
}

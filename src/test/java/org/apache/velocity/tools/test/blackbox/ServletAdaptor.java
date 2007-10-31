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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Helper class for LinkToolTests class</p>
 *
 * @author Christopher Schultz
 * @version $Id$
 */
public class ServletAdaptor implements InvocationHandler
{
    private Map _params;
    private String _contextPath;

    public ServletAdaptor(String contextPath,
                          Map params)
    {
        _contextPath = contextPath;
        if(null == _contextPath)
        {
            _contextPath = "";
        }

        _params = params;

        if(null == _params)
        {
            _params = Collections.EMPTY_MAP;
        }
    }

    public Object invoke(Object proxy,
                         Method method,
                         Object[] args)
    {
        Class clazz = method.getDeclaringClass();

        if(clazz.isAssignableFrom(HttpServletRequest.class))
        {
            return request(proxy, method, args);
        }
        else if(clazz.isAssignableFrom(HttpServletResponse.class))
        {
            return response(proxy, method, args);
        }
        else if(clazz.isAssignableFrom(ServletContext.class))
        {
            return context(proxy, method, args);
        }
        else
        {
            throw new IllegalStateException("Unexpected proxy interface: "
                                            + clazz.getName());
        }
    }

    protected Object response(Object proxy,
                              Method method,
                              Object[] args)
    {
        String methodName = method.getName();

        if("encodeURL".equals(methodName)
           || "encodeUrl".equals(methodName))
        {
            // Don't worry about adding ";jsessionid" or anything.
            return args[0];
        }
        else
        {
            throw new IllegalStateException("Unexpected method call: "
                                            + method);
        }
    }

    protected Object request(Object proxy,
                             Method method,
                             Object[] args)
    {
        String methodName = method.getName();

        if("getContextPath".equals(methodName))
        {
            return _contextPath;
        }
        else if("getParameter".equals(methodName))
        {
            Object value = _params.get(args[0]);

            if(value instanceof String)
            {
                return value;
            }
            else if (value instanceof String[])
            {
                return ((String[])value)[0];
            }
            else
            {
                throw new IllegalStateException("Parameter value must be either String or String[].");
            }
        }
        else if("getParameterValues".equals(methodName))
        {
            Object value = _params.get(args[0]);

            if(value instanceof String)
            {
                return new String[] { (String)value };
            }
            else if (value instanceof String[])
            {
                return value;
            }
            else
            {
                throw new IllegalStateException("Parameter value must be either String or String[].");
            }
        }
        else if("getParameterMap".equals(methodName))
        {
            return Collections.unmodifiableMap(_params);
        }
        else if("getParameterNames".equals(methodName))
        {
            return new IteratorEnumeration(_params.keySet().iterator());
        }
        else if("getSession".equals(methodName))
        {
            return null;
        }
        else if("getAttribute".equals(methodName))
        {
            if(((String)args[0]).equals("XHTML"))
            {
                return Boolean.TRUE; // xhtml = true
            }
            else
            {
                return null;
            }
        }
        else if("getCharacterEncoding".equals(methodName))
        {
            return "UTF-8";
        }
        else
        {
            throw new IllegalStateException("Unexpected method call: "
                                            + method);
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
        else
        {
            throw new IllegalStateException("Unexpected method call: "
                                            + methodName);
        }
    }

}

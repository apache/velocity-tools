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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Helper class for LinkToolTests class</p>
 *
 * @author Christopher Schultz
 * @version $Id$
 */
public class RequestAdaptor implements InvocationHandler
{
    // helper class
    public class IteratorEnumeration implements Enumeration
    {
        private Iterator iterator;
        public IteratorEnumeration( Iterator iterator ) { this.iterator = iterator; }
        public boolean hasMoreElements() { return iterator.hasNext(); }
        public Object nextElement() { return iterator.next(); }
    }

    // the params now also serve as a cookie jar for CookieToolTests
    private Map _params;
    private String _contextPath;
    private String _pathInfo;

    public RequestAdaptor()
    {
        this(null, null, null);
    }

    public RequestAdaptor(Map cookies)
    {
        this(null, null, cookies);
    }

    public RequestAdaptor(String contextPath,
                          Map params)
    {
        this(contextPath, "", params);
    }

    public RequestAdaptor(String contextPath, String pathInfo, Map params)
    {
        _contextPath = contextPath;
        if(null == _contextPath)
        {
            _contextPath = "";
        }
        _pathInfo = pathInfo;
        if(null == _pathInfo)
        {
            _pathInfo = "";
        }

        _params = params;

        if(null == _params)
        {
            _params = new HashMap();
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
        else
        {
            throw new IllegalStateException("Unexpected proxy interface: "
                                            + clazz.getName());
        }
    }

    protected Object request(Object proxy,
                             Method method,
                             Object[] args)
    {
        String methodName = method.getName();

        switch (methodName)
        {
            case "getContextPath":
            {
                return _contextPath;
            }
            case "getParameter":
            {
                Object value = _params.get(args[0]);
                if (value instanceof String)
                {
                    return value;
                }
                else if (value instanceof String[])
                {
                    return ((String[]) value)[0];
                }
                else
                {
                    throw new IllegalStateException("Parameter value must be either String or String[].");
                }
            }
            case "getParameterValues":
            {
                Object value = _params.get(args[0]);

                if (value instanceof String)
                {
                    return new String[]{(String) value};
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
            case "getParameterMap":
            {
                return Collections.unmodifiableMap(_params);
            }
            case "getParameterNames":
            {
                return new IteratorEnumeration(_params.keySet().iterator());
            }
            case "getAttribute":
            {
                if (((String) args[0]).equals("XHTML"))
                {
                    return Boolean.TRUE; // xhtml = true
                }
                else
                {
                    return null;
                }
            }
            case "setAttribute":
            {
                return null;
            }
            case "removeAttribute":
            {
                return null;
            }
            case "getSession":
            {
                return null;
            }
            case "getScheme":
            {
                return "http";
            }
            case "getServerPort":
            {
                return new Integer(8081);
            }


            case "getServerName":
            {
                return "localhost";
            }
            case "getServletPath":
            {
                return _contextPath;
            }
            case "getPathInfo":
            {
                return _pathInfo;
            }
            case "getRequestURI":
            {
                return _pathInfo;
            }
            case "getCharacterEncoding":
            {
                return "UTF-8";
            }
            case "getCookies":
            {
                // just let params double as the cookie store
                Cookie[] jar = new Cookie[_params.size()];
                int i = 0;
                for (Iterator iter = _params.keySet().iterator(); iter.hasNext(); i++)
                {
                    Object key = iter.next();
                    Object val = _params.get(key);
                    if (val instanceof Cookie)
                    {
                        jar[i] = (Cookie) val;
                    }
                    else
                    {
                        String name = String.valueOf(key);
                        String value = String.valueOf(val);
                        jar[i] = new Cookie(name, value);
                        _params.put(name, jar[i]);
                    }
                }
                return jar;
            }
            case "getContentLength":
            {
                return getContentLength();
            }
            case "getContentType":
            {
                return getContentType();
            }
            case "getReader":
            {
                return getReader();
            }
            case "toString":
            {
                return toString();
            }
            default:
            {
                throw new IllegalStateException("Unexpected method call: "
                    + method);
            }
        }
    }

    protected int getContentLength()
    {
        return -1;
    }

    protected String getContentType()
    {
        return null;
    }

    protected BufferedReader getReader()
    {
        return null;
    }
}

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
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Helper class for LinkToolTests class</p>
 *
 * @author Christopher Schultz
 * @version $Id$
 */
public class ResponseAdaptor implements InvocationHandler
{
    // the params now also serve as a cookie jar for CookieToolTests
    private Map _params;

    public ResponseAdaptor()
    {
        this(null);
    }

    public ResponseAdaptor(Map params)
    {
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

        if(clazz.isAssignableFrom(HttpServletResponse.class))
        {
            return response(proxy, method, args);
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
        else if ("addCookie".equals(methodName))
        {
            Cookie c = (Cookie)args[0];
            if (c.getMaxAge() == 0)
            {
                _params.remove(c.getName());
            }
            else
            {
                _params.put(c.getName(), c);
            }
            return null;
        }
        else if ("getCharacterEncoding".equals(methodName))
        {
            return "UTF-8";
        }
        else if ("toString".equals(methodName))
        {
            return toString();
        }
        else
        {
            throw new IllegalStateException("Unexpected method call: "
                                            + method);
        }
    }
}

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

import org.apache.velocity.tools.ClassUtils;
import org.junit.Assert;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A base class for blackbox tools testing
 *
 * @version $$
 * @author Claude Brisson
 * @since 3.1
 */

public class BaseToolTests extends Assert
{
    protected static <T> T newTool(Class<T> clazz, InvocationHandler requestHandler, InvocationHandler responseHandler) throws Exception
    {
        ClassLoader classLoader = BaseToolTests.class.getClassLoader();
        Object requestProxy
            = Proxy.newProxyInstance(classLoader,
            new Class[] { HttpServletRequest.class },
            requestHandler);

        Object responseProxy
            = Proxy.newProxyInstance(classLoader,
            new Class[] { HttpServletResponse.class },
            responseHandler);

        HttpServletRequest request = (HttpServletRequest)requestProxy;
        HttpServletResponse response = (HttpServletResponse)responseProxy;

        T tool = clazz.getConstructor(new Class[] {}).newInstance();
        invokeMethodOrNOP(tool, "setRequest", request, HttpServletRequest.class);
        invokeMethodOrNOP(tool, "setResponse", response, HttpServletResponse.class);
        return tool;
    }

    private static void invokeMethodOrNOP(Object tool, String methodName, Object singleArg, Class singleArgClass) throws Exception
    {
        Method method = ClassUtils.findMethod(tool.getClass(), methodName, new Class[] { singleArgClass });
        if (method != null)
        {
            method.invoke(tool, new Object[] { singleArg });
        }
    }
}

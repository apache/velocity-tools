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

import org.apache.velocity.tools.view.JsonTool;
import static org.junit.Assert.*;

import org.apache.velocity.tools.view.ViewContext;
import org.junit.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>CookieTool tests.</p>
 *
 * @author Claude Brisson
 * @version $Id$
 */

public class JsonToolTests
{
    private JsonTool newJsonTool()
    {
        return newJsonTool(new HashMap(), null, null);
    }

    private JsonTool newJsonTool(Map config)
    {
        return newJsonTool(config, null, null);
    }

    private JsonTool newJsonTool(Map config, Object requestProxy, Object responseProxy)
    {
        if (config == null)
        {
            config = new HashMap();
        }

        if (requestProxy == null)
        {
            requestProxy =
                Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    new RequestAdaptor());
        }

        HttpServletRequest request = (HttpServletRequest)requestProxy;

        if (responseProxy == null)
        {
            responseProxy =
                Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{HttpServletResponse.class},
                    new ResponseAdaptor());
        }

        HttpServletResponse response = (HttpServletResponse)responseProxy;

        Object contextProxy
            = Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[]{ServletContext.class},
            new ServletContextAdaptor()
            {
                @Override
                protected URL getResource(String resource)
                {
                    // redirect towards classpath resources
                    if (!resource.startsWith("/"))
                    {
                        resource = "/" + resource;
                    }
                    resource = "webapp_mocking" + resource;
                    return getClass().getClassLoader().getResource(resource);
                }

                @Override
                protected InputStream getResourceAsStream(String resource)
                {
                    // redirect towards classpath resources
                    if (!resource.startsWith("/"))
                    {
                        resource = "/" + resource;
                    }
                    resource = "webapp_mocking" + resource;
                    return getClass().getClassLoader().getResourceAsStream(resource);
                }

                @Override
                protected RequestDispatcher getRequestDispatcher(String url)
                {
                    return new RequestDispatcher()
                    {
                        @Override
                        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
                        {
                            throw new ServletException("not implemented");
                        }

                        @Override
                        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
                        {
                            response.setContentType("application/json");
                            response.getWriter().write("{ \"hey\" : \"bro\" }");
                        }
                    };
                }

            });

        ServletContext servletContext = (ServletContext)contextProxy;

        JsonTool json = new JsonTool();
        config.put(ViewContext.REQUEST, request);
        config.put(ViewContext.RESPONSE, response);
        config.put(ViewContext.SERVLET_CONTEXT_KEY, servletContext);
        json.configure(config);
        return json;
    }

    public @Test void testConfigReadWebappResource()
    {
        Map config = new HashMap();
        config.put("resource", "/inwebapp.json");
        JsonTool json = newJsonTool(config);
        assertEquals(json.get("hey"), "there");
    }

    public @Test void testConfigReadClasspathResource()
    {
        Map config = new HashMap();
        config.put("resource", "/inclasspath.json");
        JsonTool json = newJsonTool(config);
        assertEquals(json.get("hey"), "brother");
    }

    public @Test void testConfigFetchLocalSource()
    {
        Map config = new HashMap();
        config.put("url", "/local-url.json");
        JsonTool json = newJsonTool(config);
        assertEquals(json.get("hey"), "bro");
    }

    public @Test void testConfigFetchRemoteSource()
    {
        TestURLHandler.registerTestURL("foo.json", "{ \"foo\": \"bar\", \"array\": [ \"foo1\", \"foo2\"] }");
        Map config = new HashMap();
        config.put("url", "veltest://anywhere/foo.json");
        JsonTool json = newJsonTool(config);
        assertEquals("bar", json.get("foo"));
    }

    public @Test void testRequestContent()
    {
        final String content = "{ \"hey\" : \"sister\" }";
        Object requestProxy =
            Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class},
                new RequestAdaptor()
                {
                    @Override
                    protected int getContentLength()
                    {
                        return content.length();
                    }

                    @Override
                    protected String getContentType()
                    {
                        return "application/json";
                    }

                    @Override
                    protected BufferedReader getReader()
                    {
                        return new BufferedReader(new StringReader(content));
                    }
                });
        JsonTool json = newJsonTool(null, requestProxy, null);
        assertEquals(json.get("hey"), "sister");
    }

    public @Test void testParseString()
    {
        JsonTool json = newJsonTool();
        json.parse("{\"hey\": \"you\"}");
        assertEquals(json.get("hey"), "you");
    }

    public @Test void testReadWebappResource()
    {
        JsonTool json = newJsonTool();
        json.read("/inwebapp.json");
        assertEquals(json.get("hey"), "there");
    }

    public @Test void testReadClasspathResource()
    {
        JsonTool json = newJsonTool();
        json.read("/inclasspath.json");
        assertEquals(json.get("hey"), "brother");
    }

    public @Test void testFetchLocalSource()
    {
        JsonTool json = newJsonTool();
        json.fetch("/local-url.json");
        assertEquals(json.get("hey"), "bro");
    }

    public @Test void testFetchRemoteSource()
    {
        JsonTool json = newJsonTool();
        json.fetch("http://svn.apache.org/viewvc/velocity/tools/trunk/velocity-tools-generic/src/test/resources/foo.json?revision=1776916&view=co&pathrev=1776916");
        assertNull(json.get("foo")); // CB TODO - also check there is a warning about safe mode in the logs
    }
}

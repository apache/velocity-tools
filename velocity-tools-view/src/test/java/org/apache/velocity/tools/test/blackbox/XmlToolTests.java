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

import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.tools.view.XmlTool;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * <p>CookieTool tests.</p>
 *
 * @author Claude Brisson
 * @version $Id$
 */

public class XmlToolTests
{
    private XmlTool newXmlTool()
    {
        return newXmlTool(new HashMap(), null, null);
    }

    private XmlTool newXmlTool(Map config)
    {
        return newXmlTool(config, null, null);
    }

    private XmlTool newXmlTool(Map config, Object requestProxy, Object responseProxy)
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
                            response.setContentType("application/xml");
                            response.getWriter().write("<hey>bro</hey>");
                        }
                    };
                }

            });

        ServletContext servletContext = (ServletContext)contextProxy;

        XmlTool xml = new XmlTool();
        config.put(ViewContext.REQUEST, request);
        config.put(ViewContext.RESPONSE, response);
        config.put(ViewContext.SERVLET_CONTEXT_KEY, servletContext);
        xml.configure(config);
        return xml;
    }

    public @Test void testConfigReadWebappResource()
    {
        Map config = new HashMap();
        config.put("resource", "/inwebapp.xml");
        XmlTool xml = newXmlTool(config);
        assertEquals("there", xml.getText());
    }

    public @Test void testConfigReadClasspathResource()
    {
        Map config = new HashMap();
        config.put("resource", "/inclasspath.xml");
        XmlTool xml = newXmlTool(config);
        assertEquals("brother", xml.getText());
    }

    public @Test void testConfigFetchLocalSource()
    {
        Map config = new HashMap();
        config.put("url", "/local-url.json");
        XmlTool xml = newXmlTool(config);
        assertEquals("bro", xml.getText());
    }

    /*
      TODO - Test is ignored because it would fail while offline. We need to set up a local web server using Jetty to properly test it.
      Michael Osipov suggested this example:
      https://github.com/apache/maven-wagon/blob/master/wagon-providers/wagon-http/src/test/java/org/apache/maven/wagon/providers/http/HugeFileDownloadTest.java
    */
    public @Test void testConfigFetchRemoteSource()
    {
        TestURLHandler.registerTestURL("foo.xml",
            "<foo>\n" +
            "  <bar name=\"a\"/>\n" +
            "  <baz>woogie</baz>\n" +
            "  <baz>wiggie</baz>\n" +
            "</foo>");
        Map config = new HashMap();
        config.put("url", "veltest://anywhere/foo.xml");
        XmlTool xml = newXmlTool(config);
        assertEquals("woogie\n  wiggie", xml.getText());
    }

    public @Test void testRequestContent()
    {
        final String content = "<?xml version=\"1.0\"?><hey>sister</hey>";
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
                        return "application/xml";
                    }

                    @Override
                    protected BufferedReader getReader()
                    {
                        return new BufferedReader(new StringReader(content));
                    }
                });
        XmlTool xml = newXmlTool(null, requestProxy, null);
        assertEquals("sister", xml.getText());
    }

    public @Test void testParseString()
    {
        XmlTool xml = newXmlTool();
        xml.parse("<?xml version=\"1.0\"?><hey>you</hey>");
        assertEquals("you", xml.getText());
    }

    public @Test void testReadWebappResource()
    {
        XmlTool xml = newXmlTool();
        xml.read("/inwebapp.xml");
        assertEquals("there", xml.getText());
    }

    public @Test void testReadClasspathResource()
    {
        XmlTool xml = newXmlTool();
        xml.read("/inclasspath.xml");
        assertEquals("brother", xml.getText());
    }

    public @Test void testFetchLocalSource()
    {
        XmlTool xml = newXmlTool();
        xml.fetch("/local-url.json");
        assertEquals("bro", xml.getText());
    }

    public @Test void testFetchRemoteSource()
    {
        XmlTool xml = newXmlTool();
        xml.fetch("http://svn.apache.org/viewvc/velocity/tools/trunk/velocity-tools-generic/src/test/resources/file.xml?revision=1776916&view=co&pathrev=1776916");
        assertNull(xml.getText()); // CB TODO - also check there is a warning about safe mode in the logs
    }
}

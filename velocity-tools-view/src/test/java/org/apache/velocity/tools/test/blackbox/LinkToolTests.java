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

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.LinkTool;
import org.apache.velocity.tools.view.ViewContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>LinkTool tests.</p>
 *
 * @author Christopher Schultz
 * @version $Id$
 */
public class LinkToolTests
{

    static class ConfigValues extends ValueParser
    {
        ConfigValues() { setReadOnly(false); }
    }

    private LinkTool newLinkTool(InvocationHandler requestHandler, InvocationHandler responseHandler)
    {
        Object requestProxy
            = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                     new Class[] { HttpServletRequest.class },
                                     requestHandler);

        Object responseProxy
                = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] { HttpServletResponse.class },
                responseHandler);

        HttpServletRequest request = (HttpServletRequest)requestProxy;
        HttpServletResponse response = (HttpServletResponse)responseProxy;

        LinkTool link = new LinkTool();
        ValueParser properties = new ConfigValues();
        properties.put(ViewContext.REQUEST, request);
        properties.put(ViewContext.RESPONSE, response);
        link.configure(properties);
        return link;
    }

    private LinkTool newLinkTool(Map params)
    {
        return newLinkTool(new RequestAdaptor("/test","/link.vm", params), new ResponseAdaptor(params));
    }

    private LinkTool newLinkTool(String key, Object value)
    {
        HashMap params = new HashMap();
        params.put(key, value);
        return newLinkTool(params);
    }

    public @Test void testAddAllParameters()
    {
        LinkTool link = newLinkTool("a", "b");
        Assert.assertEquals("/test", link.getContextPath());

        String url = ((LinkTool)(link.relative("/target")
            .append("foo", "bar")
            .append("bar", "baz")))
            .addRequestParams()
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=b", url);
    }

    public @Test void testAddMultiValueParameters()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = ((LinkTool)(link.relative("/target")
            .append("foo", "bar")
            .append("bar", "baz")))
            .addRequestParams()
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=a&amp;a=b&amp;a=c", url);
    }

    public @Test void testAddIgnoreParameters()
    {
        HashMap params = new HashMap();
        params.put("a", "b");
        params.put("b", "c");
        LinkTool link = newLinkTool(params);

        String url = ((LinkTool)(link.relative("/target")
            .append("foo", "bar")
            .append("bar", "baz")))
            .addRequestParamsExcept("b")
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=b", url);
    }

    public @Test void testAddAllParametersFirst()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = ((LinkTool)(link.relative("/target")))
            .addRequestParams()
            .append("foo", "bar")
            .append("bar", "baz")
            .toString();

        Assert.assertEquals("/test/target?a=b&amp;foo=bar&amp;bar=baz", url);
    }

    public @Test void testAddAdditionalValue()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = ((LinkTool)(link.relative("/target")
            .append("a", "c")))
            .addRequestParams()
            .toString();

        Assert.assertEquals("/test/target?a=c&amp;a=b", url);
    }

    public @Test void testAddAdditionalValueAfter()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = ((LinkTool)(link.relative("/target")))
            .addRequestParams()
            .append("a", "c")
            .toString();

        Assert.assertEquals("/test/target?a=b&amp;a=c", url);
    }

    public @Test void testAutoIgnore()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = link.relative("/target")
            .append("a", "c")
            .toString();

        Assert.assertEquals("/test/target?a=c", url);
    }

    public @Test void testAutoIgnoreMultiple()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = ((LinkTool)(link.relative("/target")
            .append("a", "d")))
            .addMissingRequestParams("a")
            .toString();

        Assert.assertEquals("/test/target?a=d", url);
    }

    public @Test void testNoIgnoreMultiple_WrongOrder()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = ((LinkTool)(link.relative("/target")))
            .addRequestParams()
            .append("a", "d")
            .toString();

        Assert.assertEquals("/test/target?a=a&amp;a=b&amp;a=c&amp;a=d", url);
    }

    public @Test void test_VELTOOLS_148()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        LinkTool forward = (LinkTool)link.relative("/foo")
            .append("bar", "baz");

        Assert.assertEquals("/test/foo?bar=baz&amp;a=a&amp;a=b&amp;a=c",
                            forward.addRequestParams().toString());

        Assert.assertEquals("/test/foo?bar=baz&amp;a=a&amp;a=b&amp;a=c",
                            forward.addRequestParams().toString());
    }
}

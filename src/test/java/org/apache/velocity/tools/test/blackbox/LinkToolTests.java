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
import org.apache.velocity.tools.view.tools.LinkTool;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    private LinkTool newLinkTool(InvocationHandler handler)
    {
        Object proxy
            = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                     new Class[] { HttpServletRequest.class,
                                                   HttpServletResponse.class },
                                     handler);

        HttpServletRequest request = (HttpServletRequest)proxy;
        HttpServletResponse response = (HttpServletResponse)proxy;

        LinkTool link = new LinkTool();
        link.setRequest(request);
        link.setResponse(response);
        return link;
    }

    private LinkTool newLinkTool(Map params)
    {
        return newLinkTool(new ServletAdaptor("/test","/link.vm", params));
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

        String url = link.setRelative("/target")
            .addQueryData("foo", "bar")
            .addQueryData("bar", "baz")
            .addAllParameters()
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=b", url);
    }

    public @Test void testAddMultiValueParameters()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = link.setRelative("/target")
            .addQueryData("foo", "bar")
            .addQueryData("bar", "baz")
            .addAllParameters()
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=a&amp;a=b&amp;a=c", url);
    }

    public @Test void testAddIgnoreParameters()
    {
        HashMap params = new HashMap();
        params.put("a", "b");
        params.put("b", "c");
        LinkTool link = newLinkTool(params);

        String url = link.setRelative("/target")
            .addQueryData("foo", "bar")
            .addQueryData("bar", "baz")
            .addIgnore("b")
            .addAllParameters()
            .toString();

        Assert.assertEquals("/test/target?foo=bar&amp;bar=baz&amp;a=b", url);
    }

    public @Test void testAddAllParametersFirst()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = link.setRelative("/target")
            .addAllParameters()
            .addQueryData("foo", "bar")
            .addQueryData("bar", "baz")
            .toString();

        Assert.assertEquals("/test/target?a=b&amp;foo=bar&amp;bar=baz", url);
    }

    public @Test void testAddAdditionalValue()
    {
        LinkTool link = newLinkTool("a", "b");
        link.setAutoIgnoreParameters(false);

        String url = link.setRelative("/target")
            .addQueryData("a", "c")
            .addAllParameters()
            .toString();

        Assert.assertEquals("/test/target?a=c&amp;a=b", url);
    }

    public @Test void testAddAdditionalValueAfter()
    {
        LinkTool link = newLinkTool("a", "b");
        link.setAutoIgnoreParameters(false);

        String url = link.setRelative("/target")
            .addAllParameters()
            .addQueryData("a", "c")
            .toString();

        Assert.assertEquals("/test/target?a=b&amp;a=c", url);
    }

    public @Test void testAutoIgnore()
    {
        LinkTool link = newLinkTool("a", "b");

        String url = link.setRelative("/target")
            .addQueryData("a", "c")
            .toString();

        Assert.assertEquals("/test/target?a=c", url);
    }

    public @Test void testAutoIgnoreMultiple()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = link.setRelative("/target")
            .addQueryData("a", "d")
            .addAllParameters()
            .toString();

        Assert.assertEquals("/test/target?a=d", url);
    }

    public @Test void testNoIgnoreMultiple_WrongOrder()
    {
        LinkTool link = newLinkTool("a", new String[] { "a", "b", "c" });

        String url = link.setRelative("/target")
            .addAllParameters()
            .addQueryData("a", "d")
            .toString();

        Assert.assertEquals("/test/target?a=a&amp;a=b&amp;a=c&amp;a=d", url);
    }

}

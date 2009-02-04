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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.tools.view.CookieTool;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>CookieTool tests.</p>
 *
 * @author Nathan Bubna
 * @version $Id$
 */
public class CookieToolTests
{
    private CookieTool newCookieTool(InvocationHandler handler)
    {
        Object proxy
            = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                     new Class[] { HttpServletRequest.class,
                                                   HttpServletResponse.class },
                                     handler);

        HttpServletRequest request = (HttpServletRequest)proxy;
        HttpServletResponse response = (HttpServletResponse)proxy;

        CookieTool cookies = new CookieTool();
        cookies.setRequest(request);
        cookies.setResponse(response);
        return cookies;
    }

    private CookieTool newCookieTool(Map cookies)
    {
        return newCookieTool(new ServletAdaptor(cookies));
    }

    private CookieTool newCookieTool(String name, Object value)
    {
        Map cookies = new LinkedHashMap();
        cookies.put(name, value);
        return newCookieTool(cookies);
    }

    public @Test void testCreate_StringString()
    {
        CookieTool cookies = newCookieTool(new LinkedHashMap());
        Cookie c = cookies.create("a", "b");
        assertNotNull(c);
        assertEquals("a", c.getName());
        assertEquals("b", c.getValue());
        assertEquals(-1, c.getMaxAge());
    }

    public @Test void testCreate_StringStringObject()
    {
        CookieTool cookies = newCookieTool(new LinkedHashMap());
        Cookie c = cookies.create("a", "b", 10);
        assertNotNull(c);
        assertEquals("a", c.getName());
        assertEquals("b", c.getValue());
        assertEquals(10, c.getMaxAge());
        c = cookies.create("a", "b", "500");
        assertNotNull(c);
        assertEquals(500, c.getMaxAge());
        c = cookies.create("a", "b", "asd");
        assertNull(c);
    }

    public @Test void testGet_String()
    {
        CookieTool cookies = newCookieTool("a", "b");
        assertEquals("b", cookies.get("a").toString());
    }

    public @Test void testGetAll()
    {
        CookieTool cookies = newCookieTool("a", "b");
        assertEquals("[b]", cookies.getAll().toString());

        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        jar.put("foo", "bar");
        cookies = newCookieTool(jar);
        List<Cookie> all = cookies.getAll();
        assertEquals(2, all.size());
        assertEquals("[b, bar]", all.toString());
        assertEquals("a", all.get(0).getName());
        assertEquals("foo", all.get(1).getName());
    }

    public @Test void testToString()
    {
        CookieTool cookies = newCookieTool("a", "b");
        assertEquals("[a=b]", cookies.toString());

        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        jar.put("foo", "bar");
        cookies = newCookieTool(jar);
        assertEquals("[a=b, foo=bar]", cookies.toString());
    }

    public @Test void testAdd_StringString()
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        ServletAdaptor proxy = new ServletAdaptor(jar);
        CookieTool cookies = newCookieTool(proxy);
        assertEquals("", cookies.add("a","b"));

        cookies = newCookieTool(proxy);
        assertNotNull(cookies.get("a"));
        assertEquals("b", cookies.get("a").getValue());
    }

    public @Test void testAdd_StringStringObject()
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        ServletAdaptor proxy = new ServletAdaptor(jar);
        CookieTool cookies = newCookieTool(proxy);
        assertEquals("", cookies.add("a","b", 10));

        cookies = newCookieTool(proxy);
        Cookie c = cookies.get("a");
        assertNotNull(c);
        assertEquals("b", c.getValue());
        assertEquals(10, c.getMaxAge());
    }

    public @Test void testDelete_String()
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        ServletAdaptor proxy = new ServletAdaptor(jar);
        CookieTool cookies = newCookieTool(proxy);
        assertEquals("b", cookies.get("a").toString());
        cookies.delete("a");

        cookies = newCookieTool(proxy);
        assertNull(cookies.get("a"));
    }
}

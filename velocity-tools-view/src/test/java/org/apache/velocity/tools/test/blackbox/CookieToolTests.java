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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>CookieTool tests.</p>
 *
 * @author Nathan Bubna
 * @version $Id$
 */
public class CookieToolTests extends BaseToolTests
{
    private CookieTool newCookieTool(InvocationHandler requestHandler, InvocationHandler responseHandler) throws Exception
    {
        return newTool(CookieTool.class, requestHandler, responseHandler);
    }

    private CookieTool newCookieTool(Map cookies) throws Exception
    {
        return newCookieTool(new RequestAdaptor(cookies), new ResponseAdaptor(cookies));
    }

    private CookieTool newCookieTool(String name, Object value) throws Exception
    {
        Map cookies = new LinkedHashMap();
        cookies.put(name, value);
        return newCookieTool(cookies);
    }

    public @Test void testCreate_StringString() throws Exception
    {
        CookieTool cookies = newCookieTool(new LinkedHashMap());
        Cookie c = cookies.create("a", "b");
        assertNotNull(c);
        assertEquals("a", c.getName());
        assertEquals("b", c.getValue());
        assertEquals(-1, c.getMaxAge());
    }

    public @Test void testCreate_StringStringObject() throws Exception
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

    public @Test void testGet_String() throws Exception
    {
        CookieTool cookies = newCookieTool("a", "b");
        assertEquals("b", cookies.get("a").toString());
    }

    public @Test void testGetAll() throws Exception
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

    public @Test void testToString() throws Exception
    {
        CookieTool cookies = newCookieTool("a", "b");
        assertEquals("[a=b]", cookies.toString());

        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        jar.put("foo", "bar");
        cookies = newCookieTool(jar);
        assertEquals("[a=b, foo=bar]", cookies.toString());
    }

    public @Test void testAdd_StringString() throws Exception
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        RequestAdaptor requestProxy = new RequestAdaptor(jar);
        ResponseAdaptor responseProxy = new ResponseAdaptor(jar);
        CookieTool cookies = newCookieTool(requestProxy, responseProxy);
        assertEquals("", cookies.add("a","b"));

        cookies = newCookieTool(requestProxy, responseProxy);
        assertNotNull(cookies.get("a"));
        assertEquals("b", cookies.get("a").getValue());
    }

    public @Test void testAdd_StringStringObject() throws Exception
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        RequestAdaptor requestProxy = new RequestAdaptor(jar);
        ResponseAdaptor responseProxy = new ResponseAdaptor(jar);
        CookieTool cookies = newCookieTool(requestProxy, responseProxy);
        assertEquals("", cookies.add("a","b", 10));

        cookies = newCookieTool(requestProxy, responseProxy);
        Cookie c = cookies.get("a");
        assertNotNull(c);
        assertEquals("b", c.getValue());
        assertEquals(10, c.getMaxAge());
    }

    public @Test void testDelete_String() throws Exception
    {
        Map jar = new LinkedHashMap();
        jar.put("a", "b");
        RequestAdaptor requestProxy = new RequestAdaptor(jar);
        ResponseAdaptor responseProxy = new ResponseAdaptor(jar);
        CookieTool cookies = newCookieTool(requestProxy, responseProxy);
        assertEquals("b", cookies.get("a").toString());
        cookies.delete("a");

        cookies = newCookieTool(requestProxy, responseProxy);
        assertNull(cookies.get("a"));
    }
}

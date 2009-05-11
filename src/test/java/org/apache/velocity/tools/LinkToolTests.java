package org.apache.velocity.tools.generic;

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

import org.junit.*;
import static org.junit.Assert.*;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * <p>Tests for generic version of LinkTool</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class LinkToolTests {

    public static final Map DEFAULT_PROPS = new HashMap();
    static
    {
        // don't lock configure() for testing
        DEFAULT_PROPS.put(LinkTool.SAFE_MODE_KEY, false);
        DEFAULT_PROPS.put(LinkTool.LOCK_CONFIG_KEY, false);
    }

    /**
     * Returns a new instance configured with the 
     * default testing properties.
     */
    public LinkTool newInstance()
    {
        LinkTool link = new LinkTool();
        link.configure(DEFAULT_PROPS);
        return link;
    }

    public LinkTool newInstance(String uri)
    {
        return newInstance(LinkTool.URI_KEY, uri);
    }

    /**
     * Returns a new instance configured with the 
     * default testing properties and the specified
     * non-default property.
     */
    public LinkTool newInstance(String key, Object value)
    {
        LinkTool link = new LinkTool();
        Map props = new HashMap(DEFAULT_PROPS);
        props.put(key, value);
        link.configure(props);
        return link;
    }

    public @Test void ctorLinkTool() throws Exception
    {
        try
        {
            new LinkTool();
        }
        catch (Exception e)
        {
            fail("Constructor 'LinkTool()' failed due to: " + e);
        }
    }

    public @Test void methodConfigure_ValueParser() throws Exception
    {
        LinkTool link = newInstance("mailto:nbubna@apache.org");
        assertEquals("mailto", link.getScheme());
        assertEquals("mailto:nbubna@apache.org", link.toString());
        assertTrue(link.getUri().isOpaque());
        assertTrue(link.isAbsolute());
    }

    public @Test void methodDuplicate() throws Exception
    {
        LinkTool link = newInstance("http://apache.org/foo.html");
        LinkTool result = link.duplicate();
        assertFalse(link == result);
        assertSame(link.getScheme(), result.getScheme());
        assertSame(link.getUser(), result.getUser());
        assertSame(link.getHost(), result.getHost());
        assertSame(link.getPath(), result.getPath());
        assertSame(link.query, result.query);
        assertSame(link.getAnchor(), result.getAnchor());
        assertSame(link.getSelf(), result.getSelf());
    }

    public @Test void methodDuplicate_boolean() throws Exception
    {
        LinkTool link = newInstance("http://apache.org/foo.html?foo=bar");
        LinkTool result = link.duplicate(true);
        assertFalse(link == result);
        assertSame(link.getPath(), result.getPath());
        assertFalse(link.query == result.query);
        assertEquals(link.getQuery(), result.getQuery());
        assertSame(link.getSelf(), result.getSelf());
        assertSame(result.query, result.duplicate(false).query);
    }

    public @Test void methodEncode_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals("this+has+spaces", link.encode("this has spaces"));
        assertEquals("%40%2F+", link.encode("@/ "));
    }

    public @Test void methodDecode_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals("this has spaces", link.decode("this+has+spaces"));
        assertEquals("@/ ", link.decode("%40%2F+"));
    }

    public @Test void methodSetScheme_Object() throws Exception
    {
        LinkTool link = newInstance(LinkTool.SCHEME_KEY, LinkTool.DEFAULT_SCHEME);
        assertEquals(LinkTool.DEFAULT_SCHEME, link.getScheme());
        link.setScheme(null);
        assertEquals(null, link.getScheme());
        link.setScheme("foo:");
        assertEquals("foo", link.getScheme());
    }

    public @Test void methodScheme_Object() throws Exception
    {
        LinkTool link = newInstance();
        LinkTool result = link.scheme(null);
        assertEquals(null, result.getScheme());
        link = newInstance("https://apache.org");
        assertEquals(null, link.getPath());
        assertEquals("https://apache.org", link.toString());
        assertEquals(LinkTool.SECURE_SCHEME, link.getScheme());
        assertTrue(link.isSecure());
        result = link.scheme(LinkTool.DEFAULT_SCHEME);
        assertEquals("http://apache.org", result.toString());
        assertFalse(result.isSecure());
    }

    public @Test void methodGetScheme() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.getScheme());
        assertEquals("mailto", link.scheme("mailto").getScheme());
    }

    public @Test void methodSecure() throws Exception
    {
        LinkTool link = newInstance();
        assertFalse(link.isSecure());
        LinkTool result = link.secure();
        assertTrue(result.isSecure());
    }

    public @Test void methodInsecure() throws Exception
    {
        LinkTool link = newInstance("https://apache.org");
        assertTrue(link.isSecure());
        LinkTool result = link.insecure();
        assertFalse(result.isSecure());
    }

    public @Test void methodIsAbsolute() throws Exception
    {
        LinkTool link = newInstance();
        assertFalse(link.isAbsolute());
        LinkTool result = link.absolute("http://apache.org");
        assertTrue(result.isAbsolute());
    }

    public @Test void methodSetUserInfo_Object() throws Exception
    {
        LinkTool link = newInstance(LinkTool.USER_KEY, "nbubna");
        assertEquals("nbubna", link.getUser());
        link.setUserInfo(null);
        assertEquals(null, link.getUser());
        // no encoding should happen here
        link.setUserInfo("@#$ /@!");
        assertEquals("@#$ /@!", link.getUser());
    }

    public @Test void methodGetUser() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.getUser());
        link = newInstance("http://nbubna@apache.org");
        assertEquals("nbubna", link.getUser());
    }

    public @Test void methodUser_Object() throws Exception
    {
        LinkTool link = newInstance("http://nbubna@apache.org");
        assertEquals(null, link.user(null).getUser());
        assertEquals("nbubna", link.user("nbubna").getUser());
        assertEquals("@#$ /!", link.user("@#$ /!").getUser());
        assertEquals("http://%40%23$%20%2F!@apache.org", link.user("@#$ /!").toString());
    }

    public @Test void methodGetHost() throws Exception
    {
        LinkTool link = newInstance("http://apache.org");
        assertEquals("apache.org", link.getHost());
        link.setFromURI("http://velocity.apache.org/tools/devel/");
        assertEquals("velocity.apache.org", link.getHost());
    }

    public @Test void methodHost_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals("apache.org", link.host("apache.org").getHost());
        link = newInstance("https://nbubna@www.apache.org");
        assertEquals("https://nbubna@people.apache.org", link.host("people.apache.org").toString());
    }

    public @Test void methodSetHost_Object() throws Exception
    {
        LinkTool link = newInstance();
        link.setHost("foo.com");
        assertEquals("foo.com", link.getHost());
    }

    public @Test void methodGetPort() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.getPort());
        link = newInstance(LinkTool.PORT_KEY, 42);
        assertEquals(42, link.getPort());
    }

    public @Test void methodPort_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.port(null).getPort());
        assertNull(link.port(":asd").getPort());
        assertEquals(1, link.port(1).getPort());
        assertEquals(42, link.port("42").getPort());
    }

    public @Test void methodSetPort_Object() throws Exception
    {
        LinkTool link = newInstance();
        link.setPort(42);
        assertEquals(42, link.getPort());
    }

    public @Test void methodGetPath() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.getPath());
        link = newInstance("http://velocity.apache.org/tools/devel");
        assertEquals("/tools/devel", link.getPath());
    }

    public @Test void methodSetPath_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.getPath());
        link.setPath("foo");
        assertEquals("/foo", link.getPath());
        link.setPath("/foo");
        assertEquals("/foo", link.getPath());
        link.setPath("/foo/");
        assertEquals("/foo/", link.getPath());
        link.setPath("/foo/");
        assertEquals("/foo/", link.getPath());
    }

    public @Test void methodPath_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.getPath());
        assertEquals("/bar", link.path("bar").getPath());
        assertEquals("/bar", link.path("/bar").getPath());
        assertEquals("/bar/", link.path("bar/").getPath());
        assertEquals("/bar/", link.path("/bar/").getPath());
        link = newInstance("http://foo.com/this/that.vm");
        assertEquals("http://foo.com/this/that.vm", link.toString());
        assertEquals("http://foo.com/bar.vm", link.path("bar.vm").toString());
    }

    public @Test void methodCombinePath_StringString() throws Exception
    {
        LinkTool link = newInstance();
        String none = null;
        String empty = "";
        String test = "test";
        String starts = "/this";
        String ends = "that/";
        String both = "/these/";
        assertNull(link.combinePath(none, none));
        assertSame(empty, link.combinePath(none, empty));
        assertSame(empty, link.combinePath(empty, none));
        assertEquals("/test", link.combinePath(empty, test));
        assertEquals("test/", link.combinePath(test, empty));
        assertEquals("/this/this", link.combinePath(starts, starts));
        assertEquals("that/that/", link.combinePath(ends, ends));
        assertEquals("/this/that/", link.combinePath(starts, ends));
        assertEquals("that/this", link.combinePath(ends, starts));
        assertEquals("/these/these/", link.combinePath(both, both));
    }

    public @Test void methodAppendPath_Object() throws Exception
    {
        LinkTool link = newInstance(LinkTool.PATH_KEY, "/foo");
        assertEquals("/foo", link.getPath());
        link.appendPath("bar");
        assertEquals("/foo/bar", link.getPath());
        link.appendPath("/bar");
        assertEquals("/foo/bar/bar", link.getPath());
        link.setPath("/foo/");
        link.appendPath("bar/");
        assertEquals("/foo/bar/", link.getPath());
        link.appendPath("/bar");
        assertEquals("/foo/bar/bar", link.getPath());
    }

    public @Test void methodAppend_Object() throws Exception
    {
        LinkTool link = newInstance(LinkTool.PATH_KEY, "/foo");
        assertEquals("/foo", link.append(null).getPath());
        link.setPath(null);
        assertNull(link.getPath());
        link = link.append("bar");
        assertEquals("/bar", link.getPath());
        assertEquals("/bar/foo", link.append("foo").getPath());
    }

    public @Test void methodGetDirectory() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("/ctx/", link.getDirectory());
        link = newInstance("http://foo.com");
        assertNull(link.getDirectory());
        link = newInstance("http://foo.com/bar");
        assertEquals("/", link.getDirectory());
        link = newInstance("http://foo.com/bar/foo/bar/foo");
        assertEquals("/bar/foo/bar/", link.getDirectory());
    }

    public @Test void methodGetFile() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("request.vm", link.getFile());
        link = newInstance("http://foo.com/foo/bar/request.vm?this=that#anc");
        assertEquals("request.vm", link.getFile());
        link = newInstance("http://foo.com/bar/");
        assertEquals("", link.getFile());
    }

    public @Test void methodGetRoot() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("http://foo.com", link.getRoot());
        link.setHost("apache.org");
        assertEquals("https://apache.org", link.secure().getRoot());
    }

    public @Test void methodDirectory() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("http://foo.com/ctx/", link.directory().toString());
        link = newInstance("http://foo.com");
        assertEquals("http://foo.com", link.directory().toString());
    }

    public @Test void methodRoot() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("http://foo.com", link.root().toString());
        link = newInstance("http://foo.com");
        assertEquals("http://foo.com", link.root().toString());
        link = newInstance("dev@velocity.apache.org");
        assertNull(link.root());
    }

    public @Test void methodSetForceRelative_boolean() throws Exception
    {
        LinkTool link = newInstance("http://apache.org/bar");
        assertEquals("http://apache.org/bar", link.toString());
        link.setForceRelative(true);
        assertEquals("/bar", link.toString());
    }

    public @Test void methodRelative_Object() throws Exception
    {
        LinkTool link = newInstance("/ctx/request.vm?this=that#anc");
        assertEquals("/ctx/request.vm", link.getPath());
        assertEquals("/ctx/", link.getDirectory());
        assertEquals("this=that", link.getQuery());
        assertEquals("anc", link.getAnchor());
        assertEquals("/ctx/request.vm?this=that#anc", link.toString());
        assertEquals("/ctx/?this=that#anc", link.relative(null).toString());
        assertEquals("/ctx/other.vm?this=that#anc", link.relative("other.vm").toString());
        link = newInstance("http://foo.com/bar/");
        assertEquals("/bar/woogie.vm", link.relative("woogie.vm").toString());
        link = newInstance("/bar/");
        assertEquals("/bar/foo/woogie.vm", link.relative("foo/woogie.vm").toString());
        assertEquals("/bar/yo", link.relative("yo").toString());
    }

    public @Test void methodIsRelative() throws Exception
    {
        LinkTool link = newInstance("/ctx/request.vm?this=that#anc");
        assertTrue(link.isRelative());
        link = newInstance("http://foo.com/bar.vm?q=woogie");
        assertFalse(link.isRelative());
        assertTrue(link.relative().isRelative());
        link = newInstance("http://apache.org").relative("foo.vm");
        assertTrue(link.isRelative());
    }

    public @Test void methodAbsolute_Object() throws Exception
    {
        LinkTool link = newInstance();
        LinkTool result = link.absolute(null);

        result = link.absolute("http://apache.org");
        assertEquals(link.uri("http://apache.org"), result);
        assertTrue(result.isAbsolute());
        assertEquals("http://apache.org", result.toString());
        assertEquals(LinkTool.DEFAULT_SCHEME, result.getScheme());
        assertEquals("apache.org", result.getHost());

        assertFalse(link.isAbsolute());
        result = link.absolute("/test/foo.vm");
        assertTrue(result.isAbsolute());
        assertEquals("/test/foo.vm", result.getPath());
        assertEquals(null, result.getHost());
        assertEquals("http:/test/foo.vm", result.toString());
        result = result.host("apache.org");
        assertEquals("http://apache.org/test/foo.vm", result.toString());
        result = result.absolute("bar.vm");
        assertEquals("http://apache.org/test/bar.vm", result.toString());
        result = result.absolute("/woogie.vm");
        assertEquals("http://apache.org/woogie.vm", result.toString());
    }

    public @Test void methodGetBaseRef() throws Exception
    {
        LinkTool link = newInstance("http://foo.com/ctx/request.vm?this=that#anc");
        assertEquals("http://foo.com/ctx/request.vm", link.getBaseRef());
        assertEquals(null, newInstance().getBaseRef());
    }

    public @Test void methodGetUri() throws Exception
    {
        LinkTool link = newInstance(LinkTool.SAFE_MODE_KEY, true);
        link.setFromURI("http://velocity.apache.org");
        assertNull(link.getUri());
        link = newInstance();
        assertNull(link.getUri());
        link = link.secure().user("nbubna").host("people.apache.org");
        assertNotNull(link.getUri());
        assertEquals("nbubna", link.getUri().getUserInfo());
    }

    public @Test void methodSetFromURI_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.toString());
        link.setFromURI("*%&$^%#$*&^!");
        assertNull(link.toString());
        link.setFromURI("http://velocity.apache.org");
        assertNotNull(link.toString());
        assertEquals("velocity.apache.org", link.getHost());
    }

    public @Test void methodToURI_Object() throws Exception
    {
        LinkTool link = newInstance();
        URI uri = new URI("http://apache.org");
        assertSame(uri, link.toURI(uri));
        assertNull(link.toURI("*%&$^%#$*&^!"));
        assertNotNull(link.toURI("http://velocity.apache.org"));
        assertNotNull(link.toURI(new Object() {
            public String toString() {
                return "http://google.com";
            }
        }));
    }

    public @Test void methodCreateURI() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.createURI());
        link.setFromURI("http://velocity.apache.org");
        assertNotNull(link.createURI());
        link.setPort("foo");
        assertNull(link.createURI());
        link.setPort(null);
        assertTrue(link.setFromURI("mailto:nbubna@apache.org"));
        assertTrue(link.isOpaque());
        assertNotNull(link.createURI());
        assertTrue(link.createURI().isOpaque());
        assertEquals(link.createURI(), link.createURI());
        assertFalse(link.createURI() == link.createURI());
    }

    public @Test void methodUri_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.uri(null).toString());
        assertEquals("http://apache.org?a=b#c", link.uri("http://apache.org?a=b#c").toString());
        link.setFromURI("https://nbubna@people.apache.org");
        assertEquals("people.apache.org", link.getHost());
        assertEquals("https://nbubna@people.apache.org", link.uri(link.createURI()).toString());
        URI uri = new URI("mailto:nbubna@apache.org");
        assertEquals("mailto:nbubna@apache.org", link.uri(uri).toString());
    }

    public @Test void methodSetAppendParams_boolean() throws Exception
    {
        LinkTool link = newInstance("/bar?a=b");
        LinkTool result = link.param("a","c");
        assertEquals("a=b&amp;a=c", result.getQuery());
        link.setAppendParams(false);
        result = link.param("a", "d");
        assertEquals("a=d", result.getQuery());
    }

    public @Test void methodSetQuery_Object() throws Exception
    {
        LinkTool link = newInstance("/bar?a=b");
        assertNotNull(link.getQuery());
        assertEquals("a=b", link.getQuery());
        link.setQuery("c=d&e=f");
        assertEquals("c=d&amp;e=f", link.getQuery());
        link.setXHTML(false);
        link.setQuery("x=1&amp;y=2");
        assertEquals("x=1&y=2", link.getQuery());
        link.setQuery(null);
        assertEquals(null, link.getQuery());
    }

    public @Test void methodNormalizeQuery_String() throws Exception
    {
        LinkTool link = new LinkTool();
        assertEquals("a=b", link.normalizeQuery("a=b"));
        assertEquals("a=b&amp;b=c", link.normalizeQuery("a=b&b=c"));
        assertEquals("a=b&amp;b=c", link.normalizeQuery("a=b&amp;b=c"));
        assertEquals("a=b&amp;b=c&amp;t=f", link.normalizeQuery("a=b&amp;b=c&t=f"));
    }

    public @Test void methodGetQuery() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.getQuery());
        assertEquals("this=that", link.query("this=that").getQuery());
    }

    public @Test void methodQuery_Object() throws Exception
    {
        LinkTool link = newInstance("https://gmail.com");
        assertEquals("https://gmail.com", link.query(null).toString());
        assertEquals("https://gmail.com?v=2", link.query("v=2").toString());
        link = newInstance("http://go.com?foo=bar");
        assertEquals("foo=wog", link.query("foo=wog").getQuery());
        assertEquals("http://go.com", link.query(null).toString());
    }

    // this method also tests setXHTML
    public @Test void methodCombineQuery_StringString() throws Exception
    {
        LinkTool link = newInstance();
        String none = null;
        String empty = "";
        String test = "test=1";
        String test2 = "a=b";
        assertSame(none, link.combineQuery(none, none));
        assertSame(none, link.combineQuery(none, empty));
        assertSame(test, link.combineQuery(test, none));
        assertEquals("test=1", link.combineQuery(test, empty));
        assertEquals("test=1&amp;a=b", link.combineQuery(test, test2));
        link.setXHTML(false);
        assertEquals("a=b&test=1", link.combineQuery(test2, test));
    }

    public @Test void methodAppendQuery_Object() throws Exception
    {
        LinkTool link = newInstance("/foo?bar=woogie");
        link.appendQuery("x=1");
        assertEquals("bar=woogie&amp;x=1", link.getQuery());
        link.appendQuery("y=2");
        assertEquals("bar=woogie&amp;x=1&amp;y=2", link.getQuery());
        link.setQuery(null);
        assertEquals(null, link.getQuery());
        link.appendQuery("z=3");
        assertEquals("z=3", link.getQuery());
    }

    public @Test void methodToQuery_Map() throws Exception
    {
        LinkTool link = new LinkTool();
        Map test = new LinkedHashMap();
        test.put("a", "b");
        assertEquals("a=b", link.toQuery(test));
        test.put("b", "c");
        assertEquals("a=b&amp;b=c", link.toQuery(test));
        Boolean[] array = new Boolean[] { Boolean.TRUE, Boolean.FALSE };
        test.put("b", array);
        assertEquals("a=b&amp;b=true&amp;b=false", link.toQuery(test));
    }

    public @Test void methodToQuery_ObjectObject() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals("null=", link.toQuery(null, null));
        assertEquals("a+b=c", link.toQuery("a b", "c"));
        assertEquals("x=1", link.toQuery('x', 1));
        assertEquals("true=false", link.toQuery(true, false));
        assertEquals("path=%2Ffoo+bar%2Fnew", link.toQuery("path", "/foo bar/new"));
        // try all URI reserved chars
        assertEquals("x=%2C%3B%3A%24%26%2B%3D%3F%2F%5B%5D%40", link.toQuery('x', ",;:$&+=?/[]@"));
    }

    public @Test void methodSetParam_ObjectObjectboolean() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.query);
        link.setParam("a","b", true);
        assertNotNull(link.query);
        assertEquals("a=b", link.getQuery());
        link.setParam("a", "c", true);
        assertEquals("a=b&amp;a=c", link.getQuery());
        link.setParam("a", "foo", false);
        assertEquals("a=foo", link.getQuery());
    }

    public @Test void methodSetParams_Objectboolean() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.query);
        link.setParams("a=b", true);
        assertNotNull(link.query);
        assertEquals("a=b", link.getQuery());
        link.setParams("a=c&a=d", true);
        assertEquals("a=b&amp;a=c&amp;a=d", link.getQuery());
        Map test = new LinkedHashMap();
        test.put("a", "foo");
        link.setParams(test, false);
        assertEquals("a=foo", link.getQuery());
    }

    public @Test void methodParams_Object() throws Exception
    {
        LinkTool link = newInstance();
        Map test = new LinkedHashMap();
        test.put("a", "foo");
        assertEquals("a=foo", link.params(test).getQuery());
        assertEquals("a=b&amp;b=c", link.params("a=b&b=c").getQuery());
        link = newInstance("/foo?q=a&a=q");
        assertEquals("/foo", link.params(false).toString());
        assertEquals("/foo?q=a&amp;a=q", link.params(true).toString());
    }

    public @Test void methodParam_ObjectObject() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals("null=", link.param(null, null).getQuery());
        assertEquals("x=1", link.param("x",1).getQuery());
        assertEquals("x=1&amp;y=2", link.param("x",1).param("y",2).getQuery());
        link = newInstance("/hee/haa.vm?a=b");
        assertEquals("/hee/haa.vm?a=b&amp;b=true", link.param('b', true).toString());
    }

    public @Test void methodAppend_ObjectObject() throws Exception
    {
        LinkTool link = newInstance();
        link.setAppendParams(false); //using append(key,val) should override
        assertEquals("x=1", link.append("x",1).getQuery());
        assertEquals("x=1&amp;x=2", link.append("x",1).append("x",2).getQuery());
    }

    public @Test void methodSet_ObjectObject() throws Exception
    {
        LinkTool link = newInstance();
        link.setAppendParams(true); //using set(key,val) should override
        assertEquals("x=1", link.set("x",1).getQuery());
        assertEquals("x=2", link.set("x",1).set("x",2).getQuery());
    }

    public @Test void methodRemove_Object() throws Exception
    {
        LinkTool link = newInstance("/foo?q=bar");
        assertEquals("q=bar", link.getQuery());
        assertEquals("", link.remove("q").getQuery());
        assertEquals("q=bar", link.set("x",1).remove("x").getQuery());
    }

    public @Test void methodRemoveParam_Object() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.removeParam("a"));
        link.setParam("a","b",true);
        assertEquals("a=b", link.getQuery());
        assertEquals("b", link.removeParam("a"));
        assertNull(link.removeParam("a"));
    }

    public @Test void methodHandleParamsBoolean_boolean() throws Exception
    {
        LinkTool link = newInstance();
        assertNull(link.query);
        link.setParam("a","b",true);
        Map q = link.query;
        link.handleParamsBoolean(true);
        assertSame(q, link.query);
        assertEquals("a=b", link.getQuery());
        link.handleParamsBoolean(false);
        assertNull(link.query);
    }

    public @Test void methodParams_Map() throws Exception
    {
        LinkTool link = newInstance("http://go.com");
        Map params = new LinkedHashMap();
        params.put("this", "that");
        params.put('x', 1);
        params.put(true, false);
        assertEquals("http://go.com?this=that&amp;x=1&amp;true=false", link.params(params).toString());
        assertEquals("http://go.com", link.params(null).toString());
        assertEquals("http://go.com", link.params(new HashMap()).toString());
    }

    public @Test void methodParseQuery_String() throws Exception
    {
        LinkTool link = newInstance();
        Map result = link.parseQuery("a=b&x=1");
        assertEquals("b", result.get("a"));
        assertEquals("1", result.get("x"));
        link.setXHTML(false);
        result = link.parseQuery("true=false&amp;false=true&black=white");
        assertEquals("false", result.get("true"));
        assertEquals("true", result.get("false"));
        assertEquals("white", result.get("black"));
    }

    public @Test void methodGetParams() throws Exception
    {
        LinkTool link = newInstance("/foo?a=b&amp;x=true");
        Map result = link.getParams();
        assertEquals("b", result.get("a"));
        assertEquals("true", result.get("x"));
        Map newresult = link.param('y',false).getParams();
        assertFalse(result.equals(newresult));
        assertEquals("b", newresult.get("a"));
        assertEquals(Boolean.FALSE, newresult.get("y"));
    }

    public @Test void methodSetFragment_Object() throws Exception
    {
        LinkTool link = newInstance();
        link.setFragment("foo");
        assertEquals("#foo", link.toString());
        link.setFragment(null);
        assertEquals(null, link.toString());
        link = newInstance("/foo#bar");
        link.setFragment("woo gie");
        assertEquals("/foo#woo%20gie", link.toString());
    }

    public @Test void methodGetAnchor() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.getAnchor());
        link.setFragment("foo");
        assertEquals("foo", link.getAnchor());
        link = newInstance("http://go.com#espn");
        assertEquals("espn", link.getAnchor());
        link = newInstance(LinkTool.FRAGMENT_KEY, "foo");
        assertEquals("foo", link.getAnchor());
    }

    public @Test void methodAnchor_Object() throws Exception
    {
        LinkTool link = newInstance();
        // here are possible string values to test:
        String none = null;
        String empty = "";
        String space = "a b";
        String test = "test";
        assertEquals(null, link.anchor(none).getAnchor());
        assertEquals(null, link.anchor(empty).getAnchor());
        assertEquals(test, link.anchor(test).getAnchor());
        assertEquals("a b", link.anchor(space).getAnchor());
        link = newInstance("http://go.com#foo");
        assertEquals("http://go.com#true", link.anchor(true).toString());
        assertEquals("http://go.com#a%20b", link.anchor(space).toString());
    }

    public @Test void methodGetSelf() throws Exception
    {
        LinkTool link = newInstance();
        assertSame(link, link.getSelf());
        assertSame(link, link.uri("http://go.com").getSelf());
        assertSame(link, link.path("foo").param(1,true).anchor('a').getSelf());
    }

    public @Test void methodToString() throws Exception
    {
        LinkTool link = newInstance();
        assertEquals(null, link.toString());
        assertEquals(null, link.secure().toString());
        assertEquals("http://go.com", link.host("go.com").toString());
        assertEquals(null, link.port(42).toString());
        assertEquals("/foo", link.path("foo").toString());
        assertEquals("?a=1", link.param('a',1).toString());
        assertEquals("#42", link.anchor(42).toString());
    }

    public @Test void methodNoDoubleEncode() throws Exception
    {
        LinkTool link = newInstance().relative("/foo");
        assertEquals("/foo", link.toString());
        link = link.param("q","a:b c");
        assertEquals("/foo?q=a%3Ab+c", link.toString());
        link = link.anchor("a(b, c)");
        assertEquals("/foo?q=a%3Ab+c#a(b,%20c)", link.toString());
        link = link.param("evil","%25%24%").anchor(null);
        assertEquals("/foo?q=a%3Ab+c&amp;evil=%2525%2524%25", link.toString());
    }

}
        
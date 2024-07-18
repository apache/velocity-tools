package org.apache.velocity.tools.view;

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

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.generic.SafeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;

/**
 * <p>View tool for convenient cookie access and creation.</p>
 * <p>Template example(s):</p>
 * <pre>
 *  $cookie.foo
 *  $cookie.add("bar",'woogie')
 * </pre>
 * <p>tools.xml configuration:</p>
 * <pre>
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.CookieTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>This class is only designed for use as a request-scope tool.</p>
 *
 * @author <a href="mailto:dim@colebatch.com">Dmitri Colebatch</a>
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
@DefaultKey("cookies")
@ValidScope(Scope.REQUEST)
public class CookieTool extends SafeConfig
{
    protected static Logger log = LoggerFactory.getLogger(CookieTool.class);

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    private List<Cookie> jar;

    // --------------------------------------- Setup Methods -------------

    /**
     * Sets the current {@link HttpServletRequest}. This is required
     * for this tool to operate and will throw a NullPointerException
     * if this is not set or is set to {@code null}.
     * @param request servlet request
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request == null)
        {
            throw new NullPointerException("request should not be null");
        }
        this.request = request;
    }

    /**
     * Sets the current {@link HttpServletResponse}. This is required
     * for this tool to operate and will throw a NullPointerException
     * if this is not set or is set to {@code null}.
     * @param response servlet response
     */
    public void setResponse(HttpServletResponse response)
    {
        if (response == null)
        {
            throw new NullPointerException("response should not be null");
        }
        this.response = response;
    }

    // --------------------------------------- Setup Methods -------------

    /**
     * Expose array of Cookies for this request to the template.
     *
     * <p>This is equivalent to <code>$request.cookies</code>.</p>
     *
     * @return list of Cookie objects for this request
     */
    public List<Cookie> getAll()
    {
        if (jar == null) {
            Cookie[] array = request.getCookies();
            if (array == null)
            {
                return null;
            }

            jar = new ArrayList<Cookie>(array.length);
            for (Cookie c : array)
            {
                Cookie sc = new SugarCookie(c);
                jar.add(sc);
            }
        }
        return jar;
    }

    /**
     * Returns the Cookie with the specified name, if it exists.
     *
     * <p>So, if you had a cookie named 'foo', you'd get it's value
     * by <code>$cookies.foo.value</code> or it's max age
     * by <code>$cookies.foo.maxAge</code></p>
     * @param name cookie name
     * @return found cookie or null
     */
    public Cookie get(String name)
    {
        List<Cookie> all = getAll();
        if (all != null)
        {
            for (Cookie c : all)
            {
                if (c.getName().equals(name))
                {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Adds the specified Cookie to the HttpServletResponse.
     * This does *not* add the Cookie to the current request.
     *
     * @param c is for cookie (that's good enough for me)
     * @since VelocityTools 2.0
     * @return an empty String to prevent the reference from rendering
     *         unless the cookie passed in is null, then it returns null
     */
    public String add(Cookie c)
    {
        if (c == null)
        {
            return null;
        }
        response.addCookie(c);
        return "";
    }

    /**
     * Adds a new Cookie with the specified name and value
     * to the HttpServletResponse.  This does *not* add a Cookie
     * to the current request.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @return an empty String to prevent the reference from rendering
     */
    public String add(String name, String value)
    {
        return add(create(name, value));
    }

    /**
     * Convenience method to add a new Cookie to the response
     * and set an expiry time for it.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @param maxAge the expiry to be set for this cookie
     * @return an empty String to prevent the reference from rendering
     */
    public String add(String name, String value, Object maxAge)
    {
        return add(create(name, value, maxAge));
    }

    /**
     * Creates a new Cookie with the specified name and value.
     * This does *not* add the Cookie to the response, so the
     * created Cookie will not be set unless you do
     * <code>$cookies.add($myCookie)</code>.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @return The new SugarCookie object.
     * @since VelocityTools 1.3
     */
    public Cookie create(String name, String value)
    {
        try
        {
            return new SugarCookie(name, value);
        }
        catch (IllegalArgumentException iae)
        {
            getLog().error("Could not create cookie with name \"{}\"", name, iae);
            return null;
        }
    }

    /**
     * Convenience method to create a new Cookie
     * and set an expiry time for it.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @param maxAge the expiry to be set for this cookie
     * @return The new SugarCookie object.
     * @since VelocityTools 1.3
     */
    public Cookie create(String name, String value, Object maxAge)
    {
        SugarCookie sc = (SugarCookie)create(name, value);
        if (sc == null)
        {
            return null;
        }
        return sc.maxAge(maxAge);
    }

    /**
     * Retrieves the specified cookie and sets the Max-Age to 0
     * to tell the browser to delete the cookie.  Then this returns
     * an empty string to make this render silently. If no such cookie
     * exists, then it returns null to show the error.
     *
     * @param name the name of the cookie to be eaten
     * @return empty string, or null if no such cookie exists
     * @see Cookie#setMaxAge
     * @see #add(Cookie)
     * @see #get(String)
     */
    public String delete(String name)
    {
        Cookie c = get(name);
        if (c == null)
        {
            return null;
        }
        c.setMaxAge(0);
        return add(c);
    }

    @Override
    public String toString()
    {
        List<Cookie> all = getAll();
        if (all == null)
        {
            return super.toString();
        }
        StringBuilder out = new StringBuilder();
        out.append('[');
        for (int i=0; i < all.size(); i++)
        {
            if (i != 0)
            {
                out.append(", ");
            }
            Cookie c = all.get(i);
            out.append(c.getName());
            out.append('=');
            out.append(c.getValue());
        }
        out.append(']');
        return out.toString();
    }


    /**
     * Extends {@link Cookie} to add some fluid API sugar and
     * a toString() method that renders the Cookie's value
     * instead of the usual Object.toString() shenanigans.
     */
    public static class SugarCookie extends Cookie
    {
        private Cookie plain;

        /* c is for cookie. that's good enough for me. */
        public SugarCookie(Cookie c)
        {
            this(c.getName(), c.getValue());
            setMaxAge(c.getMaxAge());
            setComment(c.getComment());
            setPath(c.getPath());
            setVersion(c.getVersion());
            setSecure(c.getSecure());
            // avoid setDomain NPE
            if (c.getDomain() != null)
            {
                setDomain(c.getDomain());
            }
            this.plain = c;
        }

        public SugarCookie(String name, String value)
        {
            super(name, value);
        }

        public SugarCookie value(Object obj)
        {
            String value = ConversionUtils.toString(obj);
            setValue(value);
            if (plain != null)
            {
                plain.setValue(value);
            }
            return this;
        }

        public SugarCookie maxAge(Object obj)
        {
            Number maxAge = ConversionUtils.toNumber(obj);
            if (maxAge == null)
            {
                return null;
            }
            setMaxAge(maxAge.intValue());
            if (plain != null)
            {
                plain.setMaxAge(maxAge.intValue());
            }
            return this;
        }

        public SugarCookie comment(Object obj)
        {
            String comment = ConversionUtils.toString(obj);
            setComment(comment);
            if (plain != null)
            {
                plain.setComment(comment);
            }
            return this;
        }

        public SugarCookie domain(Object obj)
        {
            String domain = ConversionUtils.toString(obj);
            if (domain == null)
            {
                return null;
            }
            setDomain(domain);
            if (plain != null)
            {
                plain.setDomain(domain);
            }
            return this;
        }

        public SugarCookie path(Object obj)
        {
            String path = ConversionUtils.toString(obj);
            setPath(path);
            if (plain != null)
            {
                plain.setPath(path);
            }
            return this;
        }

        public SugarCookie version(Object obj)
        {
            Number version = ConversionUtils.toNumber(obj);
            if (version == null)
            {
                return null;
            }
            setVersion(version.intValue());
            if (plain != null)
            {
                plain.setVersion(version.intValue());
            }
            return this;
        }

        public SugarCookie secure(Object obj)
        {
            Boolean secure = ConversionUtils.toBoolean(obj);
            if (secure == null)
            {
                return null;
            }
            setSecure(secure.booleanValue());
            if (plain != null)
            {
                plain.setSecure(secure.booleanValue());
            }
            return this;
        }

        public Cookie getPlain()
        {
            return plain;
        }

        @Override
        public String toString()
        {
            return getValue();
        }
    }

}

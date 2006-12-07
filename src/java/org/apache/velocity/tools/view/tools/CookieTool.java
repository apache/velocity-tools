package org.apache.velocity.tools.view.tools;

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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.context.ViewContext;

/**
 * <p>View tool for convenient cookie access and creation.</p>
 * <p><pre>
 * Template example(s):
 *  $cookie.foo.value
 *  $cookie.add("bar",'woogie')
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;cookie&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.view.tools.CookieTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This class is only designed for use as a request-scope tool.</p>
 *
 * @author <a href="mailto:dim@colebatch.com">Dmitri Colebatch</a>
 * @author Nathan Bubna
 * @since VelocityTools 1.1
 * @version $Id$
 */
public class CookieTool
{

    protected HttpServletRequest request;
    protected HttpServletResponse response;


    /**
     * Initializes this instance for the current request.
     *
     * @param obj the ViewContext of the current request
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        this.request = context.getRequest();
        this.response = context.getResponse();
    }


    /**
     * Expose array of Cookies for this request to the template.
     *
     * <p>This is equivalent to <code>$request.cookies</code>.</p>
     *
     * @return array of Cookie objects for this request
     */
    public Cookie[] getAll()
    {
        return request.getCookies();
    }


    /**
     * Returns the Cookie with the specified name, if it exists.
     *
     * <p>So, if you had a cookie named 'foo', you'd get it's value
     * by <code>$cookies.foo.value</code> or it's max age
     * by <code>$cookies.foo.maxAge</code></p>
     */
    public Cookie get(String name)
    {
        Cookie[] all = getAll();
        if (all == null)
        {
            return null;
        }

        for (int i = 0; i < all.length; i++)
        {
            Cookie cookie = all[i];
            if (cookie.getName().equals(name))
            {
                return cookie;
            }
        }
        return null;
    }


    /**
     * Adds a new Cookie with the specified name and value
     * to the HttpServletResponse.  This does *not* add a Cookie
     * to the current request.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     */
    public void add(String name, String value)
    {
        response.addCookie(create(name, value));
    }


    /**
     * Convenience method to add a new Cookie to the response
     * and set an expiry time for it.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @param maxAge the expiry to be set for this cookie
     */
    public void add(String name, String value, Object maxAge)
    {
        Cookie c = create(name, value, maxAge);
        if (c == null)
        {
            /* TODO: something better? */
            return;
        }
        response.addCookie(c);
    }


    /**
     * Creates a new Cookie with the specified name and value.
     * This does *not* add the Cookie to the response, so the
     * created Cookie will not be set unless you do
     * <code>$response.addCookie($myCookie)</code>.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @returns the new Cookie object
     * @since VelocityTools 1.3
     */
    public Cookie create(String name, String value)
    {
        return new Cookie(name, value);
    }


    /**
     * Convenience method to create a new Cookie
     * and set an expiry time for it.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @param maxAge the expiry to be set for this cookie
     * @returns the new Cookie object
     * @since VelocityTools 1.3
     */
    public Cookie create(String name, String value, Object maxAge)
    {
        int expiry;
        if (maxAge instanceof Number)
        {
            expiry = ((Number)maxAge).intValue();
        }
        else
        {
            try
            {
                expiry = Integer.parseInt(String.valueOf(maxAge));
            }
            catch (NumberFormatException nfe)
            {
                return null;
            }
        }
        
        /* c is for cookie.  that's good enough for me. */
        Cookie c = new Cookie(name, value);
        c.setMaxAge(expiry);
        return c;
    }
}

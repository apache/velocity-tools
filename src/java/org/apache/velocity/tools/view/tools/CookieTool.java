/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.view.tools;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.velocity.tools.view.context.ViewContext;

/**
 * <p>View tool for convenient cookie access and creation.</p>
 *
 * <p>This class is only designed for use as a request-scope tool.</p>
 *
 * @author <a href="mailto:dim@colebatch.com">Dmitri Colebatch</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: CookieTool.java,v 1.1 2003/07/22 21:05:34 nbubna Exp $
 */
public class CookieTool implements ViewTool
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
        response.addCookie(new Cookie(name, value));
    }


    /**
     * Convenience method to add a new Cookie to the response
     * and set an expiry time for it.
     *
     * @param name the name to give this cookie
     * @param value the value to be set for this cookie
     * @param maxAge the expiry to be set for this cookie
     */
    public void add(String name, String value, int maxAge)
    {
        /* c is for cookie.  that's good enough for me. */
        Cookie c = new Cookie(name, value);
        c.setMaxAge(maxAge);
        response.addCookie(c);
    }

}

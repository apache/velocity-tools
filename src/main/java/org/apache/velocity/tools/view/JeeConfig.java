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

import java.util.Enumeration;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * <p>Because sometimes you don't care about the difference between a
 * servlet and a filter.  Why isn't there a common interface for
 * {@link FilterConfig} and {@link ServletConfig} already? Sheesh.</p>
 * <p>Anyway, this also adds the ability to fake everything if you 
 * don't have either a FilterConfig or a ServletConfig handy. Just
 * subclass it and override the methods that return things you care
 * about.  Oh, and if you don't have any init-params at all, just use
 * the constructor that just takes a {@link ServletContext} as that's
 * the only really essential thing for creating a {@link VelocityView}.
 *
 * @version $Id: ServletUtils.java 471244 2006-11-04 18:34:38Z henning $
 */
public class JeeConfig
{
    protected FilterConfig filter;
    protected ServletConfig servlet;
    protected ServletContext context;

    /**
     * If your subclass uses this, you better make sure
     * that {@link #getServletContext()} doesn't return null!
     */
    protected JeeConfig(){}

    public JeeConfig(FilterConfig filter)
    {
        if (filter == null)
        {
            throw new NullPointerException("FilterConfig should not be null; there must be a way to get a ServletContext");
        }
        this.filter = filter;
    }

    public JeeConfig(ServletConfig servlet)
    {
        if (servlet == null)
        {
            throw new NullPointerException("ServletConfig should not be null; there must be a way to get a ServletContext");
        }
        this.servlet = servlet;
    }

    public JeeConfig(ServletContext context)
    {
        if (filter == null)
        {
            throw new NullPointerException("ServletContext must not be null");
        }
        this.context = context;
    }

    public String getInitParameter(String name)
    {
        if (filter != null)
        {
            return filter.getInitParameter(name);
        }
        if (servlet != null)
        {
            return servlet.getInitParameter(name);
        }
        return null;
    }

    public Enumeration getInitParameterNames()
    {
        if (filter != null)
        {
            return filter.getInitParameterNames();
        }
        if (servlet != null)
        {
            return servlet.getInitParameterNames();
        }
        return null;
    }

    public String getName()
    {
        if (filter != null)
        {
            return filter.getFilterName();
        }
        if (servlet != null)
        {
            return servlet.getServletName();
        }
        return null;
    }

    public ServletContext getServletContext()
    {
        if (filter != null)
        {
            return filter.getServletContext();
        }
        if (servlet != null)
        {
            return servlet.getServletContext();
        }
        return context;
    }

}

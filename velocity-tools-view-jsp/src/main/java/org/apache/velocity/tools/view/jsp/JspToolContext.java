package org.apache.velocity.tools.view.jsp;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.ViewToolContext;

/**
 * <p>Velocity context implementation specific to the JSP environment.</p>
 *
 * @author Nathan Bubna
 * @version $Id: ViewContext.java 514727 2007-03-05 16:49:03Z nbubna $
 */
public class JspToolContext extends ViewToolContext
{
    public static final String PAGE_CONTEXT_KEY = "pageContext";

    private final PageContext pageContext;

    public JspToolContext(VelocityEngine velocity,
                          PageContext pageContext)
    {
        super(velocity,
              (HttpServletRequest)pageContext.getRequest(),
              (HttpServletResponse)pageContext.getResponse(),
              pageContext.getServletContext());

        this.pageContext = pageContext;
    }

    protected void putToolProperties()
    {
        putToolProperty(PAGE_CONTEXT_KEY, getPageContext());

        super.putToolProperties();
    }

    public PageContext getPageContext()
    {
        return this.pageContext;
    }

    protected Object getServletApi(String key)
    {
        if (key.equals(PAGE_CONTEXT_KEY))
        {
            return getPageContext();
        }
        return super.getServletApi(key);
    }

    public Object getAttribute(String key)
    {
        Object o = getPageContext().getAttribute(key);
        if (o == null)
        {
            o = super.getAttribute(key);
        }
        return o;
    }
}

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
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Implements {@link JeeConfig} with a {@link ServletConfig}.
 *
 * @version $Id$
 * @since 2.0
 */
public class JeeServletConfig implements JeeConfig
{
    protected ServletConfig servlet;

    public JeeServletConfig(ServletConfig servlet)
    {
        if (servlet == null)
        {
            throw new NullPointerException("ServletConfig should not be null; there must be a way to get a ServletContext");
        }
        this.servlet = servlet;
    }

    /** {@inheritDoc} */
    public String getInitParameter(String name)
    {
        return servlet.getInitParameter(name);
    }

    /** {@inheritDoc} */
    public String findInitParameter(String key)
    {
        String param = getInitParameter(key);
        if (param == null)
        {
            param = getServletContext().getInitParameter(key);
        }
        return param;
    }

    /** {@inheritDoc} */
    public Enumeration getInitParameterNames()
    {
        return servlet.getInitParameterNames();
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return servlet.getServletName();
    }

    /** {@inheritDoc} */
    public ServletContext getServletContext()
    {
        return servlet.getServletContext();
    }

}

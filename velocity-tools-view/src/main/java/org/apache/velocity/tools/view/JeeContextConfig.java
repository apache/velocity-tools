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
import javax.servlet.ServletContext;

/**
 * Implements {@link JeeConfig} with only a {@link ServletContext}.
 *
 * @version $Id$
 * @since 2.0
 */
public class JeeContextConfig implements JeeConfig
{
    protected ServletContext context;

    public JeeContextConfig(ServletContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("ServletContext should not be null; there must be a way to get a ServletContext");
        }
        this.context = context;
    }

    /** {@inheritDoc} */
    public String getInitParameter(String name)
    {
        return null;
    }

    /**
     * Only checks the {@link ServletContext}'s init-params
     * for the specified parameter.
     */
    public String findInitParameter(String key)
    {
        return context.getInitParameter(key);
    }

    /** {@inheritDoc} */
    public Enumeration getInitParameterNames()
    {
        return null;
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return null;
    }

    /** {@inheritDoc} */
    public ServletContext getServletContext()
    {
        return context;
    }

}

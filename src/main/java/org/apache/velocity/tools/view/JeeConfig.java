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
 * <p>Because sometimes you don't care about the difference between a
 * servlet and a filter.  Why isn't there a common interface for
 * {@link FilterConfig} and {@link ServletConfig} already? Sheesh.</p>
 * </p>
 * <p>
 * Anyway, this also adds the ability to fake everything if you don't have
 * either a FilterConfig or a ServletConfig handy. Just implement it and
 * override the methods that return things you care about. Oh, and if you don't
 * have any init-params at all, just use {@link JeeContextConfig} as
 * {@link ServletContext} is the only really essential thing for creating a
 * {@link VelocityView}.</p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface JeeConfig
{
    /**
     * Returns an initialization parameter.
     *
     * @param name The name of the initialization parameter.
     * @return The value of the parameter.
     */
    String getInitParameter(String name);

    /**
     * Looks for the specified init-param in the servlet/filter config
     * (i.e. calls {@link #getInitParameter}). If no such init-param is
     * found there, it checks the {@link ServletContext}'s init-params
     * for the specified parameter.
     *
     * @param key The name of the initialization parameter.
     * @return The value of the initialization parameter.
     */
    String findInitParameter(String key);

    /**
     * Returns all the parameter names.
     *
     * @return The enumeration containing the parameter names.
     */
    @SuppressWarnings("unchecked")
    Enumeration getInitParameterNames();

    /**
     * Returns the name of the servlet (or filter) being used.
     *
     * @return The name of the configuration.
     */
    String getName();

    /**
     * Returns the servlet context.
     *
     * @return The servlet context.
     */
    ServletContext getServletContext();

}

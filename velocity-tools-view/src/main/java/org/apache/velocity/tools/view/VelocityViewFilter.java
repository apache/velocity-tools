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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.view.VelocityView;
import org.apache.velocity.tools.view.ViewToolContext;

/**
 * <p>A filter to ensure VelocityTools {@link Toolbox}es are
 * available in the request/session/application scopes. This
 * can simplify the process of integration with other frameworks.</p>
 *
 * <p>VelocityViewFilter supports the following configuration parameters
 * in web.xml:</p>
 * <dl>
 *   <dt>org.apache.velocity.tools</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, the servlet will check for a toolbox file at
 *     '/WEB-INF/tools.xml'.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity will check for a properties file at
 *     '/WEB-INF/velocity.properties'.  If no file is found there, then
 *     Velocity is initialized with the settings in the classpath at
 *     'org.apache.velocity.tools.view.velocity.properties'.</dd>
 *   <dt>org.apache.velocity.tools.loadDefaults</dt>
 *   <dd>By default, this is {@code true}. If set to {@code false}, then
 *     the default toolbox configuration will not be added to your (if any)
 *     custom configuration.  NOTE: The default configuration will also be
 *     suppressed if you are using a deprecated toolbox.xml format and do not
 *     explicitly set this to {@code true}.</dd>
 *   <dt>org.apache.velocity.tools.cleanConfiguration</dt>
 *   <dd>By default, this is {@code false}. If set to {@code true}, then
 *     then the final toolbox configuration (the combination of any custom
 *     one(s) provided by yourself and/or the default configuration(s))
 *     will have all invalid tools, properties, and/or data removed prior to
 *     configuring the ToolboxFactory for this servlet by a
 *     {@link org.apache.velocity.tools.config.ConfigurationCleaner}</dd>
 *   <dt>org.apache.velocity.tools.shared.config</dt>
 *   <dd>By default, this is {@code true}. If set to {@code false}, then
 *     the {@link VelocityView} used by this filter will not be shared
 *     with other VelocityViewFilters, {@link VelocityViewServlet}s or 
 *     {@link org.apache.velocity.tools.view.jsp.VelocityViewTag}s in the
 *     application.</dd>
 *   <dt>org.apache.velocity.tools.context.key</dt>
 *   <dd>If you set a value for this property, this filter will create
 *     and prepare a {@link ViewToolContext} for each request, and then
 *     place it into the request attributes under the key you set. This
 *     is primarily for those who have this filter NOT share a config
 *     (i.e. non-shared VelocityView) and thus will find it easier to
 *     retrieve a working context from the request attributes than it
 *     would be to get the VelocityView for this filter and have it
 *     create the context for them.  Most users will have no trouble
 *     getting a shared VelocityView and creating the context themselves.</dd>
 * </dl>
 *
 * @version $Id: VelocityViewFilter.java 611011 2008-01-11 01:32:59Z nbubna $
 */
public class VelocityViewFilter implements Filter
{
    public static final String CONTEXT_KEY =
        "org.apache.velocity.tools.context.key";

    private VelocityView view;
    private FilterConfig config;
    private String contextKey = null;

    /**
     * <p>Initializes VelocityView used to process requests.
     * Called by the servlet container on loading.</p>
     *
     * @param config filter configuation
     */
    public void init(FilterConfig config) throws ServletException
    {
        this.config = config;

        // init the VelocityView (if it hasn't been already)
        getVelocityView();

        // look for a context key
        contextKey = findInitParameter(CONTEXT_KEY);
    }

    protected FilterConfig getFilterConfig()
    {
        return this.config;
    }

    protected VelocityView getVelocityView()
    {
        if (this.view == null)
        {
            this.view = ServletUtils.getVelocityView(getFilterConfig());
            assert (view != null);
        }
        return this.view;
    }

    protected String getContextKey()
    {
        return this.contextKey;
    }

    /**
     * Looks up an init parameter with the specified key in either the
     * FilterConfig or, failing that, in the ServletContext.
     */
    protected String findInitParameter(String key)
    {
        FilterConfig conf = getFilterConfig();
        String param = conf.getInitParameter(key);
        if (param == null || param.length() == 0)
        {
            param = conf.getServletContext().getInitParameter(key);
        }
        return param;
    }

    /**
     * Simply prepares the request (and/or session) toolbox(es)
     * for other filters, servlets or whatnot to use.  If a context key
     * has been provided in the init-params of the filter or webapp,
     * then this will also create a {@link ViewToolContext} and put
     * it in the request attributes under that key.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws java.io.IOException, ServletException
    {
        // can/should we create the context for the request?
        if (contextKey != null && request instanceof HttpServletRequest)
        {
            Context context = createContext((HttpServletRequest)request,
                                            (HttpServletResponse)response);
            request.setAttribute(contextKey, context);
        }
        else
        {
            // just publish the toolboxes
            getVelocityView().publishToolboxes(request);
        }

        // move down the chain
        chain.doFilter(request, response);
    }

    protected Context createContext(HttpServletRequest request,
                                    HttpServletResponse response)
    {
        return getVelocityView().createContext(request, response);
    }

    public void destroy()
    {
        // do anything else?
        this.view = null;
        this.config = null;
        this.contextKey = null;
    }

}

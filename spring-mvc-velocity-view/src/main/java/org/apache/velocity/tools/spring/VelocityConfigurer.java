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
package org.apache.velocity.tools.spring;

import java.io.IOException;

import jakarta.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.spring.VelocityEngineFactory;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.view.ViewToolManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.web.context.ServletContextAware;

/**
 * Spring bean that builds and holds the {@link VelocityEngine} and the Velocity Tools
 * {@link ViewToolManager} for the MVC views to share.
 *
 * <p>Extends the engine-side {@link VelocityEngineFactory} (so all its engine-configuration
 * setters — {@code velocityProperties}, {@code resourceLoaderPath}, … — are available as bean
 * properties), and adds the toolbox on top. A pre-built engine may be injected instead via
 * {@link #setVelocityEngine}.</p>
 */
public class VelocityConfigurer extends VelocityEngineFactory
        implements VelocityConfig, InitializingBean, ResourceLoaderAware, ServletContextAware
{
    private VelocityEngine velocityEngine;
    private ViewToolManager toolManager;
    private ServletContext servletContext;
    private String toolboxConfigLocation;
    private boolean autoConfigure = false;
    private boolean includeDefaultToolbox = true;
    private boolean createSession = false;

    /** Inject a pre-configured engine instead of having this factory build one. */
    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    /** Path to an extra tools configuration (e.g. {@code tools.xml}), resolved by Velocity Tools. */
    public void setToolboxConfigLocation(String toolboxConfigLocation)
    {
        this.toolboxConfigLocation = toolboxConfigLocation;
    }

    /**
     * Whether to pick up an external tools configuration from the {@code org.apache.velocity.tools}
     * system property or one injected into the servlet-context attributes (default {@code false}).
     * Spring apps normally configure via beans ({@link #setToolboxConfigLocation}) instead.
     */
    public void setAutoConfigure(boolean autoConfigure)
    {
        this.autoConfigure = autoConfigure;
    }

    /** Whether to load the bundled default tools (esc, date, link, etc.) (default {@code true}). */
    public void setIncludeDefaultToolbox(boolean includeDefaultToolbox)
    {
        this.includeDefaultToolbox = includeDefaultToolbox;
    }

    /**
     * Whether the views may create an {@code HttpSession} to hold session-scoped tools
     * (default {@code false}). Velocity Tools' own per-toolbox {@code createSession} attribute does
     * not reach the manager, so this is the effective control: set {@code true} only if you rely on
     * session-scoped tools persisting across requests; leave {@code false} to keep the views from
     * creating sessions (stateless-friendly).
     */
    public void setCreateSession(boolean createSession)
    {
        this.createSession = createSession;
    }

    @Override
    public void setServletContext(@NonNull ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    // Override only to carry ResourceLoaderAware's @NonNull contract — the inherited
    // VelocityEngineFactory.setResourceLoader (engine module) isn't annotated yet.
    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader)
    {
        super.setResourceLoader(resourceLoader);
    }

    @Override
    public void afterPropertiesSet() throws IOException, VelocityException
    {
        if (this.servletContext == null)
        {
            throw new IllegalStateException("VelocityConfigurer requires a ServletContext");
        }
        if (this.velocityEngine == null)
        {
            this.velocityEngine = createVelocityEngine();
        }
        // We drive configuration explicitly (no constructor auto-config, no constructor defaults).
        this.toolManager = new ViewToolManager(this.servletContext, false, false);
        this.toolManager.setVelocityEngine(this.velocityEngine);
        this.toolManager.setCreateSession(this.createSession);
        if (this.includeDefaultToolbox)
        {
            // autoConfigure() does NOT load the bundled defaults; do it explicitly.
            this.toolManager.configure(ConfigurationUtils.getDefaultTools());
        }
        if (this.autoConfigure)
        {
            // a config from the "org.apache.velocity.tools" system property or one injected
            // into the servlet context attributes.
            this.toolManager.autoConfigure(this.includeDefaultToolbox);
        }
        if (this.toolboxConfigLocation != null)
        {
            this.toolManager.configure(ConfigurationUtils.find(this.toolboxConfigLocation));
        }
    }

    @Override
    public VelocityEngine getVelocityEngine()
    {
        return this.velocityEngine;
    }

    @Override
    public ViewToolManager getToolManager()
    {
        return this.toolManager;
    }
}

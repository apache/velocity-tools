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

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.ToolboxFactory;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.tools.view.ViewToolContext;

/**
 * Manages tools for web applications. This simplifies the process
 * of getting a tool-populated Velocity context for merging with templates.
 * It allows for both direct configuration by passing in a {@link FactoryConfiguration}
 * or having one in the ServletContext attributes under
 * {@link ServletUtils#CONFIGURATION_KEY}, as well as configuration
 * via a tools.xml or tools.properties file in
 * either the classpath or the local file system.
 *
 * @author Nathan Bubna
 * @version $Id: ToolManager.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ViewToolManager extends ToolManager
{
    public static final String CREATE_SESSION_PROPERTY = "createSession";
    public static final String PUBLISH_TOOLBOXES_PROPERTY = "publishToolboxes";
    public static final String DEFAULT_TOOLBOX_KEY = Toolbox.KEY;

    protected ServletContext servletContext;
    private boolean createSession = true;
    private boolean publishToolboxes = true;
    private boolean appToolsPublished = false;
    private String toolboxKey = DEFAULT_TOOLBOX_KEY;

    /**
     * Constructs an instance already configured to use the 
     * {@link ConfigurationUtils#getAutoLoaded()()} configuration
     * and any configuration specified via a "org.apache.velocity.tools"
     * system property.
     */
    public ViewToolManager(ServletContext app)
    {
        this(app, true, true);
    }

    public ViewToolManager(ServletContext app, boolean includeDefaults)
    {
        this(app, true, includeDefaults);
    }

    public ViewToolManager(ServletContext app,
                           boolean autoConfig, boolean includeDefaults)
    {
        super(autoConfig, includeDefaults);

        if (app == null)
        {
            throw new NullPointerException("ServletContext is required");
        }
        this.servletContext = app;
    }

    @Override
    public void autoConfigure(boolean includeDefaults)
    {
        super.autoConfigure(includeDefaults);

        // check for a configuration in application attributes
        FactoryConfiguration injected = ServletUtils.getConfiguration(servletContext);
        if (injected != null)
        {
            configure(injected);
        }
    }

    /**
     * Sets whether or not the creation of a new {@link ViewToolContext}
     * should make the various scoped {@link Toolbox} instances available
     * publically via the HttpServletRequest/HttpSession/ServletContext
     * attributes or simply add the Toolbox instances directly to the
     * context. <b>It is important to note that if this is set to false,
     * session-scoped tools will NOT be stored in the session, but instead
     * be recreated for each request.</b>
     * @see #publishToolboxes
     * @see #setToolboxKey
     */
    public void setPublishToolboxes(boolean publish)
    {
        if (publish != this.publishToolboxes)
        {
            debug("Publish toolboxes setting was changed to %s", publish);
            this.publishToolboxes = publish;
        }
    }

    public boolean getPublishToolboxes()
    {
        return this.publishToolboxes;
    }

    /**
     * Sets a new attribute key to be used for publishing each {@link Toolbox}.
     * @see #setPublishToolboxes
     * @see #publishToolboxes
     */
    public void setToolboxKey(String key)
    {
        if (key == null)
        {
            throw new NullPointerException("toolboxKey cannot be null");
        }
        if (!key.equals(toolboxKey))
        {
            this.toolboxKey = key;
            unpublishApplicationTools();
            debug("Toolbox key was changed to %s", key);
        }
    }

    public String getToolboxKey()
    {
        return this.toolboxKey;
    }

    /**
     * Sets whether or not a new HttpSession should be created
     * when there are session scoped tools to be stored in the session,
     * but no session has been created yet.
     * @see #publishToolboxes
     */
    public void setCreateSession(boolean create)
    {
        if (create != this.createSession)
        {
            debug("Create session setting was changed to %s", create);
            this.createSession = create;
        }
    }

    public boolean getCreateSession()
    {
        return this.createSession;
    }

    /**
     * Checks the internal {@link ToolboxFactory} for any changes to
     * the createSession or publishToolboxes settings.
     */
    protected void updateGlobalProperties()
    {
        // check for a createSession setting
        Boolean create = 
            (Boolean)this.factory.getGlobalProperty(CREATE_SESSION_PROPERTY);
        if (create != null)
        {
            setCreateSession(create);
        }

        // check for a publishToolboxes setting
        Boolean publish = 
            (Boolean)this.factory.getGlobalProperty(PUBLISH_TOOLBOXES_PROPERTY);
        if (publish != null)
        {
            setPublishToolboxes(publish);
        }
    }

    /**
     * Removes any published {@link Scope#APPLICATION} Toolbox.
     */
    protected void unpublishApplicationTools()
    {
        if (appToolsPublished)
        {
            // clear the published application toolbox
            servletContext.removeAttribute(this.toolboxKey);
            appToolsPublished = false;
        }
    }

    @Override
    public void configure(FactoryConfiguration config)
    {
        super.configure(config);

        // reset things as best we can
        unpublishApplicationTools();
        updateGlobalProperties();
    }

    @Override
    protected FactoryConfiguration findConfig(String path)
    {
        return ServletUtils.getConfiguration(path, servletContext, false);
    }

    @Override
    protected void addToolboxes(ToolContext context)
    {
        super.addToolboxes(context);
        if (hasSessionTools())
        {
            context.addToolbox(getSessionToolbox());
        }
    }

    @Override
    public ToolContext createContext(Map<String,Object> toolProps)
    {
        ToolContext context = super.createContext(toolProps);
        context.putToolProperty(ViewContext.SERVLET_CONTEXT_KEY, servletContext);
        debug("Non-ViewToolContext was requested from ViewToolManager.");
        return context;
    }

    public ViewToolContext createContext(HttpServletRequest request,
                                         HttpServletResponse response)
    {
        ViewToolContext context =
            new ViewToolContext(getVelocityEngine(), request, response, servletContext);
        prepareContext(context, request);
        return context;
    }

    public void prepareContext(ViewToolContext context, HttpServletRequest request)
    {
        context.setToolboxKey(this.toolboxKey);
        if (this.publishToolboxes)
        {
            // put the toolboxes where the ViewToolContext
            // and others can find them
            publishToolboxes(request);
            
            // these would otherwise be done in super.prepareContext
            VelocityEngine engine = getVelocityEngine();
            if (engine != null)
            {
                context.putVelocityEngine(engine);
            }
            context.setUserCanOverwriteTools(getUserCanOverwriteTools());
        }
        else
        {
            // super class takes care of engine
            // and adds toolboxes directly
            prepareContext(context);
        }
    }

    protected boolean hasSessionTools()
    {
        return hasTools(Scope.SESSION);
    }

    protected Toolbox getSessionToolbox()
    {
        return createToolbox(Scope.SESSION);
    }

    /**
     * Places the {@link Scope#REQUEST} {@link Toolbox} (if any)
     * into the {@link ServletRequest} attributes using
     * {@link Toolbox#KEY} as the key.
     */
    public void publishToolboxes(ServletRequest request)
    {
        publishToolbox(request);
    }

    private void publishToolbox(ServletRequest request)
    {
        if (hasRequestTools() &&
            request.getAttribute(this.toolboxKey) == null)
        {
            request.setAttribute(this.toolboxKey, getRequestToolbox());
        }
    }

    /**
     * Places the {@link Scope#REQUEST} {@link Toolbox} (if any)
     * into the {@link HttpServletRequest} attributes using
     * {@link Toolbox#KEY} as the key, places the {@link Scope#SESSION}
     * Toolbox (if any) into the attributes of the {@link HttpSession} (if any)
     * then ensures that the {@link Scope#APPLICATION} Toolbox (if any)
     * has been placed in the {@link ServletContext} attributes.
     */
    public void publishToolboxes(HttpServletRequest request)
    {
        publishToolbox(request);

        if (hasSessionTools())
        {
            HttpSession session = request.getSession(this.createSession);
            if (session != null)
            {
                // allow only one thread per session
                synchronized(ServletUtils.getMutex(session, "session.mutex", this))
                {
                    if (session.getAttribute(this.toolboxKey) == null)
                    {
                        session.setAttribute(this.toolboxKey, getSessionToolbox());
                    }
                }
            }
        }
        if (!appToolsPublished && hasApplicationTools())
        {
            servletContext.setAttribute(this.toolboxKey, getApplicationToolbox());
        }
    }

}

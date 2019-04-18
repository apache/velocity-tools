package org.apache.velocity.tools;

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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages tools for non-web applications. This simplifies the process
 * of getting a tool-populated Velocity context for merging with templates.
 * It allows for both direct configuration by passing in a {@link FactoryConfiguration}
 * as well as configuration via a tools.xml or tools.properties file in
 * either the classpath or the local file system.
 *
 * @author Nathan Bubna
 * @version $Id: ToolManager.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolManager
{
    protected VelocityEngine velocity;
    protected ToolboxFactory factory;
    protected Logger log = null;
    private Toolbox application;
    private boolean userOverwrite = true;

    /**
     * Constructs an instance already configured to use the default tools and
     * any configuration specified via a "org.apache.velocity.tools"
     * system property.
     */
    public ToolManager()
    {
        this(true, true);
    }

    /**
     * Constructs an instance that may or not include default tools,
     * and already configured with
     * any configuration specified via a "org.apache.velocity.tools"
     * system property.
     * @param includeDefaults whether to include default tools
     */
    public ToolManager(boolean includeDefaults)
    {
        this(true, includeDefaults);
    }

    /**
     * Constructs an instance that may or not include default tools,
     * and which may or not be already configured with
     * any configuration specified via a "org.apache.velocity.tools"
     * system property.
     * @param autoConfig whether to use configuration file specified
     *        in the <code>org.apache.velocity.tools</code> system property
     * @param includeDefaults whether to include default tools
     */
    public ToolManager(boolean autoConfig, boolean includeDefaults)
    {
        this.factory = new ToolboxFactory();
        
        if (autoConfig)
        {
            autoConfigure(includeDefaults);
        }
    }

    /**
     * Autoconfiguration using the configuration file potentially found
     * in the <code>org.apache.velocity.tools</code> system property.
     * @param includeDefaults whether to include default tools
     */
    public void autoConfigure(boolean includeDefaults)
    {
        // look for any specified via system property
        FactoryConfiguration sys = ConfigurationUtils.findFromSystemProperty();
        if (sys != null)
        {
            configure(sys);
        }
    }

    /**
     * Configure the tool manager with this toolbox factory config
     * @param config toolbox factory config
     */
    public void configure(FactoryConfiguration config)
    {
        // clear the cached application toolbox
        this.application = null;
        this.factory.configure(config);
    }

    /**
     * Configure the tool manager with the provided configuration file
     * @param path path to configuration file
     */
    public void configure(String path)
    {
        FactoryConfiguration config = findConfig(path);
        if (config != null)
        {
            configure(config);
        }
        else
        {
            throw new RuntimeException("Could not find any configuration at "+path);
        }
    }

    /**
     * Find a configuration file
     * @param path path to a configuration file
     * @return toolbox factory configuration
     */
    protected FactoryConfiguration findConfig(String path)
    {
        return ConfigurationUtils.find(path);
    }

    /**
     * Returns the underlying {@link ToolboxFactory} being used.
     * @return underlying toolbox factory
     */
    public ToolboxFactory getToolboxFactory()
    {
        return this.factory;
    }

    /**
     * Sets the underlying ToolboxFactory being used.
     * <b>If you use this, be sure that your ToolboxFactory
     * is already properly configured.</b>
     * @param factory toolbox factory
     */
    public void setToolboxFactory(ToolboxFactory factory)
    {
        if (this.factory != factory)
        {
            if (factory == null)
            {
                throw new NullPointerException("ToolboxFactory cannot be null");
            }
            getLog().debug("ToolboxFactory instance was changed to {}", factory);
            this.factory = factory;
        }
    }

    /**
     * Sets the underlying VelocityEngine being used.
     * <b>If you use this, be sure that your VelocityEngine
     * is already properly configured and initialized.</b>
     * @param engine VelocityEngine instance
     */
    public void setVelocityEngine(VelocityEngine engine)
    {
        if (velocity != engine)
        {
            getLog().debug("VelocityEngine instance was changed to {}", engine);
            this.velocity = engine;
        }
    }

    /**
     * Get the underlying VelocityEngine being used.
     * @return VelocityEngine instance
     */
    public VelocityEngine getVelocityEngine()
    {
        return this.velocity;
    }

    /**
     * Set whether template user can overwrite tools keys
     * @param overwrite flag value
     */
    public void setUserCanOverwriteTools(boolean overwrite)
    {
        this.userOverwrite = overwrite;
    }

    /**
     * Get whether template user can overwrite tools keys
     * @return flag value
     */    
    public boolean getUserCanOverwriteTools()
    {
        return this.userOverwrite;
    }

    /**
     * Get logger
     * @return logger
     */
    public Logger getLog()
    {
        if (log == null)
        {
            synchronized (this)
            {
                if (log == null)
                {
                    initLog();
                }
            }
        }
        return log;
    }

    /**
     * init logger
     */
    protected void initLog()
    {
        if (velocity == null)
        {
            log = LoggerFactory.getLogger(ToolManager.class);
        }
        else
        {
            log = ConfigurationUtils.getLog(velocity, "tools");
        }
    }

    /**
     * create new context with configured toolboxes tools
     * @return newly created context
     */
    public ToolContext createContext()
    {
        return createContext(null);
    }

    /**
     * create new context with configured toolboxes tools,
     * using the provided tools properties
     * @param toolProps tools properties
     * @return newly created context
     */
    public ToolContext createContext(Map<String,Object> toolProps)
    {
        ToolContext context = new ToolContext(toolProps);
        prepareContext(context);
        return context;
    }

    /**
     * Prepare context
     * @param context tool context
     */
    protected void prepareContext(ToolContext context)
    {
        context.setUserCanOverwriteTools(this.userOverwrite);
        if (this.velocity != null)
        {
            context.putVelocityEngine(this.velocity);
        }
        addToolboxes(context);
    }

    /**
     * Add toolboxes to contex
     * @param context context
     */
    protected void addToolboxes(ToolContext context)
    {
        if (hasApplicationTools())
        {
            context.addToolbox(getApplicationToolbox());
        }
        if (hasRequestTools())
        {
            context.addToolbox(getRequestToolbox());
        }
    }

    /**
     * Check for the presence of tools in a given scope
     * @param scope scope to check
     * @return whether this scope contains tools
     */
    protected boolean hasTools(String scope)
    {
        return this.factory.hasTools(scope);
    }

    /**
     * Create a toolbox for the given scope
     * @param scope scope
     * @return newly created toolbox
     */
    protected Toolbox createToolbox(String scope)
    {
        return this.factory.createToolbox(scope);
    }

    /**
     * Check whether this tool manager has request scoped tools
     * @return <code>true</code> if this tool manager has request scoped tools,
     *         <code>false</code> otherwise
     */
    public boolean hasRequestTools()
    {
        return hasTools(Scope.REQUEST);
    }

    /**
     * Get the toolbox for request scoped tools
     * @return toolbox of request scoped tools
     */
    public Toolbox getRequestToolbox()
    {
        return createToolbox(Scope.REQUEST);
    }

    /**
     * Check whether this tool manager has application scoped tools
     * @return <code>true</code> if this tool manager has application scoped tools,
     *         <code>false</code> otherwise
     */
    public boolean hasApplicationTools()
    {
        return hasTools(Scope.APPLICATION);
    }

    /**
     * Get the toolbox for application scoped tools
     * @return toolbox of application scoped tools
     */
    public Toolbox getApplicationToolbox()
    {
        if (this.application == null && hasApplicationTools())
        {
            this.application = createToolbox(Scope.APPLICATION);
        }
        return this.application;
    }

}

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
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;

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
    private Toolbox application;
    private boolean userOverwrite = true;

    /**
     * Constructs an instance already configured to use the 
     * {@link ConfigurationUtils#getAutoLoaded()} configuration
     * and any configuration specified via a "org.apache.velocity.tools"
     * system property.
     */
    public ToolManager()
    {
        this(true, true);
    }

    public ToolManager(boolean includeDefaults)
    {
        this(true, includeDefaults);
    }

    public ToolManager(boolean autoConfig, boolean includeDefaults)
    {
        this.factory = new ToolboxFactory();
        
        if (autoConfig)
        {
            autoConfigure(includeDefaults);
        }
    }

    public void autoConfigure(boolean includeDefaults)
    {
        FactoryConfiguration config =
            ConfigurationUtils.getAutoLoaded(includeDefaults);

        // look for any specified via system property
        FactoryConfiguration sys = ConfigurationUtils.findFromSystemProperty();
        if (sys != null)
        {
            config.addConfiguration(sys);
        }
        configure(config);
    }

    public void configure(FactoryConfiguration config)
    {
        // clear the cached application toolbox
        this.application = null;
        this.factory.configure(config);
    }

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

    protected FactoryConfiguration findConfig(String path)
    {
        return ConfigurationUtils.find(path);
    }

    /**
     * Returns the underlying {@link ToolboxFactory} being used.
     */
    public ToolboxFactory getToolboxFactory()
    {
        return this.factory;
    }

    /**
     * Sets the underlying ToolboxFactory being used.
     * <b>If you use this, be sure that your ToolboxFactory
     * is already properly configured.</b>
     */
    public void setToolboxFactory(ToolboxFactory factory)
    {
        if (this.factory != factory)
        {
            if (factory == null)
            {
                throw new NullPointerException("ToolboxFactory cannot be null");
            }
            debug("ToolboxFactory instance was changed to %s", factory);
            this.factory = factory;
        }
    }

    /**
     * Sets the underlying VelocityEngine being used.
     * <b>If you use this, be sure that your VelocityEngine
     * is already properly configured and initialized.</b>
     */
    public void setVelocityEngine(VelocityEngine engine)
    {
        if (velocity != engine)
        {
            debug("VelocityEngine instance was changed to %s", engine);
            this.velocity = engine;
        }
    }

    public VelocityEngine getVelocityEngine()
    {
        return this.velocity;
    }

    public void setUserCanOverwriteTools(boolean overwrite)
    {
        this.userOverwrite = overwrite;
    }

    public boolean getUserCanOverwriteTools()
    {
        return this.userOverwrite;
    }

    public Log getLog()
    {
        if (velocity == null)
        {
            return null;
        }
        return velocity.getLog();
    }

    protected final void debug(String msg, Object... args)
    {
        Log log = getLog();
        if (log != null && log.isDebugEnabled())
        {
            log.debug(String.format(msg, args));
        }
    }

    public ToolContext createContext()
    {
        return createContext(null);
    }

    public ToolContext createContext(Map<String,Object> toolProps)
    {
        ToolContext context = new ToolContext(toolProps);
        prepareContext(context);
        return context;
    }

    protected void prepareContext(ToolContext context)
    {
        context.setUserCanOverwriteTools(this.userOverwrite);
        if (this.velocity != null)
        {
            context.putVelocityEngine(this.velocity);
        }
        addToolboxes(context);
    }

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

    protected boolean hasTools(String scope)
    {
        return this.factory.hasTools(scope);
    }

    protected Toolbox createToolbox(String scope)
    {
        return this.factory.createToolbox(scope);
    }

    protected boolean hasRequestTools()
    {
        return hasTools(Scope.REQUEST);
    }

    protected Toolbox getRequestToolbox()
    {
        return createToolbox(Scope.REQUEST);
    }

    protected boolean hasApplicationTools()
    {
        return hasTools(Scope.APPLICATION);
    }

    protected Toolbox getApplicationToolbox()
    {
        if (this.application == null && hasApplicationTools())
        {
            this.application = createToolbox(Scope.APPLICATION);
        }
        return this.application;
    }

}

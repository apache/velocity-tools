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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.FileFactoryConfiguration;
import org.apache.velocity.tools.config.PropertiesFactoryConfiguration;
import org.apache.velocity.tools.config.XmlFactoryConfiguration;

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
    private VelocityEngine engine;
    private ToolboxFactory factory;
    private Toolbox application;

    /**
     * Constructs an instance already configured to use the 
     * {@link FactoryConfiguration#getDefault()} configuration.
     */
    public ToolManager()
    {
        this(true);
    }

    public ToolManager(boolean startWithDefault)
    {
        //TODO? should we look for a tools.xml in the current dir or classpath root?
        if (startWithDefault)
        {
            this.factory = FactoryConfiguration.createDefaultFactory();
        }
        else
        {
            this.factory = new ToolboxFactory();
        }
    }

    public void configure(FactoryConfiguration config)
    {
        // clear the cached application toolbox
        this.application = null;
        this.factory.configure(config);
    }

    public void configure(String path)
    {
        configure(getConfiguration(path));
    }

    public void setVelocityEngine(VelocityEngine engine)
    {
        this.engine = engine;
    }

    public ToolContext createContext()
    {
        ToolContext context;
        if (this.engine != null)
        {
            context = new ToolContext(this.engine);
        }
        else
        {
            context = new ToolContext((Map<String,Object>)null);
        }
        addToolboxes(context);
        return context;
    }

    public ToolContext createContext(Map<String,Object> toolProps)
    {
        ToolContext context;
        if (this.engine != null)
        {
            context = new ToolContext(this.engine);
            if (toolProps != null && !toolProps.isEmpty())
            {
                for (String key : toolProps.keySet())
                {
                    context.putToolProperty(key, toolProps.get(key));
                }
            }
        }
        else
        {
            context = new ToolContext(toolProps);
        }
        addToolboxes(context);
        return context;
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

    protected boolean hasRequestTools()
    {
        return this.factory.hasTools(ToolboxFactory.DEFAULT_SCOPE);
    }

    protected Toolbox getRequestToolbox()
    {
        return this.factory.createToolbox(ToolboxFactory.DEFAULT_SCOPE);
    }

    protected boolean hasApplicationTools()
    {
        return this.factory.hasTools(ToolboxFactory.APPLICATION_SCOPE);
    }

    protected Toolbox getApplicationToolbox()
    {
        if (this.application == null)
        {
            this.application =
                this.factory.createToolbox(ToolboxFactory.APPLICATION_SCOPE);
        }
        return this.application;
    }

    protected FactoryConfiguration getConfiguration(String path)
    {
        FileFactoryConfiguration config = null;
        if (path.endsWith(".xml"))
        {
            config = new XmlFactoryConfiguration();
        }
        else if (path.endsWith(".properties"))
        {
            config = new PropertiesFactoryConfiguration();
        }
        else
        {
            String msg = "Unknown configuration file type: " + path +
                         "\nOnly .xml and .properties configuration files are supported at this time.";
            throw new UnsupportedOperationException(msg);
        }

        // now, try to read the file
        InputStream inputStream = getInputStream(path);
        try
        {
            config.read(inputStream);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Failed to load configuration at: "+path, ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                throw new RuntimeException("Failed to close input stream for "+path, ioe);
            }
        }
        return config;
    }

    protected InputStream getInputStream(String path)
    {
        // first, search the classpath
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null)
        {
            // then, try the file system directly
            File file = new File(path);
            if (file.exists())
            {
                try
                {
                    inputStream = new FileInputStream(file);
                }
                catch (FileNotFoundException fnfe)
                {
                    // we should not be able to get here
                    // since we already checked whether the file exists
                    throw new IllegalStateException(fnfe);
                }
            }
        }

        // if we still haven't found one
        if (inputStream == null)
        {
            throw new ResourceNotFoundException("Could not find file at: "+path);
        }
        return inputStream;
    }
    
}

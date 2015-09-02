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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

/**
 * {@link Context} implementation that keeps a list of {@link Toolbox}es
 * and returns them as requested, using its internal context Map as the
 * dynamic properties passed to the requested tools when they are first
 * created.
 *
 * @author Nathan Bubna
 * @version $Id: ToolContext.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolContext implements Context
{
    public static final String PATH_KEY = "requestPath";
    public static final String CONTEXT_KEY = "velocityContext";
    public static final String ENGINE_KEY = "velocityEngine";
    public static final String LOCALE_KEY = "locale";
    public static final String LOG_KEY = "log";
    public static final String CATCH_EXCEPTIONS_KEY = "catchExceptions";

    private List<Toolbox> toolboxes = new ArrayList<Toolbox>();
    // this is meant solely for tool setup,
    // values in here are not part of the Context
    private Map<String,Object> toolProps = new HashMap<String,Object>(12);
    // this is only for values added during use of this context
    private Map<String,Object> localContext = new HashMap<String,Object>();
    private boolean userOverwrite = true;

    public ToolContext()
    {
        // add this as a common tool property
        putToolProperty(CONTEXT_KEY, this);
    }

    /**
     * Creates an instance that automatically has the specified
     * VelocityEngine and related tool properties set.
     */
    public ToolContext(VelocityEngine engine)
    {
        this();

        putVelocityEngine(engine);
    }

    /**
     * Creates an instance starting with the specified tool properties.
     */
    public ToolContext(Map<String,Object> toolProps)
    {
        this();

        if (toolProps != null)
        {
            this.toolProps.putAll(toolProps);
        }
    }

    /**
     * Set whether or not tool references can be overwritten within a template.
     * The default value is {@code true}.  Set this to false if you want to
     * ensure that your tool references are never replaced within the course
     * of a template.
     */
    public void setUserCanOverwriteTools(boolean overwrite)
    {
        this.userOverwrite = overwrite;
    }

    /**
     * Default is {@code true}.
     * @see #setUserCanOverwriteTools
     */
    public boolean getUserCanOverwriteTools()
    {
        return this.userOverwrite;
    }

    public void addToolbox(Toolbox toolbox)
    {
        toolboxes.add(toolbox);
    }

    /**
     * Returns a {@link Map} of all tools available to this
     * context. NOTE: this is not a cheap operation as it will
     * request and initialize an instance of every available tool.
     */
    public Map<String,Object> getToolbox()
    {
        Map<String,Object> aggregate = new HashMap<String,Object>();
        Map<String,Object> toolProps = getToolProperties();
        for (Toolbox toolbox : getToolboxes())
        {
            aggregate.putAll(toolbox.getAll(toolProps));
        }
        return aggregate;
    }

    /**
     * Gets a map of keys to classes for all available tools.
     * This does not include any data nor any local context values.
     */
    public Map<String,Class> getToolClassMap()
    {
        Map<String,Class> toolClasses = new HashMap<String,Class>();
        // go thru toolboxes backwards so final map matches
        // what would be found in lookups
        int n = getToolboxes().size();
        for (int i = n - 1; i >= 0; i--)
        {
            Toolbox toolbox = getToolboxes().get(i);
            toolClasses.putAll(toolbox.getToolClassMap());
        }
        return toolClasses;
    }

    protected List<Toolbox> getToolboxes()
    {
        return this.toolboxes;
    }

    protected Map<String,Object> getToolProperties()
    {
        return this.toolProps;
    }

    /**
     * Puts the specified VelocityEngine in the tool properties,
     * as well as the Log for that engine.  Last, if the specified
     * engine has a MethodExceptionEventHandler configured, then
     * this will automatically set {@link #CATCH_EXCEPTIONS_KEY}
     * to false in the tool properties.
     */
    public void putVelocityEngine(VelocityEngine engine)
    {
        // add the engine and log as common tool properties
        putToolProperty(ENGINE_KEY, engine);
        putToolProperty(LOG_KEY, engine.getLog());

        // tell interested tools not to catch exceptions whenever there's a
        // method exception event handler configured for the engine
        Object ehme =
            engine.getProperty(VelocityEngine.EVENTHANDLER_METHODEXCEPTION);
        if (ehme != null)
        {
            putToolProperty(CATCH_EXCEPTIONS_KEY, Boolean.FALSE);
        }
    }

    public Object putToolProperty(String key, Object value)
    {
        return toolProps.put(key, value);
    }

    public void putToolProperties(Map<String,Object> props)
    {
        if (props != null)
        {
            for (Map.Entry<String,Object> prop : props.entrySet())
            {
                putToolProperty(prop.getKey(), prop.getValue());
            }
        }
    }

    public Object put(String key, Object value)
    {
        return localContext.put(key, value);
    }

    public Object get(String key)
    {
        // for user overwriting, it's all a matter of which we check first
        Object value = userOverwrite ? internalGet(key) : findTool(key);
        if (value == null)
        {
            value = userOverwrite ? findTool(key) : internalGet(key);
        }
        return value;
    }

    protected Object internalGet(String key)
    {
        return localContext.get(key);
    }

    protected Object findTool(String key)
    {
        String path = (String)toolProps.get(PATH_KEY);
        for (Toolbox toolbox : getToolboxes())
        {
            Object tool = toolbox.get(key, path, toolProps);
            if (tool != null)
            {
                return tool;
            }
        }
        return null;
    }
        

    public Set<String> keySet()
    {
        Set<String> keys = new HashSet<String>();
        for (Toolbox toolbox : getToolboxes())
        {
            keys.addAll(toolbox.getKeys());
        }
        keys.addAll(localContext.keySet());
        return keys;
    }

    public boolean containsKey(Object key)
    {
        return keySet().contains(key);
    }

    public Object[] getKeys()
    {
        return keySet().toArray();
    }

    public Object remove(Object key)
    {
        //tools and their props cannot be removed
        return localContext.remove(key);
    }

    public void putAll(Map context)
    {
        localContext.putAll(context);
    }
}

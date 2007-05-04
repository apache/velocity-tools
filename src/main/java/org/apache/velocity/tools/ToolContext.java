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
    public static final String LOG_KEY = "log";

    private List<Toolbox> toolboxes;
    // this is meant solely for tool setup,
    // values in here are not part of the Context
    private Map<String,Object> toolProps;
    // this is only for values added during use of this context
    private Map<String,Object> localContext;

    public ToolContext(VelocityEngine engine)
    {
        this(null, null);

        // add the engine and log as common tool properties
        putToolProperty(ENGINE_KEY, engine);
        putToolProperty(LOG_KEY, engine.getLog());
    }

    public ToolContext(Map<String,Object> toolProps)
    {
        this(toolProps, null);
    }

    public ToolContext(Toolbox toolbox)
    {
        this(null, toolbox);
    }

    public ToolContext(Map<String,Object> toolProps, Toolbox toolbox)
    {
        if (toolProps != null)
        {
            this.toolProps = toolProps;
        }
        else
        {
            this.toolProps = new HashMap<String,Object>(8);
        }
        // add this as a common tool property
        putToolProperty(CONTEXT_KEY, this);

        toolboxes = new ArrayList<Toolbox>();
        if (toolbox != null)
        {
            toolboxes.add(toolbox);
        }

        this.localContext = new HashMap<String,Object>();
    }

    public void addToolbox(Toolbox toolbox)
    {
        toolboxes.add(toolbox);
    }

    protected List<Toolbox> getToolboxes()
    {
        return this.toolboxes;
    }

    protected Map<String,Object> getToolProperties()
    {
        return this.toolProps;
    }

    public Object putToolProperty(String key, Object value)
    {
        return toolProps.put(key, value);
    }

    public Object put(String key, Object value)
    {
        return localContext.put(key, value);
    }

    public Object get(String key)
    {
        Object value = findTool(key);
        if (value == null)
        {
            return internalGet(key);
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
        for (Toolbox toolbox : toolboxes)
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
}

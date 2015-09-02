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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Instances of this class are typically created by a {@link ToolboxFactory}
 * on a one-per-scope basis.  So, for each application, there would be one
 * application-scoped Toolbox from which you would retrieve tool instances,
 * and for each request, there would be one request-scoped Toolbox.
 * Of course, none of the above is enforced.  There's no reason that you can't
 * manually create a Toolbox or have multiple Toolboxes for each scope.
 * </p><p>
 * When a Toolbox creates a tool instance asked of it (see {@link #get}),
 * it will cache that instance for future requests.
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: Toolbox.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Toolbox implements java.io.Serializable
{
    /**
     * The key used to place instances in various scopes.
     */
    public static final String KEY = Toolbox.class.getName();

    private static final long serialVersionUID = 888081253188664649L;

    private Map<String,ToolInfo> infoMap;
    private Map<String,Object> properties;
    private Map<String,Object> cache;

    public Toolbox(Map<String,ToolInfo> toolInfo)
    {
        this(toolInfo, null);
    }

    public Toolbox(Map<String,ToolInfo> toolInfo, Map<String,Object> properties)
    {
        if (toolInfo == null)
        {
            this.infoMap = Collections.emptyMap();
        }
        else
        {
            this.infoMap = toolInfo;
        }
        this.properties = properties;
    }

    protected void cacheData(Map<String,Object> data)
    {
        if (data != null && !data.isEmpty())
        {
            cache = new HashMap<String,Object>(data);
        }
    }


    public Map<String,Object> getProperties()
    {
        return properties;
    }

    public Object get(String key)
    {
        return get(key, null, null);
    }

    public Object get(String key, String path)
    {
        return get(key, path, null);
    }

    public Object get(String key, Map<String,Object> context)
    {
        return get(key, null, context);
    }

    public Object get(String key, String path, Map<String,Object> context)
    {
        Object tool = null;
        if (cache != null)
        {
            tool = getFromCache(key, path);
        }
        if (tool == null)
        {
            tool = getFromInfo(key, path, context);
        }
        return tool;
    }


    protected Object getFromCache(String key, String path)
    {
        if (cache == null)
        {
            return null;
        }
        else
        {
            Object tool = cache.get(key);
            if (tool == null)
            {
                return null;
            }
            else if (path == null)
            {
                return tool;
            }
            else if (hasPermission(infoMap.get(key), path))
            {
                return tool;
            }
            else
            {
                return null;
            }
        }
    }

    protected Object getFromInfo(String key, String path,
                                 Map<String,Object> context)
    {
        ToolInfo info = infoMap.get(key);
        if (info != null && (path == null || hasPermission(info, path)))
        {
            Object tool = info.create(context);
            if (cache == null)
            {
                cache = new HashMap<String,Object>();
            }
            cache.put(key, tool);
            return tool;
        }
        return null;
    }

    protected boolean hasPermission(ToolInfo info, String path)
    {
        if (info == null || path == null)
        {
            return true;
        }
        return info.hasPermission(path);
    }

    public Set<String> getKeys()
    {
        // add keys for all available tools
        Set<String> keys = new HashSet<String>(infoMap.keySet());
        // be sure to add cache, which holds data keys
        if (cache != null)
        {
            keys.addAll(cache.keySet());
        }
        return keys;
    }

    /**
     * Return a new {@link Map} link tools' keys to their {@link Class}es.
     * This will not instantiate any tools, it is merely informational.
     * This will not include the keys for any cached data. Note that inclusion
     * in this map does NOT mean that all these tools will be available for
     * all requests, as this map ignores all path restrictions on the tools.
     */
    public Map<String,Class> getToolClassMap()
    {
        Map<String,Class> classMap = new HashMap<String,Class>(infoMap.size());
        for (Map.Entry<String,ToolInfo> entry : infoMap.entrySet())
        {
            classMap.put(entry.getKey(), entry.getValue().getToolClass());
        }
        return classMap;
    }

    public Map<String,Object> getAll(Map<String,Object> context)
    {
        // request all tools we have info for
        for (ToolInfo info : infoMap.values())
        {
            get(info.getKey(), context);
        }
        // then return a copy of the cache
        return new HashMap<String,Object>(this.cache);
    }

    /**
     * Returns a new {@link Toolbox} that is a combination of
     * this Toolbox with one or more specified {@link Toolbox}es.
     * Neither this instance nor those specified are modified.
     */
    public Toolbox combine(Toolbox... toolboxes)
    {
        Map<String,ToolInfo> info = new HashMap<String,ToolInfo>(this.infoMap);
        Map<String,Object> props = new HashMap<String,Object>(this.properties);
        Map<String,Object> data = new HashMap<String,Object>(this.cache);
        for (Toolbox toolbox : toolboxes)
        {
            info.putAll(toolbox.infoMap);
            props.putAll(toolbox.properties);
            data.putAll(toolbox.cache);
        }
        Toolbox combination = new Toolbox(info, props);
        combination.cacheData(data);
        return combination;
    }

}

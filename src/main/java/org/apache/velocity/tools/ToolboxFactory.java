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

import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.tools.config.Data;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ToolboxConfiguration;
import org.apache.velocity.tools.config.ToolConfiguration;

/**
 * <p>This class is the central point of action for VelocityTools.
 * It manages the configured and scoped {@link ToolInfo} and {@link Data}
 * and is meant to stick around for the life of the application.
 * </p><p>
 * It works like this:
 * <ol>
 * <li>Build up your {@link FactoryConfiguration}(s)</li>
 * <li>Create a {@link ToolboxFactory} instance</li>
 * <li>Pass the configuration to {@link #configure}</li>
 * <li>When appropriate for each scope, use {@link #createToolbox}
 * to create the {@link Toolbox} for that scope and put that toolbox
 * somewhere appropriate to that scope.</li>
 * <li>When you want a tool, get that {@link Toolbox} and
 * ask it for the tool you want (e.g. <code>toolbox.get("math")</code>).</li>
 * </ol>
 * </p><p>
 * Of course, most users will not have to do any of this
 * as much of it is handled for them by some combination of 
 * {@link ToolManager} or {@link org.apache.velocity.tools.view.VelocityView}
 * and a {@link ToolContext} or {@link org.apache.velocity.tools.view.ViewToolContext}.
 * </p><p>
 * <strong>NOTE:</strong> While you are free to pass in new configuration info
 * at any time, that data will only affect {@link Toolbox}es created subsequently.
 * Any previously created toolboxes will have to be re-created and replaced to
 * reflect the changes to the configuration.
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: ToolboxFactory.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolboxFactory
{
    public static final String DEFAULT_SCOPE = Scope.REQUEST;

    private final Map<String,Map<String,ToolInfo>> scopedToolInfo;
    private final Map<String,Map<String,Object>> scopedProperties;
    private Map<String,Object> data;
    private Map<String,Object> globalProperties;
    
    public ToolboxFactory()
    {
        this.scopedToolInfo = new HashMap<String,Map<String,ToolInfo>>();
        this.scopedProperties = new HashMap<String,Map<String,Object>>();
    }


    public synchronized void configure(FactoryConfiguration config)
    {
        // this will throw a ConfigurationException if there is a problem
        config.validate();

        // first do the easy part and add any data
        for (Data datum : config.getData())
        {
            putData(datum.getKey(), datum.getConvertedValue());
        }

        // property precedence follows two rules:
        //      newer Foo-level props beat older ones
        //      narrower-scoped props beat broader-scoped ones

        // next add the toolboxes
        for (ToolboxConfiguration toolbox : config.getToolboxes())
        {
            String scope = toolbox.getScope();

            // starting with the toolinfo
            for (ToolConfiguration tool : toolbox.getTools())
            {
                addToolInfo(scope, tool.createInfo());
            }

            // then add the properties for this toolbox
            Map<String,Object> newToolboxProps = toolbox.getPropertyMap();
            putProperties(scope, newToolboxProps);

            // now go thru all toolinfo for this scope old and new
            for (ToolInfo info : getToolInfo(scope).values())
            {
                // and add these new toolbox properties, which have
                // lower precedence than the props already in the info
                info.addProperties(newToolboxProps);
            }
        }

        // now set all the factory-level properties
        // new ones will override old ones
        Map<String,Object> newGlobalProps = config.getPropertyMap();
        putGlobalProperties(newGlobalProps);

        // now go thru all toolboxes in this factory
        for (Map<String,ToolInfo> toolbox : scopedToolInfo.values())
        {
            // iterating over all the toolinfo in them
            for (ToolInfo info : toolbox.values())
            {
                // and adding the new global properties last
                // since they have the lowest precedence
                info.addProperties(newGlobalProps);
            }
        }
    }



    protected synchronized Object putData(String key, Object value)
    {
        if (data == null)
        {
            data = new HashMap<String,Object>();
        }
        return data.put(key, value);
    }

    protected void addToolInfo(String scope, ToolInfo tool)
    {
        //TODO? check the scope against any "ValidScopes"
        //      annotation on the tool class, or do we leave
        //      validation like this to FactoryConfiguration?
        getToolInfo(scope).put(tool.getKey(), tool);
    }

    protected synchronized Map<String,ToolInfo> getToolInfo(String scope)
    {
        Map<String,ToolInfo> tools = scopedToolInfo.get(scope);
        if (tools == null)
        {
            tools = new HashMap<String,ToolInfo>();
            scopedToolInfo.put(scope, tools);
        }
        return tools;
    }

    protected synchronized void putGlobalProperties(Map<String,Object> props)
    {
        if (props != null && !props.isEmpty())
        {
            if (globalProperties == null)
            {
                globalProperties = new HashMap<String,Object>(props);
            }
            else
            {
                globalProperties.putAll(props);
            }
        }
    }

    protected synchronized void putProperties(String scope, Map<String,Object> props)
    {
        if (props != null && !props.isEmpty())
        {
            Map<String,Object> properties = scopedProperties.get(scope);
            if (properties == null)
            {
                properties = new HashMap<String,Object>(props);
                scopedProperties.put(scope, properties);
            }
            else
            {
                properties.putAll(props);
            }
        }
    }



    public Object getGlobalProperty(String name)
    {
        if (globalProperties == null)
        {
            return null;
        }
        return globalProperties.get(name);
    }

    public Map<String,Object> getData()
    {
        return data;
    }

    public boolean hasTools(String scope)
    {
        Map<String,ToolInfo> tools = scopedToolInfo.get(scope);
        if (tools != null && !tools.isEmpty())
        {
            return true;
        }
        else if (data != null && Scope.APPLICATION.equals(scope))
        {
            return true;
        }
        return false;
    }

    public Toolbox createToolbox(String scope)
    {
        Map<String,ToolInfo> tools = scopedToolInfo.get(scope);
        Map properties = scopedProperties.get(scope);

        Toolbox toolbox;
        if (properties == null)
        {
            if (globalProperties == null)
            {
                toolbox = new Toolbox(tools);
            }
            else
            {
                toolbox = new Toolbox(tools, globalProperties);
            }
        }
        else
        {
            //TODO: this will waste cycles on subsequent retrievals
            //      of the same toolbox. consider improving...
            if (globalProperties != null)
            {
                properties.putAll(globalProperties);
            }
            toolbox = new Toolbox(tools, properties);
        }

        // if application scoped or if there's only one toolbox,
        // then automatically include data, if we have any.
        if (data != null &&
            (scopedToolInfo.size() == 1 || scope.equals(Scope.APPLICATION)))
        {
            toolbox.cacheData(getData());
        }
        return toolbox;
    }

}

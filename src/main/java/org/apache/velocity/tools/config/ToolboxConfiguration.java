package org.apache.velocity.tools.config;

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

import java.util.List;
import org.apache.velocity.tools.ToolboxFactory;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: ToolboxConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolboxConfiguration
    extends CompoundConfiguration<ToolConfiguration>
{
    private String scope = ToolboxFactory.DEFAULT_SCOPE;


    protected ToolConfiguration findMatchingChild(ToolConfiguration newTool)
    {
        String newKey = newTool.getKey();
        if (newKey == null)
        {
            // we consider it impossible to equal null here
            return null;
        }

        for (ToolConfiguration tool : getTools())
        {
            // matching key means matching tool
            if (newKey.equals(tool.getKey()))
            {
                return tool;
            }
        }
        return null;
    }

    public void addConfiguration(ToolboxConfiguration config)
    {
        // add config's properties to ours
        super.addConfiguration(config);

        // add config's children to ours
        for (ToolConfiguration newTool : config.getTools())
        {
            ToolConfiguration child = findMatchingChild(newTool);
            if (child == null)
            {
                addTool(newTool);
            }
            else
            {
                child.addConfiguration(newTool);
                // also, override the classname for tools
                String newClass = newTool.getClassname();
                if (newClass != null)
                {
                    child.setClassname(newClass);
                }
            }
        }
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getScope()
    {
        return this.scope;
    }

    public void addTool(ToolConfiguration tool)
    {
        addChild(tool);
    }

    public List<ToolConfiguration> getTools()
    {
        return getChildren();
    }

    @Override
    public void validate()
    {
        super.validate();

        if (getScope() == null)
        {
            throw new ConfigurationException(this, "Toolbox scope cannot be null");
        }

        // validate that all tools are allowed in this scope
        for (ToolConfiguration tool : getTools())
        {
            // check if this toolbox's scope has been declared invalid
            for (String invalidScope : tool.getInvalidScopes())
            {
                if (getScope().equals(invalidScope))
                {
                    throw new InvalidScopeException(this, tool);
                }
            }
 
            // if the set of valid scopes has been limited, check to be
            // sure that this toolbox's scope is in the set
            String[] validScopes = tool.getValidScopes();
            if (validScopes != null && validScopes.length > 0)
            {
                boolean found = false;
                for (String validScope : validScopes)
                {
                    if (getScope().equals(validScope))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    throw new InvalidScopeException(this, tool);
                }
            }
        }
    }

    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("Toolbox '");
        out.append(this.scope);
        out.append("' ");
        appendProperties(out);
        appendChildren(out, "tools: \n  ", "\n  ");
        return out.toString();
    }

}

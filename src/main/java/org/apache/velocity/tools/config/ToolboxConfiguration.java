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

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: ToolboxConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolboxConfiguration
    extends CompoundConfiguration<ToolConfiguration>
{
    private String scope;


    protected ToolConfiguration findMatchingChild(ToolConfiguration newTool)
    {
        for (ToolConfiguration tool : getTools())
        {
            // matching key means matching tool
            if (newTool.getKey().equals(tool.getKey()))
            {
                return tool;
            }
        }
        return null;
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

}

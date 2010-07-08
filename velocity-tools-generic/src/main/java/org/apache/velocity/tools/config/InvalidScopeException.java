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

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: InvalidScopeException.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class InvalidScopeException extends ConfigurationException
{
    // this isn't crucial to keep around if the exception is serialized
    private final transient ToolConfiguration tool;

    public InvalidScopeException(ToolboxConfiguration toolbox,
                                 ToolConfiguration tool)
    {
        super(toolbox, "Toolbox with scope '" +
                       toolbox.getScope() +
                       "' may not contain a " +
                       tool.getClassname() + '.');
        this.tool = tool;
    }

    public ToolConfiguration getToolConfiguration()
    {
        return tool;
    }

}

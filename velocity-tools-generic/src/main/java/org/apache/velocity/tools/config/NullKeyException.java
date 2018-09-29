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
 * @version $Id: NullKeyException.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class NullKeyException extends ConfigurationException
{
    private static final long serialVersionUID = -3939817560016273430L;

    public NullKeyException(Data data)
    {
        super(data, "Key is null for data with value of '"+data.getValue()+'\'');
    }

    public NullKeyException(ToolConfiguration tool)
    {
        super(tool, "Key is null for tool whose class is '"+tool.getClassname()+'\'');
    }

}

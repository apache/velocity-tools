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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.velocity.tools.ToolContext;

/**
 * Manages old tools which still use deprecated init() and configure()
 * methods.
 *
 * @author Nathan Bubna
 * @version $Id: OldToolInfo.java 511959 2007-02-26 19:24:39Z nbubna $
 */
//TODO: make this class serializable
public class OldToolInfo extends ToolInfo
{
    public static final String INIT_METHOD_NAME = "init";

    private Method init;

    /**
     * Creates a new instance using the minimum required info
     * necessary for a tool.
     */
    public OldToolInfo(String key, Class clazz)
    {
        super(key, clazz);
    }

    /**
     * 
     *
     * @param clazz the java.lang.Class of the tool
     */
    @Override
    public void setClass(Class clazz)
    {
        super.setClass(clazz);

        try
        {
            // try to get an init(Object) method
            this.init = clazz.getMethod("init",
                                        new Class[]{ Object.class });
        }
        catch (NoSuchMethodException nsme)
        {
            // ignore
        }
    }


    @Override
    public Object create(Map<String,Object> dynamicProperties)
    {
        Object tool = super.create(dynamicProperties);

        if (this.init != null)
        {
            // ctx should, in all cases where a tool has such a method,
            // actually be a View(Tool)Context, but we don't want to link
            // to that class here, so as not to pollute the generic jar
            Object ctx =
                dynamicProperties.get(ToolContext.CONTEXT_KEY);
            if (ctx != null)
            {
                invoke(this.init, tool, ctx);
            }
        }
        return tool;
    }

}

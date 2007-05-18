package org.apache.velocity.tools.config;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional debugrmation
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

import java.util.Iterator;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Configuration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ConfigurationCleaner extends LogSupport
{
    private static final String LOG_PREFIX = "ConfigurationCleaner : ";

    @Override
    protected String logPrefix()
    {
        return LOG_PREFIX;
    }

    public void clean(FactoryConfiguration factory)
    {
        if (isTraceEnabled())
        {
            trace("Cleaning factory: "+factory);
        }

        cleanProperties(factory);

        // go thru data to log and remove debug ones
        Iterator<Data> i = factory.getData().iterator();
        while (i.hasNext())
        {
            Data datum = i.next();
            try
            {
                datum.validate();
            }
            catch (ConfigurationException ce)
            {
                if (isDebugEnabled())
                {
                    debug(ce.getMessage());
                }
                if (isWarnEnabled())
                {
                    warn("Removing "+datum);
                }
                i.remove();
            }
        }

        // clean all toolboxes
        for (ToolboxConfiguration toolbox : factory.getToolboxes())
        {
            clean(toolbox);
        }
    }

    public void clean(ToolboxConfiguration toolbox)
    {
        cleanProperties(toolbox);

        // go thru tools to log and remove debug ones
        Iterator<ToolConfiguration> i = toolbox.getTools().iterator();
        while (i.hasNext())
        {
            ToolConfiguration tool = i.next();
            cleanProperties(tool);
            try
            {
                tool.validate();
            }
            catch (ConfigurationException ce)
            {
                if (isDebugEnabled())
                {
                    debug(ce.getMessage());
                }
                if (isWarnEnabled())
                {
                    warn("Removing "+tool);
                }
                i.remove();
            }
        }

        //TODO: loop on validate() until all debug scoped tools are removed
    }

    public void cleanProperties(Configuration config)
    {
        // go thru properties to log and remove debug ones
        Iterator<Property> i = config.getConvertableProperties().iterator();
        while (i.hasNext())
        {
            Property prop = i.next();
            try
            {
                prop.validate();
            }
            catch (ConfigurationException ce)
            {
                if (isDebugEnabled())
                {
                    debug(ce.getMessage());
                }
                if (isWarnEnabled())
                {
                    warn("Removing "+prop);
                }
                i.remove();
            }
        }
    }

}

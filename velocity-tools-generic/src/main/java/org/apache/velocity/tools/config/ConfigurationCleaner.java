package org.apache.velocity.tools.config;

import org.slf4j.Logger;

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
public class ConfigurationCleaner
{
    private Logger LOG = null;

    public void setLog(Logger log)
    {
        if (log == null)
        {
            throw new NullPointerException("log should not be set to null");
        }
        LOG = log;
    }

    public void clean(FactoryConfiguration factory)
    {
        if (LOG != null)
        {
            LOG.trace("ConfigurationCleaner : Cleaning factory: {}", factory);
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
                if (LOG != null)
                {
                    LOG.debug("ConfigurationCleaner : ", ce);
                    LOG.warn("ConfigurationCleaner : Removing {}", datum);
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
                if (LOG != null)
                {
                    LOG.debug("ConfigurationCleaner : ", ce);
                    LOG.warn("ConfigurationCleaner : Removing {}", tool);
                }
                i.remove();
            }
        }

        //TODO: loop on validate() until all debug scoped tools are removed
    }

    public void clean(Configuration config)
    {
        // delegate to the appropriate method...
        if (config instanceof FactoryConfiguration)
        {
            clean((FactoryConfiguration)config);
        }
        else if (config instanceof ToolboxConfiguration)
        {
            clean((ToolboxConfiguration)config);
        }
        else
        {
            cleanProperties(config);
        }
    }

    public void cleanProperties(Configuration config)
    {
        // go thru properties to log and remove debug ones
        Iterator<Property> i = config.getProperties().iterator();
        while (i.hasNext())
        {
            Property prop = i.next();
            try
            {
                prop.validate();
            }
            catch (ConfigurationException ce)
            {
                if (LOG != null)
                {
                    LOG.debug("ConfigurationCleaner : ", ce);
                    LOG.warn("ConfigurationCleaner : Removing {}", prop);
                }
                i.remove();
            }
        }
    }

}

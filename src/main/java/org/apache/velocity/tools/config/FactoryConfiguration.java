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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.tools.ToolboxFactory;

/**
 * //TODO: add ability to log all this stuff
 *
 * @author Nathan Bubna
 * @version $Id: FactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class FactoryConfiguration
    extends CompoundConfiguration<ToolboxConfiguration>
{
    private Set<Data> data = new LinkedHashSet<Data>();


    @Override
    protected ToolboxConfiguration findMatchingChild(ToolboxConfiguration newToolbox)
    {
        for (ToolboxConfiguration toolbox : getToolboxes())
        {
            // matching scope means matching toolbox
            if (newToolbox.getScope().equals(toolbox.getScope()))
            {
                return toolbox;
            }
        }
        return null;
    }


    public void addData(Data datum)
    {
        data.add(datum);
    }

    public boolean removeData(Data datum)
    {
        return data.remove(datum);
    }

    public Data getData(String key)
    {
        for (Data datum : getData())
        {
            if (key.equals(datum.getKey()))
            {
                return datum;
            }
        }
        return null;
    }

    public Set<Data> getData()
    {
        return data;
    }

    public void addToolbox(ToolboxConfiguration toolbox)
    {
        addChild(toolbox);
    }

    public void removeToolbox(ToolboxConfiguration toolbox)
    {
        removeChild(toolbox);
    }

    public ToolboxConfiguration getToolbox(String scope)
    {
        for (ToolboxConfiguration toolbox : getToolboxes())
        {
            if (scope.equals(toolbox.getScope()))
            {
                return toolbox;
            }
        }
        return null;
    }

    public List<ToolboxConfiguration> getToolboxes()
    {
        return getChildren();
    }

    public void addConfiguration(FactoryConfiguration config)
    {
        // add config's properties to ours
        super.addConfiguration(config);

        // add config's children to ours
        for (ToolboxConfiguration newToolbox : config.getToolboxes())
        {
            ToolboxConfiguration child = findMatchingChild(newToolbox);
            if (child == null)
            {
                addToolbox(newToolbox);
            }
            else
            {
                child.addConfiguration(newToolbox);
            }
        }

        // add config's Data to the bottom of our Data list
        // don't worry about duplicate data names, last one will win
        for (Data datum : config.getData())
        {
            addData(datum);
        }
    }

    @Override
    public void validate()
    {
        super.validate();

        for (Data data : getData())
        {
            data.validate();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("\nFactoryConfiguration ");
        appendProperties(out);
        if (!getData().isEmpty())
        {
            out.append("including ");
            out.append(getData().size());
            out.append(" data");
        }
        appendChildren(out, "toolboxes: \n ", "\n ");
        if (!getData().isEmpty())
        {
            for (Data datum : getData())
            {
                out.append(datum);
                out.append("\n ");
            }
        }
        return out.toString();
    }


    public ToolboxFactory createFactory()
    {
        ToolboxFactory factory = new ToolboxFactory();
        factory.configure(this);
        return factory;
    }

}

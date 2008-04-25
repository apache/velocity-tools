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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.velocity.tools.ToolboxFactory;

/**
 * //TODO: add ability to log all this stuff and/or 
 *         keep a running, ordered list of sources
 *
 * @author Nathan Bubna
 * @version $Id: FactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class FactoryConfiguration
    extends CompoundConfiguration<ToolboxConfiguration>
{
    private final SortedSet<Data> data = new TreeSet<Data>();
    private final List<String> sources = new ArrayList<String>();

    public FactoryConfiguration()
    {
        this("");
    }
    
    /**
     * Creates a new instance with the specified source name
     * combined with this class's name as the initial source.
     */
    public FactoryConfiguration(String source)
    {
        this(FactoryConfiguration.class, source);
    }

    /**
     * Allows subclasses to construct an instance that uses their classname.
     */
    protected FactoryConfiguration(Class clazz, String source)
    {
        addSource(clazz.getName()+"("+source+")");
    }

    /**
     * Returns the list of sources for this configuration info in
     * order starting from the source name given to this instance
     * (if any) and going to the most recently added source.
     */
    public List<String> getSources()
    {
        return this.sources;
    }

    public void addSource(String source)
    {
        this.sources.add(source);
    }

    public void addData(Data newDatum)
    {
        // check if we already have a matching datum
        Data datum = getData(newDatum);
        if (datum != null)
        {
            // newer overrides older, so...
            // remove the old datum
            removeData(datum);
        }
        // add the new datum
        data.add(newDatum);
    }

    public boolean removeData(Data datum)
    {
        return data.remove(datum);
    }

    public Data getData(String key)
    {
        // create an example to search with
        Data findme = new Data();
        findme.setKey(key);
        return getData(findme);
    }

    public Data getData(Data findme)
    {
        for (Data datum : data)
        {
            if (datum.equals(findme))
            {
                return datum;
            }
        }
        return null;
    }

    public boolean hasData()
    {
        return !data.isEmpty();
    }

    public SortedSet<Data> getData()
    {
        return data;
    }

    public void setData(Collection<Data> data)
    {
        for (Data datum : data)
        {
            addData(datum);
        }
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

    public SortedSet<ToolboxConfiguration> getToolboxes()
    {
        return getChildren();
    }

    public void setToolboxes(Collection<ToolboxConfiguration> toolboxes)
    {
        setChildren(toolboxes);
    }

    public void addConfiguration(FactoryConfiguration config)
    {
        // add config's Data to our own
        setData(config.getData());

        // add config's sources to our own
        for (String source : config.getSources())
        {
            addSource(source);
        }

        // pass to CompoundConfiguration's to add properties
        super.addConfiguration(config);
    }

    @Override
    public void validate()
    {
        super.validate();

        for (Data datum : data)
        {
            datum.validate();
        }
    }

    @Override
    public String toString()
    {
        return toString(true);
    }

    public String toString(boolean includeSources)
    {
        StringBuilder out = new StringBuilder();
        out.append("\nFactoryConfiguration from ");
        if (includeSources)
        {
            out.append(getSources().size());
            out.append(" sources ");
        }
        appendProperties(out);
        if (hasData())
        {
            out.append("including ");
            out.append(data.size());
            out.append(" data");
        }
        if (getToolboxes().isEmpty())
        {
            out.append("\n ");
        }
        else
        {
            appendChildren(out, "toolboxes: \n ", "\n ");
        }
        if (hasData())
        {
            for (Data datum : data)
            {
                out.append(datum);
                out.append("\n ");
            }
        }
        if (includeSources)
        {
            int count = 0;
            for (String source : getSources())
            {
                out.append("\n Source ");
                out.append(count++);
                out.append(": ");
                out.append(source);
            }
            out.append("\n");
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

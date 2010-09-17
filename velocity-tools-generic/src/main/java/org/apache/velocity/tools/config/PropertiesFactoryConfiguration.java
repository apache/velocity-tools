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

import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.ExtendedProperties;

/**
 * <p> This reads in configuration info formatted as a property
 * file using {@link ExtendedProperties} from Commons-Collections.</p>
 * <p>Example usage:
 * <pre>
 * FactoryConfiguration cfg = new PropertiesFactoryConfiguration();
 * cfg.read("my.properties");
 * ToolboxFactory factory = cfg.createFactory();
 * </pre></p>
 * <p>This reads in a configuration such as:
 * <pre>
 * tools.toolbox = request,application
 * tools.property.locale = en_us
 * tools.property.locale.class = java.util.Locale
 * tools.property.locale.converter = org.apache.velocity.tools.config.LocaleConverter
 * tools.request.property.xhtml = true
 * tools.request.render = org.apache.velocity.tools.view.ViewRenderTool
 * tools.request.render.parseDepth = 5
 * tools.request.search = com.foo.tools.MySearchTool
 * tools.request.search.itemsPerPage = 10
 * tools.application.math = org.apache.velocity.tools.generic.MathTool
 * tools.data.foo = bar
 * tools.data.foo.class = java.lang.String
 * tools.data.foo.converter = org.apache.commons.beanutils.converter.StringConverter
 * tools.data.version = 1.0
 * tools.data.version.type = number
 * tools.data.debug = false
 * tools.data.debug.type = boolean
 * </pre>
 * <strong>NOTE</strong>: "property", "data", and "toolbox" are
 * reserved words do not use them as tool keys or toolbox scopes.</p>
 *
 * @author Nathan Bubna
 * @version $Id: PropertiesFactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class PropertiesFactoryConfiguration extends FileFactoryConfiguration
{
    public PropertiesFactoryConfiguration()
    {
        this("");
    }

    /**
     * Creates an instance using the specified string
     * as an identifier to distinguish this instance when debugging.
     *
     * @param id the name of the "source" of this instance
     * @see FactoryConfiguration#setSource(String)
     */
    public PropertiesFactoryConfiguration(String id)
    {
        super(PropertiesFactoryConfiguration.class, id);
    }

    /**
     * <p>Reads an properties file from an {@link InputStream}
     * and uses it to configure this {@link FactoryConfiguration}.</p>
     * 
     * @param input the InputStream to read from
     */
    public void read(InputStream input) throws IOException
    {
        ExtendedProperties props = new ExtendedProperties();
        props.load(input);

        // all factory settings should be prefixed with "tools"
        read(props.subset("tools"));
    }

    public void read(ExtendedProperties factory)
    {
        // get the global properties
        readProperties(factory, this);

        // get the toolboxes
        readToolboxes(factory);

        // get the data
        readData(factory.subset("data"));
    }


    protected void readProperties(ExtendedProperties configProps,
                                  Configuration config)
    {
        ExtendedProperties properties = configProps.subset("property");
        if (properties != null)
        {
            for (Iterator i = properties.getKeys(); i.hasNext(); )
            {
                String name = (String)i.next();
                String value = properties.getString(name);

                ExtendedProperties propProps = properties.subset(name);
                if (propProps.size() == 1)
                {
                    // then set this as a 'simple' property
                    config.setProperty(name, value);
                }
                else
                {
                    // add it as a convertable property
                    Property property = new Property();
                    property.setName(name);
                    property.setValue(value);

                    // set the type/converter properties
                    setProperties(propProps, property);
                }
            }
        }
    }

    protected void readToolboxes(ExtendedProperties factory)
    {
        String[] scopes = factory.getStringArray("toolbox");
        for (String scope : scopes)
        {
            ToolboxConfiguration toolbox = new ToolboxConfiguration();
            toolbox.setScope(scope);
            addToolbox(toolbox);

            ExtendedProperties toolboxProps = factory.subset(scope);
            readTools(toolboxProps, toolbox);
            readProperties(toolboxProps, toolbox);
        }
    }

    protected void readTools(ExtendedProperties tools,
                             ToolboxConfiguration toolbox)
    {
        for (Iterator i = tools.getKeys(); i.hasNext(); )
        {
            String key = (String)i.next();
            // if it contains a period, it can't be a context key; 
            // it must be a tool property. ignore it for now.
            if (key.indexOf('.') >= 0)
            {
                continue;
            }

            String classname = tools.getString(key);
            ToolConfiguration tool = new ToolConfiguration();
            tool.setClassname(classname);
            tool.setKey(key);
            toolbox.addTool(tool);

            // get tool properties prefixed by 'property'
            ExtendedProperties toolProps = tools.subset(key);
            readProperties(toolProps, tool);

            // ok, get tool properties that aren't prefixed by 'property'
            for (Iterator j = toolProps.getKeys(); j.hasNext(); )
            {
                String name = (String)j.next();
                if (!name.equals(tool.getKey()))
                {
                    tool.setProperty(name, toolProps.getString(name));
                }
            }

            // get special props explicitly
            String restrictTo = toolProps.getString("restrictTo");
            tool.setRestrictTo(restrictTo);
        }
    }

    protected void readData(ExtendedProperties dataset)
    {
        if (dataset != null)
        {
            for (Iterator i = dataset.getKeys(); i.hasNext(); )
            {
                String key = (String)i.next();
                // if it contains a period, it can't be a context key; 
                // it must be a data property. ignore it for now.
                if (key.indexOf('.') >= 0)
                {
                    continue;
                }

                Data data = new Data();
                data.setKey(key);
                data.setValue(dataset.getString(key));

                // get/set the type/converter properties
                ExtendedProperties props = dataset.subset(key);
                setProperties(props, data);

                addData(data);
            }
        }
    }

    protected void setProperties(ExtendedProperties props, Data data)
    {
        // let's just set/convert anything we can
        // this could be simplified to just check for type/class/converter
        try
        {
            BeanUtils.populate(data, props);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}

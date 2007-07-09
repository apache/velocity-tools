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

import org.apache.velocity.tools.ToolboxFactory;

/**
 * <p>{@link FactoryConfiguration} subclass that simplifies the process of
 * configuration a {@link ToolboxFactory} in Java without the use of an
 * xml or properties configuration file.  Below is an example:
 *
 * <code>
 * EasyFactoryConfiguration config = new EasyFactoryConfiguration();
 * config.toolbox("request").property("locale", Locale.US)
 *  .tool(DateTool.class)
 *  .tool("myTool", MyTool.class);
 * config.toolbox("application")
 *  .tool(NumberTool.class).property("locale", Locale.FR);
 * ToolboxFactory factory = config.createFactory();
 * </code></p>
 *
 * <p>Doing the above without this class would require the following to
 *    create an equivalent {@link FactoryConfiguration} in Java:
 *
 * <code>
 * FactoryConfiguration factoryConfig = new FactoryConfiguration();
 * ToolboxConfiguration toolbox = new ToolboxConfiguration();
 * toolbox.setScope("request");
 * toolbox.setProperty("locale", Locale.US);
 * ToolConfiguration tool = new ToolConfiguration();
 * tool.setClassname(DateTool.class.getName());
 * tool = new ToolConfiguration();
 * tool.setKey("myTool");
 * tool.setClassname(MyTool.class.getName());
 * toolbox.addTool(tool);
 * toolbox = new ToolboxConfiguration();
 * toolbox.setScope("application");
 * tool = new ToolConfiguration();
 * tool.setClassname(NumberTool.class.getName());
 * tool.setProperty("locale", Locale.FR);
 * toolbox.addTool(tool);
 * factoryConfig.addToolbox(toolbox);
 * ToolboxFactory factory = factoryConfig.createFactory();
 * </code></p>
 *
 * <p>Of course, you could directly configure a {@link ToolboxFactory}
 *    with relatively little effort as well:
 *
 * <code>
 * ToolboxFactory factory = new ToolboxFactory();
 * factory.putProperty("request", "locale", Locale.US);
 * factory.addToolInfo("request", new ToolInfo("date", DateTool.class));
 * factory.addToolInfo("request", new ToolInfo("render", ViewRenderTool.class));
 * ToolInfo info = new ToolInfo("number", NumberTool.class);
 * info.setProperty("locale", Locale.FR);
 * factory.addToolInfo("application", info);
 * </code>
 *
 * But this is not reusable.  Why does that matter?  Well, it doesn't matter
 * for application developers.  But, if another framework wishes to provide
 * a common VelocityTools configuration for application developers, this may
 * come in handy.  Or it may not.  In any case, it was mildly fun to write. :)
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: EasyFactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class EasyFactoryConfiguration extends FactoryConfiguration
{
    private boolean addedDefault = false;
    private EasyWrap<ToolboxConfiguration> toolbox;

    public EasyFactoryConfiguration()
    {
        this(false);
    }

    /**
     * @param startWithDefault Sets whether this instance should start with the
     *        {@link FactoryConfiguration#getDefault()} configuration or not.
     */
    public EasyFactoryConfiguration(boolean startWithDefault)
    {
        if (startWithDefault)
        {
            addDefault();
        }
    }

    /**
     * Adds the {@link FactoryConfiguration#getDefault()} configuration to this
     * the current configuration.
     */
    public EasyFactoryConfiguration addDefault()
    {
        if (!addedDefault)
        {
            addConfiguration(getDefault());
            addedDefault = true;
        }
        return this;
    }

    public EasyData data(String key, Object value)
    {
        Data datum = new Data();
        datum.setKey(key);
        datum.setValue(value);
        addData(datum);
        return new EasyData(datum, this);
    }

    public EasyFactoryConfiguration data(String key, String type, Object value)
    {
        EasyData datum = data(key, value);
        datum.type(type);
        return this;
    }

    public EasyFactoryConfiguration string(String key, Object value)
    {
        return data(key, "string", value);
    }

    public EasyFactoryConfiguration number(String key, Object value)
    {
        return data(key, "number", value);
    }

    public EasyFactoryConfiguration bool(String key, Object value)
    {
        return data(key, "boolean", value);
    }

    public EasyWrap<ToolboxConfiguration> toolbox(String scope)
    {
        ToolboxConfiguration toolbox = new ToolboxConfiguration();
        toolbox.setScope(scope);
        addToolbox(toolbox);
        this.toolbox =
            new EasyWrap<ToolboxConfiguration>(toolbox, this);
        return this.toolbox;
    }

    public EasyWrap<ToolConfiguration> tool(String classname)
    {
        return tool(null, classname);
    }

    public EasyWrap<ToolConfiguration> tool(Class clazz)
    {
        return tool(null, clazz);
    }

    public EasyWrap<ToolConfiguration> tool(String key, String classname)
    {
        if (toolbox == null)
        {
            toolbox(ToolboxFactory.DEFAULT_SCOPE);
        }
        return toolbox.tool(key, classname);
    }

    public EasyWrap<ToolConfiguration> tool(String key, Class clazz)
    {
        return tool(key, clazz.getName());
    }

    public EasyFactoryConfiguration property(String name, Object value)
    {
        setProperty(name, value);
        return this;
    }


    public class EasyData
    {
        private Data datum;
        private Configuration parent;

        public EasyData(Data datum, Configuration parent)
        {
            this.datum = datum;
            this.parent = parent;
        }

        public Data getData()
        {
            return this.datum;
        }

        public Configuration getParent()
        {
            return this.parent;
        }

        public EasyData type(String type)
        {
            this.datum.setType(type);
            return this;
        }

        public EasyData target(Class clazz)
        {
            this.datum.setTargetClass(clazz);
            return this;
        }

        public EasyData classname(String classname)
        {
            this.datum.setClassname(classname);
            return this;
        }

        public EasyData converter(String converter)
        {
            this.datum.setConverter(converter);
            return this;
        }

        public EasyData converter(Class clazz)
        {
            this.datum.setConverter(clazz);
            return this;
        }
    }


    public class EasyWrap<C extends Configuration>
    {
        private C config;
        private Configuration parent;

        public EasyWrap(C config, Configuration parent)
        {
            this.config = config;
            this.parent = parent;
        }

        public C getConfiguration()
        {
            return this.config;
        }

        public Configuration getParent()
        {
            return this.parent;
        }

        public EasyWrap<C> property(String name, Object value)
        {
            this.config.setProperty(name, value);
            return this;
        }

        public EasyWrap<C> restrictTo(String path)
        {
            if (this.config instanceof ToolConfiguration)
            {
                ToolConfiguration tool = (ToolConfiguration)this.config;
                tool.setRestrictTo(path);
                return this;
            }
            else if (this.config instanceof ToolboxConfiguration)
            {
                ToolboxConfiguration toolbox = (ToolboxConfiguration)this.config;
                for (ToolConfiguration tool : toolbox.getTools())
                {
                    tool.setRestrictTo(path);
                }
                return this;
            }
            throw new IllegalStateException("Wrapping unknown "+Configuration.class.getName()+": "+getConfiguration());
        }

        public EasyWrap addDefault()
        {
            EasyFactoryConfiguration.this.addDefault();
            return this;
        }

        public EasyWrap tool(Class clazz)
        {
            return tool(null, clazz);
        }

        public EasyWrap tool(String classname)
        {
            return tool(null, classname);
        }

        public EasyWrap tool(String key, Class clazz)
        {
            return tool(key, clazz.getName());
        }

        public EasyWrap tool(String key, String classname)
        {
            ToolConfiguration tool = new ToolConfiguration();
            if (key != null)
            {
                tool.setKey(key);
            }
            tool.setClassname(classname);
            if (this.config instanceof ToolConfiguration)
            {
                ToolboxConfiguration toolbox = (ToolboxConfiguration)getParent();
                toolbox.addTool(tool);
                return new EasyWrap(tool, toolbox);
            }
            else if (this.config instanceof ToolboxConfiguration)
            {
                ToolboxConfiguration toolbox = (ToolboxConfiguration)getConfiguration();
                toolbox.addTool(tool);
                return new EasyWrap(tool, toolbox);
            }
            throw new IllegalStateException("Wrapping unknown "+Configuration.class.getName()+": "+getConfiguration());
        }
    }

}

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

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.ClassUtils;

/**
 * <p>This class handles configuration info for tools, including their key,
 * classname, path restriction, and properties.  It also does fairly
 * aggresive validation.
 * Once configuration is complete, a {@link ToolInfo} instance can be created by calling
 * {@link #createInfo}.</p>
 * <p>
 * Most users will not find themselves directly using the API of this class.
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: ToolConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolConfiguration extends Configuration
{
    private enum Status {
        VALID, NONE, MISSING, UNSUPPORTED, UNINSTANTIABLE;
    }

    private String key;
    private String classname;
    private String factoryClassname;
    private String restrictTo;
    private Boolean skipSetters;
    private Status status;
    private Throwable problem;

    public void setKey(String key)
    {
        this.key = key;

        // ensure any non-default key is also set as a property
        if (key != null && !key.equals(getDefaultKey()))
        {
            setProperty(ToolContext.TOOLKEY_KEY, key);
        }
    }

    /**
     * This method should be avoided, since considered unsafe
     * by the security manager.
     * @param clazz the Class
     * @see #setClassname(String classname)
     */
    @Deprecated
    public void setClass(Class clazz)
    {
        setClassname(clazz.getName());
    }

    public void setClass(String classname)            
    {
        setClassname(classname);
    }    
    
    public void setClassname(String classname)
    {
        this.classname = classname;
        this.status = null;
    }

    public void setFactory(Class factory)
    {
        setFactoryClassname(factory.getName());
    }

    public void setFactoryClassname(String factoryClassname)
    {
        this.factoryClassname = factoryClassname;
        this.status = null;
    }

    public void setRestrictTo(String path)
    {
        this.restrictTo = path;
    }

    public void setSkipSetters(Boolean cfgOnly)
    {
        this.skipSetters = cfgOnly;
    }

    /**
     * Returns the key set for this tool. If no key has been explicitly
     * set, this will return the result of {@link #getDefaultKey()}.
     * @return tool key
     */
    public String getKey()
    {
        if (this.key != null)
        {
            return this.key;
        }
        return getDefaultKey();
    }

    /**
     * Returns the default key value for the set tool class.  First, this
     * looks for a {@link DefaultKey} annotation on the tool class.  Then,
     * if there is no default key annotation, the {@link Class#getSimpleName()}
     * is transformed into the key by removing any 'Tool' suffix and
     * lowercasing the first character.  This will only return {@code null}
     * if there is both no key and no classname set for this tool.
     * @return default tool key
     */
    public String getDefaultKey()
    {
        if (getClassname() != null)
        {
            Class clazz = getToolClass();
            DefaultKey defaultKey =
                (DefaultKey)clazz.getAnnotation(DefaultKey.class);
            if (defaultKey != null)
            {
                return defaultKey.value();
            }
            else
            {
                // convert 'FooTool' to 'foo' to mirror most default keys
                String name = clazz.getSimpleName();
                if (name.endsWith("Tool")) {
                    int i = name.indexOf("Tool");
                    name = name.substring(0, i);
                }
                if (name.length() > 1) {
                    name = name.substring(0, 1).toLowerCase() +
                           name.substring(1, name.length());
                } else {
                    name = name.toLowerCase();
                }
                return name;
            }
        }
        return null;
    }

    public String getClassname()
    {
        return this.classname;
    }

    public String getFactoryClassname() { return factoryClassname; }

    public Class getToolClass()
    {
        try
        {
            return ClassUtils.getClass(getClassname());
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new ConfigurationException(this, cnfe);
        }
    }

    public Class getFactory()
    {
        try
        {
            String factoryClassname = getFactoryClassname();
            return factoryClassname == null ? null : ClassUtils.getClass(factoryClassname);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new ConfigurationException(this, cnfe);
        }
    }

    public String[] getInvalidScopes()
    {
        InvalidScope invalid =
            (InvalidScope)getToolClass().getAnnotation(InvalidScope.class);
        if (invalid != null)
        {
            return invalid.value();
        }
        else
        {
            return new String[] {};
        }
    }

    public String[] getValidScopes()
    {
        ValidScope valid =
            (ValidScope)getToolClass().getAnnotation(ValidScope.class);
        if (valid != null)
        {
            return valid.value();
        }
        else
        {
            return new String[] {};
        }
    }

    private final Status getStatus()
    {
        if (this.status == null)
        {
            if (getClassname() == null)
            {
                this.status = Status.NONE;
            }
            else
            {
                try
                {
                    // make sure the classname resolves to a class we have
                    Class clazz = ClassUtils.getClass(getClassname());

                    // try hard to ensure we have all necessary supporting classes
                    digForDependencies(clazz);

                    // create an instance to make sure we can do that
                    if (factoryClassname == null)
                    {
                        clazz.newInstance();
                    }
                    else
                    {
                        Class factory = ClassUtils.getClass(getFactoryClassname());
                        Method factoryMethod = ClassUtils.findFactoryMethod(factory, clazz);
                        if (factoryMethod == null)
                        {
                            String target = clazz.getSimpleName();
                            throw new IllegalArgumentException("Factory class hasn't any method named create" + target + "(), new" + target + "() or get" + target + "()");
                        }
                        Object instance = factoryMethod.invoke(null, new Object[]{});
                        if (instance == null)
                        {
                            throw new WrongMethodTypeException("Factory method '" + factoryMethod.toString() + "' returned null");
                        }
                        Class instanceClass = instance.getClass();
                        if (!clazz.isAssignableFrom(instanceClass))
                        {
                            throw new WrongMethodTypeException("Factory method '" + factoryMethod + "' is expected to return an instance of class '" + getClassname() + "'");
                        }
                    }

                    this.status = Status.VALID;
                    this.problem = null;
                }
                catch (ClassNotFoundException cnfe)
                {
                    this.status = Status.MISSING;
                    this.problem = cnfe;
                }
                catch (NoClassDefFoundError ncdfe)
                {
                    this.status = Status.UNSUPPORTED;
                    this.problem = ncdfe;
                }
                catch (Throwable t)
                {
                    // for all other problems...
                    this.status = Status.UNINSTANTIABLE;
                    this.problem = t;
                }
            }
        }
        return this.status;
    }

    private void digForDependencies(Class clazz)
    {
        clazz.getDeclaredMethods();
        clazz.getDeclaredFields();

        Class superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            digForDependencies(superClass);
        }
    }

    public String getRestrictTo()
    {
        return this.restrictTo;
    }

    public Boolean getSkipSetters()
    {
        return this.skipSetters;
    }

    public ToolInfo createInfo()
    {
        ToolInfo info = null;
        Status status = getStatus();
        switch (status)
        {
            case VALID:
                info = new ToolInfo(getKey(), getToolClass(), getFactory());
                break;
            default:
                throw problem == null ?
                    new ConfigurationException(this, getError(status)) : 
                    new ConfigurationException(this, getError(status), problem);
        }

        info.restrictTo(getRestrictTo());
        if (getSkipSetters() != null)
        {
            info.setSkipSetters(getSkipSetters());
        }
        // it's ok to use this here, because we know it's the
        // first time properties have been added to this ToolInfo
        Map<String,Object> properties = getPropertyMap();
        // make sure key is present in properties, even for default properties
        properties.put(ToolContext.TOOLKEY_KEY, getKey());
        info.addProperties(properties);
        return info;
    }

    private final String getError(Status status)
    {
        switch (status)
        {
            case NONE:
                return "No classname set for: "+this;
            case MISSING:
                return "Couldn't find tool class in the classpath for: "+this+
                       "("+this.problem+")";
            case UNSUPPORTED:
                return "Couldn't find necessary supporting classes for: "+this+
                       "("+this.problem+")";
            case UNINSTANTIABLE:
                return "Couldn't instantiate instance of tool for: "+this+
                       "("+this.problem+")";
            default:
                return "";
        }
    }

    @Override
    public void addConfiguration(Configuration config)
    {
        // copy properties
        super.addConfiguration(config);

        // copy values specific to tool configs
        if (config instanceof ToolConfiguration)
        {
            ToolConfiguration that = (ToolConfiguration)config;
            if (that.getClassname() != null)
            {
                setClassname(that.getClassname());
            }
            if (that.getRestrictTo() != null)
            {
                setRestrictTo(that.getRestrictTo());
            }
        }
    }

    @Override
    public void validate()
    {
        super.validate();
        
        // make sure the key is not null
        if (getKey() == null)
        {
            throw new NullKeyException(this);
        }

        Status status = getStatus();
        switch (status)
        {
            case VALID:
                break;
            default:
                throw new ConfigurationException(this, getError(status));
        }
    }

    @Override
    public int compareTo(Configuration conf)
    {
        if (!(conf instanceof ToolConfiguration))
        {
            throw new UnsupportedOperationException("ToolConfigurations can only be compared to other ToolConfigurations");
        }

        ToolConfiguration tool = (ToolConfiguration)conf;
        if (getKey() == null && tool.getKey() == null)
        {
            return 0;
        }
        else if (getKey() == null)
        {
            return -1;
        }
        else if (tool.getKey() == null)
        {
            return 1;
        }
        else
        {
            return getKey().compareTo(tool.getKey());
        }
    }

    @Override
    public int hashCode()
    {
        if (getKey() == null)
        {
            return super.hashCode();
        }
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (getKey() == null || !(obj instanceof ToolConfiguration))
        {
            return super.equals(obj);
        }
        return getKey().equals(((ToolConfiguration)obj).getKey());
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        if (getClassname() == null)
        {
            out.append("Tool '");
            out.append(this.key);
        }
        else
        {
            if (status == null)
            {
                // toString() sbould not change the status
                out.append("Unchecked ");
            }
            else
            {
                switch (getStatus())
                {
                    case VALID:
                        break;
                    case NONE:
                    case MISSING:
                        out.append("Invalid ");
                        break;
                    case UNSUPPORTED:
                        out.append("Unsupported ");
                        break;
                    case UNINSTANTIABLE:
                        out.append("Unusable ");
                        break;
                    default:
                        break;
                }
            }
            out.append("Tool '");
            out.append(getKey());
        }
        out.append("' ");
        out.append("=> ");
        out.append(getClassname());
        if (getRestrictTo() != null)
        {
            out.append(" only for '");
            out.append(getRestrictTo());
            out.append('\'');
        }
        out.append(" ");
        appendProperties(out);
        return out.toString();
    }

}

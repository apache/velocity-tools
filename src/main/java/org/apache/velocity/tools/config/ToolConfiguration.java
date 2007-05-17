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

import java.lang.reflect.Method;
import java.util.Map;
import org.apache.velocity.tools.OldToolInfo;
import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.Utils;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: ToolConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolConfiguration extends Configuration
{
    private String key;
    private String classname;
    private String restrictTo;

    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * This doesn't take a {@link Class} parameter because
     * this class was not created for all-java configuration.
     */
    public void setClass(Class clazz)
    {
        this.classname = clazz.getName();
    }

    public void setClassname(String classname)
    {
        this.classname = classname;
    }

    public void setRestrictTo(String path)
    {
        this.restrictTo = path;
    }

    public String getKey()
    {
        if (this.key != null)
        {
            return this.key;
        }

        if (getClassname() != null)
        {
            DefaultKey defaultKey = 
                (DefaultKey)getToolClass().getAnnotation(DefaultKey.class);
            if (defaultKey != null)
            {
                return defaultKey.value();
            }
        }
        return null;
    }

    public String getClassname()
    {
        return this.classname;
    }

    public Class getToolClass()
    {
        try
        {
            return Utils.getClass(getClassname());
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

    private enum Status {
        VALID, OLD, NONE, MISSING, UNSUPPORTED;
    }

    private final Status getStatus()
    {
        if (getClassname() == null)
        {
            return Status.NONE;
        }

        // check for mere presence of init() or configure()
        try
        {
            Class clazz = Utils.getClass(getClassname());

            Method init = clazz.getMethod("init", new Class[]{ Object.class });

            // if init is deprecated, then we'll consider it a
            // new tool with BC support, not an old tool
            Deprecated bc = init.getAnnotation(Deprecated.class);
            if (bc == null)
            {
                return Status.OLD;
            }
        }
        catch (NoSuchMethodException nsme)
        {
            // ignore this
        }
        catch (ClassNotFoundException cnfe)
        {
            return Status.MISSING;
        }
        catch (NoClassDefFoundError ncdfe)
        {
            return Status.UNSUPPORTED;
        }
        return Status.VALID;
    }

    public String getRestrictTo()
    {
        return this.restrictTo;
    }

    public ToolInfo createInfo()
    {
        ToolInfo info = null;
        Status status = getStatus();
        switch (status)
        {
            case VALID:
                info = new ToolInfo(getKey(), getToolClass());
                break;
            case OLD:
                info = new OldToolInfo(getKey(), getToolClass());
                break;
            default:
                throw new ConfigurationException(this, getError(status));
        }

        info.restrictTo(getRestrictTo());
        // it's ok to use this here, because we know it's the
        // first time properties have been added to this ToolInfo
        info.addProperties(getProperties());
        return info;
    }

    private final String getError(Status status)
    {
        switch (status)
        {
            case NONE:
                return "No classname set for: "+this;
            case MISSING:
                return "Couldn't find tool class in the classpath for: "+this;
            case UNSUPPORTED:
                return "Couldn't find necessary supporting classes for: "+this;
            default:
                return "";
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
            case OLD:
                break;
            default:
                throw new ConfigurationException(this, getError(status));
        }
    }

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
            switch (getStatus())
            {
                case VALID:
                    break;
                case OLD:
                    out.append("Old ");
                    break;
                case NONE:
                case MISSING:
                    out.append("Invalid ");
                    break;
                case UNSUPPORTED:
                    out.append("Unsupported ");
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

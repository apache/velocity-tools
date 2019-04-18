package org.apache.velocity.tools.model.config;
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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.util.ExtProperties;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.Map;

public class ConfigHelper
{
    public ConfigHelper()
    {
        this(null);
    }

    public ConfigHelper(Map<String, Object> values)
    {
        if (values != null)
        {
            config = new ValueParser() {{ setReadOnly(false); }};
            Object velocityObject = values.get(ToolContext.ENGINE_KEY);
            if (velocityObject != null && velocityObject instanceof VelocityEngine)
            {
                velocityEngine = (VelocityEngine)velocityObject;
                // hack to get access to underlying RuntimeInstance,
                // while waiting for a way to get subproperties as a whole
                // from the engine
                try
                {
                    Field ri = VelocityEngine.class.getDeclaredField("ri");
                    ri.setAccessible(true);
                    RuntimeInstance runtimeInstance = (RuntimeInstance)ri.get(velocityEngine);
                    ExtProperties velModelProps = runtimeInstance.getConfiguration().subset("model");
                    if (velModelProps != null)
                    {
                        config.putAll(velModelProps);
                    }
                }
                catch (IllegalAccessException | NoSuchFieldException e)
                {
                    throw new ConfigurationException("unable to access model properties in velocity engine");
                }
            }
            config.putAll(values);
        }
        else
        {
            config = new ValueParser();
        }
    }

    public Object get(String key, Object defaultValue)
    {
        Object value = config.get(key);
        return value == null ? defaultValue : value;
    }

    public Object get(String key)
    {
        return get(key, null);
    }

    public String getString(String key, String defaultValue)
    {
        return (String)get(key, defaultValue);
    }

    public String getString(String key)
    {
        return getString(key, null);
    }

    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        Object b = get(key, defaultValue);
        if (b instanceof Boolean)
        {
            return (Boolean)b;
        }
        else if (b instanceof String)
        {
            return BooleanUtils.toBoolean((String)b);
        }
        else
        {
            return defaultValue;
        }
    }

    public Boolean getBoolean(String key)
    {
        return getBoolean(key, null);
    }

    public ValueParser getSubProperties(String key)
    {
        return config.getSubkey(key);
    }

    public <T extends Enum<T>> T getEnum(String key, Enum<T> defaultValue) throws IllegalArgumentException
    {
        Object value = get(key, defaultValue);
        if (value == null)
        {
            return null;
        }
        else if (defaultValue.getClass().isAssignableFrom(value.getClass()))
        {
            return (T)value;
        }
        else if (value instanceof String)
        {
            return (T)Enum.valueOf(defaultValue.getClass(), String.valueOf(value).toUpperCase(Locale.ROOT));
        }
        else
        {
            return (T)defaultValue;
        }
    }

    public URL findURL(String path)
    {
        URL url = null;
        boolean webContext = false;
        if (config != null)
        {
            // check if we're a view tool:
            // 1) we must find the  ServletContext and ServletUtils classes
            // 2) we must have a servletContext
            Class servletContextClass = null;
            Class servletUtilsClass = null;
            try
            {
                servletContextClass = Class.forName("javax.servlet.ServletContext");
                servletUtilsClass = Class.forName("org.apache.velocity.tools.view.ServletUtils");
            }
            catch (ClassNotFoundException cnfe)
            {
            }
            if (servletContextClass != null && servletUtilsClass != null)
            {
                Object servletContext = get("servletContext");
                if (servletContext != null && servletContextClass.isAssignableFrom(servletContext.getClass()))
                {
                    webContext = true;
                    Method getURL = null;
                    try
                    {
                        getURL = servletUtilsClass.getMethod("getURL", String.class, servletContextClass);
                        // first try with a /WEB-INF/ prefix
                        if (!path.startsWith("/WEB-INF/"))
                        {
                            url = (URL)getURL.invoke(null, new Object[] { "/WEB-INF/" + path, servletContext });
                        }
                        // second try without
                        if (url == null)
                        {
                            url = (URL) getURL.invoke(null, new Object[] { path, servletContext });
                        }
                    }
                    catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
                    {
                        throw new ConfigurationException("could not get URL for path '" + path + "'", e);
                    }
                }
            }
        }
        if (!webContext)
        {
            // check class path
            url = ClassUtils.getResource(path, ConfigHelper.class);
            // check filesystem
            if (url == null)
            {
                try
                {
                    File defFile = new File(path);
                    if (fileExists(defFile))
                    {
                        url = defFile.toPath().toUri().toURL();
                    }
                    else throw new ConfigurationException("cannot find file '" + path + "'");
                }
                catch (MalformedURLException mfue)
                {
                    throw new ConfigurationException("could not get URL for path '" + path + "'", mfue);
                }
            }
        }
        return url;
    }

    protected boolean fileExists(final File file)
    {
        boolean ret;
        if (System.getSecurityManager() != null)
        {
            ret = AccessController.doPrivileged(
                new PrivilegedAction<Boolean>()
                {
                    @Override
                    public Boolean run()
                    {
                        return file.exists();
                    }
                });
        }
        else
        {
            ret = file.exists();
        }
        return ret;
    }

    public VelocityEngine getVelocityEngine()
    {
        return velocityEngine;
    }

    public VelocityEngine getVelocityEngine(VelocityEngine defaultEngine)
    {
        return velocityEngine == null ? defaultEngine : velocityEngine;
    }

    private VelocityEngine velocityEngine = null;

    private ValueParser config = null;
}

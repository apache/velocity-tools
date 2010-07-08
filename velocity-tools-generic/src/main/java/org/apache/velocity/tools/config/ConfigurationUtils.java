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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.ToolboxFactory;

/**
 * Utility methods for handling tool configurations.
 *
 * @author Nathan Bubna
 * @version $Id: ConfigurationUtils.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ConfigurationUtils
{
    public static final String GENERIC_DEFAULTS_PATH =
        "/org/apache/velocity/tools/generic/tools.xml";
    public static final String VIEW_DEFAULTS_PATH =
        "/org/apache/velocity/tools/view/tools.xml";
    public static final String STRUTS_DEFAULTS_PATH =
        "/org/apache/velocity/tools/struts/tools.xml";

    public static final String AUTOLOADED_XML_PATH = "tools.xml";
    public static final String AUTOLOADED_PROPS_PATH = "tools.properties";

    public static final String SYSTEM_PROPERTY_KEY =
        "org.apache.velocity.tools";
    public static final ConfigurationUtils INSTANCE = new ConfigurationUtils();

    private ConfigurationUtils() {}

    public ConfigurationUtils getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns the "default" {@link FactoryConfiguration}.  This includes
     * all the standard tools developed by this project and available in
     * the jar being used. In other words, if the velocity-tools-generic-2.x.jar
     * is being used, then only the generic tools will be included.  If
     * the velocity-tools-struts-2.x.jar is being used, then all VelocityTools
     * will be available.  This also means that subclasses in the larger jars
     * will override their superclasses.  So, if you are using the VelocityStruts
     * jar, then your $link reference will be a StrutsLinkTool.  If you are using
     * the VelocityView jar, it will be a standard LinkTool.
     */
    public static FactoryConfiguration getDefaultTools()
    {
        FileFactoryConfiguration config =
            new XmlFactoryConfiguration("ConfigurationUtils.getDefaultTools()");
        config.read(GENERIC_DEFAULTS_PATH);

        // view tools and struts tools may not be available
        config.read(VIEW_DEFAULTS_PATH, false);
        config.read(STRUTS_DEFAULTS_PATH, false);

        // defaults should *always* be clean!
        clean(config);
        return config;
    }

    /**
     * Returns a {@link FactoryConfiguration} including all default
     * "GenericTools" available and no others.
     */
    public static FactoryConfiguration getGenericTools()
    {
        FileFactoryConfiguration config =
            new XmlFactoryConfiguration("ConfigurationUtils.getGenericTools()");
        config.read(GENERIC_DEFAULTS_PATH);

        // defaults should *always* be clean!
        clean(config);
        return config;
    }

    /**
     * Returns a {@link FactoryConfiguration} including all default
     * "VelocityView" tools available as well as the default "GenericTools".
     * @throws {@link ConfigurationException} if a tools.xml is not found
     *   at the {@link #VIEW_DEFAULTS_PATH}.
     */
    public static FactoryConfiguration getVelocityView()
    {
        FileFactoryConfiguration config =
            new XmlFactoryConfiguration("ConfigurationUtils.getVelocityView()");
        config.read(GENERIC_DEFAULTS_PATH);
        config.read(VIEW_DEFAULTS_PATH);

        // defaults should *always* be clean!
        clean(config);
        return config;
    }

    /**
     * Returns a {@link FactoryConfiguration} including all default
     * "VelocityStruts" tools available as well as the default "VelocityView"
     * tools and "GenericTools".
     * @throws {@link ConfigurationException} if a tools.xml is not found
     *   at the {@link #VIEW_DEFAULTS_PATH} or {@link #STRUTS_DEFAULTS_PATH}.
     */
    public static FactoryConfiguration getVelocityStruts()
    {
        FileFactoryConfiguration config =
            new XmlFactoryConfiguration("ConfigurationUtils.getVelocityStruts()");
        config.read(GENERIC_DEFAULTS_PATH);
        config.read(VIEW_DEFAULTS_PATH);
        config.read(STRUTS_DEFAULTS_PATH);

        // defaults should *always* be clean!
        clean(config);
        return config;
    }

    /**
     * Returns a {@link FactoryConfiguration} including all 
     * {@link #getDefaultTools()} as well as any tools that can be
     * automatically loaded from "tools.xml" or "tools.properties" found
     * at the root of the classpath or in the current directory.
     *
     * @see #getAutoLoaded(boolean includeDefaults)
     */
    public static FactoryConfiguration getAutoLoaded()
    {
        return getAutoLoaded(true);
    }

    /**
     * Returns a {@link FactoryConfiguration} composed, in order of the
     * following configurations:
     * <ul>
     *   <li>{@link #getDefaultTools()} (only if includeDefaults is {@code true})</li>
     *   <li>All "tools.xml" configurations found in the classpath root, in the order found</li>
     *   <li>All "tools.properties" configurations found in the classpath root, in the order found</li>
     *   <li>The "tools.xml" file in the current directory (if any)</li>
     *   <li>The "tools.properties" file in the current directory (if any)</li>
     * </ul>
     * If the includeDefaults parameter is null and no such files described above
     * can be found, then the configuration returned by this method will be
     * empty, but it should never be {@code null}.
     */
    public static FactoryConfiguration getAutoLoaded(boolean includeDefaults)
    {
        FactoryConfiguration auto;
        if (includeDefaults)
        {
            // start with the available defaults
            auto = getDefaultTools();
        }
        else
        {
            // start out blank
            auto = new FactoryConfiguration("ConfigurationUtils.getAutoLoaded(false)");
        }

        //TODO: look for any Tools classes in the root of the classpath

        // look for all tools.xml in the classpath
        FactoryConfiguration cpXml = findInClasspath(AUTOLOADED_XML_PATH);
        if (cpXml != null)
        {
            auto.addConfiguration(cpXml);
        }

        // look for all tools.properties in the classpath
        FactoryConfiguration cpProps = findInClasspath(AUTOLOADED_PROPS_PATH);
        if (cpProps != null)
        {
            auto.addConfiguration(cpProps);
        }

        // look for tools.xml in the current file system
        FactoryConfiguration fsXml = findInFileSystem(AUTOLOADED_XML_PATH);
        if (fsXml != null)
        {
            auto.addConfiguration(fsXml);
        }

        // look for tools.properties in the file system
        FactoryConfiguration fsProps = findInFileSystem(AUTOLOADED_PROPS_PATH);
        if (fsProps != null)
        {
            auto.addConfiguration(fsProps);
        }

        // return the config we've accumulated
        return auto;
    }

    /**
     * Returns a {@link FactoryConfiguration} loaded from the path specified
     * in the "org.apache.velocity.tools" system property (if any).
     * If no such property has been set {@code null} will be returned.
     * @throws ResourceNotFoundException if the system property has a value
     *         but no configuration file was found at the specified location
     */
    public static FactoryConfiguration findFromSystemProperty()
    {
        String path = System.getProperty(SYSTEM_PROPERTY_KEY);
        if (path == null || path.length() == 0)
        {
            return null;
        }
        return load(path);
    }

    /**
     * Returns a new, standard {@link ToolboxFactory} configured
     * with the results of both {@link #getAutoLoaded()} and
     * {@link #findFromSystemProperty()}.
     */
    public static ToolboxFactory createFactory()
    {
        // get the automatically loaded config(s)
        FactoryConfiguration auto = getAutoLoaded();

        // include any config specified via system property
        FactoryConfiguration sys = findFromSystemProperty();
        if (sys != null)
        {
            auto.addConfiguration(sys);
        }

        ToolboxFactory factory = new ToolboxFactory();
        factory.configure(auto);
        return factory;
    }

    /**
     * Convenience method that automatically creates a new
     * {@link ConfigurationCleaner} and applies it to the specified
     * {@link Configuration}.
     */
    public static void clean(Configuration config)
    {
        // since most config will happen at startup and a cleaner
        // is not otherwise necessary, don't keep one of these statically
        ConfigurationCleaner cleaner = new ConfigurationCleaner();
        cleaner.clean(config);
    }

    /**
     * Returns a {@link FactoryConfiguration} loaded from a configuration file
     * at the specified path.  If no such file is found at that path, this
     * will throw a {@link ResourceNotFoundException}.
     *
     * @see #find(String path)
     */
    public static FactoryConfiguration load(String path)
    {
        FactoryConfiguration config = find(path);
        if (config == null)
        {
            throw new ResourceNotFoundException("Could not find configuration at "+path);
        }
        return config;
    }

    /**
     * Searches for a configuration file at the specified path and returns
     * it in the form of a {@link FactoryConfiguration}.  This method will
     * look for a matching file in both the classpath and the file system.
     * If perchance a match is found in both, then both are loaded and the
     * configuration loaded from the file system is given precedence (i.e.
     * it is added onto the other).  If no match is found in either, then
     * this will return {@code null}.
     */
    public static FactoryConfiguration find(String path)
    {
        FactoryConfiguration cp = findInClasspath(path);
        FactoryConfiguration fs = findInFileSystem(path);
        if (cp != null)
        {
            if (fs != null)
            {
                cp.addConfiguration(fs);
            }
            return cp;
        }
        else
        {
            return fs;
        }
    }

    /**
     * Searches the file system for a configuration file matching the
     * specified path.  If found, it will read and return it as a
     * {@link FactoryConfiguration}.  If not found, this will return
     * {@code null}.
     * @throws IllegalStateException if the file exists, but its path could not be converted to a URL for reading.
     */
    public static FactoryConfiguration findInFileSystem(String path)
    {
        File file = new File(path);
        if (file.exists())
        {
            try
            {
                return read(file.toURL());
            }
            catch (MalformedURLException mue)
            {
                throw new IllegalStateException("Could not convert existing file path \""+path+"\" to URL", mue);
            }
        }
        return null;
    }

    /**
     * @see #findInClasspath(String path, Object caller)
     */
    public static FactoryConfiguration findInClasspath(String path)
    {
        // pretend this was called by a non-static ConfigurationUtils
        return findInClasspath(path, new ConfigurationUtils());
    }

    /**
     * Searches the classpath for a configuration file matching the
     * specified path.  If found, it will read and return it as a
     * {@link FactoryConfiguration}.  If not found, this will return
     * {@code null}.  If there are multiple matching resources in the
     * classpath, then they will be added together in the order found
     * (i.e. the last one will have precedence).
     * @see ClassUtils#getResources(String path, Object caller)
     */
    public static FactoryConfiguration findInClasspath(String path, Object caller)
    {
        // find all resources at this path
        List<URL> found = ClassUtils.getResources(path, caller);
        if (found.isEmpty())
        {
            return null;
        }
        else if (found.size() == 1)
        {
            // load and return the one config at that URL (if any)
            return read(found.get(0));
        }
        else
        {
            // create a base config to combine the others into
            FactoryConfiguration config =
                new FactoryConfiguration("ConfigurationUtils.findInClassPath("+path+","+caller+")");
            boolean readAConfig = false;
            for (URL resource : found)
            {
                FactoryConfiguration c = read(resource);
                if (c != null)
                {
                    readAConfig = true;
                    config.addConfiguration(c);
                }
            }
            // only return a configuration if we really found one
            if (readAConfig)
            {
                return config;
            }
            else
            {
                return null;
            }
        }
    }


    /**
     * Returns a {@link FactoryConfiguration} read from a known configuration
     * file type at the specified {@link URL}.  If the file is missing or unreadable,
     * this will simply return {@code null} (i.e. if an IOException is thrown).
     * @throws UnsupportedOperationException if the file type (identified via suffix)
     *         is neither ".xml" or ".properties"
     */
    public static FactoryConfiguration read(URL url)
    {
        FileFactoryConfiguration config = null;
        String path = url.toString();
        String source = "ConfigurationUtils.read("+url.toString()+")";
        if (path.endsWith(".xml"))
        {
            config = new XmlFactoryConfiguration(source);
        }
        else if (path.endsWith(".properties"))
        {
            config = new PropertiesFactoryConfiguration(source);
        }
        else if (path.endsWith(".class"))
        {
            // convert the url to a FQN
            String fqn = path.substring(0, path.indexOf('.')).replace('/','.');
            // retrieve a configuration from that class
            return getFromClass(fqn);
        }
        else
        {
            String msg = "Unknown configuration file type: " + url.toString() +
                         "\nOnly .xml and .properties configuration files are supported at this time.";
            throw new UnsupportedOperationException(msg);
        }

        // now, try to read the file
        InputStream inputStream = null;
        try
        {
            inputStream = url.openStream();
            config.read(inputStream);
        }
        catch (IOException ioe)
        {
            return null;
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException ioe)
                {
                    throw new RuntimeException("Could not close input stream for "+path, ioe);
                }
            }
        }
        return config;
    }

    public static FactoryConfiguration getFromClass(String classname)
    {
        try
        {
            Class configFactory = ClassUtils.getClass(classname);
            return getFromClass(configFactory);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IllegalArgumentException("Could not find class "+classname, cnfe);
        }
    }

    public static final String CONFIG_FACTORY_METHOD = "getConfiguration";
    public static FactoryConfiguration getFromClass(Class factory)
    {
        //TODO: look for a getConfiguration() method
        Method getConf = null;
        try
        {
            // check for a public setup(Map) method first
            getConf = factory.getMethod(CONFIG_FACTORY_METHOD, (Class[])null);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new IllegalArgumentException("Could not find "+CONFIG_FACTORY_METHOD+" in class "+factory.getName(), nsme);
        }

        // get an instance if the method is not static
        Object instance = null;
        if (!Modifier.isStatic(getConf.getModifiers()))
        {
            try
            {
                instance = factory.newInstance();
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(factory.getName()+" must have usable default constructor or else "+CONFIG_FACTORY_METHOD+" must be declared static", e);
            }
        }

        // invoke the method
        try
        {
            FactoryConfiguration result =
                (FactoryConfiguration)getConf.invoke(instance, (Object[])null);
            if (result == null)
            {
                throw new IllegalArgumentException("Method "+CONFIG_FACTORY_METHOD+" in class "+factory.getName()+" should not return null or void");
            }
            else
            {
                return result;
            }
        }
        catch (IllegalAccessException iae)
        {
            throw new IllegalArgumentException("Failed to invoke "+CONFIG_FACTORY_METHOD+" in class "+factory.getName(), iae);
        }
        catch (IllegalArgumentException iae)
        {
            // if this happens, it's more a problem w/this code than the users
            throw iae;
        }
        catch (InvocationTargetException ite)
        {
            // if this happens, it's all their issue
            throw new IllegalArgumentException("There was an exception while executing "+CONFIG_FACTORY_METHOD+" in class "+factory.getName(), ite.getCause());
        }
    }

}

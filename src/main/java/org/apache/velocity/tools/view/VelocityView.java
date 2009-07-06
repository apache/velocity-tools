package org.apache.velocity.tools.view;

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
import java.io.Writer;
import java.util.List;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.generic.log.LogChuteCommonsLog;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.ToolboxFactory;
import org.apache.velocity.tools.config.ConfigurationCleaner;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.util.SimplePool;

/**
 * <p>The class provides the following features:</p>
 * <ul>
 *   <li>renders Velocity templates</li>
 *   <li>provides support for an auto-loaded, configurable toolbox</li>
 *   <li>provides transparent access to the servlet request attributes,
 *       servlet session attributes and servlet context attributes by
 *       auto-searching them</li>
 *   <li>logs to the logging facility of the servlet API</li>
 * </ul>
 *
 * <p>VelocityView supports the following configuration parameters
 * in web.xml:</p>
 * <dl>
 *   <dt>org.apache.velocity.tools</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, the servlet will check for a toolbox file at
 *     '/WEB-INF/tools.xml'.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity will check for a properties file at
 *     '/WEB-INF/velocity.properties'.  If no file is found there, then
 *     Velocity is initialized with the settings in the classpath at
 *     'org.apache.velocity.tools.view.velocity.properties'.</dd>
 * </dl>
 *
 * @author Dave Bryson
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:kjohnson@transparent.com">Kent Johnson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 *
 * @version $Id: VelocityView.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class VelocityView extends ViewToolManager
{
    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE_KEY = "default.contentType";

    /**
     * Key used to access the ServletContext in
     * the Velocity application attributes.
     */
    public static final String SERVLET_CONTEXT_KEY =
        ServletContext.class.getName();

    /** The default content type for the response */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** Default encoding for the output stream */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * Key used to access the toolbox configuration file path from the
     * Servlet or webapp init parameters ("org.apache.velocity.tools")
     * or to access a live {@link FactoryConfiguration} previously
     * placed in the ServletContext attributes.
     */
    public static final String TOOLS_KEY = ServletUtils.CONFIGURATION_KEY;
    @Deprecated
    public static final String DEPRECATED_TOOLS_KEY =
        "org.apache.velocity.toolbox";

    /**
     * Default toolbox configuration file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    public static final String USER_TOOLS_PATH =
        "/WEB-INF/tools.xml";
    @Deprecated
    public static final String DEPRECATED_USER_TOOLS_PATH =
        "/WEB-INF/toolbox.xml";

    /**
     * Default Runtime properties.
     */
    public static final String DEFAULT_PROPERTIES_PATH =
        "/org/apache/velocity/tools/view/velocity.properties";

    /**
     * This is the string that is looked for when getInitParameter is
     * called ("org.apache.velocity.properties").
     */
    public static final String PROPERTIES_KEY =
        "org.apache.velocity.properties";

    /**
     * Default velocity properties file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    public  static final String USER_PROPERTIES_PATH =
        "/WEB-INF/velocity.properties";

    /**
     * Controls loading of available default tool configurations
     * provided by VelocityTools.  The default behavior is conditional;
     * if {@link #DEPRECATION_SUPPORT_MODE_KEY} has not been set to
     * {@code false} and there is an old {@code toolbox.xml} configuration
     * present, then the defaults will not be loaded unless you explicitly
     * set this property to {@code true} in your init params.  If there
     * is no {@code toolbox.xml} and/or the deprecation support is turned off,
     * then the default tools will be loaded automatically unless you
     * explicitly set this property to {@code false} in your init params.
     */
    public static final String LOAD_DEFAULTS_KEY =
        "org.apache.velocity.tools.loadDefaults";

    /**
     * Controls removal of tools or data with invalid configurations
     * before initialization is finished.
     * The default is false; set to {@code true} to turn this feature on.
     */
    public static final String CLEAN_CONFIGURATION_KEY =
        "org.apache.velocity.tools.cleanConfiguration";

    /**
     * Controls whether or not templates can overwrite tool and servlet API
     * variables in the local context. The default is true; set to {@code false}
     * to prevent overwriting of any tool variables.
     */
    public static final String USER_OVERWRITE_KEY =
        "org.apache.velocity.tools.userCanOverwriteTools";

    /**
     * Controls support for deprecated tools and configuration.
     * The default is {@code true}; set to {@code false} to turn off
     * support for deprecated tools and configuration.
     */
    public static final String DEPRECATION_SUPPORT_MODE_KEY =
        "org.apache.velocity.tools.deprecationSupportMode";


    private static SimplePool writerPool = new SimplePool(40);
    private String defaultContentType = DEFAULT_CONTENT_TYPE;
    private boolean deprecationSupportMode = true;

    public VelocityView(ServletConfig config)
    {
        this(new JeeServletConfig(config));
    }

    public VelocityView(FilterConfig config)
    {
        this(new JeeFilterConfig(config));
    }

    public VelocityView(ServletContext context)
    {
        this(new JeeContextConfig(context));
    }

    public VelocityView(JeeConfig config)
    {
        // suppress auto-config, as we have our own config lookup order here
        super(config.getServletContext(), false, false);

        init(config);
    }

    @Deprecated
    protected final void setDeprecationSupportMode(boolean support)
    {
        if (deprecationSupportMode != support)
        {
            this.deprecationSupportMode = support;
            debug("deprecationSupportMode is now %s", (support ? "on" : "off"));
        }
    }

    /**
     * Overrides super class to ensure engine is not set to null.
     */
    @Override
    public void setVelocityEngine(VelocityEngine engine)
    {
        if (engine == null)
        {
            throw new NullPointerException("VelocityEngine cannot be null");
        }
        super.setVelocityEngine(engine);
    }

    /**
     * Returns the configured default Content-Type.
     */
    public String getDefaultContentType()
    {
        return this.defaultContentType;
    }

    /**
     * Sets the configured default Content-Type.
     */
    public void setDefaultContentType(String type)
    {
        if (!defaultContentType.equals(type))
        {
            this.defaultContentType = type;
            debug("Default Content-Type was changed to %s", type);
        }
    }

    /**
     * Simplifies process of getting a property from VelocityEngine,
     * because the VelocityEngine interface sucks compared to the singleton's.
     * Use of this method assumes that {@link #init(JeeConfig,VelocityEngine)}
     * has already been called.
     */
    protected String getProperty(String key, String alternate)
    {
        String prop = (String)velocity.getProperty(key);
        if (prop == null || prop.length() == 0)
        {
            return alternate;
        }
        return prop;
    }


    /**
     * <p>Initializes ToolboxFactory, VelocityEngine, and sets default
     * encoding for processing requests.</p>
     *
     * <p>NOTE: If no charset is specified in the default.contentType
     * property (in your velocity.properties) and you have specified
     * an output.encoding property, then that will be used as the
     * charset for the default content-type of pages served by this
     * servlet.</p>
     *
     * @param config servlet configuation
     */
    protected void init(JeeConfig config)
    {
        // create an engine if none is set yet
        // (servletContext and factory should already be set by now
        if (this.velocity == null)
        {
            this.velocity = new VelocityEngine();
        }

        // default is true for these, so just watch for false
        String depMode = config.findInitParameter(DEPRECATION_SUPPORT_MODE_KEY);
        if (depMode != null && depMode.equalsIgnoreCase("false"))
        {
            setDeprecationSupportMode(false);
        }
        String allowOverwrite = config.findInitParameter(USER_OVERWRITE_KEY);
        if (allowOverwrite != null && allowOverwrite.equalsIgnoreCase("false"))
        {
            setUserCanOverwriteTools(false);
        }

        // configure and initialize the VelocityEngine
        init(config, velocity);

        // configure the ToolboxFactory
        configure(config, factory);

        // set encoding & content-type
        setEncoding(config);
    }

    /**
     * Initializes the Velocity runtime, first calling
     * loadConfiguration(JeeConfig) to get a
     * org.apache.commons.collections.ExtendedProperties
     * of configuration information
     * and then calling velocityEngine.init().  Override this
     * to do anything to the environment before the
     * initialization of the singleton takes place, or to
     * initialize the singleton in other ways.
     *
     * @param config servlet configuration parameters
     */
    protected void init(JeeConfig config, final VelocityEngine velocity)
    {
        // register this engine to be the default handler of log messages
        // if the user points commons-logging to the LogSystemCommonsLog
        LogChuteCommonsLog.setVelocityLog(getLog());

        // put the servlet context into Velocity's application attributes,
        // where the WebappResourceLoader can find them
        velocity.setApplicationAttribute(SERVLET_CONTEXT_KEY, this.servletContext);

        // configure the engine itself
        configure(config, velocity);

        // now all is ready - init Velocity
        try
        {
            velocity.init();
        }
        catch(Exception e)
        {
            String msg = "Could not initialize VelocityEngine";
            getLog().error(msg, e);
            e.printStackTrace();
            throw new RuntimeException(msg + ": "+e, e);
        }
    }

    protected void configure(final JeeConfig config, final VelocityEngine velocity)
    {
        // first get the default properties, and bail if we don't find them
	ExtendedProperties defaultProperties = getProperties(DEFAULT_PROPERTIES_PATH, true);
	// if using Velocity engine prior to 1.6.x, remove WebappUberspector
	// (this hack will disappear once tools require Velocity 1.6.x+)
	try {
	    Class.forName("org.apache.velocity.tools.view.WebappUberspector");
	} catch(Throwable t) {
	    // remove WebappUberspector from the list of introspectors
	    List introspectors = defaultProperties.getList(VelocityEngine.UBERSPECT_CLASSNAME);
	    introspectors.remove("org.apache.velocity.tools.view.WebappUberspector");
	    defaultProperties.setProperty(VelocityEngine.UBERSPECT_CLASSNAME,introspectors);
	}
        velocity.setExtendedProperties(defaultProperties);

        // check for application-wide user props in the context init params
        String appPropsPath = servletContext.getInitParameter(PROPERTIES_KEY);
        setProps(velocity, appPropsPath, true);

        // check for servlet-wide user props in the config init params at the
        // conventional location, and be silent if they're missing
        setProps(velocity, USER_PROPERTIES_PATH, false);

        // check for a custom location for servlet-wide user props
        String servletPropsPath = config.getInitParameter(PROPERTIES_KEY);
        setProps(velocity, servletPropsPath, true);
    }

    private boolean setProps(VelocityEngine velocity, String path, boolean require)
    {
        if (path == null)
        {
            // only bother with this if a path was given
            return false;
        }

        // this will throw an exception if require is true and there
        // are no properties at the path.  if require is false, this
        // will return null when there's no properties at the path
        ExtendedProperties props = getProperties(path, require);
        if (props == null)
        {
            return false;
        }

        debug("Configuring Velocity with properties at: %s", path);

        // these props will override those already set
        velocity.setExtendedProperties(props);
        // notify that new props were set
        return true;
    }


    /**
     * Here's the configuration lookup/loading order:
     * <ol>
     * <li>If deprecationSupportMode is true:
     *   <ol>
     *   <li>Config file optionally specified by {@code org.apache.velocity.toolbox} init-param (servlet or servletContext)</li>
     *   <li>If none, config file optionally at {@code /WEB-INF/toolbox.xml} (deprecated conventional location)</li>
     *   </ol>
     * </li>
     * <li>If no old toolbox or loadDefaults is true, {@link ConfigurationUtils#getDefaultTools()}</li>
     * <li>{@link ConfigurationUtils#getAutoLoaded}(false)</li>
     * <li>Config file optionally specified by servletContext {@code org.apache.velocity.tools} init-param</li>
     * <li>Config file optionally at {@code /WEB-INF/tools.xml} (new conventional location)</li>
     * <li>Config file optionally specified by servlet {@code org.apache.velocity.tools} init-param</li>
     * </ol>
     * Remember that as these configurations are added on top of each other,
     * the newer values will always override the older ones.  Also, once they
     * are all loaded, this method can "clean" your configuration of all invalid
     * tool, toolbox or data configurations if you set the 
     * {@code org.apache.velocity.tools.cleanConfiguration} init-param to true in
     * either your servlet or servletContext init-params.
     */
    protected void configure(final JeeConfig config, final ToolboxFactory factory)
    {
        FactoryConfiguration factoryConfig = new FactoryConfiguration("VelocityView.configure(config,factory)");

        boolean hasOldToolbox = false;
        if (this.deprecationSupportMode)
        {
            FactoryConfiguration oldToolbox = getDeprecatedConfig(config);
            if (oldToolbox != null)
            {
                hasOldToolbox = true;
                factoryConfig.addConfiguration(oldToolbox);
            }
        }

        // only load the default tools if they have explicitly said to
        // or if they are not using an old toolbox and have said nothing
        String loadDefaults = config.findInitParameter(LOAD_DEFAULTS_KEY);
        if ((!hasOldToolbox && loadDefaults == null) ||
            "true".equalsIgnoreCase(loadDefaults))
        {
            // add all available default tools
            getLog().trace("Loading default tools configuration...");
            factoryConfig.addConfiguration(ConfigurationUtils.getDefaultTools());
        }
        else
        {
            // let the user know that the defaults were suppressed
            debug("Default tools configuration has been suppressed%s",
                  (hasOldToolbox ?
                   " to avoid conflicts with older application's context and toolbox definition." :
                   "."));
        }

        // this gets the auto loaded config from the classpath
        // this doesn't include defaults since they're handled already
        // and it could theoretically pick up an auto-loaded config from the
        // filesystem, but that is highly unlikely to happen in a webapp env
        FactoryConfiguration autoLoaded = ConfigurationUtils.getAutoLoaded(false);
        factoryConfig.addConfiguration(autoLoaded);

        // check for application-wide user config in the context init params
        String appToolsPath = servletContext.getInitParameter(TOOLS_KEY);
        setConfig(factoryConfig, appToolsPath, true);

        // check for user configuration at the conventional location,
        // and be silent if they're missing
        setConfig(factoryConfig, USER_TOOLS_PATH, false);

        // check for a custom location for servlet-wide user props
        String servletToolsPath = config.getInitParameter(TOOLS_KEY);
        setConfig(factoryConfig, servletToolsPath, true);

        // check for "injected" configuration in application attributes
        FactoryConfiguration injected = ServletUtils.getConfiguration(servletContext);
        if (injected != null)
        {
            debug("Adding configuration instance in servletContext attributes as '%s'", TOOLS_KEY);
            factoryConfig.addConfiguration(injected);
        }

        // see if we should only keep valid tools, data, and properties
        String cleanConfig = config.findInitParameter(CLEAN_CONFIGURATION_KEY);
        if ("true".equals(cleanConfig))
        {
            // remove invalid tools, data, and properties from the configuration
            ConfigurationCleaner cleaner = new ConfigurationCleaner();
            cleaner.setLog(getLog());
            cleaner.clean(factoryConfig);
        }

        // apply this configuration to the specified factory
        debug("Configuring factory with: %s", factoryConfig);
        configure(factoryConfig);
    }

    /**
     * First tries to find a path to a toolbox under the deprecated
     * {@code org.apache.velocity.toolbox} key.
     * If found, it tries to load the configuration there and will blow up
     * if there is no config file there.
     * If not found, it looks for a config file at /WEB-INF/toolbox.xml
     * (the deprecated default location) and tries to load it if found.
     */
    @Deprecated
    protected FactoryConfiguration getDeprecatedConfig(JeeConfig config)
    {
        FactoryConfiguration toolbox = null;

        // look for specified path under the deprecated toolbox key
        String oldPath = config.findInitParameter(DEPRECATED_TOOLS_KEY);
        if (oldPath != null)
        {
            // ok, they said the toolbox.xml should be there
            // so this should blow up if it is not
            toolbox = getConfiguration(oldPath, true);
        }
        else
        {
            // check for deprecated user configuration at the old conventional
            // location.  be silent if missing, log deprecation warning otherwise
            oldPath = DEPRECATED_USER_TOOLS_PATH;
            toolbox = getConfiguration(oldPath);
        }

        if (toolbox != null)
        {
            debug("Loaded deprecated configuration from: %s", oldPath);
            getLog().warn("Please upgrade to new \"/WEB-INF/tools.xml\" format and conventional location."+
                          " Support for \"/WEB-INF/toolbox.xml\" format and conventional file name will "+
                          "be removed in a future version.");
        }
        return toolbox;
    }

    private boolean setConfig(FactoryConfiguration factory, String path, boolean require)
    {
        if (path == null)
        {
            // only bother with this if a path was given
            return false;
        }

        // this will throw an exception if require is true and there
        // is no tool config at the path.  if require is false, this
        // will return null when there's no tool config at the path
        FactoryConfiguration config = getConfiguration(path, require);
        if (config == null)
        {
            return false;
        }

        debug("Loaded configuration from: %s", path);
        factory.addConfiguration(config);

        // notify that new config was added
        return true;
    }


    protected InputStream getInputStream(String path, boolean required)
    {
        // first, search the classpath
        InputStream inputStream = ServletUtils.getInputStream(path, this.servletContext);

        // if we didn't find one
        if (inputStream == null)
        {
            String msg = "Did not find resource at: "+path;
            if (required)
            {
                getLog().error(msg);
                throw new ResourceNotFoundException(msg);
            }
            debug(msg);
            return null;
        }
        return inputStream;
    }


    protected ExtendedProperties getProperties(String path)
    {
        return getProperties(path, false);
    }

    protected ExtendedProperties getProperties(String path, boolean required)
    {
        if (getLog().isTraceEnabled())
        {
            getLog().trace("Searching for properties at: "+path);
        }

        InputStream inputStream = getInputStream(path, required);
        if (inputStream == null)
        {
            return null;
        }

        ExtendedProperties properties = new ExtendedProperties();
        try
        {
            properties.load(inputStream);
        }
        catch (IOException ioe)
        {
            String msg = "Failed to load properties at: "+path;
            getLog().error(msg, ioe);
            if (required)
            {
                throw new RuntimeException(msg, ioe);
            }
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                getLog().error("Failed to close input stream for "+path, ioe);
            }
        }
        return properties;
    }


    protected FactoryConfiguration getConfiguration(String path)
    {
        return getConfiguration(path, false);
    }

    protected FactoryConfiguration getConfiguration(String path, boolean required)
    {
        if (getLog().isTraceEnabled())
        {
            getLog().trace("Searching for configuration at: "+path);
        }

        FactoryConfiguration config = null;
        try
        {
            config = ServletUtils.getConfiguration(path,
                                                   this.servletContext,
                                                   this.deprecationSupportMode);
            if (config == null)
            {
                String msg = "Did not find resource at: "+path;
                if (required)
                {
                    getLog().error(msg);
                    throw new ResourceNotFoundException(msg);
                }
                else
                {
                    debug(msg);
                }
            }
        }
        catch (ResourceNotFoundException rnfe)
        {
            // no need to re-log this
            throw rnfe;
        }
        catch (RuntimeException re)
        {
            if (required)
            {
                getLog().error(re.getMessage(), re);
                throw re;
            }
            getLog().debug(re.getMessage(), re);
        }
        return config;
    }


    protected void setEncoding(JeeConfig config)
    {
        // we can get these now that velocity is initialized
        this.defaultContentType =
            getProperty(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);

        String encoding = getProperty(RuntimeConstants.OUTPUT_ENCODING,
                                      DEFAULT_OUTPUT_ENCODING);

        // For non Latin-1 encodings, ensure that the charset is
        // included in the Content-Type header.
        if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding))
        {
            int index = defaultContentType.lastIndexOf("charset");
            if (index < 0)
            {
                // the charset specifier is not yet present in header.
                // append character encoding to default content-type
                this.defaultContentType += "; charset=" + encoding;
            }
            else
            {
                // The user may have configuration issues.
                getLog().info("Charset was already " +
                              "specified in the Content-Type property.  " +
                              "Output encoding property will be ignored.");
            }
        }

        debug("Default Content-Type is: %s", defaultContentType);
    }





    /******************* REQUEST PROCESSING ****************************/

    /**
     * 
     *
     * @param request  HttpServletRequest object containing client request
     * @param response HttpServletResponse object for the response
     * @return the {@link Context} prepared and used to perform the rendering
     *         to allow proper cleanup afterward
     */
    public Context render(HttpServletRequest request,
                          HttpServletResponse response) throws IOException
    {
        // then get a context
        Context context = createContext(request, response);

        // get the template
        Template template = getTemplate(request, response);

        // merge the template and context into the response
        merge(template, context, response.getWriter());

        return context;
    }

    public Context render(HttpServletRequest request, Writer out)
        throws IOException
    {
        // then get a context
        Context context = createContext(request, null);

        // get the template
        Template template = getTemplate(request);

        // merge the template and context into the writer
        merge(template, context, out);

        return context;
    }


    /**
     * <p>Creates and returns an initialized Velocity context.</p>
     *
     * A new context of class {@link ViewToolContext} is created and
     * initialized.
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    @Override
    public ViewToolContext createContext(HttpServletRequest request,
                                         HttpServletResponse response)
    {
        ViewToolContext ctx;
        if (this.deprecationSupportMode)
        {
            ctx = new ChainedContext(velocity, request, response, servletContext);
        }
        else
        {
            ctx = new ViewToolContext(velocity, request, response, servletContext);
        }
        prepareContext(ctx, request);
        return ctx;
    }


    /**
     * <p>Gets the requested template.</p>
     *
     * @param request client request
     * @return Velocity Template object or null
     */
    public Template getTemplate(HttpServletRequest request)
    {
        return getTemplate(request, null);
    }

    public Template getTemplate(HttpServletRequest request,
                                   HttpServletResponse response)
    {
        String path = ServletUtils.getPath(request);
        if (response == null)
        {
            return getTemplate(path);
        }
        else
        {
            return getTemplate(path, response.getCharacterEncoding());
        }
    }


    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     */
    public Template getTemplate(String name)
    {
        return getTemplate(name, null);
    }


    /**
     * Retrieves the requested template with the specified character encoding.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @param encoding the character encoding of the template
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     */
    public Template getTemplate(String name, String encoding)
    {
        try
        {
            if (encoding == null)
            {
                return velocity.getTemplate(name);
            }
            else
            {
                return velocity.getTemplate(name, encoding);
            }
        }
        catch (RuntimeException e)  // FIXME This is useless with Velocity 1.7
        {
            throw e;
        }
        catch (Exception e)  // FIXME This is useless with Velocity 1.7
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Merges the template with the context.  Only override this if you really, really
     * really need to. (And don't call us with questions if it breaks :)
     *
     * @param template template being rendered
     * @param context Context created by the {@link #createContext}
     * @param writer into which the content is rendered
     */
    public void merge(Template template, Context context, Writer writer)
        throws IOException
    {
        VelocityWriter vw = null;
        try
        {
            vw = (VelocityWriter)writerPool.get();
            if (vw == null)
            {
                vw = new VelocityWriter(writer, 4 * 1024, true);
            }
            else
            {
                vw.recycle(writer);
            }
            performMerge(template, context, vw);

            // flush writer but don't close to allow us to play nicely with others.
            vw.flush();
        }
        finally
        {
            if (vw != null)
            {
                try
                {
                    /* This hack sets the VelocityWriter's internal ref to the
                     * PrintWriter to null to keep memory free while
                     * the writer is pooled. See bug report #18951 */
                    vw.recycle(null);
                    writerPool.put(vw);
                }
                catch (Exception e)
                {
                    getLog().error("Trouble releasing VelocityWriter: " + 
                                   e.getMessage(), e);
                }
            }
        }
    }


    /**
     * This is here so developers may override it and gain access to the
     * Writer which the template will be merged into.  See
     * <a href="http://issues.apache.org/jira/browse/VELTOOLS-7">VELTOOLS-7</a>
     * for discussion of this.
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param writer a VelocityWriter that the template is merged into
     */
    protected void performMerge(Template template, Context context, Writer writer)
        throws IOException
    {
        template.merge(context, writer);
    }

}

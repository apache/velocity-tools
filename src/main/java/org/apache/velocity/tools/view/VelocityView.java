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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.ToolboxFactory;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.FileFactoryConfiguration;
import org.apache.velocity.tools.config.PropertiesFactoryConfiguration;
import org.apache.velocity.tools.config.XmlFactoryConfiguration;
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
 *   <dt>org.apache.velocity.toolbox</dt>
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
 * <p>There are methods you may wish to override to access, alter or control
 * any part of the request processing chain.  Please see the javadocs for
 * more information on :
 * <ul>
 * <li> {@link #setContentType} : <br>for changing the content type on a request
 *                                  by request basis
 * <li> {@link #requestCleanup} : <br>post rendering resource or other cleanup
 * <li> {@link #error} : <br>error handling
 * </ul>
 * </p>
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

public class VelocityView
{
    /** serial version id */
    private static final long serialVersionUID = -3329444102562079189L;

    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE_KEY = "default.contentType";

    /**
     * Key used to access the ServletContext in
     * the Velocity application attributes.
     */
    public static final String SERVLET_CONTEXT_KEY =
        ServletContext.class.getName();

    public static final String DEFAULT_TOOLBOX_KEY =
        Toolbox.class.getName();

    public static final String CREATE_SESSION_PROPERTY = "createSession";

    /** The default content type for the response */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** Default encoding for the output stream */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * Key used to access the toolbox configuration file path from the
     * Servlet or webapp init parameters ("org.apache.velocity.tools").
     */
    public static final String TOOLS_KEY =
        "org.apache.velocity.tools";

    /**
     * Default toolbox configuration file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    public static final String USER_TOOLS_PATH =
        "/WEB-INF/tools.xml";
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

    public static final String SUPPRESS_DEFAULTS_KEY =
        "org.apache.velocity.tools.suppressDefaults";


    private static SimplePool writerPool = new SimplePool(40);
    private ToolboxFactory toolboxFactory = null;
    private VelocityEngine velocity = null;
    private ServletContext servletContext;
    private String defaultContentType = DEFAULT_CONTENT_TYPE;
    private String toolboxKey = DEFAULT_TOOLBOX_KEY;
    private boolean createSession = true;
    private boolean deprecationSupportMode = true;

    public VelocityView(ServletConfig config)
    {
        this(config, DEFAULT_TOOLBOX_KEY);
    }

    public VelocityView(ServletConfig config, String toolboxKey)
    {
        setToolboxKey(toolboxKey);

        this.servletContext = config.getServletContext();
        init(config);
    }

    public VelocityView(ServletContext context)
    {
        this(context, DEFAULT_TOOLBOX_KEY);
    }

    public VelocityView(ServletContext context, String toolboxKey)
    {
        setToolboxKey(toolboxKey);

        if (context == null)
        {
            throw new NullPointerException("ServletContext cannot be null");
        }
        this.servletContext = context;
    }

    /**
     * This method should be private until someone thinks of a good
     * reason to change toolbox keys arbitrarily.  If it is opened up,
     * then we need to be sure any "application" toolbox
     * is copied or moved to be under the new key.
     */
    private final void setToolboxKey(String toolboxKey)
    {
        if (toolboxKey == null)
        {
            throw new NullPointerException("toolboxKey cannot be null");
        }
        this.toolboxKey = toolboxKey;
    }

    protected final String getToolboxKey()
    {
        return this.toolboxKey;
    }

    protected final void setDeprecationSupportMode(boolean support)
    {
        this.deprecationSupportMode = support;
    }

    /**
     * Returns the underlying VelocityEngine being used.
     */
    protected VelocityEngine getVelocityEngine()
    {
        return velocity;
    }

    protected Log getLog()
    {
        return getVelocityEngine().getLog();
    }

    protected ToolboxFactory getToolboxFactory()
    {
        return this.toolboxFactory;
    }

    public String getDefaultContentType()
    {
        return this.defaultContentType;
    }

    /**
     * Simplifies process of getting a property from VelocityEngine,
     * because the VelocityEngine interface sucks compared to the singleton's.
     * Use of this method assumes that {@link #initVelocity(ServletConfig,VelocityEngine)}
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
    protected void init(ServletConfig config)
    {
        // initialize a new VelocityEngine
        init(config, new VelocityEngine());

        // initialize a new ToolboxFactory
        init(config, new ToolboxFactory());

        // set encoding & content-type
        setEncoding(config);
    }

    /**
     * Initializes the Velocity runtime, first calling
     * loadConfiguration(ServletConfig) to get a
     * org.apache.commons.collections.ExtendedProperties
     * of configuration information
     * and then calling velocityEngine.init().  Override this
     * to do anything to the environment before the
     * initialization of the singleton takes place, or to
     * initialize the singleton in other ways.
     *
     * @param config servlet configuration parameters
     */
    protected void init(ServletConfig config, final VelocityEngine velocity)
    {
        if (velocity == null)
        {
            throw new NullPointerException("VelocityEngine cannot be null");
        }
        this.velocity = velocity;

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
            throw new RuntimeException(msg, e);
        }
    }


    /**
     * Initializes the ToolboxFactory.
     *
     * @param config servlet configuation
     * @param factory the ToolboxFactory to be initialized for this VelocityView
     */
    protected void init(final ServletConfig config, final ToolboxFactory factory)
    {
        if (factory == null)
        {
            throw new NullPointerException("ToolboxFactory cannot be null");
        }
        this.toolboxFactory = factory;

        configure(config, toolboxFactory);

        // check for a createSession setting
        Boolean bool = 
            (Boolean)toolboxFactory.getGlobalProperty(CREATE_SESSION_PROPERTY);
        if (bool != null)
        {
            this.createSession = bool;
        }

        // add any application toolbox to the application attributes
        Toolbox appTools =
            toolboxFactory.createToolbox(ToolboxFactory.APPLICATION_SCOPE);
        if (appTools != null &&
            this.servletContext.getAttribute(this.toolboxKey) == null)
        {
            this.servletContext.setAttribute(this.toolboxKey, appTools);
        }
    }


    protected void configure(final ServletConfig config, final VelocityEngine velocity)
    {
        // first get the default properties, and bail if we don't find them
        velocity.setExtendedProperties(getProperties(DEFAULT_PROPERTIES_PATH, true));

        String configMessage = "Configuring Velocity with properties at: ";

        // check for application-wide user props in the context init params
        String appPropsPath = servletContext.getInitParameter(PROPERTIES_KEY);
        if (appPropsPath != null)
        {
            // since the user said props are there, complain if they aren't!
            ExtendedProperties appProps = getProperties(appPropsPath, true);
            getLog().debug(configMessage + appPropsPath);

            // set the props, letting them override framework defaults
            velocity.setExtendedProperties(appProps);
        }

        // check for servlet-wide user props in the config init params at the
        // conventional location, and be silent if they're missing
        ExtendedProperties servletProps = getProperties(USER_PROPERTIES_PATH);
        if (servletProps != null)
        {
            getLog().debug(configMessage + USER_PROPERTIES_PATH);

            // set them, letting servlet props override app props and defaults
            velocity.setExtendedProperties(servletProps);
        }

        // check for a custom location for servlet-wide user props
        String servletPropsPath = config.getInitParameter(PROPERTIES_KEY);
        if (servletPropsPath != null)
        {
            // since the user said props are there, complain if they aren't!
            servletProps = getProperties(servletPropsPath, true);
            getLog().debug(configMessage + servletPropsPath);

            // set them, again, let these specified with most effort override all
            velocity.setExtendedProperties(servletProps);
        }
    }


    protected void configure(final ServletConfig config, final ToolboxFactory factory)
    {
        // determine whether or not to include default tools
        String suppressDefaults = config.getInitParameter(SUPPRESS_DEFAULTS_KEY);
        if (suppressDefaults == null)
        {
            suppressDefaults =
                servletContext.getInitParameter(SUPPRESS_DEFAULTS_KEY);
        }

        FactoryConfiguration factoryConfig;
        if ("true".equalsIgnoreCase(suppressDefaults))
        {
            // start out blank
            factoryConfig = new FactoryConfiguration();
            getLog().debug("Default tool configurations have been suppressed.");
        }
        else
        {
            getLog().trace("Loading default tool configurations...");
            // start out with all available default tools
            factoryConfig = FactoryConfiguration.getDefault();
        }

        String configMessage = "Loading configuration from: ";

        // check for application-wide user config in the context init params
        String appToolsPath = servletContext.getInitParameter(TOOLS_KEY);
        if (appToolsPath != null)
        {
            // since the user said a config is there, complain if it isn't!
            FactoryConfiguration appConfig = getConfiguration(appToolsPath, true);
            getLog().debug(configMessage + appToolsPath);

            factoryConfig.addConfiguration(appConfig);
        }

        if (this.deprecationSupportMode)
        {
            // check for deprecated user configuration at the old conventional
            // location.  be silent if missing, log deprecation warning otherwise
            FactoryConfiguration deprecatedConfig =
                getConfiguration(DEPRECATED_USER_TOOLS_PATH);
            if (deprecatedConfig != null)
            {
                getLog().warn("Please upgrade to new \"/WEB-INF/tools.xml\" format and conventional location."+
                              " Support for \"/WEB-INF/toolbox.xml\" format and conventional file name will "+
                              "be removed in a future version.");
                getLog().debug(configMessage + DEPRECATED_USER_TOOLS_PATH);

                factoryConfig.addConfiguration(deprecatedConfig);
            }
        }

        // check for user configuration at the conventional location,
        // and be silent if they're missing
        FactoryConfiguration servletConfig = getConfiguration(USER_TOOLS_PATH);
        if (servletConfig != null)
        {
            getLog().debug(configMessage + USER_TOOLS_PATH);

            factoryConfig.addConfiguration(servletConfig);
        }

        // check for a custom location for servlet-wide user props
        String servletToolsPath = config.getInitParameter(TOOLS_KEY);
        if (servletToolsPath != null)
        {
            // since the user said props are there, complain if they aren't!
            servletConfig = getConfiguration(servletToolsPath, true);
            getLog().debug(configMessage + servletToolsPath);

            factoryConfig.addConfiguration(servletConfig);
        }

        getLog().debug("Configuring toolboxFactory with: "+factoryConfig);

        // apply this configuration to the specified factory
        factory.configure(factoryConfig);
    }


    protected InputStream getInputStream(String path, boolean required)
    {
        // first, search the classpath
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null)
        {
            // then, try the servlet context
            inputStream = this.servletContext.getResourceAsStream(path);

            if (inputStream == null)
            {
                // then, try the file system directly
                File file = new File(path);
                if (file.exists())
                {
                    try
                    {
                        inputStream = new FileInputStream(file);
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        // we should not be able to get here
                        // since we already checked whether the file exists
                        throw new IllegalStateException(fnfe);
                    }
                }
            }
        }

        // if we still haven't found one
        if (inputStream == null)
        {
            String msg = "Could not find file at: "+path;
            getLog().debug(msg);
            if (required)
            {
                throw new ResourceNotFoundException(msg);
            }
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
                throw new RuntimeException(ioe);
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

        // first make sure we can even get such a file
        InputStream inputStream = getInputStream(path, required);
        if (inputStream == null)
        {
            return null;
        }

        // then make sure it's a file type we recognize
        FileFactoryConfiguration config = null;
        if (path.endsWith(".xml"))
        {
            config = new XmlFactoryConfiguration(this.deprecationSupportMode);
        }
        else if (path.endsWith(".properties"))
        {
            config = new PropertiesFactoryConfiguration();
        }
        else
        {
            String msg = "Unknown configuration file type: " + path +
                         "\nOnly .xml and .properties configuration files are supported at this time.";
            getLog().debug(msg);
            if (required)
            {
                throw new UnsupportedOperationException(msg);
            }
        }

        // now, try to read the file
        try
        {
            config.read(inputStream);
        }
        catch (IOException ioe)
        {
            String msg = "Failed to load configuration at: "+path;
            getLog().error(msg, ioe);
            if (required)
            {
                throw new RuntimeException(ioe);
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
        return config;
    }


    protected void setEncoding(ServletConfig config)
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

        getLog().debug("Default content-type is: " + defaultContentType);
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
        Context context = getContext(request, response);

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
        Context context = getContext(request);

        // get the template
        Template template = getTemplate(request);

        // merge the template and context into the writer
        merge(template, context, out);

        return context;
    }


    protected void prepareToolboxes(HttpServletRequest request)
    {
        // only set a new toolbox if we need one
        if (toolboxFactory.hasTools(ToolboxFactory.DEFAULT_SCOPE)
            && request.getAttribute(this.toolboxKey) == null)
        {
            // add request toolbox, if any
            Toolbox reqTools =
                toolboxFactory.createToolbox(ToolboxFactory.DEFAULT_SCOPE);
            if (reqTools != null)
            {
                request.setAttribute(this.toolboxKey, reqTools);
            }
        }

        //TODO: move this string constant somewhere static
        if (toolboxFactory.hasTools("session"))
        {
            //FIXME? does this honor createSession props set on the session Toolbox?
            HttpSession session = request.getSession(this.createSession);
            if (session != null)
            {
                // allow only one thread per session at a time
                synchronized(getMutex(session))
                {
                    if (session.getAttribute(this.toolboxKey) == null)
                    {
                        Toolbox sessTools =
                            toolboxFactory.createToolbox("session");
                        session.setAttribute(this.toolboxKey, sessTools);
                    }
                }
            }
        }
    }


    /**
     * Returns a mutex (lock object) unique to the specified session
     * to allow for reliable synchronization on the session.
     */
    protected Object getMutex(HttpSession session)
    {
        // yes, this uses double-checked locking, but it is safe here
        // since partial initialization of the lock is not an issue
        Object lock = session.getAttribute("session.mutex");
        if (lock == null)
        {
            // one thread per toolbox manager at a time
            synchronized(this)
            {
                // in case another thread already came thru
                lock = session.getAttribute("session.mutex");
                if (lock == null)
                {
                    // use a Boolean because it is serializable and small
                    lock = new Boolean(true);
                    session.setAttribute("session.mutex", lock);
                }
            }
        }
        return lock;
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
    protected ViewToolContext createContext(HttpServletRequest request,
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
        prepareContext(ctx);
        return ctx;
    }

    protected void prepareContext(ViewToolContext context)
    {
        // if this view is storing toolboxes under a non-standard key,
        // then retrieve it's toolboxes here, since ViewToolContext won't
        // know where to find them
        if (!this.toolboxKey.equals(DEFAULT_TOOLBOX_KEY))
        {
            context.addToolboxesUnderKey(this.toolboxKey);
        }
    }


    public Context getContext(HttpServletRequest request)
    {
        return getContext(request, null);
    }

    public Context getContext(HttpServletRequest request,
                              HttpServletResponse response)
    {
        // first make sure request and session toolboxes are added
        prepareToolboxes(request);

        // then create the context
        return createContext(request, response);
    }



    /**
     * <p>Gets the requested template.</p>
     *
     * @param request client request
     * @return Velocity Template object or null
     */
    protected Template getTemplate(HttpServletRequest request)
    {
        return getTemplate(request, null);
    }

    protected Template getTemplate(HttpServletRequest request,
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
                return getVelocityEngine().getTemplate(name);
            }
            else
            {
                return getVelocityEngine().getTemplate(name, encoding);
            }
        }
        catch (Exception e)
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
    protected void merge(Template template, Context context, Writer writer)
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
        }
        finally
        {
            if (vw != null)
            {
                try
                {
                    // flush and put back into the pool
                    // don't close to allow us to play
                    // nicely with others.
                    vw.flush();
                    /* This hack sets the VelocityWriter's internal ref to the
                     * PrintWriter to null to keep memory free while
                     * the writer is pooled. See bug report #18951 */
                    vw.recycle(null);
                    writerPool.put(vw);
                }
                catch (Exception e)
                {
                    getLog().debug("Trouble releasing VelocityWriter: " + 
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

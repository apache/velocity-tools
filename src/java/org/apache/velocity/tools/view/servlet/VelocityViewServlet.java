/*
 * Copyright 2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.view.servlet;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;

import org.apache.velocity.tools.view.ToolboxManager;
import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;
import org.apache.velocity.tools.view.servlet.WebappLoader;


/**
 * <p>A servlet to process Velocity templates. This is comparable to the
 * the JspServlet for JSP-based applications.</p>
 *
 * <p>The servlet provides the following features:</p>
 * <ul>
 *   <li>renders Velocity templates</li>
 *   <li>provides support for an auto-loaded, configurable toolbox</li>
 *   <li>provides transparent access to the servlet request attributes,
 *       servlet session attributes and servlet context attributes by
 *       auto-searching them</li>
 *   <li>logs to the logging facility of the servlet API</li>
 * </ul>
 *
 * <p>VelocityViewServlet supports the following configuration parameters
 * in web.xml:</p>
 * <dl>
 *   <dt>org.apache.velocity.toolbox</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, no toolbox is instantiated.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity is initialized with default settings.</dd>
 * </dl>
 * 
 * <p>There are methods you may wish to override to access, alter or control
 * any part of the request processing chain.  Please see the javadocs for
 * more information on :
 * <ul>
 * <li> {@link #loadConfiguration} : <br>for loading Velocity properties and
 *                                     configuring the Velocity runtime
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
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: VelocityViewServlet.java,v 1.21 2004/03/12 20:30:32 nbubna Exp $
 */

public class VelocityViewServlet extends HttpServlet
{

    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE = "default.contentType";

    /** The default content type for the response */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";
  
    /** Default encoding for the output stream */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * Key used to access the ServletContext in 
     * the Velocity application attributes.
     */
    public static final String SERVLET_CONTEXT_KEY = 
        ServletContext.class.getName();


    /**
     * Key used to access the toolbox configuration file path from the
     * Servlet or webapp init parameters ("org.apache.velocity.toolbox").
     */
    protected static final String TOOLBOX_KEY = 
        "org.apache.velocity.toolbox";

    /**
     * This is the string that is looked for when getInitParameter is
     * called ("org.apache.velocity.properties").
     */
    protected static final String INIT_PROPS_KEY =
        "org.apache.velocity.properties";

    /** A reference to the toolbox manager. */
    protected ToolboxManager toolboxManager = null;


    /** Cache of writers */
    private static SimplePool writerPool = new SimplePool(40);

    /**
     * The default content type.  When necessary, includes the
     * character set to use when encoding textual output.
     */
    private String defaultContentType;

    /**
     * Whether we've logged a deprecation warning for
     * ServletResponse's <code>getOutputStream()</code>.
     * @since VelocityTools 1.1
     */
    private boolean warnOfOutputStreamDeprecation = true;


    /**
     * <p>Initializes servlet, toolbox and Velocity template engine.
     * Called by the servlet container on loading.</p>
     *
     * @param config servlet configuation
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // do whatever we have to do to init Velocity
        initVelocity(config);

        // init this servlet's toolbox (if any)
        initToolbox(config);

        // we can get these now that velocity is initialized
        defaultContentType = 
            RuntimeSingleton.getString(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        String encoding = 
            RuntimeSingleton.getString(RuntimeSingleton.OUTPUT_ENCODING,
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
                defaultContentType += "; charset=" + encoding;
            }
            else
            {
                // The user may have configuration issues.
                Velocity.warn("VelocityViewServlet: Charset was already " +
                              "specified in the Content-Type property.  " +
                              "Output encoding property will be ignored.");
            }
        }

        Velocity.info("VelocityViewServlet: Default content-type is: " + 
                      defaultContentType);
    }


    /**
     * Initializes the ServletToolboxManager for this servlet's
     * toolbox (if any).
     *
     * @param config servlet configuation
     */
    protected void initToolbox(ServletConfig config) throws ServletException
    {
        ServletContext servletContext = config.getServletContext();

        /* check the servlet config for a toolbox */
        String file = config.getInitParameter(TOOLBOX_KEY);

        /* check the servlet context for a toolbox */
        if (file == null || file.length() == 0) 
        {
            file = servletContext.getInitParameter(TOOLBOX_KEY);
        }

        /* if we have a toolbox, get a manager for it */
        if (file != null)
        {
            toolboxManager = 
                ServletToolboxManager.getInstance(servletContext, file);
        }
        else
        {
            Velocity.info("VelocityViewServlet: No toolbox entry in configuration.");
        }
    }


    /**
     * Initializes the Velocity runtime, first calling 
     * loadConfiguration(ServletConfig) to get a 
     * org.apache.commons.collections.ExtendedProperties
     * of configuration information
     * and then calling Velocity.init().  Override this
     * to do anything to the environment before the 
     * initialization of the singleton takes place, or to 
     * initialize the singleton in other ways.
     *
     * @param config servlet configuration parameters
     */
    protected void initVelocity(ServletConfig config) throws ServletException
    {
        Velocity.setApplicationAttribute(SERVLET_CONTEXT_KEY, getServletContext());

        // default to servletlogger, which logs to the servlet engines log
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, 
                             ServletLogger.class.getName());

        // by default, load resources with webapp resource loader
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "webapp");
        Velocity.setProperty("webapp.resource.loader.class", 
                             WebappLoader.class.getName());

        // Try reading an overriding Velocity configuration
        try
        {
            ExtendedProperties p = loadConfiguration(config);
            Velocity.setExtendedProperties(p);
        }
        catch(Exception e)
        {
            getServletContext().log("VelocityViewServlet: Unable to read Velocity configuration file: "+e);
            getServletContext().log("VelocityViewServlet: Using default Velocity configuration.");
        }   

        // now all is ready - init Velocity
        try
        {
            Velocity.init();
        }
        catch(Exception e)
        {
            getServletContext().log("VelocityViewServlet: PANIC! unable to init() - "+e);
            throw new ServletException(e);
        }
    }

     
    /**
     *  Loads the configuration information and returns that 
     *  information as an ExtendedProperties, which will be used to 
     *  initialize the Velocity runtime.
     *  <br><br>
     *  Currently, this method gets the initialization parameter
     *  VelocityServlet.INIT_PROPS_KEY, which should be a file containing
     *  the configuration information.
     *  <br><br>
     *  To configure your Servlet Spec 2.2 compliant servlet runner to pass
     *  this to you, put the following in your WEB-INF/web.xml file
     *  <br>
     *  <pre>
     *    &lt;servlet&gt;
     *      &lt;servlet-name&gt; YourServlet &lt/servlet-name&gt;
     *      &lt;servlet-class&gt; your.package.YourServlet &lt;/servlet-class&gt;
     *      &lt;init-param&gt;
     *         &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *         &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *      &lt;/init-param&gt;
     *    &lt;/servlet&gt;
     *   </pre>
     *
     * Alternately, if you wish to configure an entire context in this
     * fashion, you may use the following:
     *  <br>
     *  <pre>
     *    &lt;context-param&gt;
     *       &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *       &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *       &lt;description&gt; Path to Velocity configuration &lt;/description&gt;
     *    &lt;/context-param&gt;
     *   </pre>
     * 
     *  Derived classes may do the same, or take advantage of this code to do the loading for them via :
     *   <pre>
     *      ExtendedProperties p = super.loadConfiguration(config);
     *   </pre>
     *  and then add or modify the configuration values from the file.
     *  <br>
     *
     *  @param config ServletConfig passed to the servlets init() function
     *                Can be used to access the real path via ServletContext (hint)
     *  @return ExtendedProperties loaded with configuration values to be used
     *          to initialize the Velocity runtime.
     *  @throws IOException I/O problem accessing the specified file, if specified.
     */
    protected ExtendedProperties loadConfiguration(ServletConfig config)
        throws IOException
    {
        ServletContext servletContext = config.getServletContext();

        // grab the path to the custom props file (if any)
        String propsFile = config.getInitParameter(INIT_PROPS_KEY);
        if (propsFile == null || propsFile.length() == 0)
        {
            propsFile = servletContext.getInitParameter(INIT_PROPS_KEY);
        }
        
        ExtendedProperties p = new ExtendedProperties();
        if (propsFile != null)
        {
            p.load(servletContext.getResourceAsStream(propsFile));

            Velocity.info("VelocityViewServlet: Custom Properties File: "+propsFile);
        }
        else
        {
            Velocity.info("VelocityViewServlet: No custom properties found. " +
                          "Using default Velocity configuration.");
        }

        return p;
    }

          
    /**
     * Handles GET - calls doRequest()
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     * Handle a POST request - calls doRequest()
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     *  Handles with both GET and POST requests
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    protected void doRequest(HttpServletRequest request, 
                             HttpServletResponse response)
         throws ServletException, IOException
    {
        Context context = null;
        try
        {
            // first, get a context
            context = createContext(request, response);
            
            // set the content type 
            setContentType(request, response);

            // get the template
            Template template = handleRequest(request, response, context);        

            // bail if we can't find the template
            if (template == null)
            {
                Velocity.warn("VelocityViewServlet: couldn't find template to match request.");
                return;
            }

            // merge the template and context
            mergeTemplate(template, context, response);
        }
        catch (Exception e)
        {
            // log the exception
            Velocity.error("VelocityViewServlet: Exception processing the template: "+e);

            // call the error handler to let the derived class
            // do something useful with this failure.
            error(request, response, e);
        }
        finally
        {
            // call cleanup routine to let a derived class do some cleanup
            requestCleanup(request, response, context);
        }
    }


    /**
     * Cleanup routine called at the end of the request processing sequence
     * allows a derived class to do resource cleanup or other end of 
     * process cycle tasks.  This default implementation does nothing.
     *
     * @param request servlet request from client 
     * @param response servlet reponse 
     * @param context Context created by the {@link #createContext}
     */
    protected void requestCleanup(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  Context context)
    {
    }


    /**
     * <p>Handle the template processing request.</p> 
     *
     * @param request client request
     * @param response client response
     * @param ctx  VelocityContext to fill
     *
     * @return Velocity Template object or null
     */
    protected Template handleRequest(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     Context ctx)
        throws Exception
    {
        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following special
        // attribute holds the correct path.  See section 8.3 of the Servlet
        // 2.3 specification.
        String path = (String)request.getAttribute("javax.servlet.include.servlet_path");
        if (path == null)
        {
            path = request.getServletPath();
        }
        return getTemplate(path);
    }


    /**
     * Sets the content type of the response.  This is available to be overriden
     * by a derived class.
     *
     * <p>The default implementation is :
     * <pre>
     *
     *    response.setContentType(defaultContentType);
     * 
     * </pre>
     * where defaultContentType is set to the value of the default.contentType
     * property, or "text/html" if that is not set.</p>
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected void setContentType(HttpServletRequest request, 
                                  HttpServletResponse response)
    {
        response.setContentType(defaultContentType);
    }


    /**
     * <p>Creates and returns an initialized Velocity context.</p> 
     * 
     * A new context of class {@link ChainedContext} is created and 
     * initialized.
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected Context createContext(HttpServletRequest request, 
                                    HttpServletResponse response)
    {
        ChainedContext ctx = new ChainedContext(null, request, response, getServletContext());

        /* if we have a toolbox manager, get a toolbox from it */
        if (toolboxManager != null)
        {
            ctx.setToolbox(toolboxManager.getToolboxContext(ctx));
        }
        return ctx;
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
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate(name);
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
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate(name, encoding);
    }


    /**
     * Merges the template with the context.  Only override this if you really, really
     * really need to. (And don't call us with questions if it breaks :)
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param response servlet reponse (used to get a Writer)
     */
    protected void mergeTemplate(Template template, 
                                 Context context, 
                                 HttpServletResponse response)
        throws ResourceNotFoundException, ParseErrorException, 
               MethodInvocationException, IOException, 
               UnsupportedEncodingException, Exception
    {
        VelocityWriter vw = null;
        Writer writer = getResponseWriter(response);
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
            template.merge(context, vw);
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
                    Velocity.debug("VelocityViewServlet: " + 
                                   "Trouble releasing VelocityWriter: " +
                                   e.getMessage());
                }                
            }
        }
    }

 
    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     * 
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param e  Exception that was thrown by some other part of process.
     */
    protected void error(HttpServletRequest request, 
                         HttpServletResponse response, 
                         Exception e)
        throws ServletException
    {
        try
        {
            StringBuffer html = new StringBuffer();
            html.append("<html>\n");
            html.append("<head><title>Error</title></head>\n");
            html.append("<body>\n");
            html.append("<h2>VelocityViewServlet : Error processing the template</h2>\n");

            Throwable cause = e;

            String why = cause.getMessage();
            if (why != null && why.trim().length() > 0)
            {
                html.append(why);
                html.append("\n<br>\n");
            }

            // if it's an MIE, i want the real stack trace!
            if (cause instanceof MethodInvocationException) 
            {
                // get the real cause
                cause = ((MethodInvocationException)cause).getWrappedThrowable();
            }

            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));

            html.append("<pre>\n");
            html.append(sw.toString());
            html.append("</pre>\n");
            html.append("</body>\n");
            html.append("</html>");
            getResponseWriter(response).write(html.toString());
        }
        catch (Exception e2)
        {
            // clearly something is quite wrong.
            // let's log the new exception then give up and
            // throw a servlet exception that wraps the first one
            Velocity.error("VelocityViewServlet: Exception while printing error screen: "+e2);
            throw new ServletException(e);
        }
    }

    /**
     * <p>Procure a Writer with correct encoding which can be used
     * even if HttpServletResponse's <code>getOutputStream()</code> method
     * has already been called.</p>
     *
     * <p>This is a transitional method which will be removed in a
     * future version of Velocity.  It is not recommended that you
     * override this method.</p>
     *
     * @param response The response.
     * @return A <code>Writer</code>, possibly created using the
     *        <code>getOutputStream()</code>.
     */
    protected Writer getResponseWriter(HttpServletResponse response)
        throws UnsupportedEncodingException, IOException
    {
        Writer writer = null;
        try
        {
            writer = response.getWriter();
        }
        catch (IllegalStateException e)
        {
            // ASSUMPTION: We already called getOutputStream(), so
            // calls to getWriter() fail.  Use of OutputStreamWriter
            // assures our desired character set
            if (this.warnOfOutputStreamDeprecation)
            {
                this.warnOfOutputStreamDeprecation = false;
                Velocity.warn("VelocityViewServlet: " +
                              "Use of ServletResponse's getOutputStream() " +
                              "method with VelocityViewServlet is " +
                              "deprecated -- support will be removed in " +
                              "an upcoming release");
            }
            // Assume the encoding has been set via setContentType().
            String encoding = response.getCharacterEncoding();
            if (encoding == null)
            {
                encoding = DEFAULT_OUTPUT_ENCODING;
            }
            writer = new OutputStreamWriter(response.getOutputStream(),
                                            encoding);
        }
        return writer;
    }

}

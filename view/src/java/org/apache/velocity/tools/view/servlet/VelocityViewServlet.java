/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.view.servlet;


import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.Properties;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.collections.ExtendedProperties;

import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;


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
 * in webl.xml:</p>
 * <dl>
 *   <dt>toolbox</dt>
 *   <dd>Path and name of the toolbox configuration file. The path must be
 *     relative to the web application root directory. If this parameter is
 *     not found, no toolbox is instantiated.</dd>
 *   <dt>org.apache.velocity.properties</dt>
 *   <dd>Path and name of the Velocity configuration file. The path must be
 *     relative to the web application root directory. If this parameter
 *     is not present, Velocity is initialized with default settings.</dd>
 * </dl>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author  <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *
 * @version $Id: VelocityViewServlet.java,v 1.6 2003/01/27 17:17:20 nbubna Exp $
 */

public class VelocityViewServlet extends VelocityServlet
{

    /**
     * Key used to access the toolbox configuration file path from the
     * Servlet init parameters.
     */
    public static final String TOOLBOX_PARAM = "toolbox";


    /**
     * Key used to access the Velocity configuration file path from the
     * Servlet init parameters.
     */ 
    public static final String VELOCITY_PROPERTIES = "velocity.properties";


    /**
     * A reference to the toolbox manager.
     */
    protected ServletToolboxManager toolboxManager = null;


    /**
     *  <p>Initializes servlet, toolbox and Velocity template engine.</p>
     *
     * @param config servlet configuation
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );

        ServletContext servletContext = config.getServletContext();

        /* check the servlet config for a toolbox */
        String key = config.getInitParameter(TOOLBOX_PARAM);

        /* check the servlet context for a toolbox */
        if (key == null || key.length() == 0) 
        {
            key = servletContext.getInitParameter(TOOLBOX_PARAM);
        }

        /* if we have a toolbox, get a manager for it */
        if (key != null)
        {
            toolboxManager = 
                ServletToolboxManager.getInstance(servletContext, key);
        }
        else
        {
            Velocity.info("No toolbox entry in configuration.");
        }
    }


    /**
     * Initializes Velocity.
     *
     * @param config servlet configuration parameters
     */
    protected void initVelocity( ServletConfig config )
         throws ServletException
    {
        // Try reading Velocity configuration
        try
        {
            Properties p = super.loadConfiguration(config);
            Velocity.setExtendedProperties(ExtendedProperties.convertProperties(p));
        }
        catch(Exception e)
        {
            getServletContext().log("Unable to read Velocity configuration file: " + e);
            getServletContext().log("Using default Velocity configuration.");
        }   

        // define servletlogger, which logs to the servlet engines log
        ServletLogger sl = new ServletLogger( getServletContext() );
        Velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, sl );

        // load resources with webapp resource loader
        VelocityStrutsServletAppContext vssac = new VelocityStrutsServletAppContext( getServletContext() );
        Velocity.setApplicationAttribute( "org.apache.velocity.tools.view.servlet.WebappLoader",  vssac );
        Velocity.setProperty( "resource.loader", "webapp" );
        Velocity.setProperty( "webapp.resource.loader.class", "org.apache.velocity.tools.view.servlet.WebappLoader" );

        // now all is ready - init Velocity
        try
        {
            Velocity.init();
        }
        catch( Exception e )
        {
            getServletContext().log("VELOCITY PANIC : unable to init() : " + e );
            throw new ServletException ( e );
        }
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
                                     Context ctx )
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
     * <p>Creates and returns an initialized Velocity context.</p> 
     * 
     * A new context of class {@link ChainedContext} is created and 
     * initialized. This method overwrites 
     * {@link org.apache.velocity.servlet.VelocityServlet#createContext(HttpServletRequest request, HttpServletResponse response)}.</p>
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected Context createContext(HttpServletRequest request, 
                                    HttpServletResponse response)
    {
        /*
         *  create a ChainedContext()
         */

        ChainedContext ctx =  new ChainedContext( null, request, response, getServletContext() );

        /*
         *  if we have a toolbox manager, let it make a context for us
         */

        if (toolboxManager != null)
        {
            ToolboxContext tc = toolboxManager.getToolboxContext( ctx );
            ctx.setToolbox(  tc);
        }

        return ctx;
    }


    /**
     * <p>Wrapper class to safely pass the servlet context to the web app
     * loader.</p>
     */
    public class VelocityStrutsServletAppContext implements WebappLoaderAppContext
    {
        /**
         * A reference to the servlet context
         */
        ServletContext servletContext = null;


        /**
         * Default constructor.
         */
        VelocityStrutsServletAppContext( ServletContext sc )
        {
            servletContext = sc;
        }


        /**
         * Returns a reference to the servlet context.
         */
        public ServletContext getServletContext()
        {
           return servletContext;
        }
   }
   
}

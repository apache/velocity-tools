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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.velocity.servlet.VelocityServlet;

import org.apache.velocity.app.Velocity;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.tools.ToolboxManager;

/**
 *  <p>
 *  Servlet implementation designed for use in web applications
 *  where a controller forwards the request to a rendering servlet
 *  such as the JspServlet for JSP based applicaions.
 *  </p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author  <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *
 * @version $Id: VelocityViewServlet.java,v 1.1 2002/01/03 20:21:55 geirm Exp $
 */
public class VelocityViewServlet extends VelocityServlet
{
    public static final String TOOLBOX_PARAM = "toolbox";

    protected ToolboxManager toolboxManager = null;

    /**
     *  we want to see if there is a magickey as a context
     *  param
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );

       /*
        *  setup the toolbox if there is one
        */

       String key = config.getInitParameter( TOOLBOX_PARAM );

       if ( key != null )
       {
           InputStream is = null;

           try
           {
               /*
                *  little fix up
                */
               if ( !key.startsWith("/") )
                   key = "/" + key;

               /*
                *  get the bits
                */
               is = getServletContext().getResourceAsStream( key );

               if ( is != null)
               {
                    Velocity.info("Using toolbox configuration file '" + key +"'");

                    toolboxManager = new ToolboxManager();
                    toolboxManager.load( is );

                    Velocity.info("Toolbox setup complete.");
               }
           }
           catch( Exception e )
           {
               Velocity.error("Problem reading toolbox file properties file '" + key +"' : " + e );
           }
           finally
           {
               try{
                   if ( is != null)
                       is.close();
               }
               catch(Exception ee )
                   {}
           }
       }
       else
       {
            Velocity.info("No toolbox entry in configuration.");
       }
    }

    /**
     *  do our own init...
     */
    protected void initVelocity( ServletConfig config )
         throws ServletException
    {
        /*
         *  start with our servletlogger, which logs to the servlet
         *  engines log
         */
        ServletLogger sl = new ServletLogger( getServletContext() );
        Velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, sl );

        /*
         *  as for the webapp resource loader
         */

        VelocityStrutsServletAppContext vssac = new VelocityStrutsServletAppContext( getServletContext() );

        Velocity.setApplicationAttribute( "org.apache.velocity.tools.view.servlet.WebappLoader",  vssac );
        Velocity.setProperty( "resource.loader", "webapp" );
        Velocity.setProperty( "webapp.resource.loader.class", "org.apache.velocity.tools.view.servlet.WebappLoader" );

        /*
         * now all is ready - init()
         */

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
     *  <p>Handled the request. Current responsibilities :</p>
     *  <ul>
     *  <li> fill context with all application/session/request attributes
     *  <li> find and return Template
     *  </ul>
     *  @param request client request
     *  @param response client response
     *  @param ctx  VelocityContext to fill
     *  @return Velocity Template object or null
     */
    protected Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context ctx ) 
        throws Exception
    {
        return getTemplate(request.getServletPath() );
    }

    /**
     *  <p>Returns a Velocity context. A new context of class
     *  {@link ChainedContext} is created and returned. This method overwrites 
     *  {@link org.apache.velocity.servlet.VelocityServlet#createContext(
     *  HttpServletRequest request, HttpServletResponse response)}.
     *  </p>
     *
     *  @param request servlet request from client
     *  @param response servlet reponse to client
     *
     *  @return context
     */
    protected Context createContext(HttpServletRequest request, HttpServletResponse response )
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
     *  little wrapper class to safely pass the ServletContext to the loader
     */
    public class VelocityStrutsServletAppContext implements WebappLoaderAppContext
    {
        ServletContext servletContext = null;

        VelocityStrutsServletAppContext( ServletContext sc )
        {
            servletContext = sc;
        }

        public ServletContext getServletContext()
        {
            return servletContext;
        }
    }
}




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

package org.apache.velocity.tools.view.context;

import java.util.HashMap;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;


/**
 *  <p>
 *  Velocity context implementation that does a few things :
 *  <ul>
 *    <li>
 *      puts the request, response, session, and application objects
 *      into the Context for
 *      direct access, and keeps them read-only
 *   </li>
 *   <li>
 *      supports a read-only toolbox
 *   </li>
 *   <li>
 *    chains the servlet API scopes  <i>request</i>, <i>session</i> and
 *     <i>application</i> for auto-search if all else fails.
 *   </li>
 *  </ul>
 *
 *  This means when accessing an object in the context, and it is not either one
 *  of REQUEST, RESPONSE, SESSION or APPLICATIOn or a toolbox rquest, and not
 *  an object placed directly into the context (conventional use)  then
 *  the three servlet scopes are searched. The search order is
 *  <i>Velocity context</i>, <i>request scope</i>, <i>session scope</i>
 *  and <i>application scope</i>.
 *  </p>
 *
 *  <p>
 *  The purpose of this class is to make
 *  it easy for web designer to work with 
 *  Java servlet based web applications. They do not need to be concerned with
 *  the concepts of request, session of application scopes and the live time
 *  of objects in these scopes.
 *  </p>
 *  
 *  <p>
 *  Note that objects put into the context are always put into the
 *  Velocity context itself, not any of the servlet scopes. In order to put 
 *  an object into one of the servlet scopes, the request, session or servlet
 *  context instances must be put into the Velocity context and then accessed
 *  using like any other context variable,
 *  for example $request.setAttribute(name, value).
 *  </p>
 *
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
v *
 * @version $Id: ChainedContext.java,v 1.2 2002/01/09 11:26:40 sidler Exp $ 
 */
public class ChainedContext extends VelocityContext implements ViewContext
{
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private ServletContext application;

    private ToolboxContext toolboxContext = null;
    private String toolboxName = null;

    /**
     * Key to the HTTP request object.
     */
    public static final String REQUEST = "request";

    /**
     * Key to the HTTP response object.
     */
    public static final String RESPONSE = "response";

    /**
     * Key to the HTTP session object.
     */
    public static final String SESSION = "session";

    /**
     * Key to the servlet context object.
     */
    public static final String APPLICATION = "application";

    public ChainedContext(Context ctx, HttpServletRequest request,
                           HttpServletResponse response, ServletContext application)
    {
        super(null, ctx );

        this.request = request;
        this.response = response;
        this.session = request.getSession(false);
        this.application = application;
    }

    public void setToolbox(  ToolboxContext box )
    {
        toolboxContext = box;
    }

    /**
     * A requested object is searched in the Velocity context and the Servlet API
     * scopes <i>request</i>, <i>session</i> (if exists) and <i>servlet context</i>. 
     * The search order is <i>Velocity context</i>, <i>request scope</i>, <i>session scope</i> 
     * and <i>servlet context</i>.</p>
     *
     * @param key The key of the object requested.
     * @return The requested object or null if not found.
     */
    public Object internalGet( String key )
    {
        /*
         * make the 4 Scopes of the Apocalypse Read only
         */

        if ( key.equals( REQUEST ))
        {
            return request;
        }
        else if( key.equals(RESPONSE) )
        {
            return response;
        }
        else if ( key.equals(SESSION) )
        {
            return session;
        }
        else if ( key.equals(APPLICATION))
        {
            return application;
        }

        /*
         *  now the toolbox?
         */

        Object o = null;

        if ( toolboxContext != null)
        {
             o = toolboxContext.get( key );

            if ( o != null )
            {
                return o;
            }
        }


        /*
         * try the real impl
         */

        o = super.internalGet( key );

        /*
         *  if not, wander down the scopes...
         */

        if (o == null)
        {
            o = request.getAttribute( key );

            if ( o == null && session != null )
            {
                o = session.getAttribute( key );
                
                if ( o == null )
                {
                    o = application.getAttribute( key );
                }
            }
        }
        
        return o;
    }

      /**
     * <p>Fetch the instance of {@link HttpServletRequest} for this request.</p>
     */
    public HttpServletRequest getRequest()
    {
        return request;
    }


    /**
     * <p>Fetch the instance of {@link ServletContext} for this request.</p>
     */
    public ServletContext getServletContext()
    {
        return application;
    }

    public Context getVelocityContext()
    {
        return this;
    }

}  // ChainedContext

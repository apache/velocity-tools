/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * <p>Velocity context implementation specific to the Servlet environment.</p>
 *
 * <p>It provides the following special features:</p>
 * <ul>
 *   <li>puts the request, response, session, and servlet context objects
 *       into the Velocity context for direct access, and keeps them 
 *       read-only</li>
 *   <li>supports a read-only toolbox of view tools</li>
 *   <li>auto-searches servlet request attributes, session attributes and
 *       servlet context attribues for objects</li>
 * </ul>
 *
 * <p>The {@link #internalGet(String key)} method implements the following search order
 * for objects:</p>
 * <ol>
 *   <li>toolbox</li>
 *   <li>servlet request, servlet response, servlet session, servlet context</li>
 *   <li>local hashtable of objects (traditional use)</li>
 *   <li>servlet request attribues, servlet session attribute, servlet context
 *     attributes</li>
 * </ol> 
 *
 * <p>The purpose of this class is to make it easy for web designer to work 
 * with Java servlet based web applications. They do not need to be concerned 
 * with the concepts of request, session or application attributes and the 
 * lifetime of objects in these scopes.</p>
 *  
 * <p>Note that the put() method always puts objects into the local hashtable.
 * </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ChainedContext.java,v 1.5 2003/11/06 00:26:54 nbubna Exp $ 
 */
public class ChainedContext extends VelocityContext implements ViewContext
{

    /**
     * A local reference to the current servlet request.
     */ 
    private HttpServletRequest request;
    
    /**
     * A local reference to the current servlet response.
     */
    private HttpServletResponse response;
    
    /**
     * A local reference to the servlet session.
     */
    private HttpSession session;
    
    /**
     * A local reference to the servlet context.
     */
    private ServletContext application;

    /**
     * The toolbox. 
     */ 
    private ToolboxContext toolboxContext = null;


    /**
     * Default constructor.
     */
    public ChainedContext(Context ctx, 
                          HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext application)
    {
        super(null, ctx );

        this.request = request;
        this.response = response;
        this.session = request.getSession(false);
        this.application = application;
    }


    /**
     * <p>Sets the toolbox of view tools.</p>
     *
     * @param box toolbox of view tools
     */
    public void setToolbox(ToolboxContext box)
    {
        toolboxContext = box;
        // just in case the servlet toolbox manager
        // had to create a new session to hold session tools
        // let's make sure this context's session ref is current
        session = request.getSession(false);
    }


    /**
     * <p>Looks up and returns the object with the specified key.</p>
     * 
     * <p>See the class documentation for more details.</p>
     *
     * @param key the key of the object requested
     * 
     * @return the requested object or null if not found
     */
    public Object internalGet( String key )
    {
        Object o = null;

        // search the toolbox
        if (toolboxContext != null)
        {
            o = toolboxContext.get(key);
            if (o != null)
            {
                return o;
            }
        }

        // make the four scopes of the Apocalypse Read only
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

        // try the local hashtable
        o = super.internalGet(key);
        if (o != null)
        {
            return o;
        }

        // if not found, wander down the scopes...
        return getAttribute(key);
    }


    /**
     * <p>Searches for the named attribute in request, session (if valid), 
     * and application scope(s) in order and returns the value associated 
     * or null.</p>
     *
     * @since VelocityTools 1.1
     */
    public Object getAttribute(String key)
    {
        Object o = request.getAttribute(key);
        if (o == null)
        {
            if (session != null)
            {
                o = session.getAttribute(key);
            }

            if (o == null)
            {
                o = application.getAttribute(key);
            }
        }
        return o;
    }


    /**
     * <p>Returns the current servlet request.</p>
     */
    public HttpServletRequest getRequest()
    {
        return request;
    }


    /**
     * <p>Returns the current servlet response.</p>
     */
    public HttpServletResponse getResponse()
    {
        return response;
    }


    /**
     * <p>Returns the servlet context.</p>
     */
    public ServletContext getServletContext()
    {
        return application;
    }


    /**
     * <p>Returns a reference to the Velocity context (this object).</p>
     */
    public Context getVelocityContext()
    {
        return this;
    }


}  // ChainedContext

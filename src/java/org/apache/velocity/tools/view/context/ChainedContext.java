/*
 * Copyright 2002-2003 The Apache Software Foundation.
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
 * @version $Id: ChainedContext.java,v 1.6 2004/02/18 20:07:58 nbubna Exp $ 
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

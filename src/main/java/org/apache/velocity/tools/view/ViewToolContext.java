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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.Toolbox;

/**
 * <p>{@link ToolContext} implementation specific to the servlet environment.</p>
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
 * <p>The {@link #get(String key)} method implements the following search order
 * for objects:</p>
 * <ol>
 *   <li>tool in a request scoped toolbox</li>
 *   <li>tool in a session scoped toolbox</li>
 *   <li>tool in a application scoped toolbox</li>
 *   <li>request, response, session, or servlet context</li>
 *   <li>object in the local map of objects (traditional use)</li>
 *   <li>request attributes, session attributes, servlet context attributes</li>
 * </ol>
 *
 * <p>The purpose of this class is to make it easy for web designer to work
 * with Java servlet based web applications. They do not need to be concerned
 * with the concepts of request, session or application attributes and the
 * lifetime of objects in these scopes.</p>
 *
 * <p>Note that the put() method always puts objects into the local map
 * and does not allow tools or servlet classes to be overridden.
 * </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author Nathan Bubna
 *
 * @version $Id: ViewContext.java 514727 2007-03-05 16:49:03Z nbubna $
 */
public class ViewToolContext extends ToolContext implements ViewContext
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ServletContext application;
    private final VelocityEngine velocity;
    private String toolboxKey = DEFAULT_TOOLBOX_KEY;

    public ViewToolContext(VelocityEngine velocity,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext application)
    {
        super(velocity);

        this.velocity = velocity;
        this.request = request;
        this.response = response;
        this.application = application;

        // automagically set common tool properties
        putToolProperties();
    }

    protected void setToolboxKey(String key)
    {
        this.toolboxKey = key;
    }

    protected void putToolProperties()
    {
        putToolProperty(REQUEST, getRequest());
        if (getRequest() != null)
        {
            putToolProperty(LOCALE_KEY, getRequest().getLocale());
        }
        putToolProperty(RESPONSE, getResponse());
        putToolProperty(SESSION, getSession());
        putToolProperty(SERVLET_CONTEXT_KEY, getServletContext());
        putToolProperty(PATH_KEY, ServletUtils.getPath(getRequest()));
    }

    protected List<Toolbox> getToolboxes()
    {
        // this means once we find any toolbox,
        // then we stop looking.  adding boxes to
        // the request/session/application attributes
        // later will not work.  once one is in, any
        // later additions must be via addToolbox(Toolbox)
        // or addToolboxesUnderKey(String toolboxKey)
        if (super.getToolboxes().isEmpty())
        {
            addToolboxesUnderKey(this.toolboxKey);
        }
        return super.getToolboxes();
    }

    protected void addToolboxesUnderKey(String toolboxKey)
    {
        Toolbox reqTools = (Toolbox)getRequest().getAttribute(toolboxKey);
        if (reqTools != null)
        {
            addToolbox(reqTools);
        }

        if (getSession() != null)
        {
            Toolbox sessTools = (Toolbox)getSession().getAttribute(toolboxKey);
            if (sessTools != null)
            {
                addToolbox(sessTools);
            }
        }

        Toolbox appTools = (Toolbox)getServletContext().getAttribute(toolboxKey);
        if (appTools != null)
        {
            addToolbox(appTools);
        }
    }

    /**
     * <p>Looks up and returns the object with the specified key.</p>
     * <p>See the class documentation for more details.</p>
     *
     * @see #setUserCanOverwriteTools
     * @see #getUserVar
     * @see #getToolVar
     * @param key the key of the object requested
     * @return the requested object or null if not found
     */
    @Override
    public Object get(String key)
    {
        boolean overwrite = getUserCanOverwriteTools();
        Object o = overwrite ? getUserVar(key) : getToolVar(key);
        if (o == null)
        {
            o = overwrite ? getToolVar(key) : getUserVar(key);
        }
        return o;
    }

    /**
     * Finds "user" set values, either in the local context
     * or in the scoped attributes if none is in the local context.
     * @see #internalGet
     * @see #getAttribute
     */
    protected Object getUserVar(String key)
    {
        Object o = internalGet(key);
        if (o != null)
        {
            return o;
        }
        return getAttribute(key);
    }

    /**
     * Finds the automatically provided values, either configured
     * tools or servlet API objects (request, response, etc).
     * @see #findTool
     * @see #getServletApi
     */
    protected Object getToolVar(String key)
    {
        Object o = findTool(key);
        if (o != null)
        {
            return o;
        }
        return getServletApi(key);
    }

    /**
     * Returns the current matching servlet request, response, session, 
     * or servlet context instance, or null if the key matches none of those
     * keys.
     */
    protected Object getServletApi(String key)
    {
        if (key.equals(REQUEST))
        {
            return request;
        }
        else if(key.equals(RESPONSE))
        {
            return response;
        }
        else if (key.equals(SESSION))
        {
            return getSession();
        }
        else if (key.equals(APPLICATION))
        {
            return application;
        }
        return null;
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
            if (getSession() != null)
            {
                try
                {
                    o = getSession().getAttribute(key);
                }
                catch (IllegalStateException ise)
                {
                    // Handle invalidated session state
                    o = null;
                }
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
     * <p>Returns the current session, if any.</p>
     */
    public HttpSession getSession()
    {
        return getRequest().getSession(false);
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

    /**
     * <p>Returns a reference to the VelocityEngine.</p>
     */
    public VelocityEngine getVelocityEngine()
    {
        return velocity;
    }

     /**
      * Indicates whether the specified key is in the context.
      *
      * @param key The key to look for.
      * @return    Whether the key is in the context.
      */
     public boolean containsKey(String key)
     {
         return super.containsKey(key)
           || getAttribute(key) != null
           || key.equals(REQUEST) && request != null
           || key.equals(RESPONSE) && response != null
           || key.equals(SESSION) && getSession() != null
           || key.equals(APPLICATION) && application != null;
     }

}

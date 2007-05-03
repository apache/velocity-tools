package org.apache.velocity.tools.view.context;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.context.ViewContext;

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
 * @deprecated Use {@link ViewToolContext} instead
 * @version $Id$
 */
public class ChainedContext extends ViewToolContext implements ViewContext
{
    private Map<String,Object> oldToolbox;

    public ChainedContext(VelocityEngine velocity,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext application)
    {
        super(velocity, request, response, application);
    }

    public ChainedContext(Context ctx,
                          VelocityEngine velocity,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext application)
    {
        this(velocity, request, response, application);

        if (ctx != null)
        {
            // copy all values from the context into this
            for (Object key : ctx.getKeys())
            {
                String skey = String.valueOf(key);
                put(skey, ctx.get(skey));
            }
        }
    }


    /**
     * Sets a "toolbox" in the style of VelocityTools 1.x.
     * @deprecated
     */
    public void setToolbox(Map<String,Object> box)
    {
        this.oldToolbox = box;
    }

    /**
     * <p>Returns the tools for this context</p>
     * @since VelocityTools 1.3
     * @return any tools passed in via setToolbox() plus the results
     *         of {@link ViewToolContext#getToolbox()}.
     */
    public Map<String,Object> getToolbox()
    {
        if (this.oldToolbox != null)
        {
            Map<String,Object> box = new HashMap<String,Object>(this.oldToolbox);
            box.putAll(super.getToolbox());
            return box;
        }
        return super.getToolbox();
    }

    protected Object internalGet( String key )
    {
        Object o = null;

        /* search the toolbox */
        if (oldToolbox != null)
        {
            o = oldToolbox.get(key);
            if (o != null)
            {
                return o;
            }
        }

        /* try the local hashtable */
        return super.internalGet(key);
    }

}

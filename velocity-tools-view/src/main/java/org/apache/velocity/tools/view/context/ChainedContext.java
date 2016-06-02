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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.context.ViewContext;

/**
 * @deprecated Use {@link ViewToolContext} instead
 * @version $Id$
 */
@Deprecated
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

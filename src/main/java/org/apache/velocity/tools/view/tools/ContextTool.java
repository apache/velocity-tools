package org.apache.velocity.tools.view.tools;

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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.tools.view.ViewToolContext;

/**
 * <p>View tool for convenient access to {@link ViewContext} data and
 *  meta-data.</p>
 * <p><pre>
 * Template example(s):
 *  #foreach( $key in $context.keys )
 *    $key = $context.get($key)
 *  #end
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;context&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.view.tools.ContextTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This class is only designed for use as a request-scope VelocityView tool.</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 1.3
 * @version $Id: ContextTool.java 385122 2006-03-11 18:37:42Z nbubna $
 */
@DefaultKey("context")
@InvalidScope({"application","session"})
public class ContextTool
{
    /**
     * The key used for specifying whether to hide keys with '.' in them.
     */
    public static final String SAFE_MODE_KEY = "safe-mode";

    protected ViewContext context;
    protected Map toolbox;
    protected HttpServletRequest request;
    protected HttpSession session;
    protected ServletContext application;

    private boolean safeMode = true;


    /**
     * Initializes this instance for the current request.
     * Also looks for a safe-mode configuration setting. By default,
     * safe-mode is true and thus keys with '.' in them are hidden.
     */
    public void configure(Map params)
    {
        if (params != null)
        {
            ValueParser parser = new ValueParser(params);
            safeMode = parser.getBoolean(SAFE_MODE_KEY, true);
        }

        this.context = (ViewContext)params.get(ViewToolContext.CONTEXT_KEY);
        this.request = (HttpServletRequest)params.get(ViewContext.REQUEST);
        this.session = request.getSession(false);
        this.application = (ServletContext)params.get(ViewContext.SERVLET_CONTEXT_KEY);
    }


    /**
     * Returns the context being analyzed by this tool.
     */
    public ViewContext getThis()
    {
        return this.context;
    }

    /**
     * <p>Returns a read-only view of the toolbox {@link Map}
     * for this context.</p>
     * @return a map of all available tools for this request
     *         or {@code null} if such a map is not available
     */
    public Map getToolbox()
    {
        if (this.toolbox == null && this.context instanceof ViewToolContext)
        {
            this.toolbox = ((ViewToolContext)context).getToolbox();
        }
        return this.toolbox;
    }

    /**
     * <p>Return a {@link Set} of the available reference keys in the current
     * context.</p>
     */
    public Set getKeys()
    {
        Set keys = new HashSet();

        // get the tool keys, if there is a toolbox
        Map tools = getToolbox();
        if (tools != null)
        {
            keys.addAll(tools.keySet());
        }

        // recurse down the velocity context collecting keys
        Context velctx = this.context.getVelocityContext();
        while (velctx != null)
        {
            Object[] ctxKeys = velctx.getKeys();
            keys.addAll(Arrays.asList(ctxKeys));
            if (velctx instanceof AbstractContext)
            {
                velctx = ((AbstractContext)velctx).getChainedContext();
            }
            else
            {
                velctx = null;
            }
        }

        // get request attribute keys
        Enumeration e = request.getAttributeNames();
        while (e.hasMoreElements())
        {
            keys.add(e.nextElement());
        }

        // get session attribute keys if we have a session
        if (session != null)
        {
            e = session.getAttributeNames();
            while (e.hasMoreElements())
            {
                keys.add(e.nextElement());
            }
        }

        // get request attribute keys
        e = application.getAttributeNames();
        while (e.hasMoreElements())
        {
            keys.add(e.nextElement());
        }

        // if we're in safe mode, remove keys that contain '.'
        if (safeMode)
        {
            for (Iterator i = keys.iterator(); i.hasNext(); )
            {
                String key = String.valueOf(i.next());
                if (key.indexOf('.') >= 0)
                {
                    i.remove();
                }
            }
        }

        // return the key set
        return keys;
    }

    /**
     * <p>Return a {@link Set} of the available values in the current
     * context.</p>
     */
    public Set getValues()
    {
        //TODO: this could be a lot more efficient
        Set keys = getKeys();
        Set values = new HashSet(keys.size());
        for (Iterator i = keys.iterator(); i.hasNext(); )
        {
            String key = String.valueOf(i.next());
            values.add(this.context.getVelocityContext().get(key));
        }
        return values;
    }


    /**
     * <p>Returns {@code true} if the context contains a value for the specified
     * reference name (aka context key).</p>
     */
    public boolean contains(Object refName)
    {
        return (get(refName) != null);
    }

    /**
     * Retrieves the value for the specified reference name (aka context key).
     */
    public Object get(Object refName)
    {
        String key = String.valueOf(refName);
        if (safeMode && key.indexOf('.') >= 0)
        {
            return null;
        }
        return this.context.getVelocityContext().get(key);
    }

}

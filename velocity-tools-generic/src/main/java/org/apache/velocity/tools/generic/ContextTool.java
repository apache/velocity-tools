package org.apache.velocity.tools.generic;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * <p>Tool for convenient access to {@link Context} data and
 *  meta-data.</p>
 * <p><pre>
 * Template example(s):
 *  #foreach( $key in $context.keys )
 *    $key = $context.get($key)
 *  #end
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ContextTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This class is only designed for use as a request-scope tool.</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id: ContextTool.java 385122 2006-03-11 18:37:42Z nbubna $
 */
@DefaultKey("context")
@InvalidScope({Scope.APPLICATION,Scope.SESSION})
public class ContextTool extends SafeConfig
{
    protected Context context;
    protected Map<String,Object> toolbox;

    /**
     * Initializes this instance for the current request.
     * Also looks for a safe-mode configuration setting. By default,
     * safeMode is true and thus keys with '.' in them are hidden.
     */
    protected void configure(ValueParser parser)
    {
        this.context = (Context)parser.getValue(ToolContext.CONTEXT_KEY);
    }


    /**
     * Returns the context being analyzed by this tool.
     */
    public Context getThis()
    {
        return this.context;
    }

    /**
     * <p>Returns a read-only view of the toolbox {@link Map}
     * for this context.</p>
     * @return a map of all available tools for this request
     *         or {@code null} if such a map is not available
     */
    public Map<String,Object> getToolbox()
    {
        if (this.toolbox == null && this.context instanceof ToolContext)
        {
            this.toolbox = ((ToolContext)context).getToolbox();
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

        // fill the keyset in extendable method
        fillKeyset(keys);

        // if we're in safe mode, remove keys that contain '.'
        if (isSafeMode())
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
     * Actually do the work of filling in the set of keys
     * for {@link #getKeys} here so subclasses can add keys too.
     */
    protected void fillKeyset(Set keys)
    {
        //NOTE: we don't need to manually add the toolbox keys here
        //      because retrieval of those depends on the context being
        //      a ToolContext which would already give tool keys below

        // recurse down the velocity context collecting keys
        Context velctx = this.context;
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
            values.add(this.context.get(key));
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
        if (isSafeMode() && key.indexOf('.') >= 0)
        {
            return null;
        }
        return this.context.get(key);
    }

}

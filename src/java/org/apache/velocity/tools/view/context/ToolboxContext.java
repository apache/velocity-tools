/*
 * Copyright 2003 The Apache Software Foundation.
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

import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

/**
 * <p>Read-only context used to carry a set of view tools.</p>
 *
 * <p>Writes get dropped.</p>
 *
 * @author <a href="mailto:sidler@apache.org">Gabriel Sidler</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ToolboxContext.java,v 1.3 2004/02/18 20:07:58 nbubna Exp $ 
 */
public class ToolboxContext extends VelocityContext
{
    /**
     * The collection of view tools in this toolbox.
     */
    private Map toolbox;


    /**
     * Default constructor.
     */
    public ToolboxContext( Map tb )
    {
        toolbox = tb;
    }


    /**
     * Get value for key.
     */
    public Object internalGet( String key )
    {
        return toolbox.get( key );
    }        


    /**
     * Does nothing. Returns <code>null</code> always.
     */
    public Object internalPut( String key, Object value )
    {
        return null;
    }
    
}

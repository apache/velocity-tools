package org.apache.velocity.tools.view.servlet;

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

import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.tools.view.ViewToolInfo;


/**
 * <p>ToolInfo implementation that holds scope information for tools
 * used in a servlet environment.  The ServletToolboxManager uses
 * this to allow tool definitions to specify the scope/lifecycle
 * of individual view tools.</p>
 *
 * <p>Example of toolbox.xml definitions for servlet tools:<pre>
 *  &lt;tool&gt;
 *    &lt;key&gt;link&lt;/key&gt;
 *    &lt;scope&gt;request&lt;/scope&gt;
 *    &lt;class&gt;org.apache.velocity.tools.struts.StrutsLinkTool&lt;/class&gt;
 *  &lt;/tool&gt;
 *  &lt;tool&gt;
 *    &lt;key&gt;math&lt;/key&gt;
 *    &lt;scope&gt;application&lt;/scope&gt;
 *    &lt;class&gt;org.apache.velocity.tools.generic.MathTool&lt;/class&gt;
 *  &lt;/tool&gt;
 *  &lt;tool&gt;
 *    &lt;key&gt;user&lt;/key&gt;
 *    &lt;scope&gt;session&lt;/scope&gt;
 *    &lt;class&gt;com.mycompany.tools.MyUserTool&lt;/class&gt;
 *  &lt;/tool&gt;
 *  </pre></p>
 *
 * @author Nathan Bubna
 *
 * @version $Id$
 */
public class ServletToolInfo extends ViewToolInfo
{

    private String scope;
    private boolean exactPath;
    private String path;


    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return the scope of the tool
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * @param path the full or partial request path restriction of the tool
     * @since VelocityTools 1.3
     */
    public void setRequestPath(String path)
    {
        // make sure all paths begin with slash
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        if (path.equals("/*"))
        {
            // match all paths
            this.path = null;
        }
        else if(path.endsWith("*"))
        {
            // match some paths
            exactPath = false;
            this.path = path.substring(0, scope.length() - 1);
        }
        else
        {
            // match one path
            exactPath = true;
            this.path = path;
        }
    }

    /**
     * @return request path restriction for this tool
     * @since VelocityTools 1.3
     */
    public String getRequestPath()
    {
        return this.path;
    }

    /**
     * @param requestedPath the path of the current servlet request
     * @return <code>true</code> if the path of the specified
     *         request path matches the request path of this tool.
     *         If there is no request path restriction for this tool,
     *         it will always return <code>true</code>.
     * @since VelocityTools 1.3
     */
    public boolean allowsRequestPath(String requestedPath)
    {
        if (this.path == null)
        {
            return true;
        }

        if (exactPath)
        {
            return this.path.equals(requestedPath);
        }
        else if (requestedPath != null)
        {
            return requestedPath.startsWith(this.path);
        }
        return false;
    }

}

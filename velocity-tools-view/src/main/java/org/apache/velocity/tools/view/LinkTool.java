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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ServletUtils;

/**
 * <p>The LinkTool provides many methods to work with URIs and can help you:
 * <ul>
 *     <li>construct full URIs (absolute or relative)</li>
 *     <li>encode session ID into a URI</li>
 *     <li>retrieve server, port and path info for the current request</li>
 *     <li>reconstruct or alter the current request URI</li>
 *     <li>and more..</li>
 * </ul>
 *
 * <p>The LinkTool is somewhat special in that nearly all public methods return
 * a new instance of LinkTool. This facilitates greatly the repeated use
 * of the LinkTool in Velocity and leads to an elegant syntax.</p>
 *
 * <p>Template example(s):</p><pre>
 *
 *   #set( $base = $link.path('MyPage.vm').anchor('view') )
 *   &lt;a href="$base.param('select','this')"&gt;this&lt;/a&gt;
 *   &lt;a href="$base.param('select','that')"&gt;that&lt;/a&gt;
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.LinkTool"
 *              forceRelative="true" includeRequestParams="true"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>This tool may only be used in the request scope.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author Nathan Bubna
 * @author Chris Schultz
 * @since VelocityTools 2.0
 * @version $Id$
 */

public class LinkTool extends org.apache.velocity.tools.generic.LinkTool
{
    private static final long serialVersionUID = 6814069794929110755L;

    public static final String INCLUDE_REQUEST_PARAMS_KEY = "includeRequestParams";

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected boolean includeRequestParams;

    public LinkTool()
    {
        super();
        includeRequestParams = false;
    }

    // --------------------------------------- Setup Methods -------------

    @Override
    protected void configure(ValueParser props)
    {
        // request values override configured defaults
        //NOTE: not sure this is the most intuitive way in all cases;
        // it might make sense to provide the option of whether req/res
        // values override configured ones or vice versa.
        super.configure(props);

        this.request = (HttpServletRequest)props.getValue(ViewContext.REQUEST);
        Boolean incParams = props.getBoolean(INCLUDE_REQUEST_PARAMS_KEY);
        if (incParams != null)
        {
            setIncludeRequestParams(incParams);
        }

        // set default/start values from request & response
        this.response =
            (HttpServletResponse)props.getValue(ViewContext.RESPONSE);
        setCharacterEncoding(response.getCharacterEncoding());
        setFromRequest(this.request);
    }

    protected void setFromRequest(HttpServletRequest request)
    {
        setScheme(request.getScheme());
        setPort(request.getServerPort());
        setHost(request.getServerName());
        // the path we get from ViewToolContext lacks the context path
        String ctx = request.getContextPath();
        String pth = ServletUtils.getPath(request);
        setPath(combinePath(ctx, pth));
        if (this.includeRequestParams)
        {
            setQuery(request.getParameterMap());
        }
    }

    /**
     * <p>Controls whether or not this tool starts off with all parameters
     * from the last request automatically.  Default is false.</p>
     * @param includeRequestParams whether or not to keep current request params
     */
    public void setIncludeRequestParams(boolean includeRequestParams)
    {
        this.includeRequestParams = includeRequestParams;
    }

    /**
     * Adds the specified parameters (if they exist) from the current
     * request to the query data of a copy of this instance.
     * If no parameters are specified,
     * then all of the current request's parameters will be added.
     *
     * @param butOnlyThese curent request parameters to keep
     * @return A LinkTool object with the specified parameters from
     *         the current request added to it or all the params if none specified.
     */
    public LinkTool addRequestParams(Object... butOnlyThese)
    {
        return addRequestParams(false, butOnlyThese);
    }

    /**
     * Adds all of the current request's parameters to this link's
     * "query data" except for those whose keys match any of the specified strings.
     *
     * @param ignoreThese curent request parameters to ignore
     * @return A LinkTool object with all of the current request's parameters
     *         added to it, except those specified.
     */
    public LinkTool addRequestParamsExcept(Object... ignoreThese)
    {
        return addRequestParams(true, ignoreThese);
    }

    /**
     * Adds all of the current request's parameters to this link's
     * "query data" except for those whose keys match any of the specified strings
     * or already have a value set for them in the current instance.
     *
     * @param ignoreThese curent request parameters to ignore
     * @return A LinkTool object with all of the current request's parameters
     *         added to it, except those specified or those that already have
     *         values.
     */
    public LinkTool addMissingRequestParams(Object... ignoreThese)
    {
        Object[] these;
        if (query != null && !query.isEmpty())
        {
            Set keys = query.keySet();
            these = new Object[keys.size() + ignoreThese.length];
            int i = 0;
            for (; i < ignoreThese.length; i++)
            {
                these[i] = ignoreThese[i];
            }
            for (Iterator iter = keys.iterator(); iter.hasNext(); i++)
            {
                these[i] = String.valueOf(iter.next());
            }
        }
        else
        {
            these = ignoreThese;
        }
        return addRequestParams(true, these);
    }

    private LinkTool addRequestParams(boolean ignore, Object... special)
    {
        LinkTool copy = (LinkTool)duplicate(true);
        Map reqParams = request.getParameterMap();
        boolean noSpecial = (special == null || special.length == 0);
        for (Object e : reqParams.entrySet())
        {
            Map.Entry entry = (Map.Entry)e;
            String key = String.valueOf(entry.getKey());
            boolean isSpecial = (!noSpecial && contains(special, key));
            // we actually add the parameter only under three cases:
            //  take all     not one being ignored     one of the privileged few
            if (noSpecial || (ignore && !isSpecial) || (!ignore && isSpecial))
            {
                // this is not one being ignored
                copy.setParam(key, entry.getValue(), this.appendParams);
            }
        }
        return copy;
    }

    private boolean contains(Object[] set, String name)
    {
        for (Object i : set)
        {
            if (name.equals(i))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isPathChanged()
    {
        if (this.path == self.getPath())
        {
            return false;
        }
        if (this.path == null)
        {
            return true;
        }
        return (!this.path.equals(self.getPath()));
    }

    /**
     * <p>Initially, this returns the context path that addresses this web
     * application, e.g. <code>/myapp</code>. This string starts
     * with a "/" but does not end with a "/".  If the path has been
     * changed (e.g. via a call to {@link #path(Object)}), then this will
     * simply be the first "directory" in the path (i.e. everything from
     * the start up to the second backslash).
     * @return context path
     * @see #relative(Object)
     */
    @Override
    public String getContextPath()
    {
        if (!isPathChanged())
        {
            return request.getContextPath();
        }
        if (this.path == null || this.opaque)
        {
            return null;
        }
        int firstInternalSlash = this.path.indexOf('/', 1);
        if (firstInternalSlash <= 0)
        {
            return this.path;
        }
        return this.path.substring(0, firstInternalSlash);
    }

    /**
     * <p>Initially, this retrieves the path for the current
     * request regardless of
     * whether this is a direct request or an include by the
     * RequestDispatcher. This string should always start with
     * a "/".  If the path has been changed (e.g. via a call to
     * {@link #path(Object)}), then this will
     * simply be everything in the path after the {@link #getContextPath()}
     * (i.e. the second "/" in the path and everything after).
     * @return request path
     */
    public String getRequestPath()
    {
        if (this.path == null || this.opaque)
        {
            return null;
        }
        if (!isPathChanged())
        {
            return ServletUtils.getPath(request);
        }
        int firstInternalSlash = this.path.indexOf('/', 1);
        if (firstInternalSlash <= 0)
        {
            return this.path;
        }
        return this.path.substring(firstInternalSlash, this.path.length());
    }

    /**
     * <p>Returns a URL that addresses the web application. (e.g.
     * <code>http://myserver.net/myapp/</code>.
     * This essentially just replaces the full path with
     * the {@link #getContextPath()} and removes the anchor and query
     * data.
     * @return context URL
     */
    public String getContextURL()
    {
        LinkTool copy = (LinkTool)duplicate();
        copy.setQuery(null);
        copy.setFragment(null);
        copy.setPath(getContextPath());
        return copy.toString();
    }

    /**
     * Overrides to use response.encodeURL to get session id into URL
     * if sessions are used but cookies are not supported.
     * @return final string representation
     */
    @Override
    public String toString()
    {
        String str = super.toString();
        if (str.length() == 0)
        {
            // avoid a potential NPE from Tomcat's response.encodeURL impl
            return str;
        }
        else
        {
            // encode session ID into URL if sessions are used but cookies are
            // not supported
            return response.encodeURL(str);
        }
    }

}

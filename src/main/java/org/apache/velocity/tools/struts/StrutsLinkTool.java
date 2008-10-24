package org.apache.velocity.tools.struts;

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

import javax.servlet.ServletContext;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.LinkTool;
import org.apache.velocity.tools.view.ViewContext;

/**
 * <p>The StrutsLinkTool extends the standard {@link LinkTool} to add methods
 * for working with Struts' Actions and Forwards:</p>
 * <ul>
 *     <li>translate logical names (Struts forwards, actions ) to URI references, see
 *       methods {@link #setAction} and {@link #setForward}</li>
 * </ul>
 * 
 * <p><pre>
 * Template example(s):
 *   &lt;a href="$link.action.update"&gt;update something&lt;/a&gt;
 *   #set( $base = $link.forward.MyPage.anchor('view') )
 *   &lt;a href="$base.param('select','this')"&gt;view this&lt;/a&gt;
 *   &lt;a href="$base.param('select','that')"&gt;view that&lt;/a&gt;
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.struts.StrutsLinkTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This tool may only be used in the request scope.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author Nathan Bubna
 *
 * @version $Id$
 */
public class StrutsLinkTool extends LinkTool
{
    protected ServletContext application;
    private String get;

    @Override
    protected void configure(ValueParser props)
    {
        // request values override configured defaults 
        //NOTE: not sure this is the most intuitive way in all cases;
        // it might make sense to provide the option of whether req/res
        // values override configured ones or vice versa.
        super.configure(props);

        this.application = (ServletContext)props.getValue(ViewContext.SERVLET_CONTEXT_KEY);
    }

    /**
     * <p>This exists to enable a simplified syntax for using this tool in a
     * template. Now, users can do <code>$link.action.saveFoo</code> instead of
     * <code>$link.setAction('saveFoo')</code> and
     * <code>$link.forward.profile</code> instead of
     * <code>$link.setForward('profile')</code>. Neat, eh? :)</p>
     * @since VelocityTools 1.3
     */
    public StrutsLinkTool get(String getme)
    {
        StrutsLinkTool sub = null;
        if ("action".equalsIgnoreCase(this.get))
        {
            sub = setAction(getme);
        }
        else if ("forward".equalsIgnoreCase(this.get))
        {
            sub = setForward(getme);
        }
        else
        {
            sub = (StrutsLinkTool)this.duplicate();
        }
        if (sub == null)
        {
            return null;
        }
        sub.get = getme;
        return sub;
    }


    /**
     * <p>Returns a copy of the link with the given action name
     * converted into a server-relative URI reference. This method
     * does not check if the specified action really is defined.
     * This method will overwrite any previous URI reference settings
     * but will copy the query string.</p>
     *
     * @param action an action path as defined in struts-config.xml
     *
     * @return a new instance of StrutsLinkTool
     */
    public StrutsLinkTool setAction(String action)
    {
        String url =
            StrutsUtils.getActionMappingURL(application, request, action);
        if (url == null)
        {
            debug("StrutsLinkTool : In method setAction("+action+
                  "): Parameter does not map to a valid action.");
            return null;
        }
        return (StrutsLinkTool)absolute(url);
    }


    /**
     * <p>Returns a copy of the link with the given global or local forward
     * name converted into a server-relative URI reference. If the parameter
     * does not map to an existing global forward name, <code>null</code>
     * is returned. This method will overwrite any previous URI reference
     * settings but will copy the query string.</p>
     *
     * @param forward a forward name as defined in struts-config.xml
     *                in either global-forwards or in the currently executing
     *                action mapping.
     *
     * @return a new instance of StrutsLinkTool
     */
    public StrutsLinkTool setForward(String forward)
    {
        String url = StrutsUtils.getForwardURL(request, application, forward);
        if (url == null)
        {
            debug("StrutsLinkTool : In method setForward(" + forward +
                  "): Parameter does not map to a valid forward.");
            return null;
        }
        return (StrutsLinkTool)absolute(url);
    }

}

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

import java.util.Enumeration;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import org.apache.velocity.tools.generic.ContextTool;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ViewContext;

/**
 * <p>Extension of {@link ContextTool} that includes keys and values
 * from the {@link HttpServletRequest}, {@link HttpSession} and
 * {@link ServletContext}.</p>
 * <p><pre>
 * Template example(s):
 *  #foreach( $key in $context.keys )
 *    $key = $context.get($key)
 *  #end
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.ViewContextTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This class is only designed for use as a request-scope VelocityView tool.</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id: ViewContextTool.java 385122 2006-03-11 18:37:42Z nbubna $
 */
public class ViewContextTool extends ContextTool
{
    protected HttpServletRequest request;
    protected HttpSession session;
    protected ServletContext application;


    @Override
    protected void configure(ValueParser parser)
    {
        // do ContextTool config first
        super.configure(parser);

        this.request = (HttpServletRequest)parser.getValue(ViewContext.REQUEST);
        this.session = request.getSession(false);
        this.application = (ServletContext)parser.getValue(ViewContext.SERVLET_CONTEXT_KEY);
    }

    @Override
    protected void fillKeyset(Set keys)
    {
        // start with the standard ContextTool's keys
        super.fillKeyset(keys);

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
    }

}

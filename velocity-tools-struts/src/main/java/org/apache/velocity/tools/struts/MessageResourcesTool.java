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

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import org.slf4j.Logger;

import org.apache.struts.util.MessageResources;
import org.apache.velocity.tools.view.ViewContext;

/**
 * <p>Abstract view tool that provides access to Struts' message resources.</p>
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @since VelocityTools 1.1
 * @version $Id$
 */
public abstract class MessageResourcesTool
{
    protected Logger LOG;
    protected ServletContext application;
    protected HttpServletRequest request;
    private Locale locale;
    private MessageResources resources;

    @Deprecated
    public void init(Object obj)
    {
        if (obj instanceof ViewContext)
        {
            ViewContext ctx = (ViewContext)obj;
            this.request = ctx.getRequest();
            this.application = ctx.getServletContext();
            this.LOG = ctx.getVelocityEngine().getLog();
        }
    }

    /**
     * Initializes this tool.
     *
     * @param params the Map of configuration parameters
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void configure(Map params)
    {
        this.request = (HttpServletRequest)params.get(ViewContext.REQUEST);
        this.application = (ServletContext)params.get(ViewContext.SERVLET_CONTEXT_KEY);
        this.LOG = (Logger)params.get("log");
    }


    /**
     * Retrieves the {@link Locale} for this request.
     * @since VelocityTools 2.0
     */
    protected Locale getLocale()
    {
        if (this.locale == null)
        {
            this.locale =
                StrutsUtils.getLocale(request, request.getSession(false));
        }
        return this.locale;
    }


    /**
     * Retrieves the specified {@link MessageResources} bundle, or the
     * application's default MessageResources if no bundle is specified.
     * @since VelocityTools 1.1
     */
    protected MessageResources getResources(String bundle)
    {
        if (bundle == null)
        {
            if (this.resources == null)
            {
                this.resources =
                    StrutsUtils.getMessageResources(request, application);
                if (this.resources == null)
                {
                    LOG.error("MessageResourcesTool : Message resources are not available.");
                }
            }
            return resources;
        }

        MessageResources res =
            StrutsUtils.getMessageResources(request, application, bundle);
        if (res == null)
        {
            LOG.error("MessageResourcesTool : MessageResources bundle '{}' is not available.", bundle);
        }
        return res;
    }

}

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.struts;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import org.apache.struts.util.MessageResources;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * <p>Abstract view tool that provides access to Struts' message resources.</p>
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @since VelocityTools 1.1
 * @version $Id: MessageResourcesTool.java,v 1.2 2004/01/07 19:07:38 nbubna Exp $
 */
public abstract class MessageResourcesTool implements ViewTool
{

    protected ServletContext application;
    protected HttpServletRequest request;
    protected Locale locale;
    protected MessageResources resources;


    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        if (!(obj instanceof ViewContext))
        {
            throw new IllegalArgumentException("Tool can only be initialized with a ViewContext");
        }

        ViewContext context = (ViewContext)obj;
        this.request = context.getRequest();
        this.application = context.getServletContext();
        this.resources = 
            StrutsUtils.getMessageResources(request, application);
        this.locale = 
            StrutsUtils.getLocale(request, request.getSession(false));
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
            if (resources == null) 
            {
                Velocity.error("MessageResourcesTool: Message resources are not available.");
            }
            return resources;
        }
        
        MessageResources res = 
            StrutsUtils.getMessageResources(request, application, bundle);
        if (res == null)
        {
            Velocity.error("MessageResourcesTool: MessageResources bundle '"
                           + bundle + "' is not available.");
        }
        return res;
    }

}

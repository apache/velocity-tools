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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.*;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * <p>View tool to work with HTML forms in Struts.</p> 
 * <p><pre>
 * Template example(s):
 *  &lt;input type="hidden" name="$form.tokenName" value="$form.token"&gt;
 *  &lt;input type="submit" name="$form.cancelName" value="Cancel"&gt;
 *
 * Toolbox configuration:
 *
 * &lt;tool&gt;
 *   &lt;key&gt;form&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.FormTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @since VelocityTools 1.0
 * @version $Id: FormTool.java,v 1.5 2003/11/06 18:03:43 nbubna Exp $
 */
public class FormTool implements ViewTool
{

    // --------------------------------------------- Properties ---------------

    
    /**
     * A reference to the HtttpServletRequest.
     */ 
    protected HttpServletRequest request;
    

    /**
     * A reference to the HtttpSession.
     */ 
    protected HttpSession session;


    
    // --------------------------------------------- Constructors -------------

    /**
     * Default constructor. Tool must be initialized before use.
     */
    public FormTool()
    {
    }
    
    
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
        this.session = request.getSession(false);
    }



    // --------------------------------------------- View Helpers -------------

    /**
     * <p>Returns the form bean associated with this action mapping.</p>
     *
     * <p>This is a convenience method. The form bean is automatically 
     * available in the Velocity context under the name defined in the 
     * Struts configuration.</p> 
     * 
     * <p>If the form bean is used repeatedly, it is recommended to create a 
     * local variable referencing the bean rather than calling getBean()
     * multiple times.</p>
     * 
     * <pre>   
     * Example:
     * #set ($defaults = $form.bean) 
     * &lt;input type="text" name="username" value="$defaults.username"&gt;
     * </pre>
     *
     * @return the {@link ActionForm} associated with this request or 
     * <code>null</code> if there is no form bean associated with this mapping
     */
    public ActionForm getBean()
    {
        return StrutsUtils.getActionForm(request, session);    
    }
    

    /**
     * <p>Returns the query parameter name under which a cancel button press 
     * must be reported if form validation is to be skipped.</p> 
     *
     * <p>This is the value of 
     * <code>org.apache.struts.taglib.html.Constants.CANCEL_PROPERTY</code></p>
     */
    public String getCancelName()
    {
        return org.apache.struts.taglib.html.Constants.CANCEL_PROPERTY;
    }
    

    /**
     * Returns the transaction control token for this session or 
     * <code>null</code> if no token exists.
     */
    public String getToken()
    {
        return StrutsUtils.getToken(session);
    }


    /**
     * <p>Returns the query parameter name under which a transaction token
     * must be reported. This is the value of 
     * <code>org.apache.struts.taglib.html.Constants.TOKEN_KEY</code></p>
     */
    public String getTokenName()
    {
        return org.apache.struts.taglib.html.Constants.TOKEN_KEY;
    }

}

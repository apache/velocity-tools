/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
import org.apache.velocity.tools.view.tools.ContextTool;


/**
 * <p>Context tool to work with forms in Struts. Extends ServletContextTool 
 * to profit from the logging facilities of that class.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: FormTool.java,v 1.1 2002/03/12 11:36:49 sidler Exp $
 * 
 */
public class FormTool extends ServletContextTool
{

    // --------------------------------------------- Private Properties -------

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
     * Returns a factory. Use method {@link #init(ViewContext context)} to 
     * obtain instances of this class.
     */
    public FormTool()
    {
    }
    
    
    /**
     * For internal use only! Use method {@link #init(ViewContext context)} 
     * to obtain instances of the tool.
     */
    private FormTool(ViewContext context)
    {
        this.request = context.getRequest();
        this.session = request.getSession(false);
        this.application = context.getServletContext();    
    }
    


    // --------------------------------------------- ContextTool Interface ----

    /**
     * A new tool object will be instantiated per-request by calling 
     * this method. A context tool is effectively a factory used to 
     * create objects for use in templates. Some tools may simply return
     * themselves from this method others may instantiate new objects
     * to hold the per-request state.
     */
    public Object init(ViewContext context)
    {
        return new FormTool(context);
    }


    /**
     * Perform any cleanup needed. This method is called after the template
     * has been processed.
     */
    public void destroy(Object o)
    {
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
     * &lt;input type="text" name="username" value="$form.username"&gt;
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
     * must be reported if form validation is to be skipped. This is the value
     * of <code>org.apache.struts.taglib.html.Constants.CANCEL_PROPERTY</code></p>
     */
    public String getCancelName()
    {
        return StrutsUtils.getCancelName();
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
        return StrutsUtils.getTokenName();
    }

}

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


package org.apache.velocity.tools.view.servlet;


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
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: ServletToolInfo.java,v 1.4 2003/07/22 18:31:30 nbubna Exp $
 */
public class ServletToolInfo extends ViewToolInfo
{

    /** @deprecated use ServletToolboxManager.REQUEST_SCOPE */
    public static final String REQUEST_SCOPE = ServletToolboxManager.REQUEST_SCOPE;
    /** @deprecated use ServletToolboxManager.SESSION_SCOPE */
    public static final String SESSION_SCOPE = ServletToolboxManager.SESSION_SCOPE;
    /** @deprecated use ServletToolboxManager.APPLICATION_SCOPE */
    public static final String APPLICATION_SCOPE = ServletToolboxManager.APPLICATION_SCOPE;
        
    private String scope;


    public ServletToolInfo() {}


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

}

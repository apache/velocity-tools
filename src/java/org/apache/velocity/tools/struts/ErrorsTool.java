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

import org.apache.struts.action.ActionErrors;

/**
 * <p>View tool to work with the Struts error messages.</p>
 * <p><pre>
 * Template example(s):
 *   #if( $errors.exist() )
 *     &lt;div class="errors"&gt;
 *     #foreach( $e in $errors.all )
 *       $e &lt;br&gt;
 *     #end
 *     &lt;/div&gt;
 *   #end
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;errors&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.ErrorsTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 * <p>Since VelocityTools 1.1, ErrorsTool extends ActionMessagesTool.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @since VelocityTools 1.0
 * @version $Id: ErrorsTool.java,v 1.11 2004/01/08 18:51:06 nbubna Exp $
 */
public class ErrorsTool extends ActionMessagesTool
{
    
    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        //setup superclass instance members
        super.init(obj);

        this.actionMsgs = StrutsUtils.getActionErrors(this.request);
    }


    /**
     * <p>Renders the queued error messages as a list. This method expects
     * the message keys <code>errors.header</code> and <code>errors.footer</code>
     * in the message resources. The value of the former is rendered before
     * the list of error messages and the value of the latter is rendered
     * after the error messages.</p>
     * 
     * @return The formatted error messages. If no error messages are queued, 
     * an empty string is returned.
     */
    public String getMsgs()
    {
        return getMsgs(null, null);    
    }


    /**
     * <p>Renders the queued error messages of a particual category as a list. 
     * This method expects the message keys <code>errors.header</code> and 
     * <code>errors.footer</code> in the message resources. The value of the 
     * former is rendered before the list of error messages and the value of 
     * the latter is rendered after the error messages.</p>
     * 
     * @param property the category of errors to render
     * 
     * @return The formatted error messages. If no error messages are queued, 
     * an empty string is returned. 
     */
    public String getMsgs(String property)
    {
        return getMsgs(property, null);
    }


    /**
     * <p>Renders the queued error messages of a particual category as a list. 
     * This method expects the message keys <code>errors.header</code> and 
     * <code>errors.footer</code> in the message resources. The value of the 
     * former is rendered before the list of error messages and the value of 
     * the latter is rendered after the error messages.</p>
     * 
     * @param property the category of errors to render
     * @param bundle the message resource bundle to use
     * 
     * @return The formatted error messages. If no error messages are queued, 
     * an empty string is returned. 
     * @since VelocityTools 1.1
     */
    public String getMsgs(String property, String bundle)
    {
        return StrutsUtils.errorMarkup(property, bundle, request, 
                                       request.getSession(false), application);
    }


    /**
     * Overrides {@link ActionMessagesTool#getGlobalName()}
     * to return the "global" key for action errors.
     *
     * @see org.apache.struts.action.ActionErrors.GLOBAL_ERROR
     * @deprecated This will be removed after VelocityTools 1.1.
     */
    public String getGlobalName()
    {
        return ActionErrors.GLOBAL_ERROR;
    }

}

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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


package org.apache.velocity.tools.view.tools;

import org.apache.velocity.tools.view.context.ViewContext;


/**
 * <p>An interface for Velocity view tools in a servlet environment.</p>
 * 
 * <p>View tools that implement this interface receive special treatment
 * by a compatible toolbox manager, e.g. 
 * {@link org.apache.velocity.tools.view.servlet.ServletToolboxManager}:</p>
 * <ul>
 *   <li>The toolbox manager supports three different life cycle models for
 *       view tools: <i>request</i>, <i>session</i> and <i>application</i>. 
 *       An application developer can choose through configuration of the 
 *       toolbox which of the three life cycle models should be applied to a 
 *       particular tool class. Since not every tool class is capable to be used 
 *       with all of the three livecylce models, it is important to consult the
 *       tool documentation. See also 
 *       {@link org.apache.velocity.tools.view.servlet.ServletToolboxManager} for 
 *       more information.</li>
 *   <li>Upon instantiation of a new view tool, the toolbox manager passes
 *       to the view tool an object of class {@link ViewContext}. This
 *       gives the view tool access to HttpServletRequest, HttpSession and 
 *       ServletContext.</li>
 * </ul>
 * 
 * <p>The three static fields {@link #REQUEST}, {@link #SESSION} and 
 * {@link #APPLICATION} define constants to be used to specify one of the 
 * three life cycle models. These constants are used in the toolbox 
 * configuration file.</p>
 * 
 * <p>Note that view tools that do not need any of the special treatment 
 * described above, do not need to implement this interface. Please refer to 
 * the documentation of 
 * {@link org.apache.velocity.tools.view.servlet.ServletToolboxManager} for 
 * more information on the default treatment of view tools.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ServletViewTool.java,v 1.1 2002/04/15 18:30:29 sidler Exp $
 * 
 */
public interface ServletViewTool
{

    /**
     * Constant for a live cycle of <i>request</i>.
     */
    public static final String REQUEST = "request";
   
    /**
     * Constant for a live cycle of <i>session</i>.
     */
    public static final String SESSION = "session";

    /**
     * Constant for a live cycle of <i>application</i>.
     */
    public static final String APPLICATION = "application";
   

    /**
     * A new tool object will be instantiated per-request by calling 
     * this method. A ViewTool is effectively a factory used to 
     * create objects for use in templates. Some tools may simply return
     * themselves from this method, others may instantiate new objects
     * to hold the per-request state.
     *
     * @param context A reference to the Velocity context. Through this
     *     object, view tools gain access to HttpServletRequest,
     *     HttpSession and ServletContext.
     */
    public Object getInstance(ViewContext context);

    
    /**
     * <p>Returns the default life cycle for the view tool. Some
     * toolbox managers may allow an application developer to overwrite
     * this default life cycle by defining a life cycle attribute in 
     * the toolbox configuration. An implementation of this interface
     * should clearly document if life cycles other than the default
     * life cycle is allowed for the tool.</p>
     *
     * @returns one of {@link #REQUEST}, {@link SESSION} or {@link APPLICATION}.
     */
    public String getDefaultLifecycle();
    

}

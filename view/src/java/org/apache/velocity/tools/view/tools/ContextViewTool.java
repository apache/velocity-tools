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
import org.apache.velocity.context.Context;

/**
 * <p>An interface for Velocity view tools that need access to the Velocity
 * context.</p>
 * 
 * <p>View tools that implement this interface receive special treatment
 * by a compatible toolbox manager, e.g. 
 * {@link org.apache.velocity.tools.view.servlet.ServletToolboxManager}:</p>
 * <ul>
 *   <li>Upon creation of a new instance, the toolbox manager passes
 *       to the view tool a reference to the Velocity context.</li>
 * </ul>
 * 
 * <p>Unlike some other tools, tools that implement this interface do not
 * support multiple different life cycles. All implementations of this 
 * interface are assigned a life cycle of 'request', meaning that the 
 * tool is valid only for the processing of the currently requested template.
 * A new instance is created for every request. The life cycle cannot be 
 * configured.</p>
 *
 * <p>Examples of view tools that typically would implement this 
 * interface are tools like a view tool loader or a context inspector. 
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ContextViewTool.java,v 1.1 2002/04/15 18:30:29 sidler Exp $
 * 
 */
public interface ContextViewTool
{

    /**
     * <p>Returns a new instance.</p>
     *
     * <p>This is effectively a factory method used to create
     * object instances for use in templates. It is important 
     * that only instances obtained from this method are used 
     * in templates.</p>
     *
     * @param context a reference to the Velocity context 
     */
    public Object getInstance(Context context);

}

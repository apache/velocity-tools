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

package org.apache.velocity.tools.tools;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ContextViewTool;
import org.apache.velocity.tools.view.tools.LogEnabledViewToolImpl;
import org.apache.velocity.context.Context;


/**
 * <p>A view tool that allows template designers to load
 * other view tools from within the template. Any object
 * with a public constructor without parameters can be used
 * as a view tool.</p>
 *
 * <p>Example: Assuming that an instance of this class has
 * been loaded into the Velocity context under key "toolloader",
 * then from within a template a designer would call:<br>
 * <br>
 * <code>$toolloader.load("math", "xxx.yyy.zzz.MathTool")</code><br>
 * <br>
 * to load a math tool into the context under key "math". This tool
 * is then available for use within the template, for example:<br>
 * <br>
 * <code>$math.random(1, 100)</code><br>
 * </p>
 *
 * <p>THIS CLASS IS HERE AS A PROOF OF CONCEPT ONLY. IT NEEDS TO BE
 * REFACTORED.</p>
 * 
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ToolLoader.java,v 1.2 2002/04/15 18:30:28 sidler Exp $
 * 
 */

public class ToolLoader extends LogEnabledViewToolImpl 
    implements ContextViewTool
{

    // -------------------------------------------- Properties ----------------

    /**
     * <p>A reference to the Velocity context.</p>
     */
    private Context ctx;
    

    
    // -------------------------------------------- Constructors --------------
    
    /**
     * Returns a factory for instances of this class. Use method 
     * {@link #getInstance(Context context)} to obtain instances 
     * of this class. Do not use instance obtained from this method
     * in templates. They are not properly initialized.
     */
    public ToolLoader()
    {
    }

    
    /**
     * Contructor for internal use only. 
     */
    private ToolLoader(Context context)
    {
        this.ctx = context;
    }

    

    // --------------------------------------- Interface ContextViewTool ---
    
    /**
     * Returns an initialized instance of this view tool.
     */
    public Object getInstance(Context context)
    {
        return new ToolLoader(context);
    }



    // -------------------------------------------- Public Utility Methods ----

    /**
     * <p>Loads a view tool of class <i>clazz</i> and inserts it
     * into the Velocity context with key <i>key</i>. On order to be
     * loadable, view tools must provide a constructor with no 
     * parameters. The life cycle of a view tool loaded using
     * this method is the current request.</p>
     *
     * @param key the key used to add the tool to the context
     * @param clazz the fully qualified class name of the tool that
     *     is to be instantiated and added to the context
     */
    public void load(String key, String clazz)
    {
        try
        {
            Object tool = Class.forName(clazz).newInstance();
            ctx.put(key, tool);
            log(INFO, "Loaded tool: Key: " + key + " Class: " + clazz);
        }
        catch (Exception e)
        {
            log(ERROR, "Error loading view tool: " + clazz + " with key: " + key + ". " + e);            
        }
    }

}

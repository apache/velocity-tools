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

package org.apache.velocity.tools.struts;

import org.apache.velocity.tools.view.context.ViewContext;

import org.apache.velocity.tools.view.tools.ContextTool;


/**
 * <p>A factory to produce instances of tool loaders. At the same
 * time, this class implements a tool loader. This class gives
 * the template designer the means to load context tools from
 * within the template.</p>
 *
 * <p>Example: Assuming that an instance of this class has
 * been loaded into the Velocity context under key "toolloader",
 * then from within a template a designer would call:<br>
 * <br>
 * <code>$toolloader.load("math", "xxx.yyy.zzz.MathTool")</code><br>
 * <br>
 * to load a math tool into the context under key "math". This tool
 * is then accessible as:<br>
 * <br>
 * <code>$math.random(1, 100)</code><br>
 * </p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ToolLoaderTool.java,v 1.2 2002/01/09 11:25:44 sidler Exp $
 * 
 */

public class ToolLoaderTool implements ContextTool 
{

    // -------------------------------------------- Properties ----------------

    /**
     * <p>A reference to the Velocity context.</p>
     */
    private ViewContext viewCtx;
    

    
    // -------------------------------------------- Public Utility Methods ----

    /**
     * <p>Loads a context tool of class <i>clazz</i> and inserts it
     * into the Velocity context with key <i>key</i>. Context tools
     * must implement the interface {@link ContextTool} and provide
     * a constructor with no parameters.
     */
    public void load(String key, String clazz)
    {
        // add check if instance was properly obtained through init()

        try
        {
            Object tool = Class.forName(clazz).newInstance();
            
            if ( tool instanceof ContextTool)
            {
                viewCtx.getVelocityContext().put(key, ((ContextTool)tool).init( viewCtx ));
            }
            else
            {
                // Log error to servlet log
                System.out.println("[Error] ToolLoaderTool: Context tool does not implement ContextTool interface");
            }
        }
        catch (Exception e)
        {
            // write error to servlet log
            System.out.println("[Error] ToolLoaderTool: Error adding context tool: " + clazz + " under name: " + key);            
        }
    }



    // -------------------------------------------- Constructor ---------------
    
    /**
     * <p>Instantiate a factory of tool loaders. Use this contructor to obtain an 
     * instance of a tool loader factory and then call method {@link #init(ChainedContext context)}
     * to obtain actual instances of tool loaders.</p>
     */
    public ToolLoaderTool()
    {
    }

    
    /**
     * Contructor for internal use only. 
     */
    public ToolLoaderTool( ViewContext context)
    {
        this.viewCtx = context;
    }
    

   
    // -------------------------------------------- ContextTool Methods -------
    
    /**
     * <p>Request an instance of a tool loader object.</p>
     */
    public Object init( ViewContext context)
    {
        return new ToolLoaderTool(context);
    }


    /**
     * <p>Return tool instance to the factory in order to perform 
     * necessary cleanup work.</p>
     */
    public void destroy(Object o) 
    {
    }

}

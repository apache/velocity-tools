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


package org.apache.velocity.tools.view;


import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * ToolInfo implementation for view tools. New instances
 * are returned for every call to getInstance(obj), and tools
 * that implement {@link ViewTool} are initialized with the
 * given object before being returned.
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: ViewToolInfo.java,v 1.4 2003/07/22 18:30:27 nbubna Exp $
 */
public class ViewToolInfo implements ToolInfo
{

    private String key;
    private Class clazz;
    private boolean initializable = false;


    public ViewToolInfo() {}


    /***********************  Mutators *************************/

    public void setKey(String key)
    { 
        this.key = key;
    }


    /**
     * If an instance of the tool cannot be created from
     * the classname passed to this method, it will throw an exception.
     *
     * @param classname the fully qualified java.lang.Class of the tool
     */
    public void setClassname(String classname) throws Exception
    {
        this.clazz = Class.forName(classname);
        /* create an instance and see if it is initializable */
        if (clazz.newInstance() instanceof ViewTool)
        {
            this.initializable = true;
        }
    }


    /***********************  Accessors *************************/

    public String getKey()
    {
        return key;
    }


    public String getClassname()
    {
        return clazz.getName();
    }


    /**
     * Returns a new instance of the tool. If the tool
     * implements {@link ViewTool}, the new instance
     * will be initialized using the given data.
     */
    public Object getInstance(Object initData)
    {
        Object tool = null;
        try
        {
            tool = clazz.newInstance();
        }
        catch (Exception e)
        {
            //we should never get here since we
            //got a new instance just fine when we
            //created this tool info
        }
        
        if (initializable) {
            ((ViewTool)tool).init(initData);
        }
        return tool;
    }

}

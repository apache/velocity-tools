/*
 * Copyright 2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.view;

import org.apache.velocity.tools.view.tools.ViewTool;
import org.apache.velocity.app.Velocity;

/**
 * ToolInfo implementation for view tools. New instances
 * are returned for every call to getInstance(obj), and tools
 * that implement {@link ViewTool} are initialized with the
 * given object before being returned.
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: ViewToolInfo.java,v 1.7 2004/02/18 20:08:29 nbubna Exp $
 */
public class ViewToolInfo implements ToolInfo
{

    private String key;
    private Class clazz;
    private boolean initializable = false;


    public ViewToolInfo() {}


    //TODO: if classloading becomes needed elsewhere, move this to a utils class
    /**
     * Return the <code>Class</code> object for the specified fully qualified
     * class name, from this web application's class loader.  If no 
     * class loader is set for the current thread, then the class loader
     * that loaded this class will be used.
     *
     * @param className Fully qualified class name to be loaded
     * @return Class object
     * @exception ClassNotFoundException if the class cannot be found
     * @since VelocityTools 1.1
     */
    protected Class getApplicationClass(String name) throws ClassNotFoundException
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null)
        {
            loader = ViewToolInfo.class.getClassLoader();
        }
        return loader.loadClass(name);
    }


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
        this.clazz = getApplicationClass(classname);
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
        /* we shouldn't get exceptions here because we already 
         * got an instance of this class during setClassname().
         * but to be safe, let's catch the declared ones and give 
         * notice of them, and let other exceptions slip by. */
        catch (IllegalAccessException e)
        {
            Velocity.error("Exception while instantiating instance of \"" +
                           getClassname() + "\": " + e);
        }
        catch (InstantiationException e)
        {
            Velocity.error("Exception while instantiating instance of \"" +
                           getClassname() + "\": " + e);
        }
        
        if (initializable) {
            ((ViewTool)tool).init(initData);
        }
        return tool;
    }

}

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ToolInfo implementation for view tools. New instances
 * are returned for every call to getInstance(obj), and tools
 * that have an init(Object) method are initialized with the
 * given object before being returned. And tools that have a
 * configure(Map) method will be configured before being returned
 * if there are parameters specified for the tool.
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:henning@schmiedehausen.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class ViewToolInfo implements ToolInfo
{
    protected static final Log LOG = LogFactory.getLog(ViewToolInfo.class);

    private String key;
    private Class clazz;
    private Map parameters;
    private Method init = null;
    private Method configure = null;

    public ViewToolInfo() {}


    //TODO: if classloading becomes needed elsewhere, move this to a utils class
    /**
     * Return the <code>Class</code> object for the specified fully qualified
     * class name, from this web application's class loader.  If no 
     * class loader is set for the current thread, then the class loader
     * that loaded this class will be used.
     *
     * @param name Fully qualified class name to be loaded
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
     * @param classname the fully qualified java.lang.Class name of the tool
     */
    public void setClassname(String classname) throws Exception
    {
        if (classname != null && classname.length() != 0)
        {
            this.clazz = getApplicationClass(classname);
            // create an instance to make sure we can
            Object instance = clazz.newInstance();
            try
            {
                // try to get an init(Object) method
                this.init = clazz.getMethod("init", new Class[]{ Object.class });
            }
            catch (NoSuchMethodException nsme)
            {
                // ignore
            }
            try
            {
                // check for a configure(Map) method
                this.configure = clazz.getMethod("configure", new Class[]{ Map.class });
            }
            catch (NoSuchMethodException nsme)
            {
                // ignore
            }
        }
        else
        {
            this.clazz = null;
        }
    }

    /**
     * Set parameter map for this tool.
     *
     * @since VelocityTools 1.1
     */
    public void setParameters(Map parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Set/add new parameter for this tool.
     *
     * @since VelocityTools 1.1
     */
    public void setParameter(String name, String value)
    {
        if (parameters == null)
        {
            parameters = new HashMap();
        }
        parameters.put(name, value);
    }


    /***********************  Accessors *************************/

    public String getKey()
    {
        return key;
    }


    public String getClassname()
    {
        return clazz != null ? clazz.getName() : null;
    }

    /**
     * Get parameters for this tool.
     * @since VelocityTools 1.1
     */
    public Map getParameters()
    {
        return parameters;
    }

    /**
     * Returns a new instance of the tool. If the tool
     * has an init(Object) method, the new instance
     * will be initialized using the given data. If parameters
     * have been specified and the tool has a configure(Map)
     * method, the tool will be passed the parameters also.
     */
    public Object getInstance(Object initData)
    {
        if (clazz == null)
        {
            LOG.error("Tool "+this.key+" has no Class definition!");
            return null;
        }

        /* Get the tool instance */
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
            LOG.error("Exception while instantiating instance of \"" +
                    getClassname() + "\"", e);
        }
        catch (InstantiationException e)
        {
            LOG.error("Exception while instantiating instance of \"" +
                    getClassname() + "\"", e);
        }

        /* if the tool is configurable and we have parameters... */
        if (configure != null && parameters != null)
        {
            try
            {
                // call the configure method on the instance
                configure.invoke(tool, new Object[]{ parameters });
            }
            catch (IllegalAccessException iae)
            {
                LOG.error("Exception when calling configure(Map) on "+tool, iae);
            }
            catch (InvocationTargetException ite)
            {
                LOG.error("Exception when calling configure(Map) on "+tool, ite);
            }
        }

        /* if the tool is initializable... */
        if (init != null)
        {
            try
            {
                // call the init method on the instance
                init.invoke(tool, new Object[]{ initData });
            }
            catch (IllegalAccessException iae)
            {
                LOG.error("Exception when calling init(Object) on "+tool, iae);
            }
            catch (InvocationTargetException ite)
            {
                LOG.error("Exception when calling init(Object) on "+tool, ite);
            }
        }
        return tool;
    }

}

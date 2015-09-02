package org.apache.velocity.tools.generic;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>
 * This is a simple tools class to allow easy access to static fields in a class,
 * such as string constants from within a template.  Velocity will not introspect
 * for class fields (and won't in the future :), but writing setter/getter methods
 * to do this is a pain,  so use this if you really have to access fields.
 *
 * <p>
 * <pre>
 * Example uses in a template:
 *   ## here we access a constant in a class include in the configuration
 *     $field.COUNTER_NAME
 *
 *   ## here we dynamically lookup a class' fields to find another constant
 *     $field.in("org.com.SomeClass").ANOTHER_CONSTANT
 *
 *   ## here we pass an object instance in (an Integer in this case) and
 *   ## retrieve a static constant from that instance's class
 *     $field.in(0).MIN_VALUE
 *
 *   ## by default, once we've searched a class' fields, those fields stay
 *   ## available in the tool (change this by storeDynamicLookups="false")
 *   ## so here we get another constant from the Integer class
 *     $field.MAX_VALUE
 *
 *
 * Example tools.xml config:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.FieldTool"
 *              include="org.apache.velocity.runtime.RuntimeConstants,com.org.MyConstants"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>
 * Right now, this tool only gives access to <code>public static</code> fields.
 * It seems that anything else is too dangerous.  This is for convenient access
 * to 'constants'.  If you have fields that aren't <code>static</code>,
 * handle them by explicitly placing them into the context or writing a getter
 * method.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id: FieldTool.java 463298 2006-10-12 16:10:32Z henning $
 */
@DefaultKey("field")
public class FieldTool extends SafeConfig
{
    /**
     * The key used for specifying which classes should be inspected
     * for public static methods to be made available.
     */
    public static final String INCLUDE_KEY = "include";

    /**
     * The key used for specifying whether or not the tool should store
     * fields in classes dynamically looked up from within a template.
     * The default value is true.
     */
    public static final String STORE_DYNAMIC_KEY = "storeDynamicLookups";

    protected Log log;
    protected HashMap storage = new HashMap();
    protected boolean storeDynamicLookups = true;

    protected void configure(ValueParser values)
    {
        // see if there's a log in there
        this.log = (Log)values.getValue("log");

        // retrieve any classnames to be inspected and inspect them
        // *before* setting the storeDynamicLookups property!
        String[] classnames = values.getStrings(INCLUDE_KEY);
        if (classnames != null)
        {
            for (String classname : classnames)
            {
                // make sure we get results for each classname
                // since these come from the configuration, it's
                // an error if they're invalid
                if (in(classname) == null)
                {
                    // shame that ClassNotFoundException is checked...
                    throw new RuntimeException("Could not find "+classname+" in the classpath");
                }
            }
        }

        // find out whether or not we should store dynamic lookups
        this.storeDynamicLookups =
            values.getBoolean(STORE_DYNAMIC_KEY, this.storeDynamicLookups);
    }


    /**
     * Returns the value for the specified field name as found
     * in the stored {@link Map} of field names to values (or placeholders).
     * Returns {@code null} if there is no matching field.
     */
    public Object get(String name)
    {
        Object o = storage.get(name);
        // if it was not a final field, get the current value
        if (o instanceof MutableField)
        {
            return ((MutableField)o).getValue();
        }
        // if we have no value and the name looks like a path
        else if (o == null && name.indexOf('.') > 0)
        {
            // treat the name as a full fieldpath
            try
            {
                return ClassUtils.getFieldValue(name);
            }
            catch (Exception e)
            {
                if (log != null)
                {
                    log.debug("Unable to retrieve value of field at "+name, e);
                }
            }
        }
        // otherwise, we should have stored the value directly
        return o;
    }

    /**
     * Returns a {@link FieldToolSub} holding a {@link Map}
     * of all the public static field names to values (or a placeholder
     * if the value is not final) for the specified class(name). If the
     * {@link Class} with the specified name cannot be loaded, this will
     * return {@code null}, rather than throw an exception.
     *
     * @see #in(Class clazz)
     */
    public FieldToolSub in(String classname)
    {
        try
        {
            return in(ClassUtils.getClass(classname));
        }
        catch (ClassNotFoundException cnfe)
        {
            return null;
        }
    }

    /**
     * Returns a {@link FieldToolSub} holding a {@link Map}
     * of all the public static field names to values (or a placeholder
     * if the value is not final) for the {@link Class} of the
     * specified Object.
     * @see #in(Class clazz)
     */
    public FieldToolSub in(Object instance)
    {
        if (instance == null)
        {
            return null;
        }
        return in(instance.getClass());
    }

    /**
     * Returns a {@link FieldToolSub} holding a {@link Map}
     * of all the public static field names to values (or a placeholder
     * if the value is not final) for the specified {@link Class}.
     */
    public FieldToolSub in(Class clazz)
    {
        if (clazz == null)
        {
            return null;
        }

        Map<String,Object> results = inspect(clazz);
        if (storeDynamicLookups && !results.isEmpty())
        {
            storage.putAll(results);
        }
        return new FieldToolSub(results);
    }


    /**
     * Looks for all public, static fields in the specified class and
     * stores their value (if final) or else a {@link MutableField} for
     * in a {@link Map} under the fields' names.  This will never return
     * null, only an empty Map if there are no public static fields.
     */
    protected Map<String,Object> inspect(Class clazz)
    {
        Map<String,Object> results = new HashMap<String,Object>();
        for(Field field : clazz.getFields())
        {
            // ignore anything non-public or non-static
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod))
            {
                // make it easy to debug key collisions
                if (log != null && log.isDebugEnabled() &&
                    results.containsKey(field.getName()))
                {
                    log.debug("FieldTool: "+field.getName()+
                              " is being overridden by "+clazz.getName());
                }
                // if the field is final
                if (Modifier.isFinal(mod))
                {
                    // just get the value now
                    results.put(field.getName(), retrieve(field, clazz, log));
                }
                else
                {
                    // put a wrapper with easy access
                    results.put(field.getName(),
                                new MutableField(field, clazz, log));
                }
            }
        }
        return results;
    }

    /**
     * Retrieves and returns the value of the specified {@link Field}
     * in the specified {@link Class}.  If {@link Log} is provided, then
     * access errors will be logged, otherwise this will fail silently
     * and return {@code null}.
     */
    protected static Object retrieve(Field field, Class clazz, Log log)
    {
        try
        {
            return field.get(clazz);
        }
        catch(IllegalAccessException iae)
        {
            if (log != null)
            {
                log.warn("IllegalAccessException while trying to access " + field.getName(), iae);
            }
            return null;
        }
    }



    /**
     * Holds a {@link Map} of results for a particular class.
     * This exists simply to enable the $field.in("class.Name").FOO
     * syntax, even when storeDynamicLookups is set to false.
     * NOTE: we can't simply return the results Map when the in()
     * methods are called, because the Map contains placeholders
     * for any mutable fields found. We want to put off reading non-final
     * field values to the last moment, in case their values change.
     */
    public static class FieldToolSub
    {
        private final Map<String,Object> results;

        public FieldToolSub(Map<String,Object> results)
        {
            if (results == null)
            {
                throw new NullPointerException("Cannot create sub with null field results map");
            }
            this.results = results;
        }

        public Object get(String name)
        {
            Object o = results.get(name);
            // if it was not a final field, get the current value
            if (o instanceof MutableField)
            {
                return ((MutableField)o).getValue();
            }
            // otherwise, we should have stored the value directly
            return o;
        }

        /**
         * Return the toString() value of the internal results Map for this sub.
         */
        public String toString()
        {
            return results.toString();
        }
    }



    /**
     * Holds a {@link Field} and {@link Class} reference for later
     * retrieval of the value of a field that is not final and may
     * change at different lookups.  If a {@link Log} is passed in,
     * then this will log errors, otherwise it will fail silently.
     */
    public static class MutableField
    {
        private final Class clazz;
        private final Field field;
        private final Log log;

        public MutableField(Field f, Class c, Log l)
        {
            if (f == null || c == null)
            {
                throw new NullPointerException("Both Class and Field must NOT be null");
            }

            field = f;
            clazz = c;
            log = l;
        }

        public Object getValue()
        {
            return FieldTool.retrieve(field, clazz, log);
        }
    }

}

package org.apache.velocity.tools.view;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;

import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.SetExecutor;

/**
 * <p>This custom uberspector allows getAttribute() and setAttribute() as standard
 * getters and setters for the "request","session" and "application" keys.
 * <p>It allows VTL statements like:</p>
 * <pre>
 * #set($session.foo = 'youpi')
 * session parameter 'foo' has value: $session.foo
 * </pre>
 * <p>This uberspector requires Velocity 1.6+ ; to use it, you must specify <code>org.apache.velocity.tools.view.WebappUberspector</code>
 * as the last uberspector to the <code>runtime.introspector.uberspect</code> property in you <code>velocity.properties</code> file.</p>
 *
 * <p>For instance:</p>
 *
 * <p><code>runtime.introspector.uberspect = org.apache.velocity.util.introspection.UberspectImpl,org.apache.velocity.tools.view.WebappUberspector</code></p>
 *
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @version $Id: WebappUberspector.java $  */

public class WebappUberspector extends AbstractChainableUberspector
{

    /**
     * Property getter
     * @param obj target object
     * @param identifier property key
     * @param i tool info
     * @return A Velocity Getter Method.
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        if (obj == null)
        {
            return null;
        }
        VelPropertyGet ret = super.getPropertyGet(obj,identifier,i);

        if(ret == null)
        {
            Class claz = obj.getClass();
            if(obj instanceof HttpServletRequest
                || obj instanceof HttpSession
                || obj instanceof ServletContext)
            {
                AbstractExecutor executor = new GetAttributeExecutor(log, introspector, claz, identifier);
                ret = executor.isAlive() ? new VelGetterImpl(executor) : null;
            }
        }
        return ret;
    }

    /**
     * init method
     */
    @Override
    public void init()
    {
        super.init();

        // we need our own introspector since the inner one is hidden by the Uberspect interface
        introspector = new Introspector(log);
    }

    /**
     * Property setter
     * @param obj target object
     * @param identifier property key
     * @param arg value to set
     * @param i tool info
     * @return A Velocity Setter method.
     */
    public VelPropertySet getPropertySet(Object obj, String identifier,
                                         Object arg, Info i)
    {
        if (obj == null)
        {
            return null;
        }
        VelPropertySet ret = super.getPropertySet(obj,identifier,arg,i);

        if(ret == null) {
            Class claz = obj.getClass();
            if(obj instanceof HttpServletRequest
                || obj instanceof HttpSession
                || obj instanceof ServletContext)
            {
                SetExecutor executor = new SetAttributeExecutor(log, introspector, claz, arg, identifier);
                ret = executor.isAlive() ? new VelSetterImpl(executor) : null;
            }
        }
        return ret;
    }


    /**
     * Executor for getAttribute(name) method.
     */
    public class GetAttributeExecutor extends AbstractExecutor
    {
        private final Introspector introspector;

        // This is still threadsafe because this object is only read except in the C'tor.
        private Object [] params;

        /**
         * @param log logger
         * @param introspector introspector instance
         * @param clazz class name
         * @param property property name
         */
        public GetAttributeExecutor(final Logger log, final Introspector introspector,
                final Class clazz, final String property)
        {
            this.log = log;
            this.introspector = introspector;
            this.params = new Object[] { property };

            discover(clazz);
        }

        protected void discover(final Class clazz)
        {
            try
            {
                setMethod(introspector.getMethod(clazz, "getAttribute", params));
            }
            /**
             * pass through application level runtime exceptions
             */
            catch( RuntimeException e )
            {
                throw e;
            }
            catch(Exception e)
            {
                log.error("While looking for getAttribute('{}') method:", params[0], e);
            }
        }

        /**
         * @param o target object
         * @return execution result
         * @throws IllegalAccessException if thrown by underlying code
         * @throws InvocationTargetException if thrown by underlying code
         * @see org.apache.velocity.runtime.parser.node.AbstractExecutor#execute(java.lang.Object)
         */
        public Object execute(final Object o)
            throws IllegalAccessException, InvocationTargetException
        {
            return isAlive() ? getMethod().invoke(o, params) : null;
        }
    }

    /**
     * Executor for setAttribute(name,value) method
     */
    public class SetAttributeExecutor extends SetExecutor
    {
        private final Introspector introspector;
        private final String property;

        /**
         * @param log logger
         * @param introspector introspector instance
         * @param clazz target class
         * @param arg value to set
         * @param property property name
         */
        public SetAttributeExecutor(final Logger log, final Introspector introspector,
                final Class clazz, final Object arg, final String property)
        {
            this.log = log;
            this.introspector = introspector;
            this.property = property;

            discover(clazz, arg);
        }

        /**
         * @param clazz target class
         * @param arg expected arguments
         */
        protected void discover(final Class clazz, final Object arg)
        {
            Object [] params = new Object[] { property, arg };

            try
            {
                setMethod(introspector.getMethod(clazz, "setAttribute", params));
            }
            /**
             * pass through application level runtime exceptions
             */
            catch( RuntimeException e )
            {
                throw e;
            }
            catch(Exception e)
            {
                log.error("While looking for put('{}') method:", params[0], e);
            }
        }

        /**
         * @param o target object
         * @param value value to set
         * @throws IllegalAccessException if thrown by underlying code
         * @throws InvocationTargetException if thrown by underlying code
         * @see org.apache.velocity.runtime.parser.node.SetExecutor#execute(java.lang.Object, java.lang.Object)
         */
        public Object execute(final Object o, final Object value)
            throws IllegalAccessException,  InvocationTargetException
        {
            Object [] params;

            if (isAlive())
            {
                params = new Object [] { property, value };
                return getMethod().invoke(o, params);
            }

            return null;
        }
    }
}

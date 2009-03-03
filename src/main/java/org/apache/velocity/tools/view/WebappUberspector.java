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

import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.SetExecutor;
import org.apache.velocity.runtime.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;

/**
 * <p>This custom uberspector allows getAttribute() and setAttribute() as standard
 * getters and setters for the "request","session" and "application" keys.
 * <p>It allows VTL statements like:
 * <pre>
 * #set($session.foo = 'youpi')
 * session parameter 'foo' has value: $session.foo
 * </pre>
 * </p>
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
     * @param obj
     * @param identifier
     * @param i
     * @return A Velocity Getter Method.
     * @throws Exception
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception
    {
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
        try
        {
            super.init();
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        // we need our own introspector since the inner one is hidden by the Uberspect interface
        introspector = new Introspector(log);
    }

    /**
     * Property setter
     * @param obj
     * @param identifier
     * @param arg
     * @param i
     * @return A Velocity Setter method.
     * @throws Exception
     */
    public VelPropertySet getPropertySet(Object obj, String identifier,
                                         Object arg, Info i)
            throws Exception
    {
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
         * @param log
         * @param introspector
         * @param clazz
         * @param property
         */
        public GetAttributeExecutor(final Log log, final Introspector introspector,
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
                log.error("While looking for getAttribute('" + params[0] + "') method:", e);
            }
        }

        /**
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
         * @param log
         * @param introspector
         * @param clazz
         * @param arg
         * @param property
         */
        public SetAttributeExecutor(final Log log, final Introspector introspector,
                final Class clazz, final Object arg, final String property)
        {
            this.log = log;
            this.introspector = introspector;
            this.property = property;

            discover(clazz, arg);
        }

        /**
         * @param clazz
         * @param arg
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
                log.error("While looking for put('" + params[0] + "') method:", e);
            }
        }

        /**
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

package org.apache.velocity.tools.model.context;

import org.apache.velocity.tools.model.Attribute;
import org.apache.velocity.tools.model.impl.AttributeHolder;
import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This uberspector allows getting a property while specifying extra external parameters
 * to the property. It resolves calls that look like a method call, with one Map argument,
 * but where the method name is in fact a property of the underlying object, which must implement
 * the HasParapetrizedGetter interface.
 *
 * @see org.apache.velocity.util.introspection.Uberspect#getMethod(java.lang.Object, java.lang.String,
 *      java.lang.Object[], org.apache.velocity.util.introspection.Info)
 */
public class ModelUberspector extends AbstractChainableUberspector
{
    protected static Logger logger = LoggerFactory.getLogger(ModelUberspector.class);

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        VelMethod ret = super.getMethod(obj, methodName, args, i);
        if (ret == null)
        {
            try
            {
                if (obj instanceof ModelTool)
                {
                    ret = getAttributeMethod(ModelTool.class, ((ModelTool)obj).getModel(), methodName, args);
                }
                else if (obj instanceof InstanceReference)
                {
                    ret = getAttributeMethod(InstanceReference.class, ((InstanceReference)obj).getInstance().getEntity(), methodName, args);
                }
            }
            catch(NoSuchMethodException e)
            {
                logger.trace("model uberspector failed", e);
            }
        }
        return ret;
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        VelPropertyGet ret = super.getPropertyGet(obj, identifier, i);
        if (ret == null)
        {
            try
            {
                if (obj instanceof ModelTool)
                {
                    ret = getAttributeGetter(ModelTool.class, ((ModelTool)obj).getModel(), identifier);
                }
                else if (obj instanceof InstanceReference)
                {
                    ret = getAttributeGetter(InstanceReference.class, ((InstanceReference)obj).getInstance().getEntity(), identifier);
                }
            }
            catch (NoSuchMethodException nsme)
            {
                logger.trace("model uberspector failed",nsme);
            }
        }
        return ret;
    }

    private VelMethod getAttributeMethod(Class targetClass, AttributeHolder attributeHolder, String attributeName, Object[] args) throws NoSuchMethodException
    {
        VelMethod ret = null;
        Attribute attribute = attributeHolder.getAttribute(attributeName);
        if (attribute != null)
        {
            Class[] classes;
            boolean vararg;
            if (args.length == 1 && args[0] instanceof Map)
            {
                vararg = false;
                classes = new Class[] { String.class, Map.class };
            }
            else
            {
                vararg = true;
                classes = new Class[] { String.class, Serializable[].class };
            }
            Method method = targetClass.getMethod(attribute.getQueryMethodName(), classes);
            ret = new AttributeMethod(method, attributeName, vararg);

        }
        return ret;
    }

    private VelPropertyGet getAttributeGetter(Class targetClass, AttributeHolder attributeHolder, String attributeName) throws NoSuchMethodException
    {
        VelPropertyGet ret = null;
        Attribute attribute = attributeHolder.getAttribute(attributeName);
        if (attribute != null)
        {
            Class[] classes = new Class[] { String.class, Serializable[].class };
            Method method = targetClass.getMethod(attribute.getQueryMethodName(), classes);
            ret = new AttributeGetter(attributeName, method);

        }
        return ret;
    }

    public static class AttributeMethod implements VelMethod
    {
        private String property;
        private Method method;
        private boolean vararg;

        public AttributeMethod(Method m, String p, boolean v)
        {
            property = p;
            method = m;
            vararg = v;
        }

        public Object invoke(Object obj, Object[] args)
        {
            Object ret= null;
            try
            {
                Object[] arguments;
                if (vararg)
                {
                    arguments = new Object[] { property, args };
                }
                else
                {
                    arguments = new Object[] { property, args[0] };
                }
                ret = method.invoke(obj, arguments);
            }
            catch(IllegalAccessException | InvocationTargetException e)
            {
                logger.error("attribute call failed", e);
            }
            return ret;
        }

        public boolean isCacheable()
        {
            return true;
        }

        public String getMethodName()
        {
            return method.getName();
        }

        public Class getReturnType()
        {
            return method.getReturnType();
        }

        public Method getMethod()
        {
            return method;
        }
    }

    public static class AttributeGetter implements VelPropertyGet
    {
        private String property;
        private Method method;

        public AttributeGetter(String p, Method m)
        {
            property = p;
            method = m;
        }

        @Override
        public Object invoke(Object o) throws Exception
        {
            return method.invoke(o, property, new Serializable[0]);
        }

        @Override
        public boolean isCacheable()
        {
            return true;
        }

        @Override
        public String getMethodName()
        {
            return method.getName();
        }
    }

}

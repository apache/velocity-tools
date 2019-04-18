package org.apache.velocity.tools.model.filter;

import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.ConfigurationException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

public abstract class FilterHandler <T extends Serializable> extends TypeMapper<Filter<T>>
{
    public FilterHandler(String configurationPrefix)
    {
        super(configurationPrefix);
        setDefaultColumnLeaf(Filter.identity());
    }

    @Override
    protected Filter<T> composeLeaves(Filter<T> left, Filter<T> right)
    {
        return left.compose(right);
    }

    protected Filter<T> newObjectToLeaf(Object obj)
    {
        Filter<T> ret = null;
        if (ret instanceof Filter)
        {
            return super.newObjectToLeaf(obj);
        }
        else
        {
            Method stringGetter = ClassUtils.findMethod(obj.getClass(), "get", String.class);
            final Method getter = stringGetter == null ?
                ClassUtils.findMethod(obj.getClass(), "get", Object.class) :
                stringGetter;
            if (getter == null)
            {
                throw new ConfigurationException(getConfigurationPrefix() + ": don't know what to do with class " + obj.getClass().getName());
            }
            return x ->
            {
                try
                {
                    return (T)getter.invoke(obj, x);
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    throw new SQLException("could not apply operator from class " + obj.getClass().getName());
                }
            };
        }
    }
}

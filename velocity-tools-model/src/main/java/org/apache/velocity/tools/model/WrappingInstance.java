package org.apache.velocity.tools.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WrappingInstance extends Instance implements Wrapper
{
    public WrappingInstance(Entity entity, Object pojo)
    {
        super(entity);
        this.pojo = pojo;
        this.getters = Optional.ofNullable(entity.getWrappedInstanceGetters()).orElse(new HashMap<>());
        this.setters = Optional.ofNullable(entity.getWrappedInstanceSetters()).orElse(new HashMap<>());
    }

    @Override
    public void setInitialValue(String columnName, Serializable value)
    {
        Method setter = setters.get(columnName);
        if (setter == null)
        {
            super.setInitialValue(columnName, value);
        }
        else
        {
            try
            {
                setter.invoke(pojo, value);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException("could not set property " + columnName, e);
            }
        }
    }

    @Override
    public Serializable get(Object key)
    {
        Method getter = getters.get(key);
        if (getter == null)
        {
            return super.get(key);
        }
        else
        {
            try
            {
                return (Serializable)getter.invoke(pojo);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException("could not get property " + key, e);
            }
        }
    }

    @Override
    protected Serializable putImpl(String key, Serializable value)
    {
        Method setter = setters.get(key);
        if (setter == null)
        {
            return super.putImpl(key, value);
        }
        else
        {
            try
            {
                return (Serializable)setter.invoke(pojo, value);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException("could not set property " + key, e);
            }
        }
    }

    private Object pojo;
    private Map<String, Method> getters = null;
    private Map<String, Method> setters = null;

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        if (isWrapperFor(iface))
        {
            return (T)pojo;
        }
        else
        {
            throw new SQLException("cannot unwrap towards " + iface.getName());
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return iface.isAssignableFrom(pojo.getClass());
    }
}

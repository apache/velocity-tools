package org.apache.velocity.tools.model.filter;

import org.apache.velocity.tools.ClassUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class TypeMapper<T extends Serializable> extends ColumnMapper<T>
{
    public TypeMapper(String configurationPrefix)
    {
        super(configurationPrefix);
    }

    @Override
    protected void addEntry(String key, T leaf)
    {
        try
        {
            Class clazz = ClassUtils.getClass(key);
            addTypeEntry(clazz, leaf);
        }
        catch (ClassNotFoundException cnfe)
        {
            super.addEntry(key, leaf);
        }
    }

    protected void addTypeEntry(Class clazz, T leaf)
    {
        typesMapping.put(clazz, leaf);
    }

    public T getTypeEntry(Class clazz)
    {
        T ret = null;
        // apply the most specific one
        while (clazz != null)
        {
            ret = typesMapping.get(clazz);
            if (ret != null)
            {
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return ret;
    }


    private Map<Class, T> typesMapping = new HashMap<>();
}

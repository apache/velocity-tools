package org.apache.velocity.tools.model.filter;

import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.model.util.GlobToRegex;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Mapper<T extends Serializable>
{
    public Mapper(String configurationPrefix)
    {
        this.configurationPrefix = configurationPrefix;
    }

    protected abstract Logger getLogger();

    public void initialize() throws ConfigurationException
    {
    }

    protected T valueToLeaf(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof String)
        {
            T ret = null;
            String[] leaves = ((String)value).split(",\\s*");
            for (String strLeaf : leaves)
            {
                T leaf = stringToLeaf(strLeaf);
                ret = ret == null ? leaf : composeLeaves(leaf, ret);
            }
            return ret;
        }
        try
        {
            return (T)value;
        }
        catch (ClassCastException cce)
        {
            throw new ConfigurationException("cannot convert mapped value to proper type", cce);
        }
    }

    protected T stringToLeaf(String value)
    {
        T ret = getStockObject(value);
        if (ret == null)
        {
            ret = classnameToLeaf(value);
        }
        return ret;
    }

    protected T classnameToLeaf(String clazz)
    {
        try
        {
            T ret = null;
            Class leafClass = ClassUtils.getClass(clazz);
            return classToLeaf(leafClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new ConfigurationException(getConfigurationPrefix() + ": could not find class " + clazz);
        }
    }

    protected T classToLeaf(Class leafClass)
    {
        try
        {
            Object obj = leafClass.newInstance();
            return newObjectToLeaf(obj);
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            throw new ConfigurationException(getConfigurationPrefix() + " : could not instanciate class " + leafClass.getName());
        }
    }

    protected T newObjectToLeaf(Object obj)
    {
        try
        {
            return (T)obj;
        }
        catch (ClassCastException cce)
        {
            throw new ConfigurationException(getConfigurationPrefix() + ": unexpected object class", cce);
        }
    }

    protected abstract T composeLeaves(T left, T right);

    protected abstract void addEntry(String key, T leaf);

    public void setMapping(String value)
    {
        Map map = new HashMap();
        String[] parts = value.split(",");
        for (String part : parts)
        {
            int eq = part.indexOf('=');
            if (eq == -1)
            {
                // a single default ?
                T leaf = stringToLeaf(part);
                map.put("*", leaf);
                map.put("*.*", leaf);
            }
            else
            {
                String key = part.substring(0, eq);
                String val = part.substring(eq +  1);
                map.put(key, val);
            }
        }
        setMapping(map);
    }

    public void setMapping(Map map)
    {
        if (map == null)
        {
            return;
        }
        for (Map.Entry entry : (Set<Map.Entry>)map.entrySet())
        {
            String key = (String)entry.getKey();
            T leaf = valueToLeaf(entry.getValue());
            addEntry(key, leaf);
        }
    }

    public class MappingEntry
    {
        MappingEntry(String pattern)
        {
            this(pattern, null);
        }

        MappingEntry(String pattern, T value)
        {
            this.pattern = Pattern.compile(GlobToRegex.toRegex(pattern, "."), Pattern.CASE_INSENSITIVE);
            this.leaf = value;
        }

        public T getLeaf()
        {
            return leaf;
        }

        public void setLeaf(T leaf)
        {
            this.leaf = leaf;
        }

        public boolean matches(String name)
        {
            return pattern.matcher(name).matches();
        }

        private Pattern pattern;
        private T leaf;
    }

    protected String getConfigurationPrefix()
    {
        return configurationPrefix;
    }

    protected void addStockObject(String key, T object)
    {
        stockObjects.put(key, object);
    }

    protected T getStockObject(String key)
    {
        return stockObjects.get(key);
    }

    private String configurationPrefix;

    private Map<String, T> stockObjects = new HashMap<>();

}

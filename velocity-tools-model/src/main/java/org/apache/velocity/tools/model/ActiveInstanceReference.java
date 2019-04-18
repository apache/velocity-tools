package org.apache.velocity.tools.model;

import java.io.Serializable;
import java.util.Map;

public class ActiveInstanceReference extends InstanceReference
{
    public ActiveInstanceReference(Instance instance)
    {
        super(instance);
    }

    @Override
    public Serializable put(String key, Serializable value)
    {
        return super.putImpl(key, value);
    }

    @Override
    public Serializable remove(Object key)
    {
        return super.removeImpl(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m)
    {
        super.putAllImpl(m);
    }

    @Override
    public void clear()
    {
        super.clearImpl();
    }

    @Override
    public int perform(String name, Map params)
    {
        return super.performImpl(name, params);
    }

    @Override
    public int perform(String name, Serializable... params)
    {
        return super.performImpl(name, params);
    }

    @Override
    public boolean delete()
    {
        return super.deleteImpl();
    }

    @Override
    public boolean insert()
    {
        return super.insertImpl();
    }

    @Override
    public boolean update()
    {
        return super.updateImpl();
    }

}

package org.apache.velocity.tools.model.context;

import org.apache.velocity.tools.model.Instance;
import org.apache.velocity.tools.model.util.SlotMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InstanceReference implements SlotMap
{
    protected static Logger logger = LoggerFactory.getLogger(InstanceReference.class);

    public InstanceReference(Instance instance)
    {
        this.instance = instance;
    }

    private Instance instance;

    @Override
    public int size()
    {
        return instance.size();
    }

    @Override
    public boolean isEmpty()
    {
        return instance.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return instance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return instance.containsValue(value);
    }

    @Override
    public Serializable get(Object key)
    {
        return instance.get(key);
    }

    @Override
    public Serializable put(String key, Serializable value)
    {
        logger.error("cannot change read-only instance");
        return null;
    }

    protected Serializable putImpl(String key, Serializable value)
    {
        return instance.put(key, value);
    }

    @Override
    public Serializable remove(Object key)
    {
        logger.error("cannot change read-only instance");
        return null;
    }

    protected Serializable removeImpl(Object key)
    {
        return instance.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m)
    {
        logger.error("cannot change read-only instance");
    }

    public void putAllImpl(Map<? extends String, ? extends Serializable> m)
    {
        instance.putAll(m);
    }

    @Override
    public void clear()
    {
        logger.error("cannot change read-only instance");
    }

    public void clearImpl()
    {
        instance.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return instance.keySet();
    }

    @Override
    public Collection<Serializable> values()
    {
        return instance.values();
    }

    @Override
    public Set<Entry<String, Serializable>> entrySet()
    {
        return instance.entrySet();
    }

    public Serializable evaluate(String name, Map params)
    {
        try
        {
            return instance.evaluate(name, params);
        }
        catch (SQLException sqle)
        {
            logger.error("could not evaluate instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public Serializable evaluate(String name, Serializable... params)
    {
        try
        {
            return instance.evaluate(name, params);
        }
        catch (SQLException sqle)
        {
            logger.error("could not evaluate instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public InstanceReference retrieve(String name, Map params)
    {
        try
        {
            Instance inst = instance.retrieve(name, params);
            return inst == null ? null : new InstanceReference(inst);
        }
        catch (SQLException sqle)
        {
            logger.error("could not retrieve instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public InstanceReference retrieve(String name, Serializable... params)
    {
        try
        {
            Instance inst = instance.retrieve(name, params);
            return inst == null ? null : new InstanceReference(inst);
        }
        catch (SQLException sqle)
        {
            logger.error("could not retrieve instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public Iterator<InstanceReference> query(String name, Map params)
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(instance.query(name, params));
        }
        catch (SQLException sqle)
        {
            logger.error("could not query instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public Iterator<InstanceReference> query(String name, Serializable... params)
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(instance.query(name, params));
        }
        catch (SQLException sqle)
        {
            logger.error("could not query instance property {}.{}", instance.getEntity().getName(), name, sqle);
            return null;
        }
    }

    public int perform(String name, Map params)
    {
        logger.error("instance is read-only");
        return 0;
    }

    protected int performImpl(String name, Map params)
    {
        try
        {
            return instance.perform(name, params);
        }
        catch (SQLException sqle)
        {
            logger.error("could not perform instance action {}.{}", instance.getEntity().getName(), name, sqle);
            return 0;
        }

    }

    public int perform(String name, Serializable... params)
    {
        logger.error("instance is read-only");
        return 0;
    }

    protected int performImpl(String name, Serializable... params)
    {
        try
        {
            return instance.perform(name, params);
        }
        catch (SQLException sqle)
        {
            logger.error("could not perform instance action {}.{}", instance.getEntity().getName(), name, sqle);
            return 0;
        }
    }

    protected Instance getInstance()
    {
        return instance;
    }

    public boolean delete()
    {
        logger.error("cannot delete read-only instance");
        return false;
    }

    protected boolean deleteImpl()
    {
        try
        {
            instance.delete();
            return true;
        }
        catch (SQLException sqle)
        {
            logger.error("could not delete instance", sqle);
            return false;
        }
    }

    public boolean insert()
    {
        logger.error("cannot insert read-only instance");
        return false;
    }

    protected boolean insertImpl()
    {
        try
        {
            instance.insert();
            return true;
        }
        catch (SQLException sqle)
        {
            logger.error("could not insert instance", sqle);
            return false;
        }
    }

    public boolean update()
    {
        logger.error("cannot update read-only instance");
        return false;
    }

    protected boolean updateImpl()
    {
        try
        {
            instance.update();
            return true;
        }
        catch (SQLException sqle)
        {
            logger.error("could not update instance", sqle);
            return false;
        }
    }


}

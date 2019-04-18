package org.apache.velocity.tools.model;

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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class EntityReference
{
    public EntityReference(Entity entity)
    {
        this.entity = entity;
    }

    protected Entity getEntity()
    {
        return entity;
    }

    public Iterator<InstanceReference> iterator()
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(entity.iterate());
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("cannot iterate on instances of " + entity.getName(), sqle);
            return null;
        }
    }

    public long getCount()
    {
        try
        {
            return entity.getCount();
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not get {} count", entity.getName(), sqle);
            return 0;
        }
    }

    protected InstanceReference createInstanceReference(Instance instance)
    {
        switch (getEntity().getModel().getWriteAccess())
        {
            case NONE:
            case JAVA:
                return new InstanceReference(instance);
            case VTL:
                return new ActiveInstanceReference(instance);
            default:
                getEntity().getModel().getLogger().error("unhandled write-access enum: {}", getEntity().getModel().getWriteAccess());
                return null;
        }
    }

    public InstanceReference fetch(Serializable... key) throws SQLException
    {
        try
        {
            Instance instance = entity.fetch(key);
            return instance == null ? null : new InstanceReference(instance);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not fetch instance of {}", entity.getName(), sqle);
            return null;
        }
    }

    public Object get(String key)
    {
        try
        {
            Attribute attribute = entity.getAttribute(key);
            if (attribute != null)
            {
                if (attribute instanceof ScalarAttribute)
                {
                    return ((ScalarAttribute)attribute).evaluate();
                }
                else if (attribute instanceof RowAttribute)
                {
                    Instance instance = ((RowAttribute)attribute).retrieve();
                    return instance == null ? null : new InstanceReference(instance);
                }
                else if (attribute instanceof RowsetAttribute)
                {
                    return new ModelTool.InstanceReferenceIterator(((RowsetAttribute)attribute).query());
                }
            }
            return null;
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not get property {}.{}", entity.getName(), key, sqle);
            return null;
        }
    }

    public Serializable evaluate(String name, Serializable... params)
    {
        try
        {
            return entity.evaluate(name, params);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not evaluate property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Serializable evaluate(String name, Map params)
    {
        try
        {
            return entity.evaluate(name, params);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not evaluate property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Serializable evaluate(String name)
    {
        try
        {
            return entity.evaluate(name);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not evaluate property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public InstanceReference retrieve(String name, Serializable... params)
    {
        try
        {
            Instance instance = entity.retrieve(name, params);
            return instance == null ? null : new InstanceReference(instance);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not retrieve property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public InstanceReference retrieve(String name, Map params)
    {
        try
        {
            Instance instance = entity.retrieve(name, params);
            return instance == null ? null : new InstanceReference(instance);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not retrieve property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public InstanceReference retrieve(String name)
    {
        try
        {
            Instance instance = entity.retrieve(name);
            return instance == null ? null : new InstanceReference(instance);
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not retrieve property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Iterator<InstanceReference> query(String name, Serializable... params)
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(entity.query(name, params));

        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not query property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Iterator<InstanceReference> query(String name, Map params)
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(entity.query(name, params));

        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not query property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Iterator<InstanceReference> query(String name)
    {
        try
        {
            return new ModelTool.InstanceReferenceIterator(entity.query(name));

        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not query property {}.{}", entity.getName(), name, sqle);
            return null;
        }
    }

    public Object get(String key, Map params)
    {
        try
        {
            Attribute attribute = entity.getAttribute(key);
            if (attribute != null)
            {
                if (attribute instanceof ScalarAttribute)
                {
                    return ((ScalarAttribute)attribute).evaluate(params);
                }
                else if (attribute instanceof RowAttribute)
                {
                    Instance instance = ((RowAttribute)attribute).retrieve(params);
                    return instance == null ? null : new InstanceReference(instance);
                }
                else if (attribute instanceof RowsetAttribute)
                {
                    return new ModelTool.InstanceReferenceIterator(((RowsetAttribute)attribute).query(params));
                }
            }
            return null;
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not get property {}.{}", entity.getName(), key, sqle);
            return null;
        }
    }

    public Object get(String key, Serializable... params)
    {
        try
        {
            Attribute attribute = entity.getAttribute(key);
            if (attribute != null)
            {
                if (attribute instanceof ScalarAttribute)
                {
                    return ((ScalarAttribute)attribute).evaluate(params);
                }
                else if (attribute instanceof RowAttribute)
                {
                    Instance instance = ((RowAttribute)attribute).retrieve(params);
                    return instance == null ? null : new InstanceReference(instance);
                }
                else if (attribute instanceof RowsetAttribute)
                {
                    return new ModelTool.InstanceReferenceIterator(((RowsetAttribute)attribute).query(params));
                }
            }
            return null;
        }
        catch (SQLException sqle)
        {
            entity.getLogger().error("could not get property {}.{}", entity.getName(), key, sqle);
            return null;
        }
    }

    private Entity entity = null;
}

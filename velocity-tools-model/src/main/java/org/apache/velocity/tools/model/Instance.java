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

import org.apache.velocity.tools.model.filter.ValueFilterHandler;
import org.apache.velocity.tools.model.sql.RowValues;
import org.apache.velocity.tools.model.util.ChainedMap;
import org.apache.velocity.tools.model.filter.Filter;
import org.apache.velocity.tools.model.util.SlotTreeMap;
import org.apache.velocity.tools.model.util.TypeUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Instance extends SlotTreeMap
{
    public Instance(Model model)
    {
        this.model = model;
        this.dirtyFlags = new BitSet();
        this.canWrite = model.getWriteAccess() != Model.WriteAccess.NONE;
    }

    protected Instance(Entity entity)
    {
        this(entity.getModel());
        this.entity = entity;
    }

    public void setInitialValues(RowValues values) throws SQLException
    {
        Filter<String> defaultNameMapper = getModel().getIdentifiers().getDefaultColumnLeaf();
        ValueFilterHandler readFilters = getModel().getFilters().getReadFilters();
        for (String key : values.keySet())
        {
            Serializable value = values.get(key);
            String colName = entity == null ? null : entity.translateColumnName(key);
            if (colName == null)
            {
                colName = defaultNameMapper.apply(key);
            }
            else
            {
                value = entity.getColumn(colName).read(value);
            }
            if (value != null)
            {
                Filter<Serializable> filter = readFilters.getTypeEntry(value.getClass());
                if (filter != null)
                {
                    value = filter.apply(value);
                }
            }
            setInitialValue(colName, value);
        }
        setClean();
        persisted = entity != null && entity.getPrimaryKey().size() > 0;
    }

    public void setInitialValue(String columnName, Serializable value)
    {
        super.put(columnName, value);
    }

    public Serializable evaluate(String name, Map params) throws SQLException
    {
        return entity.evaluate(name, params == null ? this : new ChainedMap(this, params));
    }

    public Serializable evaluate(String name, Serializable... params) throws SQLException
    {
        return entity.evaluate(name, this, params);
    }

    public Instance retrieve(String name, Map params) throws SQLException
    {
        return entity.retrieve(name, params == null ? this : new ChainedMap(this, params));
    }

    public Instance retrieve(String name, Serializable... params) throws SQLException
    {
        return entity.retrieve(name, this, params);
    }

    public Iterator<Instance> query(String name, Map params) throws SQLException
    {
        return entity.query(name, params == null ? this : new ChainedMap(this, params));
    }

    public Iterator<Instance> query(String name, Serializable... params) throws SQLException
    {
        return entity.query(name, this, params);
    }

    public int perform(String name, Map params) throws SQLException
    {
        if (!canWrite)
        {
            throw new SQLException("instance is read-only");
        }
        return entity.perform(name, params == null ? this : new ChainedMap(this, params));
    }

    public int perform(String name, Serializable... params) throws SQLException
    {
        if (!canWrite)
        {
            throw new SQLException("instance is read-only");
        }
        return entity.perform(name, this, params);
    }

    public String getString(String name)
    {
        return TypeUtils.toString(get(name));
    }

    public Boolean getBoolean(String name)
    {
        return TypeUtils.toBoolean(get(name));
    }

    public Short getShort(String name)
    {
        return TypeUtils.toShort(get(name));
    }

    public Integer getInteger(String name)
    {
        return TypeUtils.toInteger(get(name));
    }

    public Long getLong(String name)
    {
        return TypeUtils.toLong(get(name));
    }

    public Float getFloat(String name)
    {
        return TypeUtils.toFloat(get(name));
    }

    public Double getDouble(String name)
    {
        return TypeUtils.toDouble(get(name));
    }

    public final Model getModel()
    {
        return model;
    }

    public final Entity getEntity()
    {
        return entity;
    }

    protected void setClean()
    {
        dirtyFlags.clear();
    }

    public Serializable[] getPrimaryKey()
    {
        List<Entity.Column> pk = entity.getPrimaryKey();
        if (pk == null)
        {
            return null;
        }
        Serializable[] ret = new Serializable[pk.size()];
        int col = 0;
        for (Entity.Column column : pk)
        {
            ret[col++] = get(column.name);
        }
        return ret;
    }

    public BitSet getDirtyFlags()
    {
        return dirtyFlags;
    }

    public boolean isDirty()
    {
        return dirtyFlags.cardinality() > 0;
    }

    public void refresh() throws SQLException
    {
        ensurePersisted();
        Instance myself = getEntity().fetch(getPrimaryKey());
        super.putAll(myself);
    }

    public void delete() throws SQLException
    {
        if (!canWrite)
        {
            throw new SQLException("instance is read-only");
        }
        ensurePersisted();
        entity.delete(this);
        persisted = false;
    }

    public void insert() throws SQLException
    {
        if (!canWrite)
        {
            throw new SQLException("instance is read-only");
        }
        ensureNotPersisted();
        entity.insert(this);
        persisted = entity != null && entity.getPrimaryKey().size() > 0;
    }

    public void update() throws SQLException
    {
        if (!canWrite)
        {
            throw new SQLException("instance is read-only");
        }
        ensurePersisted();
        if (!isDirty())
        {
            return;
        }
        entity.update(this);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> map)
    {
        Serializable[] pk = null;
        if (persisted)
        {
            pk = getPrimaryKey();
        }
        super.putAll(map);
        if (persisted)
        {
            Serializable[] newpk = getPrimaryKey();
            if (Arrays.equals(pk, newpk))
            {
                entity.getNonKeyMask().stream().filter(col -> map.containsKey(entity.getColumn(col).name)).forEach(col -> dirtyFlags.set(col));
            }
            else
            {
                persisted = false;
            }

        }
    }

    protected Serializable putImpl(String key, Serializable value)
    {
        return super.put(key, value);
    }

    @Override
    public final Serializable put(String key, Serializable value)
    {
        Serializable ret = putImpl(key, value);
        if (persisted)
        {
            Entity.Column column = entity.getColumn(key);
            if (column != null)
            {
                if (entity.getPrimaryKeyMask().get(column.getIndex()))
                {
                    if (!ret.equals(value))
                    {
                        persisted = false;
                    }
                }
                else
                {
                    dirtyFlags.set(column.getIndex());
                }
            }
        }
        return ret;
    }

    private void ensurePersisted()
    {
        if (!persisted)
        {
            throw new IllegalStateException("instance must be persisted");
        }
    }

    private void ensureNotPersisted()
    {
        if (persisted)
        {
            throw new IllegalStateException("instance must not be persisted");
        }
    }

    private Model model = null;

    private Entity entity = null;

    private BitSet dirtyFlags = null;

    private boolean canWrite = false;

    private boolean persisted = false;
}

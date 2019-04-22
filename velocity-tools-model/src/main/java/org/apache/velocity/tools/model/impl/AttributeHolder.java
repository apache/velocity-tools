package org.apache.velocity.tools.model.impl;

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

import org.apache.velocity.tools.model.Action;
import org.apache.velocity.tools.model.Attribute;
import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.Instance;
import org.apache.velocity.tools.model.Model;
import org.apache.velocity.tools.model.RowAttribute;
import org.apache.velocity.tools.model.RowsetAttribute;
import org.apache.velocity.tools.model.ScalarAttribute;
import org.slf4j.Logger;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public abstract class AttributeHolder implements Serializable
{
    protected abstract Model getModel();

    protected abstract Logger getLogger();

    protected void initializeAttributes()
    {
        for (Attribute attribute : attributesMap.values())
        {
            attribute.initialize();
        }
    }

    public Attribute getAttribute(String name)
    {
        return attributesMap.get(name); // TODO resolveCase?
    }

    public ScalarAttribute getScalarAttribute(String name)
    {
        Attribute attr = getAttribute(name);
        return attr instanceof ScalarAttribute ? (ScalarAttribute)attr : null;
    }

    public RowAttribute getRowAttribute(String name)
    {
        Attribute attr = getAttribute(name);
        return attr instanceof RowAttribute ? (RowAttribute)attr : null;
    }

    public RowsetAttribute getRowsetAttribute(String name)
    {
        Attribute attr = getAttribute(name);
        return attr instanceof RowsetAttribute ? (RowsetAttribute)attr : null;
    }

    public Action getAction(String name)
    {
        Attribute attr = getAttribute(name);
        return attr instanceof Action ? (Action)attr : null;
    }

    public Serializable evaluate(String name, Serializable... params) throws SQLException
    {
       Attribute attribute = getAttribute(name);
       if (attribute == null)
       {
           throw new IllegalArgumentException("unknown baseAttribute: " + name);
       }
       if (!(attribute instanceof ScalarAttribute))
       {
           throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
       }
       return ((ScalarAttribute)attribute).evaluate(params);
    }

    public Serializable evaluate(String name, Map source) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown baseAttribute: " + name);
        }
        if (!(attribute instanceof ScalarAttribute))
        {
            throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
        }
        return ((ScalarAttribute)attribute).evaluate(source);
    }

    protected Serializable evaluate(String name, Map source, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown baseAttribute: " + name);
        }
        if (!(attribute instanceof ScalarAttribute))
        {
            throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
        }
        return ((ScalarAttribute)attribute).evaluate(source, params);
    }

    public Instance retrieve(String name, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown baseAttribute: " + name);
        }
        if (!(attribute instanceof RowAttribute))
        {
            throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
        }
        return ((RowAttribute)attribute).retrieve(params);
    }

    public Instance retrieve(String name, Map source) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown baseAttribute: " + name);
        }
        if (!(attribute instanceof RowAttribute))
        {
            throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
        }
        return ((RowAttribute)attribute).retrieve(source);
    }

    public Instance retrieve(String name, Map source, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown baseAttribute: " + name);
        }
        if (!(attribute instanceof RowAttribute))
        {
            throw new IllegalArgumentException("not a scalar baseAttribute: " + name);
        }
        return ((RowAttribute)attribute).retrieve(source, params);
    }

    public Iterator<Instance> query(String name, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof RowsetAttribute))
        {
            throw new IllegalArgumentException("not a scalar attribute: " + name);
        }
        return ((RowsetAttribute)attribute).query(params);
    }

    public Iterator<Instance> query(String name, Map source) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof RowsetAttribute))
        {
            throw new IllegalArgumentException("not a scalar attribute: " + name);
        }
        return ((RowsetAttribute)attribute).query(source);
    }

    public Iterator<Instance> query(String name, Map source, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof RowsetAttribute))
        {
            throw new IllegalArgumentException("not a scalar attribute: " + name);
        }
        return ((RowsetAttribute)attribute).query(source, params);
    }

    public int perform(String name, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof Action))
        {
            throw new IllegalArgumentException("not an action attribute: " + name);
        }
        return ((Action)attribute).perform(params);
    }

    public int perform(String name, Map source) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof Action))
        {
            throw new IllegalArgumentException("not an action attribute: " + name);
        }
        return ((Action)attribute).perform(source);
    }

    public int perform(String name, Map source, Serializable... params) throws SQLException
    {
        Attribute attribute = getAttribute(name);
        if (attribute == null)
        {
            throw new IllegalArgumentException("unknown attribute: " + name);
        }
        if (!(attribute instanceof Action))
        {
            throw new IllegalArgumentException("not an action attribute: " + name);
        }
        return ((Action)attribute).perform(source, params);
    }

    protected Entity resolveEntity(String name)
    {
        return getModel().getEntity(name);
    }

    protected void addAttribute(Attribute attribute)
    {
        attributesMap.put(attribute.getName(), attribute);
    }

    public NavigableMap<String, Attribute> getAttributes()
    {
        return Collections.unmodifiableNavigableMap(attributesMap);
    }

    private NavigableMap<String, Attribute> attributesMap = new TreeMap<>();
}

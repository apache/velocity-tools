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

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.model.impl.BaseEntity;
import org.apache.velocity.tools.model.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class Entity extends BaseEntity implements Iterable<Instance>
{
    protected static Logger logger = LoggerFactory.getLogger(Entity.class);

    public Entity(String name, Model model)
    {
        super(name, model);
        setInstanceBuilder(() -> new Instance(this));
    }

    public Logger getLogger()
    {
        return logger;
    }

    public Iterator<Instance> iterate() throws SQLException
    {
        return getIterateAttribute().query();
    }

    public Iterator<Instance> iterator()
    {
        try
        {
            return iterate();
        }
        catch (SQLException sqle)
        {
            throw new VelocityException("cannot iterate on instances of " + getName(), sqle);
        }
    }

    public long getCount() throws SQLException
    {
        return TypeUtils.toLong(getCountAttribute().evaluate());
    }

    public Instance fetch(Serializable... key) throws SQLException
    {
        return getFetchAttribute().retrieve(key);
    }

    public Instance fetch(Map key) throws SQLException
    {
        return getFetchAttribute().retrieve(key);
    }

    @Override
    protected Map<String, Method> getWrappedInstanceGetters()
    {
        return super.getWrappedInstanceGetters();
    }

    protected Map<String, Method> getWrappedInstanceSetters()
    {
        return super.getWrappedInstanceSetters();
    }


}

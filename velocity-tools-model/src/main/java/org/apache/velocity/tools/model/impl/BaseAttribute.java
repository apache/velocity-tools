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

import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.Instance;
import org.apache.velocity.tools.model.sql.RowValues;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>BaseAttribute interface</p>
 * @author Claude Brisson
 * @version $Revision: $
 * @since 3.1
 */

public abstract class BaseAttribute extends InstanceProducer implements Serializable
{
    public BaseAttribute(String name, AttributeHolder parent)
    {
        super(parent.getModel());
        this.parent = parent;
        this.attributeName = name;
    }

    protected void initialize()
    {
        if (query != null)
        {
            query = query.trim();
        }
    }

    public String getName()
    {
        return attributeName;
    }

    public AttributeHolder getParent()
    {
        return parent;
    }

    protected void setResultEntity(String entityName)
    {
        setResultEntity(parent.resolveEntity(entityName));
    }

    public List<String> getParameterNames()
    {
        return Collections.unmodifiableList(parameterNames);
    }

    protected void addQueryPart(String queryPart)
    {
        query = query + queryPart;
    }

    protected void addParameter(String paramName)
    {
        parameterNames.add(paramName);
        query = query + "?";
    }

    protected boolean getCaching()
    {
        return caching;
    }

    protected Serializable[] getParamValues(RowValues source) throws SQLException
    {
        Serializable[] paramValues = new Serializable[parameterNames.size()];
        for (int i = 0; i < paramValues.length; ++i)
        {
            paramValues[i] = source.get(parameterNames.get(i));
        }
        return paramValues;
    }

    protected Serializable[] getParamValues(Serializable[] paramValues) throws SQLException
    {
        Entity entity = (Entity)Optional.ofNullable(getParent()).filter(x -> x instanceof Entity).orElse(null);
        if (entity != null)
        {
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                paramValues[i] = entity.filterValue(paramName, paramValues[i]);
            }
        }
        return paramValues;
    }

    protected Serializable[] getParamValues(Map source) throws SQLException
    {
        Serializable[] paramValues = new Serializable[parameterNames.size()];
        Entity parentEntity = (Entity)Optional.ofNullable(getParent()).filter(x -> x instanceof Entity).orElse(null);
        Entity sourceEntity = Optional.ofNullable(source).filter(x -> x instanceof Instance).map(i -> ((Instance)i).getEntity()).orElse(null);
        if (parentEntity == null && sourceEntity == null)
        {
            for (int i = 0; i < paramValues.length; ++i)
            {
                paramValues[i] = (Serializable)source.get(parameterNames.get(i));
            }
        }
        else if (parentEntity == null || sourceEntity == null || parentEntity == sourceEntity)
        {
            Entity entity = parentEntity == null ? sourceEntity : parentEntity;
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                paramValues[i] = entity.filterValue(paramName, (Serializable)source.get(paramName));
            }
        }
        else
        {
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                Serializable value = (Serializable)source.get(paramName);
                paramValues[i] = parentEntity.hasColumn(paramName) ?
                    parentEntity.filterValue(paramName, value) :
                    sourceEntity.filterValue(paramName, value);
            }
        }
        return paramValues;
    }

    protected Serializable[] getParamValues(Map source, Serializable[] additionalParams) throws SQLException
    {
        Serializable[] paramValues = new Serializable[parameterNames.size()];
        Entity parentEntity = (Entity)Optional.ofNullable(getParent()).filter(x -> x instanceof Entity).orElse(null);
        Entity sourceEntity = Optional.ofNullable(source).filter(x -> x instanceof Instance).map(i -> ((Instance)i).getEntity()).orElse(null);
        int additionalParamIndex = 0;
        if (parentEntity == null && sourceEntity == null)
        {
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                paramValues[i] = source.containsKey(paramName) ? (Serializable)source.get(paramName) : additionalParams[additionalParamIndex++];
            }
        }
        else if (parentEntity == null || sourceEntity == null || parentEntity == sourceEntity)
        {
            Entity entity = parentEntity == null ? sourceEntity : parentEntity;
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                Serializable value = source.containsKey(paramName) ? (Serializable)source.get(paramName) : additionalParams[additionalParamIndex++];
                paramValues[i] = entity.filterValue(paramName, value);
            }
        }
        else
        {
            for (int i = 0; i < paramValues.length; ++i)
            {
                String paramName = parameterNames.get(i);
                Serializable value = source.containsKey(paramName) ? (Serializable)source.get(paramName) : additionalParams[additionalParamIndex++];
                paramValues[i] = parentEntity.hasColumn(paramName) ?
                    parentEntity.filterValue(paramName, value) :
                    sourceEntity.filterValue(paramName, value);
            }
        }
        if (additionalParamIndex != additionalParams.length)
        {
            throw new SQLException("too many parameters provided");
        }
        return paramValues;
    }

    protected String getQuery() throws SQLException
    {
        return query;
    }

    private boolean caching = false;
    private AttributeHolder parent = null;
    private Entity resultEntity = null;
    private String attributeName = null;
    private String query = "";
    protected List<String> parameterNames = new ArrayList<>();
}

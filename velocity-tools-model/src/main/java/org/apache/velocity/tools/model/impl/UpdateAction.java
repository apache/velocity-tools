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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.model.Action;
import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.Instance;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateAction extends Action
{
    protected static String DYNAMIC_PART = "_DYNAMIC_PART_";

    public UpdateAction(AttributeHolder parent)
    {
        super("update", parent);
    }

    @Override
    protected void addParameter(String paramName)
    {
        parameterNames.add(paramName);
        if (DYNAMIC_PART.equals(paramName))
        {
            addQueryPart(DYNAMIC_PART);
        }
        else
        {
            addQueryPart("?");
        }
    }


    @Override
    public int perform(Map source) throws SQLException
    {
        if (!(source instanceof Instance))
        {
            throw new SQLException("unexpected condition");
        }
        Instance instance = (Instance)source;
        setState(instance.getDirtyFlags());
        return perform(getParamValues(source));
    }

    @Override
    public String getQuery() throws SQLException
    {
        if (state == null)
        {
            throw new SQLException("update action called without state");
        }
        Entity entity = (Entity)getParent();
        List<String> dirtyColumns = state.stream().mapToObj(col -> entity.quoteIdentifier(entity.getColumn(col).sqlName)).collect(Collectors.toList());
        String dirtyPart = StringUtils.join(dirtyColumns, " = ?, ") + " = ?";
        return super.getQuery().replace(DYNAMIC_PART, dirtyPart);
    }

    public void setState(BitSet state)
    {
        this.state = state;
    }

    @Override
    protected Serializable[] getParamValues(Serializable[] params) throws SQLException
    {
        // already filtered
        return params;
    }

    @Override
    protected Serializable[] getParamValues(Map source) throws SQLException
    {
        Instance instance = (Instance)source;
        Entity entity = instance.getEntity();
        if (entity != getParent())
        {
            throw new SQLException("inconsistency");
        }
        List<String> columnNames = entity.getColumnNames();
        Serializable[] paramValues = new Serializable[state.cardinality() + parameterNames.size() - 1];
        int paramIndex = 0;
        for (int i = 0; i < paramValues.length;)
        {
            String paramName = parameterNames.get(paramIndex++);
            if (DYNAMIC_PART.equals(paramName))
            {
                int col = -1;
                while ((col = state.nextSetBit(col + 1)) != -1)
                {
                    String columnName = entity.getColumnName(col);
                    paramValues[i++] = entity.filterValue(columnName, instance.get(columnName));
                }
            }
            else
            {
                paramValues[i] = entity.filterValue(paramName, instance.get(paramName));
                ++i;
            }
        }
        return paramValues;
    }

    private BitSet state = null;
}

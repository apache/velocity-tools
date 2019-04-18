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

import org.apache.velocity.tools.model.impl.AttributeHolder;
import org.apache.velocity.tools.model.sql.PooledStatement;
import org.apache.velocity.tools.model.util.TypeUtils;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ScalarAttribute extends Attribute
{
    public ScalarAttribute(String name, AttributeHolder parent)
    {
        super(name, parent);
    }

    public Serializable evaluate(Serializable... paramValues) throws SQLException
    {
        return evaluateImpl(getParamValues(paramValues));
    }

    public Serializable evaluate(Map source) throws SQLException
    {
        return evaluateImpl(getParamValues(source));
    }

    public Serializable evaluate(Map source, Serializable... params) throws SQLException
    {
        return evaluateImpl(getParamValues(source, params));
    }

    protected Serializable evaluateImpl(Serializable... paramValues) throws SQLException
    {
        Serializable value = null;
        PooledStatement statement = null;
        try
        {
            statement = getModel().prepareQuery(getQuery());
            statement.getConnection().enterBusyState();
            ResultSet result = statement.executeQuery(paramValues);
            if (result.next())
            {
                value = (Serializable)result.getObject(1);
                if (result.wasNull())
                {
                    value = null;
                }
            }
        }
        finally
        {
            if (statement != null)
            {
                statement.notifyOver();
                statement.getConnection().leaveBusyState();
            }
        }
        return value;
    }

    protected String getQueryMethodName()
    {
        return "evaluate";
    }

    public String getString(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toString(evaluate(paramValues));
    }

    public String getString(Map source) throws SQLException
    {
        return TypeUtils.toString(evaluate(source));
    }

    public Boolean getBoolean(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toBoolean(evaluate(paramValues));
    }

    public Boolean getBoolean(Map source) throws SQLException
    {
        return TypeUtils.toBoolean(evaluate(source));
    }

    public Short getShort(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toShort(evaluate(paramValues));
    }

    public Short getShort(Map source) throws SQLException
    {
        return TypeUtils.toShort(evaluate(source));
    }

    public Integer getInteger(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toInteger(evaluate(paramValues));
    }

    public Integer getInteger(Map source) throws SQLException
    {
        return TypeUtils.toInteger(evaluate(source));
    }

    public Long getLong(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toLong(evaluate(paramValues));
    }

    public Long getLong(Map source) throws SQLException
    {
        return TypeUtils.toLong(evaluate(source));
    }

    public Float getFloat(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toFloat(evaluate(paramValues));
    }

    public Float getFloat(Map source) throws SQLException
    {
        return TypeUtils.toFloat(evaluate(source));
    }

    public Double getDouble(Serializable... paramValues) throws SQLException
    {
        return TypeUtils.toDouble(evaluate(paramValues));
    }

    public Double getDouble(Map source) throws SQLException
    {
        return TypeUtils.toDouble(evaluate(source));
    }
}

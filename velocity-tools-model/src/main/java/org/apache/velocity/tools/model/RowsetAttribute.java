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
import org.apache.velocity.tools.model.impl.RowIterator;
import org.apache.velocity.tools.model.sql.PooledStatement;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class RowsetAttribute extends Attribute
{
    public RowsetAttribute(String name, AttributeHolder parent)
    {
        super(name, parent);
    }

    public Iterator<Instance> query(Serializable... params) throws SQLException
    {
        return queryImpl(getParamValues(params));
    }

    public Iterator<Instance> query(Map source) throws SQLException
    {
        return queryImpl(getParamValues(source));
    }

    public Iterator<Instance> query(Map source, Serializable... params) throws SQLException
    {
        return queryImpl(getParamValues(source, params));
    }

    protected Iterator<Instance> queryImpl(Serializable... params) throws SQLException
    {
        Iterator<Instance> iterator = null;
        PooledStatement statement = null;
        ResultSet result = null;
        try
        {
            statement = getModel().prepareQuery(getQuery());
            statement.getConnection().enterBusyState();
            result = statement.executeQuery(params);
            iterator = new RowIterator(getParent(), statement, result, getResultEntity());
        }
        finally
        {
            if (statement != null)
            {
                if (result == null)
                {
                    statement.notifyOver();
                }
                statement.getConnection().leaveBusyState();
            }
        }
        return iterator;
    }

    protected String getQueryMethodName()
    {
        return "query";
    }
}

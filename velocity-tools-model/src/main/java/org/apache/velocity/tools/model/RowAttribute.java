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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class RowAttribute extends Attribute
{
    public RowAttribute(String name, AttributeHolder parent)
    {
        super(name, parent);
    }

    public Instance retrieve(Serializable... paramValues) throws SQLException
    {
        return retrieveImpl(getParamValues(paramValues));
    }

    public Instance retrieve(Map source) throws SQLException
    {
        return retrieveImpl(getParamValues(source));
    }

    public Instance retrieve(Map source, Serializable... params) throws SQLException
    {
        return retrieveImpl(getParamValues(source, params));
    }

    protected Instance retrieveImpl(Serializable... paramValues) throws SQLException
    {
        Instance instance = null;
        PooledStatement statement = null;
        try
        {
            statement = getModel().prepareQuery(getQuery());
            statement.getConnection().enterBusyState();
            ResultSet result = statement.executeQuery(paramValues);
            if (result.next())
            {
                instance = newResultInstance();
                instance.setInitialValues(statement);
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
        return instance;
    }

    protected String getQueryMethodName()
    {
        return "retrieve";
    }

}

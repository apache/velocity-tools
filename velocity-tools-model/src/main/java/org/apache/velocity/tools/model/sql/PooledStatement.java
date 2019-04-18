package org.apache.velocity.tools.model.sql;

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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * this class encapsulates a jdbc PreparedStatement (and a potential ResultSet encapsulated by its base class).
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 *
 */
public class PooledStatement extends Pooled implements RowValues
{
    protected static Logger logger = LoggerFactory.getLogger(PooledStatement.class);

    /** org.apache.velocity.tools.generic.ValueParser$ValueParserSub class, if found in the classpath. */
    private static Class valueParserSubClass = null;

    static
    {
        try
        {
            valueParserSubClass = Class.forName("org.apache.velocity.tools.generic.ValueParser$ValueParserSub");
        }
        catch(ClassNotFoundException cnfe) {}
    }

    /**
     * build a new PooledStatement.
     *
     * @param connection database connection
     * @param preparedStatement wrapped prepared statement
     */
    public PooledStatement(ConnectionWrapper connection, PreparedStatement preparedStatement)
    {
        this.connection = connection;
        this.preparedStatement = preparedStatement;
    }

    /**
     * check whether this pooled object is marked as valid or invalid.
     * (used in the recovery execute)
     *
     * @return whether this object is in use
     */
    public boolean isValid()
    {
        return super.isValid() && preparedStatement != null;
    }

    public synchronized ResultSet executeQuery(Serializable... paramValues) throws SQLException
    {
        try
        {
            setParamValues(paramValues);
            getConnection().enterBusyState();
            return resultSet = preparedStatement.executeQuery();
        }
        finally
        {
            getConnection().leaveBusyState();
        }
    }

    public synchronized int executeUpdate(Serializable... paramValues) throws SQLException
    {
        try
        {
            setParamValues(paramValues);
            getConnection().enterBusyState();
            return preparedStatement.executeUpdate();
        }
        finally
        {
            getConnection().leaveBusyState();
        }
    }


    private void setParamValues(Serializable[] paramValues) throws SQLException
    {
        for (int i = 0; i < paramValues.length; ++i)
        {
            preparedStatement.setObject(i + 1, paramValues[i]);
        }
    }

    /**
     * issue the modification query of this prepared statement.
     *
     * @param params parameter values
     * @exception SQLException thrown by the database engine
     * @return the numer of affected rows
     */
    public synchronized int update(List params) throws SQLException
    {
        try
        {
            logger.trace("update-params={}", params);
            setParams(params);
            connection.enterBusyState();

            int rows = preparedStatement.executeUpdate();

            return rows;
        }
        finally
        {
            connection.leaveBusyState();
            notifyOver();
        }
    }

    /**
     * get the object value of the specified resultset column.
     *
     * @param key the name of the resultset column
     * @exception SQLException thrown by the database engine
     * @return the object value returned by jdbc
     */
    public synchronized Serializable get(Object key) throws SQLException
    {
        if(!(key instanceof String) || resultSet == null)
        {
            return null;
        }

        Serializable ret = (Serializable)resultSet.getObject((String)key);

        /*
        if(entity != null && entity.isObfuscated((String)key))
        {
            ret = entity.obfuscate((ret));
        }
        */
        return ret;
    }

    public Set<String> keySet() throws SQLException
    {
        if(resultSet == null) return new HashSet<String>();
        return new HashSet<String>(SqlUtils.getColumnNames(resultSet));
    }

    /**
     * get the last insert id.
     *
     * @exception SQLException thrown by the database engine
     * @return the last insert id
     */
    public synchronized long getLastInsertID(String keyColumn) throws SQLException
    {
        return connection.getLastInsertId(preparedStatement, keyColumn);
    }

    /**
     * close this statement.
     *
     * @exception SQLException thrown by the database engine
     */
    public synchronized void close() throws SQLException
    {
        if(preparedStatement != null)
        {
            preparedStatement.close();
        }
    }

    /**
     * get statement Connection.
     *
     *  @return the Connection object (usually a ConnectionWrapper object)
     */
    public ConnectionWrapper getConnection()
    {
        return connection;
    }

    /**
     * set prepared parameter values.
     *
     * @param params parameter values
     * @exception SQLException thrown by the database engine
     */
    private void setParams(List params) throws SQLException
    {
        for(int i = 0; i < params.size(); i++)
        {
            Object param = params.get(i);

            if(valueParserSubClass != null && valueParserSubClass.isAssignableFrom(param.getClass()))
            {
                param = param.toString();
            }
            preparedStatement.setObject(i + 1, param);
        }
    }

    /**
     * wrapped prepared statement.
     */
    private transient PreparedStatement preparedStatement = null;
}

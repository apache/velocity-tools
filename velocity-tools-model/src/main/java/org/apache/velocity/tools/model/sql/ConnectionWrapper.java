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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Connection wrapper class. Allows the handling of a busy state
 *
 *  @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 */

public class ConnectionWrapper
    implements Connection, Serializable
{

    protected static Logger logger = LoggerFactory.getLogger(ConnectionWrapper.class);

    /**
     * Constructor.
     * @param driverInfos infos on the driver
     * @param connection connection to be wrapped
     */
    public ConnectionWrapper(DriverInfos driverInfos, Connection connection)
    {
        this.driverInfos = driverInfos;
        this.connection = connection;
    }

    /**
     * Unwrap the connection.
     * @return the unwrapped connection
     */
    public Connection unwrap()
    {
        return connection;
    }

    /**
     * Create a statement.
     * @return created statement
     * @throws SQLException
     */
    public synchronized Statement createStatement()
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createStatement();
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareStatement(s);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a callable statement.
     * @param s SQL query
     * @return prepared callable statement
     * @throws SQLException
     */
    public synchronized CallableStatement prepareCall(String s)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareCall(s);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Gets native SQL for a query.
     * @param s query
     * @return native SQL
     * @throws SQLException
     */
    public synchronized String nativeSQL(String s)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.nativeSQL(s);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Set autocommit flag.
     * @param flag autocommit
     * @throws SQLException
     */
    public void setAutoCommit(boolean flag)
        throws SQLException
    {
        connection.setAutoCommit(flag);
    }

    /**
     * Get autocommit flag.
     *
     * @return autocommit flag
     * @throws SQLException
     */
    public boolean getAutoCommit()
        throws SQLException
    {
        return connection.getAutoCommit();
    }

    /**
     * Commit.
     *
     * @throws SQLException
     */
    public synchronized void commit()
        throws SQLException
    {
        try
        {
            enterBusyState();
            connection.commit();
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Rollback.
     *
     * @throws SQLException
     */
    public synchronized void rollback()
        throws SQLException
    {
        try
        {
            enterBusyState();
            connection.rollback();
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Close.
     *
     * @throws SQLException
     */
    public void close()
        throws SQLException
    {
        // since some sql drivers refuse to close a connection that has been interrupted,
        // better handle this also ourselves
        closed = true;
        connection.close();
    }

    /**
     * Check the closed state.
     * @return closed state
     * @throws SQLException
     */
    public boolean isClosed()
        throws SQLException
    {
        return (closed || connection == null || connection.isClosed());
    }

    /**
     * Get meta data
     * @return database meta data
     * @throws SQLException
     */
    public DatabaseMetaData getMetaData()
        throws SQLException
    {
        return connection.getMetaData();
    }

    /**
     * set read-only flag
     * @param flag read-only
     * @throws SQLException
     */
    public void setReadOnly(boolean flag)
        throws SQLException
    {
        connection.setReadOnly(flag);
    }

    /**
     * Check the read-only state.
     * @return read-only state
     * @throws SQLException
     */
    public boolean isReadOnly()
        throws SQLException
    {
        return connection.isReadOnly();
    }

    /**
     * Catalog setter.
     * @param s catalog
     * @throws SQLException
     */
    public void setCatalog(String s)
        throws SQLException
    {
        connection.setCatalog(s);
    }

    /**
     * Catalog getter.
     * @return catalog
     * @throws SQLException
     */

    public String getCatalog()
        throws SQLException
    {
        return connection.getCatalog();
    }
    /**
     * Transaction isolation setter.
     * @param i transaction isolation
     * @throws SQLException
     */

    public void setTransactionIsolation(int i)
        throws SQLException
    {
        connection.setTransactionIsolation(i);
    }

    /**
     * Transaction isolation getter.
     * @return transaction isolation
     * @throws SQLException
     */
    public int getTransactionIsolation()
        throws SQLException
    {
        return connection.getTransactionIsolation();
    }

    /**
     * Get SQL warnings.
     * @return next SQL Warning.
     * @throws SQLException
     */
    public SQLWarning getWarnings()
        throws SQLException
    {
        return connection.getWarnings();
    }

    /**
     * Clear SQL warnings.
     * @throws SQLException
     */
    public void clearWarnings()
        throws SQLException
    {
        connection.clearWarnings();
    }

    /**
     * Create a statement.
     *
     * @param i result set type
     * @param j result set concurrency
     * @return new statement
     * @throws SQLException
     */
    public synchronized Statement createStatement(int i, int j)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createStatement(i, j);
        }
        finally
        {
            leaveBusyState();
        }

    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @param i result set type
     * @param j result set concurrency
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s, int i, int j)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareStatement(s, i, j);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a call.
     * @param s SQL query
     * @param i result set type
     * @param j result set concurrency
     * @return callable statement
     * @throws SQLException
     */
    public synchronized CallableStatement prepareCall(String s, int i, int j)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareCall(s, i, j);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Get type map.
     * @return type map
     * @throws SQLException
     */
    public Map getTypeMap()
        throws SQLException
    {
        return connection.getTypeMap();
    }

    /**
     * Set type map.
     * @param map type map
     * @throws SQLException
     */
    public void setTypeMap(Map map)
        throws SQLException
    {
        connection.setTypeMap(map);
    }

    /**
     * Set holdability.
     * @param i holdability
     * @throws SQLException
     */
    public void setHoldability(int i)
        throws SQLException
    {
        connection.setHoldability(i);
    }

    /**
     * Get holdability.
     * @return holdability
     * @throws SQLException
     */
    public int getHoldability()
        throws SQLException
    {
        return connection.getHoldability();
    }

    /**
     * Savepoint setter.
     * @return save point
     * @throws SQLException
     */
    public synchronized Savepoint setSavepoint()
        throws SQLException
    {
        return connection.setSavepoint();
    }

    /**
     * Set named savepoint.
     * @param s savepoint name
     * @return savepoint
     * @throws SQLException
     */
    public synchronized Savepoint setSavepoint(String s)
        throws SQLException
    {
        return connection.setSavepoint(s);
    }

    /**
     * Rollback.
     * @param savepoint savepoint
     * @throws SQLException
     */
    public synchronized void rollback(Savepoint savepoint)
        throws SQLException
    {
        connection.rollback(savepoint);
    }
    /**
     * Release savepoint.
     *
     * @param savepoint savepoint
     * @throws SQLException
     */
    public synchronized void releaseSavepoint(Savepoint savepoint)
        throws SQLException
    {
        connection.releaseSavepoint(savepoint);
    }

    /**
     * Create a statement.
     * @param i result set type
     * @param j result set concurrency
     * @param k result set holdability
     * @return created statement
     * @throws SQLException
     */
    public synchronized Statement createStatement(int i, int j, int k)
        throws SQLException
    {
        try
        {
           enterBusyState();
            return connection.createStatement(i, j, k);
        }
        finally 
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @param i result set type
     * @param j result set concurrency
     * @param k result set holdability
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s, int i, int j, int k)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareStatement(s, i, j, k);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a callable statement.
     * @param s SQL query
     * @param i result set type
     * @param j result set concurrency
     * @param k result set holdability
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized CallableStatement prepareCall(String s, int i, int j, int k)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareCall(s, i, j, k);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @param i autogenerated keys
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s, int i)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareStatement(s, i);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @param ai autogenerated keys column indexes
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s, int[] ai)
        throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.prepareStatement(s, ai);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /**
     * Prepare a statement.
     * @param s SQL query
     * @param as autogenerated keys column names
     * @return prepared statement
     * @throws SQLException
     */
    public synchronized PreparedStatement prepareStatement(String s, String[] as)
        throws SQLException
    {
        try     
        {
            enterBusyState();
            return connection.prepareStatement(s,as);
        }
        finally
        {
            leaveBusyState();
        }
    }

    /** 
     * Enter busy state.
     */
    public synchronized void enterBusyState()
    {
        //Logger.trace("connection #"+toString()+": entering busy state.");
        busy++;
    }

    /**
     * Leave busy state.
     */
    public synchronized void leaveBusyState()
    {
        lastUse = System.currentTimeMillis();
        busy--;
        //Logger.trace("connection #"+toString()+": leaving busy state.");
    }

    /**
     * Check busy state.
     * @return busy state
     */
    public boolean isBusy()
    {
        return busy > 0;
    }

    /**
     * Get last use timestamp
     *
     * @return last use
     */
    public long getLastUse()
    {
      return lastUse;
    }

    public DriverInfos getDriverInfos()
    {
        return driverInfos;
    }

    /**
     * Get last inserted ID.
     *
     * @param statement
     * @return last inserted id
     * @throws SQLException
     */
    public long getLastInsertId(Statement statement, String keyColumn) throws SQLException
    {
        long ret = -1;
        switch (driverInfos.getLastInsertIdPolicy())
        {
            case METHOD:
            {
                if (lastInsertIdMethod == null)
                {
                    synchronized(this)
                    {
                        if (lastInsertIdMethod == null)
                        {
                            try
                            {
                                lastInsertIdMethod = statement.getClass().getMethod(driverInfos.getLastInsertIdMethodName());
                            }
                            catch (NoSuchMethodException nsme)
                            {
                                throw new SQLException("cannot get last insert id", nsme);
                            }
                        }
                    }
                }
                try
                {
                    ret = ((Long)lastInsertIdMethod.invoke(statement)).longValue();
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    throw new SQLException("cannot get last insert id", e);
                }
                break;
            }
            case GENERATED_KEYS:
            {
                ResultSet rs = statement.getGeneratedKeys();
                ResultSetMetaData meta = rs.getMetaData();
                int colNum = meta.getColumnCount();
                if (rs.next())
                {
                    ret = colNum == 1 ? rs.getLong(1) : rs.getLong(keyColumn);
                    if (rs.wasNull())
                    {
                        ret = -1;
                    }
                }
                rs.close();
            }
            case QUERY:
            {
                ResultSet rs = statement.getConnection().createStatement().executeQuery(driverInfos.getLastInsertIdQuery());
                rs.next();
                ret = rs.getLong(1);
                if (rs.wasNull())
                {
                    ret = -1;
                }
                rs.close();
            }
            case RETURNING:
            {
                throw new SQLException("not implemented");
            }
        }
        return ret;
    }

    /**
     * Check connection.
     *
     * @return true if the connection is ok
     */
    public synchronized boolean check()
    {
        try
        {
            String checkQuery = driverInfos.getPingQuery();
            if (checkQuery == null)
            {
                // at least, call isClosed
                return !isClosed();
            }
            else
            {
                if (checkStatement == null)
                {
                    checkStatement = prepareStatement(checkQuery);
                }
                checkStatement.executeQuery();
            }
            return true;
        }
        catch (Exception e)
        {
            logger.warn("Exception while checking connection. Refreshing...");
            return false;
        }
    }

    /** Infos on the driver. */
    private DriverInfos driverInfos = null;

    private Method lastInsertIdMethod = null;

    /** Wrapped connection. */
    private transient Connection connection = null;

    /** Busy state. */
    private int busy = 0;

    /** Last use */
    private long lastUse = System.currentTimeMillis();

    /** Closed state. */
    private boolean closed = false;

    /** statement used to check connection ("select 1").
     */
    private transient PreparedStatement checkStatement = null;

    /*
     * 1.6 API
     */

    public Clob createClob() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createClob();
        }
        finally
        {
            leaveBusyState();
        }
		}

    public Blob createBlob() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createBlob();
        }
        finally
        {
            leaveBusyState();
        }
    }

    public NClob createNClob() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createNClob();
        }
        finally
        {
            leaveBusyState();
        }
    }

    public SQLXML createSQLXML() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createSQLXML();
        }
        finally
        {
            leaveBusyState();
        }
    }

    public boolean isValid(int timeout) throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.isValid(timeout);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public void setClientInfo(String name,String value) throws SQLClientInfoException
    {
        try
        {
            enterBusyState();
            connection.setClientInfo(name, value);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        try
        {
            enterBusyState();
            connection.setClientInfo(properties);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public Properties getClientInfo() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.getClientInfo();
        }
        finally
        {
            leaveBusyState();
        }
    }

    public String getClientInfo(String name) throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.getClientInfo(name);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createArrayOf(typeName, elements);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.createStruct(typeName, attributes);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new SQLException("Unsupported method.");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException("Unsupported method.");
    }

		/*
		 * 1.7 API
	 	*/

    public void setSchema(String schema) throws SQLException
    {
        try
        {
            enterBusyState();
            connection.setSchema(schema);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public String getSchema() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.getSchema();
        }
        finally
        {
            leaveBusyState();
        }
    }

    public void abort(Executor executor) throws SQLException
    {
        try
        {
            enterBusyState();
            connection.abort(executor);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        try
        {
            enterBusyState();
            connection.setNetworkTimeout(executor, milliseconds);
        }
        finally
        {
            leaveBusyState();
        }
    }

    public int getNetworkTimeout() throws SQLException
    {
        try
        {
            enterBusyState();
            return connection.getNetworkTimeout();
        }
        finally
        {
            leaveBusyState();
        }
    }
		
}

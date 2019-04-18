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

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Connection pool.
 *
 *  @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 */
public class ConnectionPool implements Serializable
{
    protected static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    /**
     * Constructor.
     * @param schema schema
     * @param driverInfos infos on the driverInfos
     * @param autocommit autocommit
     * @param max max connections
     * @throws SQLException
     * /
    public ConnectionPool(String schema, DriverInfos driverInfos, boolean autocommit, int max)
            throws SQLException
    {
        this.schema = schema;
        this.driverInfos = driverInfos;
        this.autocommit = autocommit;
        connections = new ArrayList<ConnectionWrapper>();
        this.max = max;
    }
    */

    /**
     *
     * @param dataSource
     * @param schema
     * @param max
     * @throws SQLException
     */
    public ConnectionPool(DataSource dataSource, Credentials credentials, DriverInfos driverInfos, String schema, boolean autocommit, int max) throws SQLException
    {
        this.dataSource = dataSource;
        this.credentials = credentials;
        this.driverInfos = driverInfos;
        this.schema = schema;
        this.autocommit = autocommit;
        this.max = max;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    /**
     * Get a connection.
     * @return a connection
     * @throws SQLException
     */
    public synchronized ConnectionWrapper getConnection() throws SQLException
    {
        for(Iterator it = connections.iterator(); it.hasNext(); )
        {
            ConnectionWrapper c = (ConnectionWrapper)it.next();

            if(c.isClosed())
            {
                it.remove();
            }
            else if(!c.isBusy())
            {
                return c;
            }
        }
        if(connections.size() == max)
        {
            logger.warn("Connection pool: max number of connections reached! ");

            // return a busy connection...
            return connections.get(0);
        }

        ConnectionWrapper newconn = createConnection();

        connections.add(newconn);
        return newconn;
    }

    /**
     * Create a connection.
     *
     * @return connection
     * @throws SQLException
     */
    private ConnectionWrapper createConnection() throws SQLException
    {
        logger.info("Creating a new connection.");

        Connection connection = credentials.getConnection(dataSource);

        // schema
        if(schema != null && schema.length() > 0)
        {
            String schemaQuery = driverInfos.getSchemaQuery();

            if(schemaQuery != null)
            {
                schemaQuery = schemaQuery.replace("$schema", schema);
                Statement stmt = connection.createStatement();

                stmt.executeUpdate(schemaQuery);
                stmt.close();
            }
        }

        // autocommit
        connection.setAutoCommit(autocommit);
        return new ConnectionWrapper(driverInfos, connection);
    }

/*
    private String getSchema(Connection connection) throws SQLException
    {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select sys_context('userenv','current_schema') from dual");
        rs.next();
        return rs.getString(1);
    }*/

    /**
     * clear all connections.
     */
    public void clear()
    {
        for(Iterator it = connections.iterator(); it.hasNext(); )
        {
            ConnectionWrapper c = (ConnectionWrapper)it.next();

            try
            {
                c.close();
            }
            catch(SQLException sqle) {}
        }
    }

    private DataSource dataSource;

    private Credentials credentials;

    /** optional schema */
    private String schema = null;

    /** infos on the driverInfos */
    private DriverInfos driverInfos = null;

    /** autocommit flag */
    private boolean autocommit = true;

    /** list of all connections */
    private List<ConnectionWrapper> connections = new ArrayList<>();

    /** Maximum number of connections. */
    private int max;
}

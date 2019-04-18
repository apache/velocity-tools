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


import org.apache.velocity.tools.model.util.HashMultiMap;
import org.apache.velocity.tools.model.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a pool of PooledPreparedStatements.
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 *
 */
public class StatementPool implements /* Runnable, */ Pool
{
    protected Logger logger = LoggerFactory.getLogger(StatementPool.class);

    public StatementPool(ConnectionPool connectionPool)
    {
        this(connectionPool, false, 0);
    }

    /**
     * build a new pool.
     *
     * @param connectionPool connection pool
     */
    public StatementPool(ConnectionPool connectionPool, boolean checkConnections, long checkInterval)
    {
        this.connectionPool = connectionPool;
        this.checkConnections = checkConnections;
        this.checkInterval = checkInterval;

//      if(checkConnections) checkTimeoutThread = new Thread(this);
//      checkTimeoutThread.start();
    }

    /**
     * get a PooledStatement associated with this query.
     *
     * @param query an SQL query
     * @exception SQLException thrown by the database engine
     * @return a valid statement
     */
    protected synchronized PooledStatement prepareStatement(String query, boolean update) throws SQLException
    {
        logger.trace("prepare-" + query);

        PooledStatement statement = null;
        ConnectionWrapper connection = null;
        List available = statementsMap.get(query);

        for(Iterator it = available.iterator(); it.hasNext(); )
        {
            statement = (PooledStatement)it.next();
            if(statement.isValid())
            {
                if(!statement.isInUse() &&!(connection = statement.getConnection()).isBusy())
                {
                    // check connection
                  if(!connection.isClosed() && (!checkConnections || System.currentTimeMillis() - connection.getLastUse() < checkInterval || connection.check()))
                    {
                        statement.notifyInUse();
                        return statement;
                    }
                    else
                    {
                        dropConnection(connection);
                        it.remove();
                    }
                }
            }
            else
            {
                it.remove();
            }
        }
        if(count == maxStatements)
        {
            throw new SQLException("Error: Too many opened prepared statements!");
        }
        connection = connectionPool.getConnection();
        statement = new PooledStatement(connection,
                update ?
                    connection.prepareStatement(
                            query, connection.getDriverInfos().getLastInsertIdPolicy() == DriverInfos.LastInsertIdPolicy.GENERATED_KEYS ?
                                    Statement.RETURN_GENERATED_KEYS :
                                    Statement.NO_GENERATED_KEYS) :
                    connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
        statementsMap.put(query, statement);
        statement.notifyInUse();
        return statement;
    }

    public synchronized PooledStatement prepareQuery(String query) throws SQLException
    {
        return prepareStatement(query, false);
    }

    public synchronized PooledStatement prepareUpdate(String query) throws SQLException
    {
        return prepareStatement(query, true);
    }

    /**
     * cycle through statements to check and recycle them.
     * 
     * public void run() {
     *   while (running) {
     *       try {
     *           Thread.sleep(checkDelay);
     *       } catch (InterruptedException e) {}
     *       long now = System.currentTimeMillis();
     *       PooledStatement statement = null;
     *       for (Iterator it=statementsMap.keySet().iterator();it.hasNext();)
     *           for (Iterator jt=statementsMap.get(it.next()).iterator();jt.hasNext();) {
     *               statement = (PooledStatement)jt.next();
     *               if (statement.isInUse() && now-statement.getTagTime() > timeout)
     *                   statement.notifyOver();
     *           }
     *   }
     * }
     */

    /**
     * close all statements.
     */
    public void clear()
    {
        // close all statements
        for(Iterator it = statementsMap.keySet().iterator(); it.hasNext(); )
        {
            for(Iterator jt = statementsMap.get(it.next()).iterator(); jt.hasNext(); )
            {
                try
                {
                    ((PooledStatement)jt.next()).close();
                }
                catch(SQLException e)
                {    // don't care now...
                    logger.error("error while clearing pool", e);
                }
            }
        }
        statementsMap.clear();
    }

    /*
     *  drop all statements relative to a specific connection
     * @param connection the connection
     */
    private void dropConnection(ConnectionWrapper connection)
    {
        for(Iterator it = statementsMap.keySet().iterator(); it.hasNext(); )
        {
            for(Iterator jt = statementsMap.get(it.next()).iterator(); jt.hasNext(); )
            {
                PooledStatement statement = (PooledStatement)jt.next();

                if(statement.getConnection() == connection)
                {
                    try
                    {
                        statement.close();
                    }
                    catch(SQLException sqle) {}
                    statement.setInvalid();
                }
            }
        }
        try
        {
            connection.close();
        }
        catch(SQLException sqle) {}
    }

    /**
     * clear statements on exit.
     */
    protected void finalize()
    {
        clear();
    }

    /**
     * debug - get usage statistics.
     *
     * @return an int array : [nb of statements in use , total nb of statements]
     */
    public int[] getUsageStats()
    {
        int[] stats = new int[] { 0, 0 };

        for(Iterator it = statementsMap.keySet().iterator(); it.hasNext(); )
        {
            for(Iterator jt = statementsMap.get(it.next()).iterator(); jt.hasNext(); )
            {
                if(!((PooledStatement)jt.next()).isInUse())
                {
                    stats[0]++;
                }
            }
        }
        stats[1] = statementsMap.size();
        return stats;
    }

    /**
     * connection pool.
     */
    private ConnectionPool connectionPool;

    /**
     * statements getCount.
     */
    private int count = 0;

    /**
     * map queries -> statements.
     */
    private MultiMap statementsMap = new HashMultiMap();    // query -> PooledStatement

    /**
     * running thread.
     */
    private Thread checkTimeoutThread = null;

    /**
     * true if running.
     */
    private boolean running = true;

    /**
     * do we need to check connections?
     */
    private boolean checkConnections = true;

    /**
     * minimal check interval
     */
    private long checkInterval;

    /**
     * check delay.
     */

//  private static final long checkDelay = 30*1000;

    /**
     * after this timeout, statements are recycled even if not closed.
     */
//  private static final long timeout = 60*60*1000;

    /**
     * max number of statements.
     */
    private static final int maxStatements = 50;
}

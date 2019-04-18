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


import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This abstract class represents a pooled object with a potential encapsulated resultset.
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 *
 */
public abstract class Pooled implements Serializable
{
    /**
     * build a new pooled object.
     */
    public Pooled()
    {
        tagTime = System.currentTimeMillis();
    }

    /**
     * get the time tag of this pooled object.
     *
     * @return the time tag
     */
    public long getTagTime()
    {
        return tagTime;
    }

    /**
     * reset the time tag.
     */
    public void resetTagTime()
    {
        tagTime = System.currentTimeMillis();
    }

    /**
     * notify this object that it is in use.
     */
    public void notifyInUse()
    {
        inUse = true;
        resetTagTime();
    }

    /**
     * notify this object that it is no more in use.
     */
    public void notifyOver()
    {
        try
        {
            if(resultSet != null && !resultSet.isClosed())
            {
                resultSet.close();
            }
        }
        catch(SQLException sqle) {}    // ignore
        resultSet = null;
        inUse = false;
    }

    /**
     * check whether this pooled object is in use.
     *
     * @return whether this object is in use
     */
    public boolean isInUse()
    {
        return inUse;
    }

    /**
     * check whether this pooled object is marked as valid or invalid.
     * (used in the recovery execute)
     *
     * @return whether this object is in use
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * definitely mark this statement as meant to be deleted.
     */
    public void setInvalid()
    {
        valid = false;
    }

    /**
     * get the connection used by this statement.
     *
     * @return the connection used by this statement
     */
    public abstract ConnectionWrapper getConnection();

    /**
     * close this pooled object.
     *
     * @exception SQLException when thrown by the database engine
     */
    public abstract void close() throws SQLException;

    /**
     * time tag.
     */
    private long tagTime = 0;

    // states (inUse - useOver) : (false-false) -> (true-false) -> (true-true) -> [delay] (false-false)

    /**
     * valid statement?
     */
    private boolean valid = true;

    /**
     * is this object in use?
     */
    private boolean inUse = false;

    /**
     * database connection.
     */
    protected ConnectionWrapper connection = null;

    /**
     * result set.
     */
    protected transient ResultSet resultSet = null;
}

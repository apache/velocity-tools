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
import org.apache.velocity.tools.model.sql.PooledStatement;
import org.apache.velocity.tools.model.sql.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import org.apache.velocity.tools.model.util.UserContext;

/**
 * This class is a context wrapper for ResultSets, and provides an iteration mecanism for #foreach loops, as long as getters for values of the current row.
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 */
public class RowIterator extends InstanceProducer implements Iterator<Instance>, Serializable
{
    Logger logger = LoggerFactory.getLogger(RowIterator.class);

    /**
     * Build a new RowIterator.
     *
     * @param pooledStatement the sql statement
     * @param resultSet the resultset
     * @param resultEntity the resulting entity (may be null)
     */
    public RowIterator(AttributeHolder parent, PooledStatement pooledStatement, ResultSet resultSet, Entity resultEntity)
    {
        super(parent.getModel(), resultEntity);
        this.pooledStatement = pooledStatement;
        this.resultSet = resultSet;
    }

    /**
     * Returns true if the iteration has more elements.
     *
     * @return <code>true</code> if the iterator has more elements.
     */
    public boolean hasNext()
    {
        boolean ret = false;

        try
        {
            /* always need to prefetch, as some JDBC drivers (like HSQLDB driver) seem buggued to this regard */
            if(isOver)
            {
                return false;
            }
            else if(prefetch)
            {
                return true;
            }
            else
            {
                try
                {
                    pooledStatement.getConnection().enterBusyState();
                    ret = resultSet.next();
                }
                finally
                {
                    pooledStatement.getConnection().leaveBusyState();
                }
                if(ret)
                {
                    prefetch = true;
                }
                else
                {
                    isOver = true;
                    pooledStatement.notifyOver();
                }
            }
            return ret;
        }
        catch(SQLException e)
        {
            logger.error(e.getMessage());
            isOver = true;
            pooledStatement.notifyOver();
            return false;
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return an Instance.
     */
    public Instance next()
    {
        try
        {
            if (isOver || !prefetch && !resultSet.next())
            {
                if(!isOver)
                {
                    isOver = true;
                    pooledStatement.notifyOver();
                }
                return null;
            }
            prefetch = false;

            Instance row =  newResultInstance();
            row.setInitialValues(pooledStatement);
            return row;
        }
        catch(SQLException sqle)
        {
            logger.error("could not get next row", sqle);
            isOver = true;
            pooledStatement.notifyOver();
            return null;
        }
    }

    // for Iterator interface, but RO (why? -> positionned updates and deletes => TODO)

    /**
     * not implemented.
     */
    public void remove()
    {
        logger.warn("'remove' not implemented");
    }

    /**
     * Generic getter for values of the current row. If no column corresponds to the specified name and a resulting entity has been specified, search among this entity's attributes.
     *
     * @param key the name of an existing column or attribute
     * @return an entity, an attribute reference, an instance, a string or null
     */
    public Serializable get(Object key) throws SQLException // TODO object ?!
    {
        String property = (String)key;
        Serializable result = null;

        if(!dataAvailable())
        {
            return null;
        }
        result = (Serializable)resultSet.getObject(property);
        /*
        if (resultEntity != null)
        {
            if (result == null)
            {
                // TODO - resolveCase? property = resultEntity.resolveName(property);
                Attribute attribute = resultEntity.getAttribute(property);
                if (attribute != null && attribute instanceof ScalarAttribute)
                {
                    result = ((ScalarAttribute)attribute).evaluate(pooledStatement);
                }
            }
        }
        */
        return result;
    }

    /**
     * Gets all the rows in a list of instances.
     *
     * @return a list of all the rows
     * /
    public List<Instance> getRows()
    {
        try
        {
            List<Instance> ret = new ArrayList<Instance>();

            pooledStatement.getConnection().enterBusyState();
            if(resultEntity != null && !resultEntity.isRootEntity())
            {
                while(!resultSet.isAfterLast() && resultSet.next())
                {
                    Instance i = resultEntity.newInstance(new ReadOnlyMap(this), true);
                    i.setClean();
                    ret.add(i);
                }
            }
            else
            {
                while(!resultSet.isAfterLast() && resultSet.next())
                {
                    Instance i = new Instance(new ReadOnlyMap(this), resultEntity == null ? null : resultEntity.getDB());
                    ret.add(i);
                }
            }
            return ret;
        }
        catch(SQLException sqle)
        {
            logger.log(sqle);
            return null;
        }
        finally
        {
            pooledStatement.getConnection().leaveBusyState();
            pooledStatement.notifyOver();
            isOver = true;
        }
    }
    */

    /*
    public List getScalars()
    {
        try
        {
            List ret = new ArrayList();

            pooledStatement.getConnection().enterBusyState();
            while(!resultSet.isAfterLast() && resultSet.next())
            {
                ret.add(resultSet.getObject(1));
            }
            return ret;
        }
        catch(SQLException sqle)
        {
            logger.log(sqle);
            return null;
        }
        finally
        {
            pooledStatement.getConnection().leaveBusyState();
            pooledStatement.notifyOver();
            isOver = true;
        }
    }
    */

    Set cachedSet = null;

    /*  */
    public Set<String> keySet() throws SQLException
    {
        if(cachedSet == null)
        {
            cachedSet = new HashSet<String>(SqlUtils.getColumnNames(resultSet));
        }
        return cachedSet;
    }

    /*  * /
    public List<String> keyList()
    {
        try
        {
            return SqlUtil.getColumnNames(resultSet);
        }
        catch(SQLException sqle)
        {
            logger.log(sqle);
            return null;
        }
    }
    */

    /**
     * Check if some data is available.
     *
     * @exception SQLException if the internal ResultSet is not happy
     * @return <code>true</code> if some data is available (ie the internal
     *     ResultSet is not empty, and not before first row neither after last
     *     one)
     */
    private boolean dataAvailable() throws SQLException
    {
        boolean ret = false;

        if(resultSet.isBeforeFirst())
        {
            try
            {
                pooledStatement.getConnection().enterBusyState();
                ret = resultSet.next();
                return ret;
            }
            finally
            {
                pooledStatement.getConnection().leaveBusyState();
                if(!ret)
                {
                    pooledStatement.notifyOver();
                    isOver = true;
                }
            }
        }
        ret = !resultSet.isAfterLast();
        return ret;
    }

    /**
     * Source statement.
     */
    private PooledStatement pooledStatement = null;

    /**
     * Wrapped result set.
     */
    private ResultSet resultSet = null;

    /**
     * Resulting entity.
     */
    private Entity resultEntity = null;

    /** whether we did prefetch a row */
    private boolean prefetch = false;

    /** whether we reached the end */
    private boolean isOver = false;
}

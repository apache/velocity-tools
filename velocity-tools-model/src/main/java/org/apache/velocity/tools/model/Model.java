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

import org.apache.velocity.tools.model.impl.BaseModel;
import org.apache.velocity.tools.model.sql.ConnectionWrapper;
import org.apache.velocity.tools.model.sql.PooledStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Model extends BaseModel
{
    protected static Logger logger = LoggerFactory.getLogger(Model.class);

    public Model()
    {
    }

    public Logger getLogger()
    {
        return logger;
    }

    public Model getModel()
    {
        return this;
    }

    /**
     * Prepare a query.
     *
     * @param query an sql query
     * @return the pooled prepared statement corresponding to the query
     */
    protected PooledStatement prepareQuery(String query) throws SQLException
    {
        return getStatementPool().prepareQuery(query);
    }

    protected PooledStatement prepareUpdate(String query) throws SQLException
    {
        return getStatementPool().prepareUpdate(query);
    }

    @Override
    protected ConnectionWrapper getTransactionConnection() throws SQLException
    {
        return super.getTransactionConnection();
    }
}

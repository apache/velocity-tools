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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * various SQL-related helpers.
 *
 *  @author <a href=mailto:claude.brisson@gmail.com>Claude Brisson</a>
 *
 */
public class SqlUtils
{
    protected static Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    // that's crazy to have to code such a method...
    // in Ruby for instance, it's :
    // '*' * n
    private static String stars(int length)
    {
        StringBuilder ret = new StringBuilder(length);
        for(int i = 0; i < length; i++)
        {
            ret.append('*');
        }
        return ret.toString();
    }

    /**
     * get the column nams of a result set
     * @param resultSet result set
     * @return list of columns
     * @throws SQLException
     */
    public static List<String> getColumnNames(ResultSet resultSet) throws SQLException
    {
        List<String> columnNames = new ArrayList<String>();
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();

        for(int c = 1; c <= count; c++)
        {
            // see http://jira.springframework.org/browse/SPR-3541
            // columnNames.add(meta.getColumnName(c));
            columnNames.add(meta.getColumnLabel(c));
        }
        return columnNames;
    }
}

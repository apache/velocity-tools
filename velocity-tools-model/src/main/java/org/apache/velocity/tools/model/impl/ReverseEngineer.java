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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.filter.Identifiers;
import org.apache.velocity.tools.model.sql.DriverInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReverseEngineer
{
    protected static Logger logger = LoggerFactory.getLogger(ReverseEngineer.class);

    private static final String STOCK_DRIVERS_PATH = "org/apache/velocity/tools/model/drivers/";

    public ReverseEngineer(DatabaseMetaData databaseMetaData, DriverInfos driverInfos)
    {
        this.databaseMetaData = databaseMetaData;
        this.driverInfos = driverInfos;
    }

    public String getCatalog() throws SQLException
    {
        return databaseMetaData.getConnection().getCatalog();
    }

    public String getSchema() throws SQLException
    {
        return databaseMetaData.getConnection().getSchema();
    }

    public static Properties getStockDriverProperties(String url) throws IOException, SQLException
    {
        // TODO - instead of relying on generic.properties, try to deduce the maximum from the metadata when vendor is unknown
        Properties stockDriverProps = null;
        Matcher matcher = Pattern.compile("^jdbc:([^:]+):").matcher(url);
        if (matcher.find())
        {
            String vendor = matcher.group(1);
            // search in stock driver properties
            InputStream is = ReverseEngineer.class.getClassLoader().getResourceAsStream(STOCK_DRIVERS_PATH + vendor + ".properties");
            if (is == null)
            {
                logger.error("no stock driver properties for vendor tag {}", vendor);
            }
            else
            {
                stockDriverProps = new Properties();
                stockDriverProps.load(is);
            }
        }
        else
        {
            logger.error("could not determine JDBC database vendor tag");
        }
        if (stockDriverProps == null)
        {
            logger.info("using generic driver properties");
            InputStream is = ReverseEngineer.class.getClassLoader().getResourceAsStream(STOCK_DRIVERS_PATH + "generic.properties");
            if (is == null)
            {
                throw new ConfigurationException("drivers/generic.properties not found in classpath");
            }
            stockDriverProps = new Properties();
            stockDriverProps.load(is);
        }
        return stockDriverProps;
    }

    public List<String> getTables() throws SQLException
    {
        List<String> ret = new ArrayList<String>();
        ResultSet tables = null;
        try
        {
            //tables = databaseMetaData.getTables(getCatalog(), getSchema(), null, new String[] { "TABLE", "VIEW" });
            tables = databaseMetaData.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
            while (tables.next())
            {
                String tableName = tables.getString("TABLE_NAME");
                String tableType = tables.getString("TABLE_TYPE");
                if (!"SYSTEM TABLE".equals(tableType) && !"SYSTEM VIEW".equals(tableType) && !driverInfos.ignoreTable(tableName))
                {
                    ret.add(tableName);
                }
            }
        }
        finally
        {
            if (tables != null)
            {
                tables.close();
            }
        }
        return ret;
    }

    public List<Entity.Column> getColumns(Entity entity) throws SQLException
    {
        Identifiers identifiers = entity.getModel().getIdentifiers();
        List<Entity.Column> ret = new ArrayList<>();
        ResultSet columns = null;
        try
        {
            // get columns
            String table = entity.getTable();
            columns = databaseMetaData.getColumns(getCatalog(), getSchema(), table, null);
            while (columns.next())
            {
                Integer size = columns.getInt("COLUMN_SIZE");
                if (columns.wasNull()) size = null;
                String colSqlName = columns.getString("COLUMN_NAME");
                String colName = identifiers.transformColumnName(table, colSqlName);
                int dataType = columns.getInt("DATA_TYPE");
                String gen1 = columns.getString("IS_AUTOINCREMENT");
                String gen2 = columns.getString("IS_GENERATEDCOLUMN");
                boolean generated = "YES".equals(gen1) || "YES".equals(gen2);
                ret.add(new Entity.Column(colName, colSqlName, dataType, size, generated));
            }
            return ret;
        }
        finally
        {
            if (columns != null)
            {
                columns.close();
            }
        }
    }

    public String[] getPrimaryKey(Entity entity) throws SQLException
    {
        ArrayList<String> keyColumns = new ArrayList<String>();
        ResultSet columns = null;
        try
        {
            // get primary key
            String table = entity.getTable();
            columns = databaseMetaData.getPrimaryKeys(getCatalog(), getSchema(), table);
            while (columns.next())
            {
                short ord = columns.getShort("KEY_SEQ");
                String columnName = columns.getString("COLUMN_NAME");
                while (keyColumns.size() < ord)
                {
                    keyColumns.add(null);
                }
                keyColumns.set(ord - 1, columnName);
            }
            return keyColumns.toArray(new String[keyColumns.size()]);
        }
        finally
        {
            if (columns != null)
            {
                columns.close();
            }
        }
    }

    public List<Pair<String, List<String>>> getJoins(Entity pkEntity) throws SQLException
    {
        List<Pair<String, List<String>>> joins = new ArrayList<>();
        List<String> knownPK = pkEntity.getSqlPrimaryKey();
        if (knownPK == null || knownPK.size() == 0)
        {
            return joins;
        }
        ResultSet exportedKeys = null;
        try
        {
            String fkTable = null;
            List<String> pkColumns = new ArrayList<String>();
            List<String> fkColumns = new ArrayList<String>();
            exportedKeys = databaseMetaData.getExportedKeys(getCatalog(), getSchema(), pkEntity.getTable());
            while (exportedKeys.next())
            {
                short ord = exportedKeys.getShort("KEY_SEQ");
                if (ord == 1 && pkColumns.size() > 0)
                {
                    // save previous key
                    fkColumns = sortColumns(pkEntity.getSqlPrimaryKey(), pkColumns, fkColumns);
                    joins.add(Pair.of(fkTable, fkColumns));
                    pkColumns.clear();
                    fkColumns.clear();
                }
                fkTable = exportedKeys.getString("FKTABLE_NAME");
                pkColumns.add(exportedKeys.getString("PKCOLUMN_NAME"));
                fkColumns.add(exportedKeys.getString("FKCOLUMN_NAME"));
            }
            // save last key
            if (fkTable != null)
            {
                fkColumns = sortColumns(pkEntity.getSqlPrimaryKey(), pkColumns, fkColumns);
                joins.add(Pair.of(fkTable, fkColumns));
            }
        }
        finally
        {
            if (exportedKeys != null)
            {
                exportedKeys.close();
            }
        }
        return joins;
    }

    /**
     * Sort columns in <code>target</code> the same way <code>unordered</code> would have to
     * be sorted to be like <code>ordered</code>.
     * @param ordered ordered list reference
     * @param unordered unordered list reference
     * @param target target list
     * @return sorted target list
     */
    private List<String> sortColumns(List<String> ordered, List<String> unordered, List<String> target)
    {
        if(ordered.size() == 1)
        {
            return target;
        }

        List<String> sorted = new ArrayList<String>();

        for(String col : ordered)
        {
            int i = unordered.indexOf(col);
            if (i == -1)
            {
                throw new ConfigurationException("foreign key inconsistency: pk column '" + col + "' not found in imported key columns");
            }
            sorted.add(target.get(i));
        }
        return sorted;
    }

    private DatabaseMetaData databaseMetaData = null;
    private DriverInfos driverInfos = null;
}

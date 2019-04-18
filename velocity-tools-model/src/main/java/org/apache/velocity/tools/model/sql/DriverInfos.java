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

import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.model.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * <p>Contains specific description and behaviour of jdbc drivers.</p>
 *
 * <p>Main sources:
 * <ul><li>http://www.schemaresearch.com/products/srtransport/doc/modules/jdbcconf.html
 * <li>http://db.apache.org/torque/ and org.apache.torque.adapter classes
 * </ul></p>
 *
 *  @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 */
public class DriverInfos implements Constants, Serializable
{
    protected static Logger logger = LoggerFactory.getLogger(DriverInfos.class);

    /*
     * Constructors
     */

    public DriverInfos()
    {
    }

    public void setDefaults(DriverInfos other)
    {
        setTag(Optional.ofNullable(getTag()).orElse(other.getTag()));
        setCatalog(Optional.ofNullable(getCatalog()).orElse(other.getCatalog()));
        setPingQuery(Optional.ofNullable(getPingQuery()).orElse(other.getPingQuery()));
        setTablesCaseSensitivity(Optional.ofNullable(getTablesCaseSensitivity()).orElse(other.getTablesCaseSensitivity()));
        setSchemaQuery(Optional.ofNullable(getSchemaQuery()).orElse(other.getSchemaQuery()));
        setLastInsertIdPolicy(Optional.ofNullable(getLastInsertIdPolicyString()).orElse(Optional.ofNullable(other.getLastInsertIdPolicyString()).orElse("none")));
        setPedanticColumnTypes(Optional.ofNullable(isPedanticColumnTypes()).orElse(Optional.ofNullable(other.isPedanticColumnTypes()).orElse(false)));
        setColumnMarkers(Optional.ofNullable(hasColumnMarkers()).orElse(Optional.ofNullable(other.hasColumnMarkers()).orElse(false)));
        Pattern ignoreTablesPattern = Optional.ofNullable(getIgnoreTablesPattern()).orElse(other.getIgnoreTablesPattern());
        setIgnoreTablesPattern(ignoreTablesPattern == null ? null : ignoreTablesPattern.toString());
        Character idQuoteChar = Optional.of(getIdentifierQuoteChar()).orElse(other.getIdentifierQuoteChar());
        setIdentifierQuoteChar(idQuoteChar == null ? null : String.valueOf(idQuoteChar));
    }

    /*
     * Getters and setters
     */

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getCatalog()
    {
        return catalog;
    }

    public void setCatalog(String catalog)
    {
        this.catalog = catalog;
    }

    public String getPingQuery()
    {
        return pingQuery;
    }

    public void setPingQuery(String query)
    {
        this.pingQuery = pingQuery;
    }

    public CaseSensitivity getTablesCaseSensitivity()
    {
        return tablesCaseSensitivity;
    }

    public void setTablesCaseSensitivity(CaseSensitivity tablesCaseSensitivity)
    {
        this.tablesCaseSensitivity = tablesCaseSensitivity;
        switch (tablesCaseSensitivity)
        {
            case LOWERCASE: filterTableName = t -> t.toLowerCase(Locale.ROOT); break;
            case UPPERCASE: filterTableName = t -> t.toUpperCase(Locale.ROOT); break;
            default: filterTableName = t -> t;
        }
    }

    public String getSchemaQuery()
    {
        return schemaQuery;
    }

    public void setSchemaQuery(String schemaQuery)
    {
        this.schemaQuery = schemaQuery;
    }

    public LastInsertIdPolicy getLastInsertIdPolicy()
    {
        return lastInsertIdPolicy;
    }

    public String getLastInsertIdPolicyString()
    {
        if (lastInsertIdPolicy == null)
        {
            return null;
        }
        switch (lastInsertIdPolicy)
        {
            case NONE: return "none";
            case GENERATED_KEYS: return "generated_keys";
            case METHOD: return "method:" + getLastInsertIdMethodName();
            case QUERY: return "query:" + getLastInsertIdQuery();
            default: return null;
        }
    }

    public void setLastInsertIdPolicy(String policy)
    {
        if (policy.startsWith("query:"))
        {
            lastInsertIdPolicy = LastInsertIdPolicy.QUERY;
            lastInsertIdQuery = policy.substring(6).trim();
        }
        else if (policy.startsWith("method:"))
        {
            lastInsertIdPolicy = LastInsertIdPolicy.METHOD;
            lastInsertIdMethodName = policy.substring(7).trim();
        }
        else
        {
            lastInsertIdPolicy = LastInsertIdPolicy.valueOf(policy.toUpperCase());
            if (lastInsertIdPolicy == LastInsertIdPolicy.RETURNING)
            {
                throw new ConfigurationException("'returning' last insert id policy is not yet supported");
            }
        }
    }
    
    public String getLastInsertIdQuery()
    {
        return lastInsertIdQuery;
    }

    public String getLastInsertIdMethodName()
    {
        return lastInsertIdMethodName;
    }

    public Boolean isPedanticColumnTypes()
    {
        return pedanticColumnTypes;
    }

    public void setPedanticColumnTypes(boolean pedanticColumnTypes)
    {
        this.pedanticColumnTypes = pedanticColumnTypes;
    }

    public Pattern getIgnoreTablesPattern()
    {
        return ignoreTablesPattern;
    }

    public void setIgnoreTablesPattern(String pattern)
    {
        if (pattern != null && pattern.length() > 0)
        {
            ignoreTablesPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
        else
        {
            ignoreTablesPattern = null;
        }
    }

    public Character getIdentifierQuoteChar()
    {
        return identifierQuoteChar;
    }

    public void setIdentifierQuoteChar(String identifierQuoteChar)
    {
        identifierQuoteChar = identifierQuoteChar.trim();
        if (identifierQuoteChar.length() > 0)
        {
            this.identifierQuoteChar = identifierQuoteChar.charAt(0);
        }
    }

    public Boolean hasColumnMarkers()
    {
        return columnMarkers;
    }

    public void setColumnMarkers(boolean columnMarkers)
    {
        this.columnMarkers = columnMarkers;
    }

    /*
     * Operations
     */

    public String getTableName(String entityName)
    {
        return filterTableName.apply(entityName);
    }

    public String quoteIdentifier(String id)
    {
        if (identifierQuoteChar == ' ')
        {
            return id;
        }
        else
        {
            return identifierQuoteChar + id + identifierQuoteChar;
        }
    }

    /**
     * Check whether to ignore or not this table.
     *
     * @param name table name
     * @return whether to ignore this table
     */
    public boolean ignoreTable(String name)
    {
        return ignoreTablesPattern != null && ignoreTablesPattern.matcher(name).matches();
    }

    /**
     * Driver-specific value filtering
     *
     * @param value value to be filtered
     * @return filtered value
     */
    public Object filterValue(Object value)
    {
        if(value instanceof Calendar && "mysql".equals(tag))
        {
            value = ((Calendar)value).getTime();
        }
        return value;
    }

    /*
     * Members
     */

    /** jdbc tag of the database vendor */
    private String tag = "unknown";

    private String catalog = null;

    /** ping SQL query */
    private String pingQuery = null;

    /** case-sensivity */
    public enum CaseSensitivity { UNKNOWN, SENSITIVE, LOWERCASE, UPPERCASE }
    private CaseSensitivity tablesCaseSensitivity = null;
    private UnaryOperator<String> filterTableName = t -> t;

    /** SQL query to set the current schema */
    private String schemaQuery = null;

    /** ID generation method */
    public enum LastInsertIdPolicy { NONE, GENERATED_KEYS, RETURNING, QUERY, METHOD }
    private LastInsertIdPolicy lastInsertIdPolicy = null;
    private String lastInsertIdQuery = null;
    private String lastInsertIdMethodName = null;

    /** whether the JDBC driver is pedantic about column types */
    private Boolean pedanticColumnTypes = null;

    /** ignore tables matching this pattern */
    private Pattern ignoreTablesPattern = null;

    /** quoteIdentifier quote character */
    private Character identifierQuoteChar = null;

    /** whether driver supports ::varchar etc... */
    private Boolean columnMarkers = null;

    /**
       * Get the last inserted id.
       * @param statement source statement
       * @param keyColumn key column name
       * @return last inserted id (or -1)
       * @throws SQLException
       * /
      public long getLastInsertId(Statement statement, String keyColumn) throws SQLException
      {
        long ret = -1;

        if("mysql".equalsIgnoreCase(getTag()))
        {    /* MySql * /
          try
          {
            Method lastInsertId = statement.getClass().getMethod("getLastInsertID", new Class[0]);
            ret = ((Long) lastInsertId.invoke(statement, new Object[0])).longValue();
          }
          catch (Throwable e)
          {
            logger.error("Could not find last insert id", e);
          }
        }
        else if (getUsesGeneratedKeys())
        {
          int col = 1;
          ResultSet rs = statement.getGeneratedKeys();
          ResultSetMetaData rsmd = rs.getMetaData();
          int numberOfColumns = rsmd.getColumnCount();
          if (rs.next())
          {
            if (numberOfColumns > 1)
            {
              ret = rs.getLong(keyColumn);
              if (rs.wasNull()) ret = -1;
            }
            else
            {
              ret = rs.getLong(1);
              if (rs.wasNull()) ret = -1;
            }
          }
        }
        else
        {
          if (lastInsertIDQuery == null)
          {
            logger.error("getLastInsertID is not [yet] implemented for your dbms... Contribute!");
          }
          else
          {
            ResultSet rs = statement.getConnection().createStatement().executeQuery(lastInsertIDQuery);
            rs.next();
            ret = rs.getLong(1);
            if (rs.wasNull()) ret = -1;
          }
        }
        return ret;
      }
      */
}

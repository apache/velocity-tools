package org.apache.velocity.tools.model.filter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.tools.config.ConfigurationException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class ColumnMapper<T extends Serializable> extends TableMapper<T>
{
    public ColumnMapper(String configurationPrefix)
    {
        super(configurationPrefix);
    }

    @Override
    protected void addEntry(String key, T leaf)
    {
        int dot = key.indexOf('.');
        if (dot == -1)
        {
            super.addEntry(key, leaf);
        }
        else
        {
            int otherDot = key.indexOf('.', dot + 1);
            if (otherDot != -1)
            {
                throw new ConfigurationException("invalid mappingEntry key: " + getConfigurationPrefix() + "." + key);
            }
            String tablePattern = key.substring(0, dot);
            String columnPattern = key.substring(dot + 1);
            MappingEntry mappingEntry = new MappingEntry(columnPattern, leaf);
            addColumnMapping(tablePattern, columnPattern, mappingEntry);
        }
    }

    protected void addColumnMapping(String tablePattern, String columnPattern, MappingEntry mappingEntry)
    {
        Pair<MappingEntry, Map<String, MappingEntry>> pair = columnsMapping.get(tablePattern);
        if (pair == null)
        {
            pair = Pair.of(new MappingEntry(tablePattern), new HashMap<String, MappingEntry>());
            columnsMapping.put(tablePattern, pair);
        }
        Map<String, MappingEntry> colsMap = pair.getRight();
        MappingEntry prevEntry = colsMap.put(columnPattern, mappingEntry);
        if (prevEntry != null)
        {
            getLogger().warn("overriding " + getConfigurationPrefix() + ".{}.{}", tablePattern, columnPattern);
        }

        if ("*".equals(columnPattern) && "*".equals(tablePattern))
        {
            defaultColumnLeaf = mappingEntry.getLeaf();
        }
    }


    /* needed ?
    public List<MappingEntry> getColumnsMapping(String table)
    {
        List<MappingEntry> ret = new ArrayList<>();
        for (Pair<MappingEntry, Map<String, MappingEntry>> pair : columnsMapping.values())
        {
            if (pair.getLeft().matches(table))
            {
                ret.addAll(pair.getRight().values());
            }
        }
        return ret;
    }
    */

    public T getColumnEntry(String table, String column)
    {
        T ret = null;
        for (MappingEntry entry : getTablesMapping().values())
        {
            if (entry.matches(table))
            {
                if (ret == null)
                {
                    ret = entry.getLeaf();
                }
                else
                {
                    ret = composeLeaves(entry.getLeaf(), ret);
                }
            }
        }
        for (Pair<MappingEntry, Map<String, MappingEntry>> pair : columnsMapping.values())
        {
            if (pair.getLeft().matches(table))
            {
                for (MappingEntry entry : pair.getRight().values())
                {
                    if (entry.matches(column))
                    {
                        if (ret == null)
                        {
                            ret = entry.getLeaf();
                        }
                        else
                        {
                            ret = composeLeaves(entry.getLeaf(), ret);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public T getDefaultColumnLeaf()
    {
        return defaultColumnLeaf;
    }

    protected void setDefaultColumnLeaf(T defaultColumnLeaf)
    {
        this.defaultColumnLeaf = defaultColumnLeaf;
    }

    private T defaultColumnLeaf = null;

    private Map<String, Pair<MappingEntry, Map<String, MappingEntry>>> columnsMapping = new HashMap<>();
}

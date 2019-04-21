package org.apache.velocity.tools.model.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class TableMapper<T extends Serializable> extends Mapper<T>
{
    public TableMapper(String configurationPrefix)
    {
        super(configurationPrefix);
    }

    @Override
    protected void addEntry(String pattern, T leaf)
    {
        MappingEntry mappingEntry = new MappingEntry(pattern, leaf);
        addTableEntry(pattern, mappingEntry);
    }

    protected void addTableEntry(String pattern, MappingEntry mappingEntry)
    {
        MappingEntry prev = tablesMapping.put(pattern, mappingEntry);
        if (prev != null)
        {
            getLogger().warn("overriding " + getConfigurationPrefix() + ".{}", pattern);
        }
    }

    public T getTableEntry(String table)
    {
        T ret = null;
        for (MappingEntry mappingEntry : tablesMapping.values())
        {
            if (mappingEntry.matches(table))
            {
                if (ret == null)
                {
                    ret = mappingEntry.getLeaf();
                }
                else
                {
                    ret = composeLeaves(mappingEntry.getLeaf(), ret);
                }
            }
        }
        return ret;
    }

    protected Map<String, MappingEntry> getTablesMapping()
    {
        return tablesMapping;
    }

    private Map<String, MappingEntry> tablesMapping = new HashMap<>();
}

package org.apache.velocity.tools.model.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

public class Identifiers extends FilterHandler<String>
{
    protected static Logger logger = LoggerFactory.getLogger(Identifiers.class);

    public Identifiers()
    {
        super("identifiers.mapping");
        addStockObject("lowercase", x -> x.toLowerCase(Locale.ROOT));
        addStockObject("uppercase", x -> x.toUpperCase(Locale.ROOT));
        addStockObject("snake_to_camel", Identifiers::snakeToCamel);
    }

    private static final String[] prefixes = { "plural", "getPlural" };

    public static String snakeToCamel(String snake)
    {
        snake = snake.toLowerCase(Locale.ROOT);
        String[] parts = snake.split("_");
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts)
        {
            if (part.length() > 0)
            {
                builder.append(first ? part : StringUtils.capitalize(part));
                first = false;
            }
        }
        return builder.length() == 0 ? "_" : builder.toString();
    }

    public void setInflector(String inflector)
    {
        if (inflector == null || inflector.length() == 0 || inflector.equals("none"))
        {
            this.inflector = Filter.identity();
        }
        else
        {
            try
            {
                Class pluralizerClass = ClassUtils.getClass(inflector);
                Method method = null;
                for (String prefix : prefixes)
                {
                    try
                    {
                        method = ClassUtils.findSetter(prefix, pluralizerClass, x -> x == String.class);
                    }
                    catch (NoSuchMethodException nsme)
                    {
                    }
                }
                if (method == null)
                {
                    throw new ConfigurationException("invalid inflector: " + inflector);
                }
                final Method m = method;
                final Object o = pluralizerClass.newInstance();
                this.inflector = x ->
                {
                    try
                    {
                        return (String)m.invoke(o, x);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new SQLException("could not apply inflector from class " + o.getClass().getName());
                    }
                };
            }
            catch (Exception e)
            {
                throw new ConfigurationException("could not instanciate inflector", e);
            }
        }
    }

    public String pluralize(String word) throws SQLException
    {
        return inflector.apply(word);
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    @Override
    protected Filter<String> stringToLeaf(String leaf)
    {
        Filter<String> ret = null;
        if (leaf.startsWith("/"))
        {
            String[] parts = leaf.substring(1).split("/");
            if (parts.length != 2)
            {
                throw new ConfigurationException("invalid regex replacement rule, expecting /search/replace/ :" + leaf);
            }
            final Pattern pattern = Pattern.compile(parts[0], Pattern.CASE_INSENSITIVE);
            final String rep = parts[1];
            return x -> pattern.matcher(x).replaceAll(rep);
        }
        return super.stringToLeaf(leaf);
    }

    public String transformTableName(String sqlTable) throws SQLException
    {
        Filter<String> filter = getTableEntry(sqlTable);
        return filter == null ? sqlTable : filter.apply(sqlTable);
    }

    public String transformColumnName(String sqlTable, String sqlColumn) throws SQLException
    {
        Filter<String> filter = getColumnEntry(sqlTable, sqlColumn);
        return filter == null ? sqlColumn : filter.apply(sqlColumn);
    }

    private Filter<String> inflector = Filter.identity();
}

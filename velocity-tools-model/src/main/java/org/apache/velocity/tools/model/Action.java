package org.apache.velocity.tools.model;

import org.apache.velocity.tools.model.impl.AttributeHolder;
import org.apache.velocity.tools.model.sql.PooledStatement;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

public class Action extends Attribute
{
    public Action(String name, AttributeHolder parent)
    {
        super(name, parent);

    }

    public int perform(Serializable... params) throws SQLException
    {
        return performImpl(getParamValues(params));
    }

    public int perform(Map source) throws SQLException
    {
        return performImpl(getParamValues(source));
    }

    public int perform(Map source, Serializable... params) throws SQLException
    {
        return performImpl(getParamValues(source, params));
    }

    protected int performImpl(Serializable... paramValues) throws SQLException
    {
        int changed = 0;
        PooledStatement statement = null;
        try
        {
            statement = getModel().prepareUpdate(getQuery());
            statement.getConnection().enterBusyState();
            changed = statement.executeUpdate(paramValues);
        }
        finally
        {
            if (statement != null)
            {
                statement.notifyOver();
                statement.getConnection().leaveBusyState();
            }
        }
        return changed;
    }

    @Override
    public String getQueryMethodName()
    {
        return "perform";
    }
}

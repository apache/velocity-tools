package org.apache.velocity.tools.model;

import org.apache.velocity.tools.model.impl.AttributeHolder;
import org.apache.velocity.tools.model.sql.ConnectionWrapper;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Transaction extends Action
{
    public Transaction(String name, AttributeHolder parent)
    {
        super(name, parent);
    }

    @Override
    public int performImpl(Serializable... paramValues) throws SQLException
    {
        ConnectionWrapper connection = null;
        try
        {
            int changed = 0;
            connection = getModel().getTransactionConnection();
            connection.enterBusyState();
            int param = 0;
            for (String individualStatement : getStatements())
            {
                PreparedStatement statement = connection.prepareStatement(individualStatement);
                int paramCount = statement.getParameterMetaData().getParameterCount();
                for (int i = 1; i <= paramCount; ++i)
                {
                    statement.setObject(i, paramValues[param++]);
                }
                changed += statement.executeUpdate();
            }
            connection.commit();
            return changed;
        }
        catch (SQLException sqle)
        {
            if (connection != null)
            {
                connection.rollback();
            }
            throw sqle;
        }
        finally
        {
            if (connection != null)
            {
                connection.leaveBusyState();
            }
        }
    }

    private List<String> getStatements() throws SQLException
    {
        List<String> statements = new ArrayList<>();
        String[] elements = getQuery().split(";");
        for (String element : elements)
        {
            element = element.trim();
            if (element.length() > 0)
            {
                statements.add(element);
            }
        }
        return statements;
    }
}

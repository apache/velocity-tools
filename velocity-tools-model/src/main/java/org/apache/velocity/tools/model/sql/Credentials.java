package org.apache.velocity.tools.model.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class Credentials
{
    public Credentials() {}

    public String getUser()
    {
        return user;
    }

    public boolean hasCredentials()
    {
        return user != null && password != null;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Connection getConnection(DataSource dataSource) throws SQLException
    {
        if (hasCredentials())
        {
            return dataSource.getConnection(user, password);
        }
        else
        {
            return dataSource.getConnection();
        }
    }

    private String user = null;
    private String password = null;
}

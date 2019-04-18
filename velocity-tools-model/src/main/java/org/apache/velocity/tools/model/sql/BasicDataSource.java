package org.apache.velocity.tools.model.sql;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class BasicDataSource implements DataSource
{
    public BasicDataSource(String databaseURL)
    {
        this.databaseURL = databaseURL;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(databaseURL);
    }

    @Override
    public Connection getConnection(String user, String password) throws SQLException
    {
        return DriverManager.getConnection(databaseURL, user, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {

    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException("not using JDK logging");
    }

    private String databaseURL;

    private PrintWriter logWriter = null;
}

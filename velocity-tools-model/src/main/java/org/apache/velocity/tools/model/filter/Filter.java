package org.apache.velocity.tools.model.filter;

import java.io.Serializable;
import java.sql.SQLException;

@FunctionalInterface
public interface Filter<T> extends Serializable
{
    T apply(T s) throws SQLException;

    default Filter<T> compose(Filter<T> other)
    {
        return x -> apply(other.apply(x));
    }

    static Filter identity()
    {
        return x -> x;
    }
}

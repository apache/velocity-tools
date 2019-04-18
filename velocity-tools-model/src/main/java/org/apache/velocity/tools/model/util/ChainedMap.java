package org.apache.velocity.tools.model.util;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ChainedMap implements Map
{
    protected static Logger logger = LoggerFactory.getLogger(ChainedMap.class);

    private Map source;
    private Map parameters;
    
    public ChainedMap(Map source, Map parameters)
    {
        this.source = source;
        this.parameters = parameters;
    }

    public Map getSource()
    {
        return source;
    }

    public Map getParameters()
    {
        return parameters;
    }
    
    public void clear()
    {
        logger.error("trying to modify a read-only ChainedMap");
    }

    public boolean containsKey(Object key)
    {
        return source.containsKey(key) || parameters.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return source.containsValue(value) || parameters.containsValue(value);
    }

    public Set<Map.Entry> entrySet()
    {
        Set<Map.Entry> ret = source.entrySet();
        ret.addAll(parameters.entrySet());
        return ret;
    }

    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof ChainedMap)) return false;
        ChainedMap other = (ChainedMap)o;
        return source.equals(other.getSource()) && parameters.equals(other.getParameters());
    }

    public Object get(Object key)
    {
        Object ret = parameters.get(key);
        return ret == null ? source.get(key) : ret;
    }

    public int hashCode()
    {
        return source.hashCode() ^ parameters.hashCode();
    }

    public boolean isEmpty()
    {
        return source.isEmpty() && parameters.isEmpty();
    }

    public Set keySet()
    {
        Set ret = source.keySet();
        ret.addAll(parameters.keySet());
        return ret;
    }

    public Object put(Object key, Object value)
    {
        logger.error("trying to modify a read-only ChainedMap");
        return null;
    }

    public void putAll(Map m)
    {
        logger.error("trying to modify a read-only ChainedMap");
    }

    public Serializable remove(Object key)
    {
        logger.error("trying to modify a read-only ChainedMap");
        return null;
    }

    public int size()
    {
        return source.size() + parameters.size();
    }

    public Collection values()
    {
        ArrayList ret = new ArrayList();
        ret.addAll(source.values());
        ret.addAll(parameters.values());
        return ret;
    }
}

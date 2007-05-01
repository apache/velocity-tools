package org.apache.velocity.tools.config;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Configuration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Configuration<T>
{
    private Map<String,Object> simpleProperties = new HashMap<String,Object>();
    private List<Property> convertableProperties = new ArrayList<Property>();

    public void addProperty(Property property)
    {
        convertableProperties.add(property);
    }

    public List<Property> getConvertableProperties()
    {
        return convertableProperties;
    }

    public void setProperty(String name, String value)
    {
        //TODO: it could be very convenient to support some very simple
        //      auto-conversion here, where we'd test if a string value
        //      is "true" or "false" or "123" and convert those to boolean
        //      or number values without it being necessary to specify type
        if ("true".equals(value))
        {
            setProperty(name, Boolean.TRUE);
        }
        else if ("false".equals(value))
        {
            setProperty(name, Boolean.FALSE);
        }
        else
        {
            simpleProperties.put(name, value);
        }
    }

    public void setProperty(String name, Object value)
    {
        simpleProperties.put(name, value);
    }

    public boolean hasProperties()
    {
        return !simpleProperties.isEmpty() || !convertableProperties.isEmpty();
    }

    public Map<String,Object> getSimpleProperties()
    {
        return simpleProperties;
    }

    public Map<String,Object> getProperties()
    {
        Map<String,Object> all = new HashMap<String,Object>(simpleProperties);
        for (Property property : getConvertableProperties())
        {
            all.put(property.getName(), property.getConvertedValue());
        }
        return all;
    }

    public void addConfiguration(Configuration<T> config)
    {
        for (Property prop : config.getConvertableProperties())
        {
            addProperty(prop);
        }

        Map<String,Object> simples = config.getSimpleProperties();
        for (String name : simples.keySet())
        {
            setProperty(name, simples.get(name));
        }
    }

    public void validate()
    {
        for (Property property : getConvertableProperties())
        {
            property.validate();
        }
    }

    protected void appendProperties(StringBuilder out)
    {
        if (hasProperties())
        {
            Map<String,Object> props = getProperties();
            out.append("with ");
            out.append(props.size());
            out.append(" properties [");
            for (String name : props.keySet())
            {
                out.append(name);
                out.append(" => ");
                out.append(props.get(name));
                out.append("; ");
            }
            out.append("]");
        }
    }

}

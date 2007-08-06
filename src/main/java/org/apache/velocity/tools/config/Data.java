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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.ClassUtils;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Data.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Data
{
    private String key;
    private Object value;
    private DataConverter converter;

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public void setClassname(String classname)
    {
        try
        {
            setTargetClass(ClassUtils.getClass(classname));
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IllegalArgumentException("Class "+classname+" could not be found.", cnfe);
        }
    }

    /**
     * This doesn't take a {@link Class} parameter because
     * this class was not created for all-java configuration.
     */
    public void setClass(String classname)
    {
        setClassname(classname);
    }

    public void setType(String type)
    {
        // first check if it is a type we support automatically
        DataConverter dc = getDataConverter(type);
        if (dc != null)
        {
            this.converter = dc;
        }
        else // assume it is a classname
        {
            setClassname(type);
        }
    }

    public void setTargetClass(Class clazz)
    {
        if (this.converter == null)
        {
            this.converter = new DataConverter();
        }
        this.converter.setTarget(clazz);
    }

    public void setConverter(Class clazz)
    {
        try
        {
            convertWith((Converter)clazz.newInstance());
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Class "+clazz+" is not a valid "+Converter.class, e);
        }
    }

    public void setConverter(String classname)
    {
        try
        {
            convertWith((Converter)ClassUtils.getInstance(classname));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Class "+classname+" is not a valid "+Converter.class, e);
        }
    }

    /**
     * This is a convenience method for those doing configuration in java.
     * It cannot be named setConverter(), or else it would confuse BeanUtils.
     */
    public void convertWith(Converter converter)
    {
        if (this.converter == null)
        {
            this.converter = new DataConverter();
        }
        this.converter.setConverter(converter);
    }

    public String getKey()
    {
        return this.key;
    }

    public Object getValue()
    {
        return this.value;
    }

    public Class getTargetClass()
    {
        if (this.converter == null)
        {
            return String.class;
        }
        return this.converter.getTarget();
    }

    public Converter getConverter()
    {
        if (this.converter == null)
        {
            return null;
        }
        return this.converter.getConverter();
    }

    public Object getConvertedValue()
    {
        if (converter == null)
        {
            return this.value;
        }
        else
        {
            return this.converter.convert(getValue());
        }
    }

    public void validate()
    {
        // make sure the key is not null
        if (getKey() == null)
        {
            throw new NullKeyException(this);
        }

        // make sure we have value and that it can be converted
        if (getValue() == null)
        {
            throw new ConfigurationException(this, "No value has been set for '"+getKey()+'\'');
        }
        else if (this.converter != null)
        {
            try
            {
                getConvertedValue();
            }
            catch (Throwable t)
            {
                throw new ConfigurationException(this, t);
            }
        }
    }


    protected DataConverter getDataConverter(String type)
    {
        DataConverter dc;
        if (type.startsWith("list."))
        {
            // use a list converter and drop to subtype
            dc = new ListConverter();
            type = type.substring(5, type.length());
        }
        else if (type.equals("list"))
        {
            // then this is all we do
            return new ListConverter();
        }
        else
        {
            // use datum converter
            dc = new DataConverter();
        }

        if (type.equals("boolean"))
        {
            dc.setTarget(Boolean.class);
            dc.setConverter(new BooleanConverter());
        }
        else if (type.equals("number"))
        {
            if (getValue() != null && getValue().toString().indexOf('.') < 0)
            {
                dc.setConverter(new IntegerConverter());
                dc.setTarget(Integer.class);
            }
            else
            {
                dc.setConverter(new DoubleConverter());
                dc.setTarget(Double.class);
            }
        }
        else if (type.equals("string"))
        {
            dc.setTarget(String.class);
            dc.setConverter(new StringConverter());
        }
        else
        {
            return null;
        }
        return dc;
    }

    @Override
    public int hashCode()
    {
        if (getKey() == null)
        {
            return super.hashCode();
        }
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (getKey() == null || !(obj instanceof Data))
        {
            return super.equals(obj);
        }
        return getKey().equals(((Data)obj).getKey());
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("Data '");
        out.append(key);
        out.append('\'');
        out.append(" -> ");
        out.append(value);
        return out.toString();
    }



    protected static class DataConverter
    {
        private Converter converter;
        private Class target = String.class;

        public void setConverter(Converter converter)
        {
            this.converter = converter;
        }

        public Converter getConverter()
        {
            return this.converter;
        }

        public void setTarget(Class target)
        {
            this.target = target;
        }

        public Class getTarget()
        {
            return this.target;
        }

        public Object convert(Object value)
        {
            if (this.converter == null)
            {
                return value;
            }
            else
            {
                return this.converter.convert(this.target, value);
            }
        }
    }

    protected static class ListConverter extends DataConverter
    {
        public List convert(Object val)
        {
            // we assume it is a string
            String value = (String)val;
            if (value == null || value.trim().length() == 0)
            {
                return null;
            }
            else
            {
                //TODO: make sure this works as expected...
                List<String> list = Arrays.asList(value.split(","));
                if (getConverter() == null || getTarget() == String.class)
                {
                    return list;
                }
                else
                {
                    List convertedList = new ArrayList();
                    for (String item : list)
                    {
                        convertedList.add(super.convert(item));
                    }
                    return convertedList;
                }
            }
        }
    }

}

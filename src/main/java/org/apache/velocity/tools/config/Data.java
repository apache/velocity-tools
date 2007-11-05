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
import java.util.Locale;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.ConversionUtils;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Data.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Data implements Comparable<Data>
{
    public static final String DEFAULT_TYPE = "auto";

    private String key;
    private String type;
    private Object value;
    private boolean isList;
    private Class target;
    private Converter converter;

    public Data()
    {
        setType(DEFAULT_TYPE);
    }

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
        // save the set type
        this.type = type;

        //TODO: check if this is a list type
        //      if auto, try to sniff
        if (type.startsWith("list."))
        {
            // use a list converter and drop to subtype
            this.isList = true;
            this.target = List.class;
            type = type.substring(5, type.length());
        }
        else if (type.equals("list"))
        {
            this.isList = true;
            this.target = List.class;
        }
        else
        {
            this.isList = false;
        }

        //TODO: support an "auto" type that tries to automatically
        //      recognize common list, boolean, field, and number formats
        if (type.equals("auto"))
        {
            this.target = Object.class;
            this.converter = new AutoConverter();
        }
        else if (type.equals("boolean"))
        {
            this.target = Boolean.class;
            this.converter = new BooleanConverter();
        }
        else if (type.equals("number"))
        {
            this.target = Number.class;
            this.converter = new NumberConverter();
        }
        else if (type.equals("string"))
        {
            this.target = String.class;
            this.converter = new StringConverter();
        }
        else if (type.equals("field"))
        {
            this.target = Object.class;
            this.converter = new FieldConverter();
        }
    }

    public void setTargetClass(Class clazz)
    {
        this.target = clazz;
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
        this.converter = converter;
    }

    public String getKey()
    {
        return this.key;
    }

    public String getType()
    {
        return this.type;
    }

    public Object getValue()
    {
        return this.value;
    }

    public Class getTargetClass()
    {
        return this.target;
    }

    public Converter getConverter()
    {
        return this.converter;
    }

    public Object getConvertedValue()
    {
        return convert(this.value);
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
                if (getConvertedValue() == null && getValue() != null)
                {
                    throw new ConfigurationException(this, "Conversion of "+getValue()+" for '"+getKey()+"' failed and returned null");
                }
            }
            catch (Throwable t)
            {
                throw new ConfigurationException(this, t);
            }
        }
    }

    public int compareTo(Data datum)
    {
        if (getKey() == null && datum.getKey() == null)
        {
            return 0;
        }
        else if (getKey() == null)
        {
            return -1;
        }
        else if (datum.getKey() == null)
        {
            return 1;
        }
        else
        {
            return getKey().compareTo(datum.getKey());
        }
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
        out.append(" -");
        out.append(type);
        out.append("-> ");
        out.append(value);
        return out.toString();
    }



    protected Object convert(Object value)
    {
        if (this.isList)
        {
            return convertList(value);
        }
        else if (this.converter == null)
        {
            return value;
        }
        else
        {
            return convertValue(value);
        }
    }

    private Object convertValue(Object value)
    {
        return this.converter.convert(this.target, value);
    }

    private List convertList(Object val)
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
            if (this.converter == null || this.target.equals(String.class))
            {
                return list;
            }
            else
            {
                List convertedList = new ArrayList();
                for (String item : list)
                {
                    convertedList.add(convertValue(item));
                }
                return convertedList;
            }
        }
    }

    protected static class FieldConverter implements Converter
    {
        public Object convert(Class type, Object value)
        {
            String fieldpath = (String)value;
            try
            {
                return ClassUtils.getFieldValue(fieldpath);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Could not retrieve value for field at "+fieldpath, e);
            }
        }
    }

    protected static class AutoConverter implements Converter
    {
        public Object convert(Class type, Object obj)
        {
            // only bother with strings for now
            if (obj instanceof String)
            {
                try
                {
                    return convert((String)obj);
                    
                }
                catch (Exception e)
                {
                    return obj;
                }
            }
            return obj;
        }

        public Object convert(String value)
        {
            // check if this looks like a typical boolean type
            if (value.matches("true|false|yes|no|y|n|on|off"))
            {
                return new BooleanConverter().convert(Boolean.class, value);
            }
            // check if this looks like a typical number
            else if (value.matches("-?[0-9]+(\\.[0-9]+)?"))
            {
                return new NumberConverter().convert(Number.class, value);
            }
            // check if this looks like a typical field
            else if (value.matches("(\\w+\\.)+\\w+"))
            {
                return new FieldConverter().convert(Object.class, value);
            }
            return value;
        }
    }

    protected static class NumberConverter implements Converter
    {

        /* use english locale by default for numbers */
        private static Locale configLocale = new Locale("en");
        
        public Object convert(Class type, Object obj)
        {
            Number num = ConversionUtils.toNumber(obj);
            if (num == null)
            {
                String value = String.valueOf(obj);
                num = ConversionUtils.toNumber(value, "default", configLocale);
                if (num == null)
                {
                    throw new IllegalArgumentException("Could not convert "+obj+" to a number");
                }
                // now, let's return integers for integer values
                else if (value.indexOf('.') < 0)
                {
                    // unless, of course, we need a long
                    if (num.doubleValue() > Integer.MAX_VALUE ||
                        num.doubleValue() < Integer.MIN_VALUE)
                    {
                        num = Long.valueOf(num.longValue());
                    }
                    else
                    {
                        num = Integer.valueOf(num.intValue());
                    }
                }
            }
            return num;
        }
    }

}

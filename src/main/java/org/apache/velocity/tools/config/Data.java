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
 * <p>This class represents configured data.  If added to a
 * {@link FactoryConfiguration}, its values will be made
 * available in the application-scoped toolboxes
 * produced by any ToolboxFactory configured using
 * that configuration.</p>
 * <p>This class also implements all the functionality of
 * {@link Property}s, which may added to <strong>any</strong>
 * {@link Configuration} subclass, including
 * {@link ToolConfiguration}, {@link ToolboxConfiguration},
 * and {@link FactoryConfiguration}.  In other words,
 * anything you can do in a {@link Data} configuration, you
 * can do with a {@link Property}.</p>
 * <p>Some features supported here are:
 * <ul>
 * <li>built in {@link Type}s for strings, booleans, numbers, fields 
 *     and lists thereof</li>
 * <li>auto-conversion of numbers, booleans and fields in data
 *     with no explicit type</li>
 * <li>support for any Commons-BeanUtils {@link Converter} implementation</li>
 * </ul>
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: Data.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Data implements Comparable<Data>
{
    protected static final Type DEFAULT_TYPE = Type.AUTO;

    private String key;
    private String typeValue;
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

    protected void setType(Type type)
    {
        this.isList = type.isList();

        // make sure we don't override a custom target or converter
        if (!type.isCustom())
        {
            this.typeValue = type.value();
            this.target = type.getTarget();
            this.converter = type.getConverter();
        }
        else if (type.isList())
        {
            // go ahead and set the target and type value for custom lists
            this.typeValue = type.value();
            this.target = type.getTarget();
        }
    }

    public void setType(String t)
    {
        // save the set type value (good for error feedback and whatnot)
        this.typeValue = t;
        // and try to convert it to a Type
        Type type = Type.get(this.typeValue);
        if (type != null)
        {
            setType(type);
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
        return this.typeValue;
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
        out.append(this.typeValue);
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



    // ------------- Subclasses -----------------

    /**
     * Delineates the standard, known types and their
     * associated target classes ({@link #setTargetClass} and
     * converters ({@link #setConverter}).
     */
    protected static enum Type
    {
        AUTO(Object.class, new AutoConverter()),
        BOOLEAN(Boolean.class, new BooleanConverter()),
        CUSTOM(null, null),
        FIELD(Object.class, new FieldConverter()),
        NUMBER(Number.class, new NumberConverter()),
        STRING(String.class, new StringConverter()),
        LIST(List.class, null),
        LIST_AUTO(List.class, AUTO.getConverter()),
        LIST_BOOLEAN(List.class, BOOLEAN.getConverter()),
        LIST_FIELD(List.class, FIELD.getConverter()),
        LIST_NUMBER(List.class, NUMBER.getConverter()),
        LIST_STRING(List.class, STRING.getConverter());

        private Class target;
        private Converter converter;

        Type(Class t, Converter c)
        {
            this.target = t;
            this.converter = c;
        }

        public boolean isCustom()
        {
            // custom ones require the user to provide the converter
            return (this.converter == null);
        }

        public boolean isList()
        {
            // all list types return lists
            return (this.target == List.class);
        }

        public Class getTarget()
        {
            return this.target;
        }

        public Converter getConverter()
        {
            return this.converter;
        }

        public String value()
        {
            // make 'LIST_AUTO' into 'list.auto'
            return name().replace('_','.').toLowerCase();
        }

        public static Type get(String type)
        {
            if (type == null || type.length() == 0)
            {
                return CUSTOM;
            }
            // make 'list.auto' eq 'LIST_AUTO'
            return valueOf(type.replace('.','_').toUpperCase());
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
                return Type.BOOLEAN.getConverter().convert(Boolean.class, value);
            }
            // check if this looks like a typical number
            else if (value.matches("-?[0-9]+(\\.[0-9]+)?"))
            {
                return Type.NUMBER.getConverter().convert(Number.class, value);
            }
            // check if this looks like a typical field
            else if (value.matches("(\\w+\\.)+\\w+"))
            {
                return Type.FIELD.getConverter().convert(Object.class, value);
            }
            return value;
        }
    }

    protected static class NumberConverter implements Converter
    {
        public Object convert(Class type, Object obj)
        {
            Number num = ConversionUtils.toNumber(obj,"default",Locale.US);
            if (num == null)
            {
                throw new IllegalArgumentException("Could not convert "+obj+" to a number");
            }
            // now, let's return integers for integer values
            else if (obj.toString().indexOf('.') < 0)
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
            return num;
        }
    }

}

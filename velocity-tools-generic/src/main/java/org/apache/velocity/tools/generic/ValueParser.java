package org.apache.velocity.tools.generic;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.config.SkipSetters;

/**
 * <p>Utility class for easy parsing of String values held in a Map.</p>
 *
 * <p>This comes in very handy when parsing parameters.</p>
 *
 * <p>When subkeys are allowed, getValue("foo") will also search for all keys
 * of the form "foo.bar" and return a ValueParser of the type "bar" -&gt; value for all found values.</p>
 *
 * TODO: someone doing java configuration ought to be able to put a source Map
 *       in the tool properties, allowing this to be used like other tools
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 * @since VelocityTools 1.2
 */

@DefaultKey("parser")
@InvalidScope(Scope.SESSION) /* session scope forbidden: Object may not be Serializable */
@SkipSetters
public class ValueParser extends FormatConfig implements Map<String,Object>
{
    public static final String STRINGS_DELIMITER_FORMAT_KEY = "stringsDelimiter";
    public static final String DEFAULT_STRINGS_DELIMITER = ",";
    private String stringsDelimiter = DEFAULT_STRINGS_DELIMITER;

    private Map<String,Object> source = null;

    private boolean allowSubkeys = true;

    /* when using subkeys, cache at least the presence of any subkey,
    so that the rendering of templates not using subkeys will only
    look once for subkeys
     */
    private Boolean hasSubkeys = null;

    /* whether the wrapped map should be read-only or not */
    private boolean readOnly = true;

    /**
     * The key used for specifying whether to support subkeys
     */
    public static final String ALLOWSUBKEYS_KEY = "allowSubkeys";

    /**
     * The key used for specifying whether to be read-only
     */
    public static final String READONLY_KEY = "readOnly";

    public ValueParser()
    {
    }

    public ValueParser(Map<String,Object> source)
    {
        setSource(source);
    }

    protected void setSource(Map<String,Object> source)
    {
        this.source = source;
    }

    protected Map<String,Object> getSource(boolean create)
    {
        // If this method has not been overrided, make sure source is not null
        if (source == null && create)
        {
            source = new HashMap<String, Object>();
        }
        return this.source;        
    }

    protected Map<String,Object> getSource()
    {
        return getSource(true);
    }

    /**
     * Are subkeys allowed ?
     * @return yes/no
     */
    protected boolean getAllowSubkeys()
    {
        return allowSubkeys;
    }

    /**
     * allow or disallow subkeys
     * @param allow flag value
     */
    protected void setAllowSubkeys(boolean allow)
    {
        allowSubkeys = allow;
    }

    /**
     * Is the Map read-only?
     * @return yes/no
     */
    protected boolean getReadOnly()
    {
        return readOnly;
    }

    /**
     * Set or unset read-only behaviour
     * @param ro flag value
     */
    protected void setReadOnly(boolean ro)
    {
        readOnly = ro;
    }

    /**
     * Sets the delimiter used for separating values in a single String value.
     * The default string delimiter is a comma.
     *
     * @param stringsDelimiter strings delimiter
     * @see #getValues(String)
     */
    protected final void setStringsDelimiter(String stringsDelimiter)
    {
        this.stringsDelimiter = stringsDelimiter;
    }

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     * @param values configuration values
     */
    @Override
    protected void configure(ValueParser values)
    {
        super.configure(values);

        String delimiter = values.getString(STRINGS_DELIMITER_FORMAT_KEY);
        if (delimiter != null)
        {
            setStringsDelimiter(delimiter);
        }

        Boolean allow = values.getBoolean(ALLOWSUBKEYS_KEY);
        if(allow != null)
        {
            setAllowSubkeys(allow);
        }

        Boolean ro = values.getBoolean(READONLY_KEY);
        if(ro != null)
        {
            setReadOnly(ro);
        }
    }

    // ----------------- public parsing methods --------------------------

    /**
     * Convenience method for checking whether a certain parameter exists.
     *
     * @param key the parameter's key
     * @return <code>true</code> if a parameter exists for the specified
     *         key; otherwise, returns <code>false</code>.
     */
    public boolean exists(String key)
    {
        return (getValue(key) != null);
    }

    /**
     * Convenience method for use in Velocity templates.
     * This allows for easy "dot" access to parameters.
     *
     * e.g. $params.foo instead of $params.getString('foo')
     *
     * @param key the parameter's key
     * @return parameter matching the specified key or
     *         <code>null</code> if there is no matching
     *         parameter
     */
    public Object get(String key)
    {
        Object value = getValue(key);
        if (value == null && getSource() != null && getAllowSubkeys())
        {
            value = getSubkey(key);
        }
        return value;
    }

    /**
     * Returns the value mapped to the specified key
     * in the {@link Map} returned by {@link #getSource()}. If there is
     * no source, then this will always return {@code null}.
     * @param key property key
     * @return property value, or null
     */
    public Object getValue(String key)
    {
        if (getSource() == null)
        {
            return null;
        }
        return getSource().get(key);
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate value
     * @return parameter matching the specified key or the
     *         specified alternate Object if there is no matching
     *         parameter
     */
    public Object getValue(String key, Object alternate)
    {
        Object value = getValue(key);
        if (value == null)
        {
            return alternate;
        }
        return value;
    }

    protected String[] parseStringList(String value)
    {
        String[] values;
        if (stringsDelimiter.length() == 0 || value.indexOf(stringsDelimiter) < 0)
        {
            values = new String[] { value };
        }
        else
        {
            values = value.split(stringsDelimiter);
        }

        return values;
    }

    /**
     * <p>Returns an array of values. If the internal value is a string, it is split using the configured delimitor
     * (',' by default).</p>
     * <p>If the internal value is not an array or is a string without any delimiter, a singletin array is returned.</p>
     * @param key the desired parameter's key
     * @return array of values, or null of the key has not been found.
     *         specified alternate Object if there is no matching
     *         parameter
     */
    public Object[] getValues(String key)
    {
        Object value = getValue(key);
        if (value == null)
        {
            return null;
        }
        if (value instanceof String)
        {
            return parseStringList((String)value);
        }
        if (value instanceof Object[])
        {
            return (Object[])value;
        }
        return new Object[] { value };
    }

    /**
     * @param key the parameter's key
     * @return parameter matching the specified key or
     *         <code>null</code> if there is no matching
     *         parameter
     */
    public String getString(String key)
    {
        return ConversionUtils.toString(getValue(key));
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate value
     * @return parameter matching the specified key or the
     *         specified alternate String if there is no matching
     *         parameter
     */
    public String getString(String key, String alternate)
    {
        String s = getString(key);
        return (s != null) ? s : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Boolean} object for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Boolean getBoolean(String key)
    {
        return ConversionUtils.toBoolean(getValue(key));
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate boolean value
     * @return boolean value for the specified key or the
     *         alternate boolean is no value is found
     */
    public boolean getBoolean(String key, boolean alternate)
    {
        Boolean bool = getBoolean(key);
        return (bool != null) ? bool.booleanValue() : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @param alternate the alternate {@link Boolean}
     * @return a {@link Boolean} for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Boolean getBoolean(String key, Boolean alternate)
    {
        Boolean bool = getBoolean(key);
        return (bool != null) ? bool : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Integer} for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Integer getInteger(String key)
    {
        Object value = getValue(key);
        if (value == null)
        {
            return null;
        }
        Number number = ConversionUtils.toNumber(value, getFormat(), getLocale());
        return number == null ? null : number.intValue();
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate Integer
     * @return an Integer for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Integer getInteger(String key, Integer alternate)
    {
        Integer num = getInteger(key);
        if (num == null)
        {
            return alternate;
        }
        return num;
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Double} for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Double getDouble(String key)
    {
        Object value = getValue(key);
        if (value == null)
        {
            return null;
        }
        Number number = ConversionUtils.toNumber(value, getFormat(), getLocale());
        return number == null ? null : number.doubleValue();
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate Double
     * @return an Double for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Double getDouble(String key, Double alternate)
    {
        Double num = getDouble(key);
        if (num == null)
        {
            return alternate;
        }
        return num;
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Number} for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Number getNumber(String key)
    {
        return ConversionUtils.toNumber(getValue(key), getFormat(), getLocale());
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Locale} for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Locale getLocale(String key)
    {
        return toLocale(getValue(key));
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate Number
     * @return a Number for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Number getNumber(String key, Number alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate int value
     * @return the int value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public int getInt(String key, int alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n.intValue() : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate double value
     * @return the double value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public double getDouble(String key, double alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n.doubleValue() : alternate;
    }

    /**
     * @param key the desired parameter's key
     * @param alternate The alternate Locale
     * @return a Locale for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Locale getLocale(String key, Locale alternate)
    {
        Locale l = getLocale(key);
        return (l != null) ? l : alternate;
    }


    /**
     * @param key the key for the desired parameter
     * @return an array of String objects containing all of the values
     *         associated with the given key, or <code>null</code>
     *         if the no values are associated with the given key
     */
    public String[] getStrings(String key)
    {
        Object[] array = getValues(key);
        if (array == null || String.class.isAssignableFrom(array.getClass().getComponentType()))
        {
            return (String[])array;
        }
        String[] ret = new String[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toString(array[i]);
        }
        return ret;
    }


    /**
     * @param key the key for the desired parameter
     * @return an array of Boolean objects associated with the given key.
     */
    public Boolean[] getBooleans(String key)
    {
        Object[] array = getValues(key);
        if (array == null || Boolean.class.isAssignableFrom(array.getClass().getComponentType()))
        {
            return (Boolean[])array;
        }
        Boolean[] ret = new Boolean[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toBoolean(array[i]);
        }
        return ret;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Number objects associated with the given key,
     *         or <code>null</code> if Numbers are not associated with it.
     */
    public Number[] getNumbers(String key)
    {
        Object[] array = getValues(key);
        if (array == null || Number.class.isAssignableFrom(array.getClass().getComponentType()))
        {
            return (Number[])array;
        }
        Number[] ret = new Number[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toNumber(array[i], getFormat(), getLocale());
        }
        return ret;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of int values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public int[] getInts(String key)
    {
        Object[] array = getValues(key);
        if (array == null)
        {
            return null;
        }
        int[] ret = new int[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toNumber(array[i], getFormat(), getLocale()).intValue();
        }
        return ret;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of double values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public double[] getDoubles(String key)
    {
        Object[] array = getValues(key);
        if (array == null)
        {
            return null;
        }
        double[] ret = new double[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toNumber(array[i], getFormat(), getLocale()).doubleValue();
        }
        return ret;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Locale objects associated with the given key,
     *         or <code>null</code> if Locales are not associated with it.
     */
    public Locale[] getLocales(String key)
    {
        Object[] array = getValues(key);
        if (array == null || Locale.class.isAssignableFrom(array.getClass().getComponentType()))
        {
            return (Locale[])array;
        }
        Locale[] ret = new Locale[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            ret[i] = ConversionUtils.toLocale(String.valueOf(array[i]));
        }
        return ret;
    }

    /**
     * Determines whether there are subkeys available in the source map.
     * @return <code>true</code> if there are subkeys (key names containing a dot)
     */
    public boolean hasSubkeys()
    {
        if (getSource() == null || !getAllowSubkeys())
        {
            return false;
        }

        if (hasSubkeys == null)
        {
            for (String key : getSource().keySet())
            {
                int dot = key.indexOf('.');
                if (dot > 0 && dot < key.length())
                {
                    hasSubkeys = Boolean.TRUE;
                    break;
                }
            }
            if (hasSubkeys == null)
            {
                hasSubkeys = Boolean.FALSE;
            }
        }
        return hasSubkeys;
    }

    /**
     * returns the set of all possible first-level subkeys, including complete keys without dots (or returns keySet() if allowSubKeys is false)
     * @return the set of all possible first-level subkeys
     */
    public Set<String> getSubkeys()
    {
        Set<String> keys = keySet();
        if (getSource() == null || !getAllowSubkeys())
        {
            return keys;
        }
        else
        {
            Set<String> result = new TreeSet<String>();
            for (String key: keys)
            {
                int dot = key.indexOf('.');
                if (dot > 0 && dot < key.length())
                {
                    result.add(key.substring(0, dot));
                }
            }
            return result;
        }
    }
    
    /**
     * subkey getter that returns a map subkey#2 -&gt; value
     * for every "subkey.subkey2" found entry
     *
     * @param subkey subkey to search for
     * @return the map of found values
     */
    protected ValueParser getSubkey(String subkey)
    {
        if (!hasSubkeys() || subkey == null || subkey.length() == 0)
        {
            return null;
        }

        Map<String,Object> values = null;
        subkey = subkey.concat(".");
        for (Map.Entry<String,Object> entry : getSource().entrySet())
        {
            if (entry.getKey().startsWith(subkey) &&
                entry.getKey().length() > subkey.length())
            {
                if (values == null)
                {
                    values = new HashMap<String, Object>();
                }
                values.put(entry.getKey().substring(subkey.length()),entry.getValue());
            }
        }
        if (values == null)
        {
            return null;
        }
        else
        {
            ValueParser ret = new ValueParser(values);
            /* honnor readOnly option on submaps */
            ret.setReadOnly(getReadOnly());
            return ret;
        }
    }

    public int size()
    {
        return getSource() == null ? 0 : getSource().size();
    }

    public boolean isEmpty()
    {
        return getSource() == null || getSource().isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return getSource() == null ? false : getSource().containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return getSource() == null ? false : getSource().containsValue(value);
    }

    public Object get(Object key)
    {
        return get(String.valueOf(key));
    }

    public Object put(String key, Object value)
    {
        if(readOnly)
        {
            throw new UnsupportedOperationException("Cannot put("+key+","+value+"); "+getClass().getName()+" is read-only");
        }
        if(hasSubkeys != null && hasSubkeys.equals(Boolean.FALSE) && key.indexOf('.') != -1)
        {
            hasSubkeys = Boolean.TRUE;
        }
        return getSource().put(key,value); // TODO this tool should be made thread-safe (the request-scoped ParameterTool doesn't need it, but other uses could...)
    }

    public Object remove(Object key)
    {
        if(readOnly)
        {
            throw new UnsupportedOperationException("Cannot remove("+key+"); "+getClass().getName()+" is read-only");
        }
        if(hasSubkeys != null && hasSubkeys.equals(Boolean.TRUE) && ((String)key).indexOf('.') != -1)
        {
            hasSubkeys = null;
        }
        return getSource().remove(key);
    }

    public void putAll(Map<? extends String,? extends Object> m) {
        if(readOnly)
        {
            throw new UnsupportedOperationException("Cannot putAll("+m+"); "+getClass().getName()+" is read-only");
        }
        hasSubkeys = null;
        getSource().putAll(m);
    }

    public void clear() {
        if(readOnly)
        {
            throw new UnsupportedOperationException("Cannot clear(); "+getClass().getName()+" is read-only");
        }
        hasSubkeys = Boolean.FALSE;
        getSource().clear();
    }

    public Set<String> keySet() {
        return getSource() == null ? null : getSource().keySet();
    }

    public Collection values() {
        return getSource() == null ? null : getSource().values();
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        return getSource().entrySet();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean empty = true;
        for(Map.Entry<String,Object> entry:entrySet())
        {
            if(!empty)
            {
                builder.append(", ");
            }
            empty = false;
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(String.valueOf(entry.getValue()));
        }
        builder.append('}');
        return builder.toString();
    }
}

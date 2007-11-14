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

import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;

import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>Utility class for easy parsing of String values held in a Map.</p>
 *
 * <p>This comes in very handy when parsing parameters.</p>
 *
 * <p>When subkeys are allowed, getValue("foo") will also search for all keys
 * of the form "foo.bar" and return a ValueParser of the type "bar" -> value for all found values.</p>
 *
 * TODO: someone doing java configuration ought to be able to put a source Map
 *       in the tool properties, allowing this to be used like other tools
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 * @since VelocityTools 1.2
 */
@DefaultKey("parser")
public class ValueParser extends ConversionTool implements Map<String,Object>
{
    private Map<String,Object> source = null;

    private boolean allowSubkeys = true; /* default to whatever, should be overridden by depreprecatedMode default value anyway */

    /* when using subkeys, cache at least the presence of any subkey,
    so that the rendering of templates not using subkeys will only
    look once for subkeys
     */
    private Boolean hasSubkeys = null;

    /**
     * The key used for specifying a whether to support subkeys
     */
    public static final String ALLOWSUBKEYS_KEY = "allowSubkeys";


    public ValueParser() {}

    public ValueParser(Map<String,Object> source)
    {
        setSource(source);
    }

    protected void setSource(Map<String,Object> source)
    {
        this.source = source;
    }

    protected Map<String,Object> getSource()
    {
        return this.source;
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
     * @param allow
     */
    protected void setAllowSubkeys(boolean allow)
    {
        allowSubkeys = allow;
    }

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);

        // if we're supporting 1.x behavior
        Boolean depMode = values.getBoolean("deprecationSupportMode");
        if (depMode != null && depMode.booleanValue())
        {
            // then don't allow subkeys
            setAllowSubkeys(false);
        }

        // except if explicitely asked for
        Boolean allow = values.getBoolean(ALLOWSUBKEYS_KEY);
        if(allow != null)
        {
            setAllowSubkeys(allow);
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
        return getValue(key);
    }

    /**
     * Returns the value mapped to the specified key
     * in the {@link Map} returned by {@link #getSource()}. If there is
     * no source, then this will always return {@code null}.
     */
    public Object getValue(String key)
    {
        if (getSource() == null)
        {
            return null;
        }
        Object value = getSource().get(key);
        if (value == null && getAllowSubkeys()) {
            value = getSubkey(key);
        }
        return value;
    }

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
        return toString(getValue(key));
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
        return toBoolean(getValue(key));
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
        return toInteger(getValue(key));
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
        return toDouble(getValue(key));
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
        return toNumber(getValue(key));
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
        return toStrings(getValues(key));
    }


    /**
     * @param key the key for the desired parameter
     * @return an array of Boolean objects associated with the given key.
     */
    public Boolean[] getBooleans(String key)
    {
        return toBooleans(getValues(key));
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Number objects associated with the given key,
     *         or <code>null</code> if Numbers are not associated with it.
     */
    public Number[] getNumbers(String key)
    {
        return toNumbers(getValues(key));
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of int values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public int[] getInts(String key)
    {
        return toInts(getValues(key));
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of double values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public double[] getDoubles(String key)
    {
        return toDoubles(getValues(key));
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Locale objects associated with the given key,
     *         or <code>null</code> if Locales are not associated with it.
     */
    public Locale[] getLocales(String key)
    {
        return toLocales(getValues(key));
    }

    /**
     * Determines whether there are subkeys available in the source map.
     */
    public boolean hasSubkeys()
    {
        if (getSource() == null)
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
     * subkey getter that returns a map <subkey#2> -> value
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
                if(values == null)
                {
                    values = new HashMap<String,Object>();
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
            return new ValueParser(values);
        }
    }

    public int size() {
        return getSource().size();
    }

    public boolean isEmpty() {
        return getSource().isEmpty();
    }

    public boolean containsKey(Object key) {
        return getSource().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return getSource().containsValue(value);
    }

    public Object get(Object key) {
        return get(String.valueOf(key));
    }

    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("ValueParser is read-only");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("ValueParser is read-only");
    }

    public void putAll(Map<? extends String,? extends Object> m) {
        throw new UnsupportedOperationException("ValueParser is read-only");
    }

    public void clear() {
        throw new UnsupportedOperationException("ValueParser is read-only");
    }

    public Set<String> keySet() {
        return getSource().keySet();
    }

    public Collection values() {
        return getSource().values();
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        return getSource().entrySet();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        for(Map.Entry<String,Object> entry:entrySet())
        {
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(String.valueOf(entry.getValue()));
            builder.append(' ');
        }
        builder.append('}');
        return builder.toString();
    }
}

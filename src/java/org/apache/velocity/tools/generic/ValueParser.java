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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * <p>Utility class for easy parsing of String values held in a Map.</p>
 * <p><pre>
 * Template example(s):
 *   $parser.foo                ->  bar
 *   $parser.getNumber('baz')   ->  12.6
 *   $parser.getInt('baz')      ->  12
 *   $parser.getNumbers('baz')  ->  [12.6]
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;parser&lt;/key&gt;
 *   &lt;class&gt;org.apache.velocity.generic.ValueParser&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This comes in very handy when parsing parameters.</p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 * @since VelocityTools 1.2
 */
public class ValueParser
{
    private Map source = null;
    private String delimiter = ",";

    public ValueParser() {}

    public ValueParser(Map source)
    {
        setSource(source);
    }

    protected void setSource(Map source)
    {
        this.source = source;
    }

    protected Map getSource()
    {
        if (source == null)
        {
            throw new NullPointerException("You must set a Map source for values to be parsed.");
        }
        return this.source;
    }

    /**
     * Sets the delimiter used for separating values in a single String value.
     * The default delimiter is a comma.
     *
     * @since VelocityTools 1.3
     * @see #parseStringList
     */
    protected final void setStringsDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    /**
     * Returns the delimiter used for separating values in a single String value.
     * The default delimiter is a comma.
     *
     * @since VelocityTools 1.3
     * @see #parseStringList
     */
    protected final String getStringsDelimiter()
    {
        return this.delimiter;
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
        return (getString(key) != null);
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
        return getString(key);
    }

    /**
     * @param key the parameter's key
     * @return parameter matching the specified key or
     *         <code>null</code> if there is no matching
     *         parameter
     */
    public String getString(String key)
    {
        Object value = getSource().get(key);
        if (value == null)
        {
            return null;
        }

        if (value instanceof Collection)
        {
            Collection values = (Collection)value;
            if (!values.isEmpty())
            {
                // take the next available value
                value = values.iterator().next();
            }
        }
        else if (value.getClass().isArray())
        {
            if (Array.getLength(value) > 0)
            {
                // take the first value
                value = Array.get(value, 0);
            }
        }
        return String.valueOf(value);
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
        String s = getString(key);
        return (s != null) ? parseBoolean(s) : null;
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
        Number num = getNumber(key);
        if (num == null || num instanceof Integer)
        {
            return (Integer)num;
        }
        return new Integer(num.intValue());
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
        Number num = getNumber(key);
        if (num == null || num instanceof Double)
        {
            return (Double)num;
        }
        return new Double(num.intValue());
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
        String s = getString(key);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        try
        {
            return parseNumber(s);
        }
        catch (Exception e)
        {
            //there is no Number with that key
            return null;
        }
    }

    /**
     * @param key the desired parameter's key
     * @return a {@link Locale} for the specified key or
     *         <code>null</code> if no matching parameter is found
     */
    public Locale getLocale(String key)
    {
        String s = getString(key);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        return parseLocale(s);
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
        Object value = getSource().get(key);
        if (value == null)
        {
            return null;
        }

        String[] strings = null;
        if (value instanceof Collection)
        {
            Collection values = (Collection)value;
            if (!values.isEmpty())
            {
                strings = new String[values.size()];
                int index = 0;
                for (Iterator i = values.iterator(); i.hasNext(); )
                {
                    strings[index++] = String.valueOf(i.next());
                }
            }
        }
        else if (value.getClass().isArray())
        {
            strings = new String[Array.getLength(value)];
            for (int i=0; i < strings.length; i++)
            {
                strings[i] = String.valueOf(Array.get(value, i));
            }
        }
        else
        {
            strings = parseStringList(String.valueOf(value));
        }
        return strings;
    }


    /**
     * @param key the key for the desired parameter
     * @return an array of Boolean objects associated with the given key.
     */
    public Boolean[] getBooleans(String key)
    {
        String[] strings = getStrings(key);
        if (strings == null)
        {
            return null;
        }

        Boolean[] bools = new Boolean[strings.length];
        for (int i=0; i<strings.length; i++)
        {
            if (strings[i] != null && strings[i].length() > 0)
            {
                bools[i] = parseBoolean(strings[i]);
            }
        }
        return bools;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Number objects associated with the given key,
     *         or <code>null</code> if Numbers are not associated with it.
     */
    public Number[] getNumbers(String key)
    {
        String[] strings = getStrings(key);
        if (strings == null)
        {
            return null;
        }

        Number[] nums = new Number[strings.length];
        try
        {
            for (int i=0; i<nums.length; i++)
            {
                if (strings[i] != null && strings[i].length() > 0)
                {
                    nums[i] = parseNumber(strings[i]);
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
        return nums;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of int values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public int[] getInts(String key)
    {
        String[] strings = getStrings(key);
        if (strings == null)
        {
            return null;
        }

        int[] ints = new int[strings.length];
        try
        {
            for (int i=0; i<ints.length; i++)
            {
                if (strings[i] != null && strings[i].length() > 0)
                {
                    ints[i] = parseNumber(strings[i]).intValue();
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
        return ints;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of double values associated with the given key,
     *         or <code>null</code> if numbers are not associated with it.
     */
    public double[] getDoubles(String key)
    {
        String[] strings = getStrings(key);
        if (strings == null)
        {
            return null;
        }

        double[] doubles = new double[strings.length];
        try
        {
            for (int i=0; i<doubles.length; i++)
            {
                if (strings[i] != null && strings[i].length() > 0)
                {
                    doubles[i] = parseNumber(strings[i]).doubleValue();
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
        return doubles;
    }

    /**
     * @param key the key for the desired parameter
     * @return an array of Locale objects associated with the given key,
     *         or <code>null</code> if Locales are not associated with it.
     */
    public Locale[] getLocales(String key)
    {
        String[] strings = getStrings(key);
        if (strings == null)
        {
            return null;
        }

        Locale[] locs = new Locale[strings.length];
        for (int i=0; i<locs.length; i++)
        {
            if (strings[i] != null && strings[i].length() > 0)
            {
                locs[i] = parseLocale(strings[i]);
            }
        }
        return locs;
    }


    // --------------------------- protected methods ------------------

    /**
     * Converts a parameter value into a {@link Number}
     * This is used as the base for all numeric parsing methods. So,
     * sub-classes can override to allow for customized number parsing.
     * (e.g. to handle fractions, compound numbers, etc.)
     *
     * @param value the string to be parsed
     * @return the value as a {@link Number}
     */
    protected Number parseNumber(String value) throws NumberFormatException
    {
        if (value.indexOf('.') >= 0)
        {
            return new Double(value);
        }
        return new Long(value);
    }

    /**
     * Converts a parameter value into a {@link Boolean}
     * Sub-classes can override to allow for customized boolean parsing.
     * (e.g. to handle "Yes/No" or "T/F")
     *
     * @param value the string to be parsed
     * @return the value as a {@link Boolean}
     */
    protected Boolean parseBoolean(String value)
    {
        return Boolean.valueOf(value);
    }

    /**
     * Converts a single String value into an array of Strings by splitting
     * it on the tool's set delimiter.  The default delimiter is a comma.
     *
     * @since VelocityTools 1.3
     */
    protected String[] parseStringList(String value)
    {
        if (value.indexOf(this.delimiter) < 0)
        {
            return new String[] { value };
        }
        return value.split(this.delimiter);
    }

    /**
     * Converts a String value into a Locale.
     *
     * @since VelocityTools 1.3
     */
    protected Locale parseLocale(String value)
    {
        if (value.indexOf("_") < 0)
        {
            return new Locale(value);
        }

        String[] params = value.split("_");
        if (params.length == 2)
        {
            return new Locale(params[0], params[1]);
        }
        else if (params.length == 3)
        {
            return new Locale(params[0], params[1], params[2]);
        }
        else
        {
            // there's only 3 possible params, so this must be invalid
            return null;
        }
    }

}

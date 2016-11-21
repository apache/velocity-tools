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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.tools.config.DefaultKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * <p>CollectionTool allows a user to sort a collection (or array, iterator, etc)
 * on any arbitrary set of properties exposed by the objects contained
 * within the collection, and to generate arrays by splitting strings.
 * </p>
 *
 * <p>The sort tool is specifically designed to use within a #foreach
 * but you may find other uses for it.</p>
 *
 * <p>The sort tool can handle all of the collection types supported by
 * #foreach and the same constraints apply as well as the following.
 * Every object in the collection must support the set of properties
 * selected to sort on. Each property which is to be sorted on must
 * return one of the follow:
 * <ul>
 *   <li>Primitive type: e.g. int, char, long etc</li>
 *   <li>Standard Object: e.g. String, Integer, Long etc</li>
 *   <li>Object which implements the Comparable interface.</li>
 * </ul>
 * </p>
 *
 * <p>During the sort operation all properties are compared by calling
 * compareTo() with the exception of Strings for which
 * compareToIgnoreCase() is called.</p>
 *
 * <p>The sort is performed by calling Collections.sort() after
 * marshalling the collection to sort into an appropriate collection type.
 * The original collection will not be re-ordered; a new list containing
 * the sorted elements will always be returned.</p>
 *
 * <p>The tool is used as follows:
 * <pre>
 * Single Property Sort
 * #foreach($obj in $sorter.sort($objects, "name"))
 *   $obj.name Ordinal= $obj.ordinal
 * #end
 * End
 *
 * Multiple Property Sort
 * #foreach($obj in $sorter.sort($objects, ["name", "ordinal"]))
 *   $obj.name, $obj.ordinal
 * #end
 * End
 * </pre>
 *
 * The sort method takes two parameters a collection and a property name
 * or an array of property names. The property names and corresponding
 * methods must conform to java bean standards since commons-beanutils
 * is used to extract the property values.</p>
 *
 * <p>By default the sort tool sorts ascending, you can override this by
 * adding a sort type suffix to any property name.</p>
 *
 * <p>The supported suffixes are:
 * <pre>
 * For ascending
 * :asc
 * For descending
 * :desc
 *
 * Example
 * #foreach($obj in $sorter.sort($objects, ["name:asc", "ordinal:desc"]))
 *   $obj.name, $obj.ordinal
 * #end
 * </pre><p>
 *
 * <p>This will sort first by Name in ascending order and then by Ordinal
 * in descending order, of course you could have left the :asc off of the
 * 'Name' property as ascending is always the default.</p>
 *
 * <p><pre>
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.SortTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author S. Brett Sutton
 * @author Nathan Bubna
 * @since VelocityTools 3.0
 * @version $Id$
 */
@DefaultKey("collection")
public class CollectionTool extends SafeConfig
{
    public static final String STRINGS_DELIMITER_FORMAT_KEY = "stringsDelimiter";
    public static final String STRINGS_TRIM_KEY = "trimStrings";

    public static final String DEFAULT_STRINGS_DELIMITER = ",";
    public static final boolean DEFAULT_STRINGS_TRIM = true;

    private String stringsDelimiter = DEFAULT_STRINGS_DELIMITER;
    private boolean stringsTrim = DEFAULT_STRINGS_TRIM;

    /**
     * Sets the delimiter used for separating values in a single String value.
     * The default string delimiter is a comma.
     *
     * @see #split(String)
     */
    protected final void setStringsDelimiter(String stringsDelimiter)
    {
        this.stringsDelimiter = stringsDelimiter;
    }

    public final String getStringsDelimiter()
    {
        return this.stringsDelimiter;
    }

    /**
     * Sets whether strings should be trimmed when separated from
     * a delimited string value.
     * The default is true.
     *
     * @see #split(String)
     */
    protected final void setStringsTrim(boolean stringsTrim)
    {
        this.stringsTrim = stringsTrim;
    }

    public final boolean getStringsTrim()
    {
        return this.stringsTrim;
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

        String delimiter = values.getString(STRINGS_DELIMITER_FORMAT_KEY);
        if (delimiter != null)
        {
            setStringsDelimiter(delimiter);
        }

        Boolean trim = values.getBoolean(STRINGS_TRIM_KEY);
        if (trim != null)
        {
            setStringsTrim(trim);
        }
    }

    /**
     * @param value the value to be converted
     * @return an array of String objects containing all of the values
     *         derived from the specified array, Collection, or delimited String
     */
    public String[] split(String value)
    {
        if (value == null)
        {
            return null;
        }
        String[] values;
        if (value.indexOf(this.stringsDelimiter) < 0)
        {
            values = new String[] { value };
        }
        else
        {
            values = value.split(this.stringsDelimiter);
        }
        if (this.stringsTrim)
        {
            for (int i=0,l=values.length; i < l; i++)
            {
                values[i] = values[i].trim();
            }
        }
        return values;
    }

    /**
     * Sorts a Collection using a Comparator. A defensive copy is made
     * of the Collection beforehand, so the original Collection is left
     * untouched.
     *
     * @param c The Collection to sort.
     * @param comparator The comparator to use for sorting.
     * @return A copy of the original Collection,
     *         sorted using the supplied Comparator.
     * @since VelocityTools 2.0.1
     */
    public <T> Collection<T> sort(final Collection<T> c,
                                  final Comparator<T> comparator)
    {
        final ArrayList<T> list = new ArrayList<T>(c);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * Sorts an array using a Comparator. A defensive copy is made
     * of the array beforehand, so the original array is left
     * untouched.
     *
     * @param a The array to sort.
     * @param comparator The comparator to use for sorting.
     * @return A copy of the original array,
     *         sorted using the supplied Comparator.
     * @since VelocityTools 2.0.1
     */
    public <T> T[] sort(final T[] a, final Comparator<T> comparator)
    {
        final T[] copy = a.clone();
        Arrays.sort(copy, comparator);
        return copy;
    }

    /**
     * Sorts a Map's values using a Comparator. A defensive copy is made
     * of the values beforehand, so the original Map is left
     * untouched.
     *
     * @param map The Map whose values should be sorted.
     * @param comparator The comparator to use for sorting.
     * @return A copy of the original Map's values,
     *         sorted using the supplied Comparator.
     * @since VelocityTools 2.0.1
     */
    public <T> Collection<T> sort(final Map<?,T> map,
                                  final Comparator<T> comparator)
    {
        return sort(map.values(), comparator);
    }

    /**
     * Sorts a Collection (or array, or Map's values)
     * using a Comparator. A defensive copy is made
     * of the original beforehand, so the original is left
     * untouched. Unsupported collection objects result in
     * a <code>null</code> return value.
     *
     * @param o The Collection to sort.
     * @param comparator The comparator to use for sorting.
     * @return A copy of the original Collection,
     *         sorted using the supplied Comparator.
     * @since VelocityTools 2.0.1
     */
    public Collection<?> sort(final Object o,
                              final Comparator<?> comparator)
    {
        if (o instanceof Collection)
        {
            return sort((Collection<?>)o, comparator);
        }
        else if (o instanceof Object[])
        {
            return sort((Object[])o, comparator);
        }
        else if (o instanceof Map)
        {
            return sort((Map<?,?>)o, comparator);
        }
        else
        {
            // the object type is not supported
            getLog().error("object type not supported: {}", o == null ? "null" : o.getClass().getName());
            return null;
        }
    }

    public Collection sort(Collection collection)
    {
        return sort(collection, (List)null);
    }

    public Collection sort(Object[] array)
    {
        return sort(array, (List)null);
    }

    public Collection sort(Map map)
    {
        return sort(map, (List)null);
    }

    public Collection sort(Object object)
    {
        if (object instanceof Collection)
        {
            return sort((Collection)object, (List)null);
        }
        else if (object instanceof Object[])
        {
            return sort((Object[])object, (List)null);
        }
        else if (object instanceof Map)
        {
            return sort((Map)object, (List)null);
        }
        // the object type is not supported
        getLog().error("object type not supported: {}", object == null ? "null" : object.getClass().getName());
        return null;
    }

    /**
     * Sorts the collection on a single property.
     *
     * @param object the collection to be sorted.
     * @param property the property to sort on.
     */
    public Collection sort(Object object, String property)
    {
        List properties = new ArrayList(1);
        properties.add(property);

        if (object instanceof Collection)
        {
            return sort((Collection)object, properties);
        }
        else if (object instanceof Object[])
        {
            return sort((Object[])object, properties);
        }
        else if (object instanceof Map)
        {
            return sort((Map)object, properties);
        }
        // the object type is not supported
        getLog().error("object type not supported: {}", object == null ? "null" : object.getClass().getName());
        return null;
    }

    public Collection sort(Collection collection, List properties)
    {
        List list = new ArrayList(collection.size());
        list.addAll(collection);
        return internalSort(list, properties);
    }

    public Collection sort(Map map, List properties)
    {
        return sort(map.values(), properties);
    }

    public Collection sort(Object[] array, List properties)
    {
        return internalSort(Arrays.asList(array), properties);
    }

    protected Collection internalSort(List list, List properties)
    {
        try
        {
            if (properties == null)
            {
                Collections.sort(list);
            } else {
                Collections.sort(list, new PropertiesComparator(properties));
            }
            return list;
        }
        catch (Exception e)
        {
            getLog().error("exception encountered while sorting: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Does all of the comparisons
     */
    public static class PropertiesComparator
        implements Comparator, java.io.Serializable
    {
        private static final int TYPE_ASCENDING = 1;
        private static final int TYPE_DESCENDING = -1;

        public static final String TYPE_ASCENDING_SHORT = "asc";
        public static final String TYPE_DESCENDING_SHORT = "desc";

        List properties;
        int[] sortTypes;

        public PropertiesComparator(List props)
        {
            // copy the list so we can safely drop :asc and :desc suffixes
            this.properties = new ArrayList(props.size());
            this.properties.addAll(props);

            // determine ascending/descending
            sortTypes = new int[properties.size()];

            for (int i = 0; i < properties.size(); i++)
            {
                if (properties.get(i) == null)
                {
                    throw new IllegalArgumentException("Property " + i
                            + "is null, sort properties may not be null.");
                }

                // determine if the property contains a sort type
                // e.g "Name:asc" means sort by property Name ascending
                String prop = properties.get(i).toString();
                int colonIndex = prop.indexOf(':');
                if (colonIndex != -1)
                {
                    String sortType = prop.substring(colonIndex + 1);
                    properties.set(i, prop.substring(0, colonIndex));

                    if (TYPE_ASCENDING_SHORT.equalsIgnoreCase(sortType))
                    {
                        sortTypes[i] = TYPE_ASCENDING;
                    }
                    else if (TYPE_DESCENDING_SHORT.equalsIgnoreCase(sortType))
                    {
                        sortTypes[i] = TYPE_DESCENDING;
                    }
                    else
                    {
                        //FIXME: log this
                        // invalide property sort type. use default instead.
                        sortTypes[i] = TYPE_ASCENDING;
                    }
                }
                else
                {
                    // default sort type is ascending.
                    sortTypes[i] = TYPE_ASCENDING;
                }
            }
        }

        public int compare(Object lhs, Object rhs)
        {
            for (int i = 0; i < properties.size(); i++)
            {
                int comparison = 0;
                String property = (String)properties.get(i);

                // properties must be comparable
                Comparable left = getComparable(lhs, property);
                Comparable right = getComparable(rhs, property);

                if (left == null && right != null)
                {
                    // find out how right feels about left being null
                    comparison = right.compareTo(null);
                    // and reverse that (if it works)
                    comparison *= -1;
                }
                else if (left instanceof String)
                {
                    //TODO: make it optional whether or not case is ignored
                    comparison = ((String)left).compareToIgnoreCase((String)right);
                }
                else if (left != null)
                {
                    comparison = left.compareTo(right);
                }

                // return the first difference we find
                if (comparison != 0)
                {
                    // multiplied by the sort direction, of course
                    return comparison * sortTypes[i];
                }
            }
            return 0;
        }
    }

    /**
     * Safely retrieves the comparable value for the specified property
     * from the specified object. Subclasses that wish to perform more
     * advanced, efficient, or just different property retrieval methods
     * should override this method to do so.
     */
    protected static Comparable getComparable(Object object, String property)
    {
        try
        {
            return (Comparable)PropertyUtils.getProperty(object, property);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Could not retrieve comparable value for '"
                                               + property + "' from " + object + ": " + e);
        }
    }

}

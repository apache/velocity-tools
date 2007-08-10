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
import java.util.Collections;
import java.util.Iterator;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * Provides general utility methods for controlling the display of references.
 * Currently, this class contains methods for "pretty printing" an array or
 * {@link Collection}, methods for truncating the string value of a reference
 * at a configured or specified length, and methods for displaying an alternate
 * value when a specified value is null.  
 *
 * <p><b>Example Use:</b>
 * <pre>
 * tools.xml...
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.DisplayTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 *
 * template...
 *   #set( $list = [1..5] )
 *   $display.list($list)
 *   $display.truncate(10, "This is a long string.")
 *   Not Null: $display.alt("not null", "--")
 *   Null: $display.alt($null, "--")
 *
 * output...
 *   1, 2, 3, 4 and 5
 *   This is...
 *   Not Null: not null
 *   Null: --
 *   
 * </pre></p>
 *
 * @since VelocityTools 2.0
 * @author <a href="sean@somacity.com">Sean Legassick</a>
 * @author <a href="dlr@collab.net">Daniel Rall</a>
 * @author Nathan Bubna
 * @version $Id: DisplayTool.java 463298 2006-10-12 16:10:32Z henning $
 */
@DefaultKey("display")
public class DisplayTool extends AbstractLockConfig
{
    public static final String LIST_DELIM_KEY = "listDelim";
    public static final String LIST_FINAL_DELIM_KEY = "listFinalDelim";
    public static final String TRUNCATE_MAX_LENGTH_KEY = "truncateMaxLength";
    public static final String TRUNCATE_SUFFIX_KEY = "truncateSuffix";
    public static final String DEFAULT_ALTERNATE_KEY = "defaultAlternate";

    private String defaultDelim = ", ";
    private String defaultFinalDelim = " and ";
    private int defaultMaxLength = 30;
    private String defaultSuffix = "...";
    private String defaultAlternate = "null";

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when 
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        String listDelim = values.getString(LIST_DELIM_KEY);
        if (listDelim != null)
        {
            setListDelimiter(listDelim);
        }

        String listFinalDelim = values.getString(LIST_FINAL_DELIM_KEY);
        if (listFinalDelim != null)
        {
            setListFinalDelimiter(listFinalDelim);
        }

        Integer truncateMaxLength = values.getInteger(TRUNCATE_MAX_LENGTH_KEY);
        if (truncateMaxLength != null)
        {
            setTruncateMaxLength(truncateMaxLength);
        }

        String truncateSuffix = values.getString(TRUNCATE_SUFFIX_KEY);
        if (truncateSuffix != null)
        {
            setTruncateSuffix(truncateSuffix);
        }

        String defaultAlternate = values.getString(DEFAULT_ALTERNATE_KEY);
        if (defaultAlternate != null)
        {
            setDefaultAlternate(defaultAlternate);
        }
    }

    protected void setListDelimiter(String delim)
    {
        this.defaultDelim = delim;
    }

    protected void setListFinalDelimiter(String finalDelim)
    {
        this.defaultFinalDelim = finalDelim;
    }

    protected void setTruncateMaxLength(int maxlen)
    {
        this.defaultMaxLength = maxlen;
    }

    protected void setTruncateSuffix(String suffix)
    {
        this.defaultSuffix = suffix;
    }

    protected void setDefaultAlternate(String dflt)
    {
        this.defaultAlternate = dflt;
    }


    /**
     * Formats a collection or array into the form "A, B and C".
     *
     * @param list A collection or array.
     * @return A String.
     */
    public String list(Object list)
    {
        return list(list, this.defaultDelim, this.defaultFinalDelim);
    }

    /**
     * Formats a collection or array into the form
     * "A&lt;delim&gt;B&lt;delim&gt;C".
     *
     * @param list A collection or array.
     * @param delim A String.
     * @return A String.
     */
    public String list(Object list, String delim)
    {
        return list(list, delim, delim);
    }

    /**
     * Formats a collection or array into the form
     * "A&lt;delim&gt;B&lt;finaldelim&gt;C".
     *
     * @param list A collection or array.
     * @param delim A String.
     * @param finaldelim A String.
     * @return A String.
     */
    public String list(Object list, String delim, String finaldelim)
    {
        if (list == null)
        {
            return null;
        }
        if (list instanceof Collection)
        {
            return format((Collection)list, delim, finaldelim);
        }
        Collection items;
        if (list.getClass().isArray())
        {
            int size = Array.getLength(list);
            items = new ArrayList(size);
            for (int i=0; i < size; i++)
            {
                items.add(Array.get(list, i));
            }
        }
        else
        {
            items = Collections.singletonList(list);
        }
        return format(items, delim, finaldelim);
    }

    /**
     * Does the actual formatting of the collection.
     */
    protected String format(Collection list, String delim, String finaldelim)
    {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
System.out.println("formatting "+size+" items");
        Iterator iterator = list.iterator();
        for (int i = 0; i < size; i++)
        {
            sb.append(iterator.next());
            if (i < size - 2)
            {
                sb.append(delim);
            }
            else if (i < size - 1)
            {
                sb.append(finaldelim);
            }
        }
        return sb.toString();
    }

    /**
     * Limits the string value of 'truncateMe' to the configured max length
     * in characters (default is 30 characters).
     * If the string gets curtailed, the configured suffix
     * (default is "...") is used as the ending of the truncated string.
     *
     * @param obj The value to be truncated.
     * @return A String.
     */
    public String truncate(Object truncateMe)
    {
        return truncate(truncateMe, this.defaultMaxLength);
    }

    /**
     * Limits the string value of 'truncateMe' to 'maxLength' characters.
     * If the string gets curtailed, the configured suffix
     * (default is "...") is used as the ending of the truncated string.
     *
     * @param maxLength An int with the maximum length.
     * @param obj The value to be truncated.
     * @return A String.
     */
    public String truncate(Object truncateMe, int maxLength)
    {
        return truncate(truncateMe, maxLength, this.defaultSuffix);
    }

    /**
     * Limits the string value of 'truncateMe' to the configured max length
     * in characters (default is 30 characters).
     * If the string gets curtailed, the specified suffix
     * is used as the ending of the truncated string.
     *
     * @param obj The value to be truncated.
     * @param suffix A String.
     * @return A String.
     */
    public String truncate(Object truncateMe, String suffix)
    {
        return truncate(truncateMe, this.defaultMaxLength, suffix);
    }

    /**
     * Limits the string value of 'truncateMe' to the specified max length
     * in characters. If the string gets curtailed, the specified suffix
     * is used as the ending of the truncated string.
     *
     * @param maxLength An int with the maximum length.
     * @param obj The value to be truncated.
     * @param suffix A String.
     * @return A String.
     */
    public String truncate(Object truncateMe, int maxLength, String suffix)
    {
        if (truncateMe == null)
        {
            return null;
        }

        String string = String.valueOf(truncateMe);
        if (string.length() <= maxLength)
        {
            return string;
        }
        return string.substring(0, maxLength - suffix.length()) + suffix;
    }

    /**
     * Returns a configured default value if specified value is null.
     * @param checkMe
     * @return a configured default value if the specified value is null.
     */
    public Object alt(Object checkMe)
    {
        return alt(checkMe, this.defaultAlternate);
    }

    /**
     * Returns the second argument if first argument specified is null.
     * @param checkMe
     * @param alternate
     * @return the second argument if the first is null.
     */
    public Object alt(Object checkMe, Object alternate)
    {
        if (checkMe == null)
        {
            return alternate;
        }
        return checkMe;
    }

}

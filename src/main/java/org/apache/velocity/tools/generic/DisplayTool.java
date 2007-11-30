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
 * at a configured or specified length, methods for displaying an alternate
 * value when a specified value is null, a method for generating whitespace, and
 * methods for forcing values into "cells" of equal size (via truncation or
 * padding with whitespace).
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
    public static final String TRUNCATE_LENGTH_KEY = "truncateLength";
    public static final String TRUNCATE_SUFFIX_KEY = "truncateSuffix";
    public static final String CELL_LENGTH_KEY = "cellLength";
    public static final String CELL_SUFFIX_KEY = "cellSuffix";
    public static final String DEFAULT_ALTERNATE_KEY = "defaultAlternate";

    private String defaultDelim = ", ";
    private String defaultFinalDelim = " and ";
    private int defaultTruncateLength = 30;
    private String defaultTruncateSuffix = "...";
    private int defaultCellLength = 30;
    private String defaultCellSuffix = "...";
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

        Integer truncateLength = values.getInteger(TRUNCATE_LENGTH_KEY);
        if (truncateLength != null)
        {
            setTruncateLength(truncateLength);
        }

        String truncateSuffix = values.getString(TRUNCATE_SUFFIX_KEY);
        if (truncateSuffix != null)
        {
            setTruncateSuffix(truncateSuffix);
        }

        Integer cellLength = values.getInteger(CELL_LENGTH_KEY);
        if (cellLength != null)
        {
            setCellLength(cellLength);
        }

        String cellSuffix = values.getString(CELL_SUFFIX_KEY);
        if (cellSuffix != null)
        {
            setCellSuffix(cellSuffix);
        }

        String defaultAlternate = values.getString(DEFAULT_ALTERNATE_KEY);
        if (defaultAlternate != null)
        {
            setDefaultAlternate(defaultAlternate);
        }
    }

    public String getListDelimiter()
    {
        return this.defaultDelim;
    }

    protected void setListDelimiter(String delim)
    {
        this.defaultDelim = delim;
    }

    public String getListFinalDelimiter()
    {
        return this.defaultFinalDelim;
    }

    protected void setListFinalDelimiter(String finalDelim)
    {
        this.defaultFinalDelim = finalDelim;
    }

    public int getTruncateLength()
    {
        return this.defaultTruncateLength;
    }

    protected void setTruncateLength(int maxlen)
    {
        this.defaultTruncateLength = maxlen;
    }

    public String getTruncateSuffix()
    {
        return this.defaultTruncateSuffix;
    }

    protected void setTruncateSuffix(String suffix)
    {
        this.defaultTruncateSuffix = suffix;
    }

    public String getCellSuffix()
    {
        return this.defaultCellSuffix;
    }

    protected void setCellSuffix(String suffix)
    {
        this.defaultCellSuffix = suffix;
    }

    public int getCellLength()
    {
        return this.defaultCellLength;
    }

    protected void setCellLength(int maxlen)
    {
        this.defaultCellLength = maxlen;
    }

    public String getDefaultAlternate()
    {
        return this.defaultAlternate;
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
     * @param truncateMe The value to be truncated.
     * @return A String.
     */
    public String truncate(Object truncateMe)
    {
        return truncate(truncateMe, this.defaultTruncateLength);
    }

    /**
     * Limits the string value of 'truncateMe' to 'maxLength' characters.
     * If the string gets curtailed, the configured suffix
     * (default is "...") is used as the ending of the truncated string.
     *
     * @param maxLength An int with the maximum length.
     * @param truncateMe The value to be truncated.
     * @return A String.
     */
    public String truncate(Object truncateMe, int maxLength)
    {
        return truncate(truncateMe, maxLength, this.defaultTruncateSuffix);
    }

    /**
     * Limits the string value of 'truncateMe' to the configured max length
     * in characters (default is 30 characters).
     * If the string gets curtailed, the specified suffix
     * is used as the ending of the truncated string.
     *
     * @param truncateMe The value to be truncated.
     * @param suffix A String.
     * @return A String.
     */
    public String truncate(Object truncateMe, String suffix)
    {
        return truncate(truncateMe, this.defaultTruncateLength, suffix);
    }

    /**
     * Limits the string value of 'truncateMe' to the specified max length
     * in characters. If the string gets curtailed, the specified suffix
     * is used as the ending of the truncated string.
     *
     * @param truncateMe The value to be truncated.
     * @param maxLength An int with the maximum length.
     * @param suffix A String.
     * @return A String.
     */
    public String truncate(Object truncateMe, int maxLength, String suffix)
    {
        if (truncateMe == null || maxLength <= 0)
        {
            return null;
        }

        String string = String.valueOf(truncateMe);
        if (string.length() <= maxLength)
        {
            return string;
        }
        if (suffix == null || maxLength - suffix.length() <= 0)
        {
            // either no need or no room for suffix
            return string.substring(0, maxLength);
        }
        // truncate early and append suffix
        return string.substring(0, maxLength - suffix.length()) + suffix;
    }

    /**
     * Returns a string of spaces of the specified length.
     * @param length the number of spaces to return
     */
    public String space(int length)
    {
        if (length < 0)
        {
            return null;
        }

        StringBuilder space = new StringBuilder();
        for (int i=0; i < length; i++)
        {
            space.append(' ');
        }
        return space.toString();
    }

    /**
     * Truncates or pads the string value of the specified object as necessary
     * to ensure that the returned string's length equals the default cell size.
     * @param obj the value to be put in the 'cell'
     */
    public String cell(Object obj)
    {
        return cell(obj, this.defaultCellLength);
    }

    /**
     * Truncates or pads the string value of the specified object as necessary
     * to ensure that the returned string's length equals the specified cell size.
     * @param obj the value to be put in the 'cell'
     * @param cellsize the size of the cell into which the object must be placed
     */
    public String cell(Object obj, int cellsize)
    {
        return cell(obj, cellsize, this.defaultCellSuffix);
    }

    /**
     * Truncates or pads the string value of the specified object as necessary
     * to ensure that the returned string's length equals the default cell size.
     * If truncation is necessary, the specified suffix will replace the end of
     * the string value to indicate that.
     * @param obj the value to be put in the 'cell'
     * @param suffix the suffix to put at the end of any values that need truncating
     *               to indicate that they've been truncated
     */
    public String cell(Object obj, String suffix)
    {
        return cell(obj, this.defaultCellLength, suffix);
    }

    /**
     * Truncates or pads the string value of the specified object as necessary
     * to ensure that the returned string's length equals the specified cell size.
     * @param obj the value to be put in the 'cell'
     * @param cellsize the size of the cell into which the object must be placed
     * @param suffix the suffix to put at the end of any values that need truncating
     *               to indicate that they've been truncated
     */
    public String cell(Object obj, int cellsize, String suffix)
    {
        if (obj == null || cellsize <= 0)
        {
            return null;
        }

        String value = String.valueOf(obj);
        if (value.length() == cellsize)
        {
            return value;
        }
        else if (value.length() > cellsize)
        {
            return truncate(value, cellsize, suffix);
        }
        else
        {
            return value + space(cellsize - value.length());
        }    
    }

    /**
     * Changes the first character of the string value of the specified object
     * to upper case and returns the resulting string.
     *
     * @param capitalizeMe The value to be capitalized.
     */
    public String capitalize(Object capitalizeMe)
    {
        if (capitalizeMe == null)
        {
            return null;
        }

        String string = String.valueOf(capitalizeMe);
        switch (string.length())
        {
            case 0:
                return string;
            case 1:
                return string.toUpperCase();
            default:
                StringBuilder out = new StringBuilder(string.length());
                out.append(string.substring(0,1).toUpperCase());
                out.append(string.substring(1, string.length()));
                return out.toString();
        }
    }

    /**
     * Changes the first character of the string value of the specified object
     * to lower case and returns the resulting string.
     *
     * @param uncapitalizeMe The value to be uncapitalized.
     */
    public String uncapitalize(Object uncapitalizeMe)
    {
        if (uncapitalizeMe == null)
        {
            return null;
        }

        String string = String.valueOf(uncapitalizeMe);
        switch (string.length())
        {
            case 0:
                return string;
            case 1:
                return string.toLowerCase();
            default:
                StringBuilder out = new StringBuilder(string.length());
                out.append(string.substring(0,1).toLowerCase());
                out.append(string.substring(1, string.length()));
                return out.toString();
        }
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

    /**
     * Returns the {@link Measurements} of the string value of the specified object.
     */
    public Measurements measure(Object measureMe)
    {
        if (measureMe == null)
        {
            return null;
        }
        return new Measurements(String.valueOf(measureMe));
    }


    /**
     * Measures the dimensions of the string given to its constructor.
     * Height is the number of lines in the string.
     * Width is the number of characters in the longest line.
     */
    public static class Measurements
    {
        private int height;
        private int width;

        public Measurements(String s)
        {
            String[] lines = s.split("\n");
            height = lines.length;
            for (String line : lines)
            {
                if (line.length() > width)
                {
                    width = line.length();
                }
            }
        }

        public int getHeight()
        {
            return height;
        }

        public int getWidth()
        {
            return width;
        }

        public String toString()
        {
            StringBuilder out = new StringBuilder(28);
            out.append("{ height: ");
            out.append(height);
            out.append(", width: ");
            out.append(width);
            out.append(" }");
            return out.toString();
        }
    }

}

/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.view.tools;

import javax.servlet.ServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * <p>Utility class for easy parsing of {@link ServletRequest} parameters.</p>
 * <p><pre>
 * Template example(s):
 *   $pp.foo                ->  bar
 *   $pp.getNumber('baz')   ->  12.6
 *   $pp.getInt('baz')      ->  12
 *   $pp.getNumbers('foo')  ->  [12.6]
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;pp&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.view.tools.ParameterParser&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>When used as a view tool, this should only be used in the request scope.
 * This class is, however, quite useful in your application's controller, filter,
 * or action code as well as in templates.</p>
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.8.2.1 $ $Date: 2004/03/12 20:16:28 $
 */
public class ParameterParser implements ViewTool
{

    private ServletRequest request;


    /**
     * Constructs a new instance
     */
    public ParameterParser()
    {}


    /**
     * Constructs a new instance using the specified request.
     *
     * @param request the {@link ServletRequest} to be parsed
     */
    public ParameterParser(ServletRequest request)
    {
        setRequest(request);
    }
    
    
    /**
     * Initializes this instance.
     *
     * @param obj the current ViewContext or ServletRequest
     * @throws IllegalArgumentException if the param is not a 
     *         ViewContext or ServletRequest
     */
    public void init(Object obj)
    {
        if (obj instanceof ViewContext)
        {
            setRequest(((ViewContext)obj).getRequest());
        }
        else if (obj instanceof ServletRequest)
        {
            setRequest((ServletRequest)obj);
        }
        else
        {
            throw new IllegalArgumentException("Was expecting " + ViewContext.class +
                                               " or " + ServletRequest.class);
        }
    }


    /**
     * Sets the current {@link ServletRequest}
     *
     * @param request the {@link ServletRequest} to be parsed
     */
    protected void setRequest(ServletRequest request)
    {
        this.request = request;
    }


    /**
     * Returns the current {@link ServletRequest} for this instance.
     *
     * @return the current {@link ServletRequest}
     * @throws UnsupportedOperationException if the request is null
     */
    protected ServletRequest getRequest()
    {
        if (request == null)
        {
            throw new UnsupportedOperationException("Request is null. ParameterParser must be initialized first!");
        }
        return request;
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
    public String get(String key)
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
        return getRequest().getParameter(key);
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
        return (s != null) ? Boolean.valueOf(s) : null;
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
     * @param key the key for the desired parameter
     * @return an array of String objects containing all of the values
     *         the given request parameter has, or <code>null</code>
     *         if the parameter does not exist
     */
    public String[] getStrings(String key)
    {
        return getRequest().getParameterValues(key);
    }


    /**
     * @param key the key for the desired parameter
     * @return an array of Number objects for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain Numbers.
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
            for(int i=0; i<nums.length; i++)
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
     * @return an array of int values for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain ints.
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
            for(int i=0; i<ints.length; i++)
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
     * @return an array of double values for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain doubles.
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
            for(int i=0; i<doubles.length; i++)
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
 
}

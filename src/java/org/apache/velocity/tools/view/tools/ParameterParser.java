/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.velocity.tools.view.tools;


import javax.servlet.ServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * Utility class for easy parsing of {@link ServletRequest} parameters
 * <br>
 * This class now implements the ViewTool interface to allow it
 * to be used as a request based tool.
 * <br>
 * It should be noted that this class is not thread-safe.  As it 
 * is wholly dependent upon the current ServletRequest, therefore each
 * ServletRequest should have its own instance.
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.2 $ $Date: 2003/03/20 05:55:09 $
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
     * Constructs a new instance using the specified request
     *
     * @param the {@link ServletRequest} to be parsed
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
     * @param the {@link ServletRequest} to be parsed
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
    public String get(String key) {
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
     * @param the desired parameter's key
     * @param the alternate value
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
     * @param the desired parameter's key
     * @return a {@link Boolean} object for the specified key or 
     *         <code>null</code> if no matching parameter is found
     */
    public Boolean getBoolean(String key)
    {
        String s = getString(key);
        return (s != null) ? Boolean.valueOf(s) : null;
    }


    /**
     * @param the desired parameter's key
     * @param the alternate boolean value
     * @return boolean value for the specified key or the 
     *         alternate boolean is no value is found
     */
    public boolean getBoolean(String key, boolean alternate)
    {
        Boolean bool = getBoolean(key);
        return (bool != null) ? bool.booleanValue() : alternate;
    }


    /**
     * @param the desired parameter's key
     * @param the alternate {@link Boolean}
     * @return a {@link Boolean} for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Boolean getBoolean(String key, Boolean alternate)
    {
        Boolean bool = getBoolean(key);
        return (bool != null) ? bool : alternate;
    }


    /**
     * @param the desired parameter's key
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
     * @param the desired parameter's key
     * @param the alternate Number
     * @return a Number for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public Number getNumber(String key, Number alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n : alternate;
    }


    /**
     * @param the desired parameter's key
     * @param the alternate int value
     * @return the int value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public int getInt(String key, int alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n.intValue() : alternate;
    }


    /**
     * @param the desired parameter's key
     * @param the alternate double value
     * @return the double value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public double getDouble(String key, double alternate)
    {
        Number n = getNumber(key);
        return (n != null) ? n.doubleValue() : alternate;
    }


    /**
     * @param the key for the desired parameter
     * @return an array of String objects containing all of the values
     *         the given request parameter has, or <code>null</code>
     *         if the parameter does not exist
     */
    public String[] getStrings(String key)
    {
        return getRequest().getParameterValues(key);
    }


    /**
     * @param the key for the desired parameter
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
     * @param the key for the desired parameter
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
     * @param the key for the desired parameter
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
     protected Number parseNumber(String value) throws NumberFormatException {
         if (value.indexOf('.') >= 0)
         {
             return new Double(value);
         }
         return new Long(value);
     }
 
 
}

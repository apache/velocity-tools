/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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


package org.apache.velocity.tools.tools;

import javax.servlet.ServletRequest;


/**
 * Utility class for easy parsing of {@link ServletRequest} parameters
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.3 $ $Date: 2002/05/10 05:42:17 $
 */

public class ParameterParser
{

    /**
     * Constructs a new instance
     */
    public ParameterParser()
    {}


    /**
     * @param request the servlet request
     * @param key the parameter's key
     * @return parameter matching the specified key or
     *         <code>null</code> if there is no matching
     *         parameter
     */
    public static String getString(ServletRequest request, String key)
    {
        return request.getParameter(key);
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate value
     * @return parameter matching the specified key or the 
     *         specified alternate String if there is no matching
     *         parameter
     */
    public static String getString(ServletRequest request, String key, String alternate)
    {
        String s = getString(request, key);
        return (s != null) ? s : alternate;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @return a {@link Boolean} object for the specified key or 
     *         <code>null</code> if no matching parameter is found
     */
    public static Boolean getBoolean(ServletRequest request, String key)
    {
        String s = getString(request, key);
        return (s != null) ? Boolean.valueOf(s) : null;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate boolean value
     * @return boolean value for the specified key or the 
     *         alternate boolean is no value is found
     */
    public static boolean getBoolean(ServletRequest request, String key, boolean alternate)
    {
        Boolean bool = getBoolean(request, key);
        return (bool != null) ? bool.booleanValue() : alternate;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate {@link Boolean}
     * @return a {@link Boolean} for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public static Boolean getBoolean(ServletRequest request, String key, Boolean alternate)
    {
        Boolean bool = getBoolean(request, key);
        return (bool != null) ? bool : alternate;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @return a {@link Number} for the specified key or 
     *         <code>null</code> if no matching parameter is found
     */
    public static Number getNumber(ServletRequest request, String key)
    {
        String s = getString(request, key);
        if (s == null || s.length() == 0)
        {
            return null;
        }
        if (s.indexOf('.') >= 0)
        {
            return new Double(s);
        }
        return new Long(s);
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate Number
     * @return a Number for the specified key or the specified
     *         alternate if no matching parameter is found
     */
    public static Number getNumber(ServletRequest request, String key, Number alternate)
    {
        Number n = getNumber(request, key);
        return (n != null) ? n : alternate;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate int value
     * @return the int value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public static int getInt(ServletRequest request, String key, int alternate)
    {
        Number n = getNumber(request, key);
        return (n != null) ? n.intValue() : alternate;
    }


    /**
     * @param request the servlet request
     * @param the desired parameter's key
     * @param the alternate double value
     * @return the double value for the specified key or the specified
     *         alternate value if no matching parameter is found
     */
    public static double getDouble(ServletRequest request, String key, double alternate)
    {
        Number n = getNumber(request, key);
        return (n != null) ? n.doubleValue() : alternate;
    }


    /**
     * @param request the servlet request
     * @param the key for the desired parameter
     * @return an array of String objects containing all of the values
     *         the given request parameter has, or <code>null</code>
     *         if the parameter does not exist
     */
    public static String[] getStrings(ServletRequest request, String key)
    {
        return request.getParameterValues(key);
    }


    /**
     * @param request the servlet request
     * @param the key for the desired parameter
     * @return an array of Number objects for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain Numbers.
     */
    public static Number[] getNumbers(ServletRequest request, String key)
    {
        String[] strings = getStrings(request, key);
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
                    if (strings[i].indexOf('.') >= 0)
                    {
                        nums[i] = new Double(strings[i]);
                    }
                    else
                    {
                        nums[i] = new Long(strings[i]);
                    }
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
     * @param request the servlet request
     * @param the key for the desired parameter
     * @return an array of int values for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain ints.
     */
    public static int[] getInts(ServletRequest request, String key)
    {
        String[] strings = getStrings(request, key);
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
                    ints[i] = Integer.parseInt(strings[i]);
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
     * @param request the servlet request
     * @param the key for the desired parameter
     * @return an array of double values for the specified key or 
     *         <code>null</code> if the parameter does not exist or the
     *         parameter does not contain doubles.
     */
    public static double[] getDoubles(ServletRequest request, String key)
    {
        String[] strings = getStrings(request, key);
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
                    doubles[i] = Double.parseDouble(strings[i]);
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
        return doubles;
    }


}
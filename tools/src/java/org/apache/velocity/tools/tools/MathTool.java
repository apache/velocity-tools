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

import java.lang.Math;


/**
 * <p>Tool for performing floating point math in Velocity.</p>
 *
 * <p>Several things should be noted here:</p>
 *
 * <ol>
 * <li>This class does not have methods that take
 * primitives.  This is simply because Velocity
 * wraps all primitives for us automagically.</li>
 *
 * <li>Most methods return {@link Double} wrappers
 * which automatically render the decimal places even
 * for whole numbers (e.g. new Double(1).toString() -> '1.0')
 * This is intentional.  This tool is for floating 
 * point arithmetic.  Integer arithmetic is already supported
 * in Velocity syntax.  if you really need '1' instead of '1.0',
 * just call intValue() on the result.</li>
 *
 * <li>No null pointer, number format, or divide by zero
 * exceptions are thrown here.  This is because such exceptions
 * thrown in template halt rendering.  It should be sufficient
 * debugging feedback that Velocity will render the reference
 * literally. (e.g. $math.div(1, 0) renders as '$math.div(1, 0)')</li>
 * </ul>
 * 
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Revision: 1.4 $ $Date: 2002/06/23 09:40:14 $
 */

public class MathTool
{
    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the sum of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double add(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(d1.doubleValue() + d2.doubleValue());
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the difference of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double sub(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(d1.doubleValue() - d2.doubleValue());
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the product of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double mul(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(d1.doubleValue() * d2.doubleValue());
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the quotient of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double div(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null || d2.doubleValue() == 0.0)
        {
            return null;
        }
        return new Double(d1.doubleValue() / d2.doubleValue());
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the first number raised to the power of the
     *         second or <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double pow(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(Math.pow(d1.doubleValue(), d2.doubleValue()));
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the largest of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double max(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(Math.max(d1.doubleValue(), d2.doubleValue()));
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the smallest of the numbers or 
     *         <code>null</code> if they're invalid
     * @see #toDouble
     */
    public static Double min(Object num1, Object num2)
    {
        Double d1 = toDouble(num1);
        Double d2 = toDouble(num2);
        if (d1 == null || d2 == null)
        {
            return null;
        }
        return new Double(Math.min(d1.doubleValue(), d2.doubleValue()));
    }


    /**
     * @param num1 the number
     * @return the absolute value of the number 
     *         <code>null</code> if it's invalid
     * @see #toDouble
     */
    public static Double abs(Object num)
    {
        Double d = toDouble(num);
        if (d == null)
        {
            return null;
        }
        return new Double(Math.abs(d.doubleValue()));
    }


    /**
     * Converts an object with a numeric value into a Double
     * Valid formats are {@link Number} or a {@link String}
     * representation of a number
     *
     * @param num the number to be converted
     * @return a {@link Double} representation of the number
     *         or <code>null</code> if it's invalid
     */
    public static Double toDouble(Object num)
    {
        double value;
        try
        {
            if (num instanceof Number)
            {
                value = ((Number)num).doubleValue();
            }
            else
            {
                value = Double.parseDouble(String.valueOf(num));
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
        return new Double(value);
    }


    /**
     * Converts an object with a numeric value into an Integer
     * Valid formats are {@link Number} or a {@link String}
     * representation of a number
     *
     * @param num the number to be converted
     * @return a {@link Integer} representation of the number
     *         or <code>null</code> if it's invalid
     */
    public static Integer toInteger(Object num)
    {
        Double d = toDouble(num);
        if (d == null)
        {
            return null;
        }
        return new Integer(d.intValue());
    }


    /**
     * Rounds a number to the specified number of decimal places.
     * This is particulary useful for simple display formatting.
     * If you want to round an number to the nearest integer, it
     * is better to use {@link #roundToInt}, as that will return
     * an {@link Integer} rather than a {@link Double}.
     *
     * @param decimals the number of decimal places
     * @param value the number to round
     * @return the value rounded to the specified number of
     *         decimal places or <code>null</code> if it's invalid
     * @see #toDouble
     * @see #toInteger
     */
    public static Double roundTo(Object decimals, Object num)
    {
        Integer i = toInteger(decimals);
        Double d = toDouble(num);
        if (i == null || d == null)
        {
            return null;
        }
        //ok, go ahead and do the rounding
        int places = i.intValue();
        double value = d.doubleValue();
        if (places == 0)
        {
            value = (int)(value + .5);
        }
        else
        {
            double shift = Math.pow(10, places);
            value = value * shift;
            value = (int)(value + .5);
            value = value / shift;
        }
        return new Double(value);
    }


    /**
     * Rounds a number to the nearest whole Integer
     *
     * @param num the number to round
     * @return the number rounded to the nearest whole Integer
     *         or <code>null</code> if it's invalid
     * @see #toDouble
     */
    public static Integer roundToInt(Object num)
    {
        Double d = toDouble(num);
        if (d == null)
        {
            return null;
        }
        return new Integer((int)Math.rint(d.doubleValue()));
    }


    /**
     * @return a pseudo-random {@link Double} greater 
     *          than or equal to 0.0 and less than 1.0
     * @see Math#random()
     */
    public static Double getRandom()
    {
        return new Double(Math.random());
    }


    /**
     * This returns a random {@link Integer} within the
     * specified range.  The return Integer will have a
     * value greater than or equal to the first number
     * and less than the second number.
     *
     * @param num1 the first number
     * @param num2 the second number
     * @return a pseudo-random {@link Integer} greater than
     *         or equal to the first number and less than
     *         the second
     * @see #toInteger
     * @see Math#random()
     */
    public static Integer random(Object num1, Object num2)
    {
        Integer i1 = toInteger(num1);
        Integer i2 = toInteger(num2);
        if (i1 == null || i2 == null)
        {
            return null;
        }
        //get the difference
        double diff = i2.intValue() - i1.intValue();
        //multiply the difference by a pseudo-random 
        //double from 0.0 to 1.0 and round to the nearest int
        int random = (int)Math.rint(diff * Math.random());
        //add the first value to the random int and return as an Integer
        return new Integer(random + i1.intValue());
    }


}

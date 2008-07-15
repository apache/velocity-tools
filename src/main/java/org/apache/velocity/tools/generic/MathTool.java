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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>Tool for performing math in Velocity.</p>
 *
 * <p>Some things should be noted here:</p>
 * <ul>
 * <li>This class does not have methods that take
 * primitives.  This is simply because Velocity
 * wraps all primitives for us automagically.</li>
 *
 * <li>No null pointer, number format, or divide by zero
 * exceptions are thrown here.  This is because such exceptions
 * thrown in template halt rendering.  It should be sufficient
 * debugging feedback that Velocity will render the reference
 * literally. (e.g. $math.div(1, 0) renders as '$math.div(1, 0)')</li>
 * </ul>
 * <p><pre>
 * Example tools.xml config:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.MathTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author Nathan Bubna
 * @author Leon Messerschmidt
 * @version $Revision$ $Date$
 */
@DefaultKey("math")
public class MathTool extends FormatConfig
{
    /* Old non-vararg methods (can be removed once we require Velocity 1.6) */

    public Number add(Object num1, Object num2)
    {
        return add(new Object[] { num1, num2 });
    }

    public Number sub(Object num1, Object num2)
    {
        return sub(new Object[] { num1, num2 });
    }

    public Number mul(Object num1, Object num2)
    {
        return mul(new Object[] { num1, num2 });
    }

    public Number div(Object num1, Object num2)
    {
        return div(new Object[] { num1, num2 });
    }

    public Number max(Object num1, Object num2)
    {
        return max(new Object[] { num1, num2 });
    }

    public Number min(Object num1, Object num2)
    {
        return min(new Object[] { num1, num2 });
    }

    /**
     * @param nums the numbers to be added
     * @return the sum of the numbers or
     *         <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number add(Object... nums)
    {
        double value = 0;
        Number[] ns = new Number[nums.length];
        for (Object num : nums)
        {
            Number n = toNumber(num);
            if (n == null)
            {
                return null;
            }
            value += n.doubleValue();
        }
        return matchType(value, ns);
    }


    /**
     * @param nums the numbers to be subtracted
     * @return the difference of the numbers (subtracted in order) or
     *         <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number sub(Object... nums)
    {
        double value = 0;
        Number[] ns = new Number[nums.length];
        for (int i=0; i<nums.length; i++)
        {
            Number n = toNumber(nums[i]);
            if (n == null)
            {
                return null;
            }
            if (i == 0)
            {
                value = n.doubleValue();
            }
            else
            {
                value -= n.doubleValue();
            }
        }
        return matchType(value, ns);
    }


    /**
     * @param nums the numbers to be multiplied
     * @return the product of the numbers or
     *         <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number mul(Object... nums)
    {
        double value = 1;
        Number[] ns = new Number[nums.length];
        for (Object num : nums)
        {
            Number n = toNumber(num);
            if (n == null)
            {
                return null;
            }
            value *= n.doubleValue();
        }
        return matchType(value, ns);
    }


    /**
     * @param nums the numbers to be divided
     * @return the quotient of the numbers or
     *         <code>null</code> if they're invalid
     *         or if any denominator equals zero
     * @see #toNumber
     */
    public Number div(Object... nums)
    {
        double value = 0;
        Number[] ns = new Number[nums.length];
        for (int i=0; i<nums.length; i++)
        {
            Number n = toNumber(nums[i]);
            if (n == null)
            {
                return null;
            }
            if (i == 0)
            {
                value = n.doubleValue();
            }
            else
            {
                double denominator = n.doubleValue();
                if (denominator == 0.0)
                {
                    return null;
                }
                value /= denominator;
            }
        }
        return matchType(value, ns);
    }


    /**
     * @param num1 the first number
     * @param num2 the second number
     * @return the first number raised to the power of the
     *         second or <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number pow(Object num1, Object num2)
    {
        Number n1 = toNumber(num1);
        Number n2 = toNumber(num2);
        if (n1 == null || n2 == null)
        {
            return null;
        }
        double value = Math.pow(n1.doubleValue(), n2.doubleValue());
        return matchType(n1, n2, value);
    }


    /**
     * Does integer division on the int values of the specified numbers.
     *
     * <p>So, $math.idiv('5.1',3) will return '1',
     *    and $math.idiv(6,'3.9') will return '2'.</p>
     *
     * @param num1 the first number
     * @param num2 the second number
     * @return the result of performing integer division
     *         on the operands.
     * @see #toInteger
     */
    public Integer idiv(Object num1, Object num2)
    {
        Number n1 = toNumber(num1);
        Number n2 = toNumber(num2);
        if (n1 == null || n2 == null || n2.intValue() == 0)
        {
            return null;
        }
        int value = n1.intValue() / n2.intValue();
        return Integer.valueOf(value);
    }


    /**
     * Does integer modulus on the int values of the specified numbers.
     *
     * <p>So, $math.mod('5.1',3) will return '2',
     *    and $math.mod(6,'3.9') will return '0'.</p>
     *
     * @param num1 the first number
     * @param num2 the second number
     * @return the result of performing integer modulus
     *         on the operands.
     * @see #toInteger
     */
    public Integer mod(Object num1, Object num2)
    {
        Number n1 = toNumber(num1);
        Number n2 = toNumber(num2);
        if (n1 == null || n2 == null || n2.intValue() == 0)
        {
            return null;
        }
        int value = n1.intValue() % n2.intValue();
        return Integer.valueOf(value);
    }


    /**
     * @param nums the numbers to be searched
     * @return the largest of the numbers or
     *         <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number max(Object... nums)
    {
        double value = Double.MIN_VALUE;
        Number[] ns = new Number[nums.length];
        for (Object num : nums)
        {
            Number n = toNumber(num);
            if (n == null)
            {
                return null;
            }
            value = Math.max(value, n.doubleValue());
        }
        return matchType(value, ns);
    }


    /**
     * @param nums the numbers to be searched
     * @return the smallest of the numbers or
     *         <code>null</code> if they're invalid
     * @see #toNumber
     */
    public Number min(Object... nums)
    {
        double value = Double.MAX_VALUE;
        Number[] ns = new Number[nums.length];
        for (Object num : nums)
        {
            Number n = toNumber(num);
            if (n == null)
            {
                return null;
            }
            value = Math.min(value, n.doubleValue());
        }
        return matchType(value, ns);
    }


    /**
     * @param num the number
     * @return the absolute value of the number or
     *         <code>null</code> if it's invalid
     * @see #toDouble
     */
    public Number abs(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        double value = Math.abs(n.doubleValue());
        return matchType(n, value);
    }


    /**
     * @param num the number
     * @return the smallest integer that is not
     *         less than the given number
     */
    public Integer ceil(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        return Integer.valueOf((int)Math.ceil(n.doubleValue()));
    }


    /**
     * @param num the number
     * @return the integer portion of the number
     */
    public Integer floor(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        return Integer.valueOf((int)Math.floor(n.doubleValue()));
    }


    /**
     * Rounds a number to the nearest whole Integer
     *
     * @param num the number to round
     * @return the number rounded to the nearest whole Integer
     *         or <code>null</code> if it's invalid
     * @see java.lang.Math#rint(double)
     */
    public Integer round(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        return Integer.valueOf((int)Math.rint(n.doubleValue()));
    }


    /**
     * Rounds a number to the specified number of decimal places.
     * This is particulary useful for simple display formatting.
     * If you want to round an number to the nearest integer, it
     * is better to use {@link #round}, as that will return
     * an {@link Integer} rather than a {@link Double}.
     *
     * @param decimals the number of decimal places
     * @param num the number to round
     * @return the value rounded to the specified number of
     *         decimal places or <code>null</code> if it's invalid
     * @see #toNumber
     */
    public Double roundTo(Object decimals, Object num)
    {
        Number i = toNumber(decimals);
        Number d = toNumber(num);
        if (i == null || d == null)
        {
            return null;
        }
        //ok, go ahead and do the rounding
        int places = i.intValue();
        double value = d.doubleValue();
        int delta = 10;
        for(int j=1;j<places;j++) {
            delta *= 10;
        }
        return new Double((double)Math.round(value*delta)/delta);
    }


    /**
     * @return a pseudo-random {@link Double} greater
     *          than or equal to 0.0 and less than 1.0
     * @see Math#random()
     */
    public Double getRandom()
    {
        return new Double(Math.random());
    }


    /**
     * This returns a random {@link Number} within the
     * specified range.  The returned value will be
     * greater than or equal to the first number
     * and less than the second number.  If both arguments
     * are whole numbers then the returned number will
     * also be, otherwise a {@link Double} will
     * be returned.
     *
     * @param num1 the first number
     * @param num2 the second number
     * @return a pseudo-random {@link Number} greater than
     *         or equal to the first number and less than
     *         the second
     * @see Math#random()
     */
    public Number random(Object num1, Object num2)
    {
        Number n1 = toNumber(num1);
        Number n2 = toNumber(num2);
        if (n1 == null || n2 == null)
        {
            return null;
        }

        double diff = n2.doubleValue() - n1.doubleValue();
        // multiply the difference by a pseudo-random double from
        // 0.0 to 1.0, round to the nearest int, and add the first
        // value to the random int and return as an Integer
        double random = (diff * Math.random()) + n1.doubleValue();

        // check if either of the args were floating points
        String in = n1.toString() + n2.toString();
        if (in.indexOf('.') < 0)
        {
            // args were whole numbers, so return the same
            return matchType(n1, n2, Math.floor(random));
        }
        // one of the args was a floating point,
        // so don't floor the result
        return new Double(random);
    }


    // --------------- public type conversion methods ---------

    /**
     * Converts an object with a numeric value into an Integer
     * Valid formats are {@link Number} or a {@link String}
     * representation of a number
     *
     * @param num the number to be converted
     * @return a {@link Integer} representation of the number
     *         or <code>null</code> if it's invalid
     */
    public Integer toInteger(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        return Integer.valueOf(n.intValue());
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
    public Double toDouble(Object num)
    {
        Number n = toNumber(num);
        if (n == null)
        {
            return null;
        }
        return new Double(n.doubleValue());
    }


    /**
     * Converts an object with a numeric value into a Number
     * Valid formats are {@link Number} or a {@link String}
     * representation of a number.  Note that this does not
     * handle localized number formats.  Use the {@link NumberTool}
     * to handle such conversions.
     *
     * @param num the number to be converted
     * @return a {@link Number} representation of the number
     *         or <code>null</code> if it's invalid
     */
    public Number toNumber(Object num)
    {
        return ConversionUtils.toNumber(num, getFormat(), getLocale());
    }


    // --------------------------- protected methods ------------------

    /**
     * @see #matchType(Number[],double)
     */
    protected Number matchType(Number in, double out)
    {
        return matchType(out, new Number[] { in });
    }

    /**
     * @see #matchType(Number[],double)
     */
    protected Number matchType(Number in1, Number in2, double out)
    {
        return matchType(out, new Number[] { in1, in2 });
    }

    /**
     * Takes the original argument(s) and returns the resulting value as
     * an instance of the best matching type (Integer, Long, or Double).
     * If either an argument or the result is not an integer (i.e. has no
     * decimal when rendered) the result will be returned as a Double.
     * If not and the result is < -2147483648 or > 2147483647, then a
     * Long will be returned.  Otherwise, an Integer will be returned.
     */
    protected Number matchType(double out, Number... in)
    {
        //NOTE: if we just checked class types, we could miss custom
        //      extensions of java.lang.Number, and if we only checked
        //      the mathematical value, $math.div('3.0', 1) would render
        //      as '3'.  To get the expected result, we check what we're
        //      concerned about: the rendered string.

        // first check if the result is even a whole number
        boolean isIntegral = (Math.rint(out) == out);
        if (isIntegral)
        {
            for (Number n : in)
            {
                if (n == null)
                {
                    break;
                }
                else if (hasFloatingPoint(n.toString()))
                {
                    isIntegral = false;
                    break;
                }
            }
        }

        if (!isIntegral)
        {
            return new Double(out);
        }
        else if (out > Integer.MAX_VALUE || out < Integer.MIN_VALUE)
        {
            return Long.valueOf((long)out);
        }
        else
        {
            return Integer.valueOf((int)out);
        }
    }

    protected boolean hasFloatingPoint(String value)
    {
        return value.indexOf('.') >= 0;
    }

    @Deprecated
    protected Number parseNumber(String value)
    {
        // check for the floating point
        if (!hasFloatingPoint(value))
        {
            // check for large numbers
            long i = Long.valueOf(value).longValue();
            if (i > Integer.MAX_VALUE || i < Integer.MIN_VALUE)
            {
                return Long.valueOf(i);
            }
            else
            {
                return Integer.valueOf((int)i);
            }
        }
        else
        {
            return new Double(value);
        }
    }



    // ------------------------- Aggregation methods ------------------

    /**
     * Get the sum of the values from a list
     *
     * @param collection  A collection containing Java beans
     * @param field A Java Bean field for the objects in <i>collection</i> that
     *              will return a number.
     * @return The sum of the values in <i>collection</i>.
     */
    public Number getTotal(Collection collection, String field)
    {
        if (collection == null || field == null)
        {
            return null;
        }
        double result = 0;
        // hold the first number and use it to match return type
        Number first = null;
        try
        {
            for (Iterator i = collection.iterator(); i.hasNext();)
            {
                Object property = PropertyUtils.getProperty(i.next(), field);
                Number value = toNumber(property);
                // skip over nulls (i.e. treat them as 0)
                if (value != null)
                {
                    if (first == null)
                    {
                        first = value;
                    }
                    result += value.doubleValue();
                }
            }
            return matchType(first, result);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Get the average of the values from a list
     *
     * @param collection  A collection containing Java beans
     * @param field A Java Bean field for the objects in <i>collection</i> that
     *              will return a number.
     * @return The average of the values in <i>collection</i>.
     */
    public Number getAverage(Collection collection, String field)
    {
        Number result = getTotal(collection, field);
        if (result == null)
        {
            return null;
        }
        double avg = result.doubleValue() / collection.size();
        return matchType(result, avg);
    }

    /**
     * Get the sum of the values from a list
     *
     * @param array  An array containing Java beans
     * @param field A Java Bean field for the objects in <i>array</i> that
     *              will return a number.
     * @return The sum of the values in <i>array</i>.
     */
    public Number getTotal(Object[] array, String field)
    {
        return getTotal(Arrays.asList(array), field);
    }

    /**
     * Get the sum of the values from a list
     *
     * @param array  A collection containing Java beans
     * @param field A Java Bean field for the objects in <i>array</i> that
     *      will return a number.
     * @return The sum of the values in <i>array</i>.
     */
    public Number getAverage(Object[] array, String field)
    {
        return getAverage(Arrays.asList(array), field);
    }

    /**
     * Get the sum of the values
     *
     * @param collection  A collection containing numeric values
     * @return The sum of the values in <i>collection</i>.
     */
    public Number getTotal(Collection collection)
    {
        if (collection == null)
        {
            return null;
        }

        double result = 0;
        // grab the first number and use it to match return type
        Number first = null;
        for (Iterator i = collection.iterator(); i.hasNext();)
        {
            Number value = toNumber(i.next());
            if (value == null)
            {
                //FIXME? or should we ignore this and keep adding?
                return null;
            }
            if (first ==  null)
            {
                first = value;
            }
            result += value.doubleValue();
        }
        return matchType(first, result);
    }

    /**
     * Get the average of the values
     *
     * @param collection  A collection containing number values
     * @return The average of the values in <i>collection</i>.
     */
    public Number getAverage(Collection collection)
    {
        Number result = getTotal(collection);
        if (result == null)
        {
            return null;
        }
        double avg = result.doubleValue() / collection.size();
        return matchType(result, avg);
    }

    /**
     * Get the sum of the values
     *
     * @param array  An array containing number values
     * @return The sum of the values in <i>array</i>.
     */
    public Number getTotal(Object... array)
    {
        return getTotal(Arrays.asList(array));
    }

    /**
     * Get the average of the values
     *
     * @param array  An array containing number values
     * @return The sum of the values in <i>array</i>.
     */
    public Number getAverage(Object... array)
    {
        return getAverage(Arrays.asList(array));
    }

    /**
     * Get the sum of the values
     *
     * @param values The list of double values to add up.
     * @return The sum of the arrays
     */
    public Number getTotal(double... values)
    {
        if (values == null)
        {
            return null;
        }

        double result = 0;
        for (int i = 0; i < values.length; i++)
        {
            result += values[i];
        }
        return new Double(result);
    }

    /**
     * Get the average of the values in an array of double values
     *
     * @param values The list of double values
     * @return The average of the array of values
     */
    public Number getAverage(double... values)
    {
        Number total = getTotal(values);
        if (total == null)
        {
            return null;
        }
        return new Double(total.doubleValue() / values.length);
    }

    /**
     * Get the sum of the values
     *
     * @param values The list of long values to add up.
     * @return The sum of the arrays
     */
    public Number getTotal(long... values)
    {
        if (values == null)
        {
            return null;
        }

        long result = 0;
        for (int i = 0; i < values.length; i++)
        {
            result += values[i];
        }
        return Long.valueOf(result);
    }

    /**
     * Get the average of the values in an array of long values
     *
     * @param values The list of long values
     * @return The average of the array of values
     */
    public Number getAverage(long... values)
    {
        Number total = getTotal(values);
        if (total == null)
        {
            return null;
        }
        double avg = total.doubleValue() / values.length;
        return matchType(total, avg);
    }

}

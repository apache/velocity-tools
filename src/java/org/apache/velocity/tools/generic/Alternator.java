/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.velocity.tools.generic;

import java.util.List;

/**
 * Utility class for easily alternating over values in a list.
 *
 * <p><b>Example usage:</b>
 * <pre>
 * Java using a List...
 *      List myColors = new ArrayList();
 *      myColors.add("red");
 *      myColors.add("blue");
 *      context.put("color", new Alternator(myColors));
 *      List myStyles = new ArrayList();
 *      myColors.add("hip");
 *      myColors.add("fly");
 *      myColors.add("groovy");
 *      // use auto alternation
 *      context.put("style", new Alternator(true, myStyles));
 *
 * or Java using Object array...
 *      context.put("color", new Alternator(true, new String[]
 *                                                { "red", "blue" }));
 *      context.put("style", new Alternator(true, new String[]
 *                                                { "hip", "fly", "groovy" }));
 *
 * template...
 *      #foreach( $foo in [1..5] )
 *       $foo is $color.next and $style
 *      #end
 *
 * output...
 *      1 is red and hip
 *      2 is blue and fly 
 *      3 is red and groovy
 *      4 is blue and hip 
 *      5 is red and fly
 * </pre></p>
 *
 * @since Velocity Tools 1.2
 * @version $Revision: 1.3 $ $Date: 2004/05/05 20:57:46 $
 */
public class Alternator
{
    private Object[] list;
    private int index = 0;
    private boolean auto = false;

    /**
     * Creates a new Alternator for the specified list.  Alternation
     * is set to explicit (e.g. it's not automatic).
     */
    public Alternator(Object[] list)
    {
        this(false, list);
    }

    /**
     * Creates a new Alternator for the specified list with the specified
     * automatic shifting preference.
     *
     * @param auto See {@link #setAuto(boolean auto)}.
     * @param list The (non-<code>null</code>) list of elements to
     * alternate.
     */
    public Alternator(boolean auto, List list)
    {
        this(auto, list.toArray(new Object[list.size()]));
    }

    /**
     * Creates a new Alternator for the specified list.  Alternation
     * is set to explicit (e.g. it's not automatic).
     */
    public Alternator(List list)
    {
        this(false, list);
    }

    /**
     * Creates a new Alternator for the specified list with the specified
     * automatic shifting preference.
     *
     * @param auto See {@link #setAuto(boolean auto)}.
     * @param list The (non-<code>null</code>) list of elements to
     * alternate.
     */
    public Alternator(boolean auto, Object[] list)
    {
        this.auto = auto;
        this.list = list;
    }

    /**
     * @return Whether this Alternator shifts the list index
     * automatically after a call to {@link #toString()}.
     */
    public boolean isAuto()
    {
        return auto;
    }

    /**
     * If set to true, the list index will shift automatically after a 
     * call to toString().
     */
    public void setAuto(boolean auto)
    {
        this.auto = auto;
    }

    /**
     * Manually shifts the list index. If it reaches the end of the list,
     * it will start over again at zero.
     */
    public void shift()
    {
        index = (index + 1) % list.length;
    }

    /**
     * Returns the current item without shifting the list index.
     */
    public Object getCurrent()
    {
        return list[index];
    }

    /**
     * Returns the current item, then shifts the list index.
     */
    public Object getNext()
    {
        Object o = getCurrent();
        shift();
        return o;
    }

    /**
     * Returns a string representation of the current item or
     * <code>null</code> if the current item is null.  Also,
     * if <i>auto</i> is true, this will shift after returning
     * the current item.
     */
    public String toString()
    {
        Object o = list[index];
        if (auto)
        {
            shift();
        }
        if (o == null)
        {
            return null;
        }
        return o.toString();
    }

}

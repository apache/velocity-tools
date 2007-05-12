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

import java.util.List;
import java.util.Map;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * Simple tool to provide easy in-template instantiation of
 * {@link Alternator}s from varying "list" types.
 *
 * <p><b>Example Use:</b>
 * <pre>
 * toolbox.xml...
 * &lt;tool&gt;
 *   &lt;key&gt;alternator&lt;/key&gt;
 *   &lt;scope&gt;application&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.generic.AlternatorTool&lt;/class&gt;
 *   &lt;parameter name="auto-alternate" value="true"/&gt;
 * &lt;/tool&gt;
 *
 * template...
 * #set( $color = $alternator.auto('red', 'blue') )
 * ## use manual alternation for this one
 * #set( $style = $alternator.manual(['hip','fly','groovy']) )
 * #foreach( $i in [1..5] )
 *   Number $i is $color and $style. I dig $style.next numbers.
 * #end *
 *
 * output...
 *   Number 1 is red and hip. I dig hip numbers.
 *   Number 2 is blue and fly. I dig fly numbers.
 *   Number 3 is red and groovy. I dig groovy numbers.
 *   Number 4 is blue and hip. I dig hip numbers.
 *   Number 5 is red and fly. I dig fly numbers.
 * </pre></p>
 *
 * @since Velocity Tools 1.2
 * @version $Revision$ $Date$
 */
@DefaultKey("alternator")
public class AlternatorTool
{
    /** @since VelocityTools 1.3 */
    public static final String AUTO_ALTERNATE_DEFAULT_KEY = "auto-alternate";

    // it's true by default in Alternator
    private boolean autoAlternateDefault = true;

    /**
     * Looks for a default auto-alternate value in the given params,
     * if not, set the default to true.
     * @since VelocityTools 1.3
     */
    public void configure(Map params)
    {
        ValueParser parser = new ValueParser(params);
        // it's true by default in Alternator
        autoAlternateDefault =
            parser.getBoolean(AUTO_ALTERNATE_DEFAULT_KEY, true);
    }

    /**
     * Returns true if the default for auto-alternating is true.
     * @since VelocityTools 1.3
     */
    public boolean getAutoAlternateDefault()
    {
        return autoAlternateDefault;
    }

    /**
     * Sets the default for auto-alternating.
     * @since VelocityTools 1.3
     */
    protected void setAutoAlternateDefault(boolean bool)
    {
        this.autoAlternateDefault = bool;
    }

    /**
     * Make an automatic {@link Alternator} from a List.
     */
    public Alternator make(List list)
    {
        return make(autoAlternateDefault, list);
    }

    /**
     * @deprecated Use {@link #auto(List list)} or
     *             {@link #manual(List list)} instead.
     */
    public Alternator make(boolean auto, List list)
    {
        if (list == null)
        {
            return null;
        }
        return new Alternator(auto, list);
    }

    /**
     * Make an automatic {@link Alternator} from an object array.
     */
    public Alternator make(Object[] array)
    {
        return make(autoAlternateDefault, array);
    }

    /**
     * @deprecated Use {@link #auto(Object[] array)} or
     *             {@link #manual(Object[] array)} instead.
     */
    public Alternator make(boolean auto, Object[] array)
    {
        if (array == null)
        {
            return null;
        }
        return new Alternator(auto, array);
    }

    /**
     * Make an automatic {@link Alternator} from a list containing the two
     * specified objects.
     *
     * @return The new Alternator, or <code>null</code> if arguments
     * were illegal.
     */
    public Alternator make(Object o1, Object o2)
    {
        return make(autoAlternateDefault, o1, o2);
    }

    /**
     * @deprecated Use {@link #auto(Object o1, Object o2)} or
     *             {@link #manual(Object o1, Object o2)} instead.
     */
    public Alternator make(boolean auto, Object o1, Object o2)
    {
        if (o1 == null || o2 == null)
        {
            return null;
        }
        return new Alternator(auto, new Object[] { o1, o2 });
    }

    /**
     * Make an automatic {@link Alternator} from values in the specified List.
     *
     * @return a new, automatic Alternator with the values in the List or 
     *         <code>null</code> if the List is <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator auto(List list)
    {
        return make(true, list);
    }

    /**
     * Make an automatic {@link Alternator} from the specified object array.
     *
     * @return a new, automatic Alternator with the values in the array or 
     *         <code>null</code> if the array is <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator auto(Object[] array)
    {
        return make(true, array);
    }

    /**
     * Make an automatic {@link Alternator} from a list containing the two
     * specified objects.
     *
     * @param o1 The first of two objects for alternation between.
     * Must be non-<code>null</code>.
     * @param o2 The second of two objects for alternation between.
     * Must be non-<code>null</code>.
     * @return The new Alternator, or <code>null</code> if an argument
     * was <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator auto(Object o1, Object o2)
    {
        return make(true, o1, o2);
    }

    /**
     * Make a manual {@link Alternator} from values in the specified List.
     *
     * @return a new, manual Alternator with the values in the List or 
     *         <code>null</code> if the List is <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator manual(List list)
    {
        return make(false, list);
    }

    /**
     * Make a manual {@link Alternator} from the specified object array.
     *
     * @return a new, manual Alternator with the values in the array or 
     *         <code>null</code> if the array is <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator manual(Object[] array)
    {
        return make(false, array);
    }

    /**
     * Make a manual {@link Alternator} from a list containing the two
     * specified objects.
     *
     * @param o1 The first of two objects for alternation between.
     * Must be non-<code>null</code>.
     * @param o2 The second of two objects for alternation between.
     * Must be non-<code>null</code>.
     * @return The new Alternator, or <code>null</code> if an argument
     * was <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator manual(Object o1, Object o2)
    {
        return make(false, o1, o2);
    }

}

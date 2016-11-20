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

import java.util.Collection;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * Simple tool to provide easy in-template instantiation of
 * {@link Alternator}s from varying "list" types or individual
 * arguments.
 *
 * <p><b>Example Use:</b>
 * <pre>
 * tools.xml...
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.AlternatorTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 *
 * template...
 * #set( $color = $alternator.auto('red', 'blue') )
 * ## use manual alternation for this one
 * #set( $style = $alternator.manual('hip','fly','groovy') )
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
 * @deprecated use CSS3 nth-child(even/odd) selectors or #if($foreach.index % 2)
 */

@ValidScope(Scope.APPLICATION)
@DefaultKey("alternator")
@Deprecated
public class AlternatorTool extends SafeConfig
{
    public static final String AUTO_ALTERNATE_DEFAULT_KEY = "autoAlternate";

    // it's true by default in Alternator
    private boolean autoAlternateDefault = true;

    /**
     * Looks for a default auto-alternate value in the given params,
     * if not, set the default to true.
     */
    protected void configure(ValueParser parser)
    {
        Boolean auto = parser.getBoolean(AUTO_ALTERNATE_DEFAULT_KEY, Boolean.TRUE);
        this.autoAlternateDefault = auto.booleanValue();
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
     * Make an automatic {@link Alternator} from the specifed objects.
     */
    public Alternator make(Object... list)
    {
        return make(autoAlternateDefault, list);
    }

    /**
     * Returns a new Alternator for the specified list with the specified
     * automatic shifting preference.
     *
     * @param auto See {@link Alternator#setAuto(boolean auto)}.
     * @param list The list of elements to alternate.
     */
    public Alternator make(boolean auto, Object... list)
    {
        if (list == null || list.length == 0)
        {
            return null;
        }
        else if (list.length == 1 &&
                 list[0] instanceof Collection &&
                 ((Collection)list[0]).isEmpty())
        {
            return null;
        }
        return new Alternator(auto, list);
    }

    /**
     * Make an automatic {@link Alternator} from the specified objects.
     *
     * @return a new, automatic Alternator with the specified values or 
     *         <code>null</code> if there are none specified.
     * @since VelocityTools 1.3
     */
    public Alternator auto(Object... list)
    {
        return make(true, list);
    }

    /**
     * Make a manual {@link Alternator} from the specified objects.
     *
     * @return a new, manual Alternator with the values in the array or 
     *         <code>null</code> if the array is <code>null</code>.
     * @since VelocityTools 1.3
     */
    public Alternator manual(Object... list)
    {
        return make(false, list);
    }
}

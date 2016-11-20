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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * SortTool allows a user to sort a collection (or array, iterator, etc)
 * on any arbitary set of properties exposed by the objects contained
 * within the collection.
 *
 * <p>The sort tool is specifically designed to use within a #foreach
 * but you may find other uses for it.</p>
 *
 * <p>The sort tool can handle all of the collection types supported by
 * #foreach and the same constraints apply as well as the following.
 * Every object in the collection must support the set of properties
 * selected to sort on. Each property which is to be sorted on must
 * return one of the follow:
 * <ul>
 *   <li>Primitive type: e.g. int, char, long etc</li>
 *   <li>Standard Object: e.g. String, Integer, Long etc</li>
 *   <li>Object which implements the Comparable interface.</li>
 * </ul>
 * </p>
 *
 * <p>During the sort operation all properties are compared by calling
 * compareTo() with the exception of Strings for which
 * compareToIgnoreCase() is called.</p>
 *
 * <p>The sort is performed by calling Collections.sort() after
 * marshalling the collection to sort into an appropriate collection type.
 * The original collection will not be re-ordered; a new list containing
 * the sorted elements will always be returned.</p>
 *
 * <p>The tool is used as follows:
 * <pre>
 * Single Property Sort
 * #foreach($obj in $sorter.sort($objects, "name"))
 *   $obj.name Ordinal= $obj.ordinal
 * #end
 * End
 *
 * Multiple Property Sort
 * #foreach($obj in $sorter.sort($objects, ["name", "ordinal"]))
 *   $obj.name, $obj.ordinal
 * #end
 * End
 * </pre>
 *
 * The sort method takes two parameters a collection and a property name
 * or an array of property names. The property names and corresponding
 * methods must conform to java bean standards since commons-beanutils
 * is used to extract the property values.</p>
 *
 * <p>By default the sort tool sorts ascending, you can override this by
 * adding a sort type suffix to any property name.</p>
 *
 * <p>The supported suffixes are:
 * <pre>
 * For ascending
 * :asc
 * For descending
 * :desc
 *
 * Example
 * #foreach($obj in $sorter.sort($objects, ["name:asc", "ordinal:desc"]))
 *   $obj.name, $obj.ordinal
 * #end
 * </pre><p>
 *
 * <p>This will sort first by Name in ascending order and then by Ordinal
 * in descending order, of course you could have left the :asc off of the
 * 'Name' property as ascending is always the default.</p>
 *
 * <p><pre>
 * Example tools.xml config (if you want to use this with VelocityView):
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.SortTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author S. Brett Sutton
 * @author Nathan Bubna
 * @since VelocityTools 1.2
 * @version $Id$
 * @deprecated use CollectionTool sort methods
 */
@DefaultKey("sorter")
@Deprecated
public class SortTool extends CollectionTool
{
}

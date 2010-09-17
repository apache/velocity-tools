package org.apache.velocity.tools;

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
import java.util.Collections;
import java.util.List;

/**
 * A specialized constants class to provide some compile-time typo checking and
 * runtime validation for scopes specified in annotations, toolbox configs, etc.
 *
 * @author Nathan Bubna
 * @version $Id: Toolbox.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public final class Scope
{
    public static final String REQUEST = "request";
    public static final String SESSION = "session";
    public static final String APPLICATION = "application";

    private static final List<String> VALUES;
    static
    {
        List<String> defaults = new ArrayList<String>(3);
        defaults.add(REQUEST);
        defaults.add(SESSION);
        defaults.add(APPLICATION);
        VALUES = Collections.synchronizedList(defaults);
    }

    // keep an instance available in case someone wants to
    // drop this into a template for some reason
    private static final Scope INSTANCE = new Scope();

    public static final Scope getInstance()
    {
        return INSTANCE;
    }

    public static final void add(String newScope)
    {
        // keep everything lower case
        newScope = newScope.toLowerCase();
        // complain if it already exists
        if (VALUES.contains(newScope))
        {
            throw new IllegalArgumentException("Scope '"+newScope+"' has already been registered.");
        }
        VALUES.add(newScope);
    }

    public static final boolean exists(String scope)
    {
        // keep everything lower case
        scope = scope.toLowerCase();
        return VALUES.contains(scope);
    }

    public static final List<String> values()
    {
        return Collections.unmodifiableList(VALUES);
    }

    private Scope()
    {
        // keep constructor private, this is a singleton
    }

}

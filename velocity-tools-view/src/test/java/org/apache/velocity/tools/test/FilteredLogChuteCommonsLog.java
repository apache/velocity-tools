package org.apache.velocity.tools.test;

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

import org.apache.velocity.tools.generic.log.LogChuteCommonsLog;

/**
 * This logging adapter filters out trace and debug messages that don't come from the velocity package
 * (provided its given name contains a class name)
 *
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @version $Id:$
 */

public class FilteredLogChuteCommonsLog  extends LogChuteCommonsLog {

    private boolean filter = false;

    public FilteredLogChuteCommonsLog(String name)
    {
        filter = !name.startsWith("org.apache.velocity");
    }

    public void trace(Object message)
    {
        if(!filter) super.trace(message);
    }

    public void trace(Object message, Throwable t)
    {
        if(!filter) super.trace(message,t);
    }

    public void debug(Object message) {
        if(!filter) super.debug(message);
    }

    public void debug(Object message, Throwable t) {
        if(!filter) super.debug(message,t);
    }

}

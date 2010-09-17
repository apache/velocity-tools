package org.apache.velocity.tools.view.servlet;

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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.tools.view.ServletLogChute;

/**
 * <p>This is basically an empty subclass of {@link ServletLogChute} that exists
 *    merely for backwards compatibility with VelocityTools 1.x. Please
 *    use {@link ServletLogChute} directly, as this will likely be removed
 *    in VelocityTools 2.1, if not earlier.
 * </p>
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @deprecated Use {@link ServletLogChute} instead
 * @version $Revision$ $Date: 2007-02-26 11:24:39 -0800 (Mon, 26 Feb 2007) $
 */
@Deprecated
public class ServletLogger extends ServletLogChute implements LogSystem
{
    @Override
    public void init(RuntimeServices rs) throws Exception
    {
        super.init(rs);
        log(LogSystem.WARN_ID, "ServletLogger has been deprecated. Use " +
                               super.getClass().getName() + " instead.");
    }

    /**
     * Send a log message from Velocity.
     */
    public void logVelocityMessage(int level, String message)
    {
        log(level, message);
    }

}

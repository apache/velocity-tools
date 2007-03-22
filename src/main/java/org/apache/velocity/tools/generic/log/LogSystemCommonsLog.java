package org.apache.velocity.tools.generic.log;

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

import org.apache.velocity.app.VelocityEngine;

/**
 * Redirects commons-logging messages to Velocity's LogSystem.
 *
 * <p>This is basically an empty subclass of LogChuteCommmonsLog that exists
 *    merely for backwards compatibility with VelocityTools 1.x. Please
 *    use LogChuteCommonsLog directly, as this will likely be removed
 *    in VelocityTools 2.1, if not earlier.
 * </p>
 *
 * @deprecated Use LogChuteCommonsLog instead
 * @version $Id$
 */
public class LogSystemCommonsLog extends LogChuteCommonsLog
{
    /**
     * @deprecated Use LogChuteCommonsLog.setVelocityLog(Log target) instead
     */
    public static void setVelocityEngine(VelocityEngine target)
    {
        setVelocityLog(target.getLog());
    }
}

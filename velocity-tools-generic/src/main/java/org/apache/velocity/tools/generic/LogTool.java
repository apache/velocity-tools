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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This tool is used to log from within templates.
 * </p>
 * <p>
 * Of course, the desired log level must have been configured, using the method specific
 * to the SLF4J implementation you are using. For instance, when using the webapp-slf4j-logger,
 * you will use the following snippet of code in you /WEB-INF/web.xml file:
 * <pre>
 * &lt;context-param&gt;
 *   &lt;param-name&gt;webapp-slf4j-logger.level&lt;/param-name&gt;
 *   &lt;param-value&gt;debug&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * </pre>
 * </p>
 *
 * <p>You can optionnaly specify the logger name in the config (the default is to re-use the engine logger):
 * <pre>
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.LogTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Id:  $
 */

@DefaultKey("log")
@ValidScope(Scope.APPLICATION)
public class LogTool
{
    public static final String LOGGER_NAME_KEY = "loggerName";
    
    private Logger LOG = null;

    /**
     * configure the logger
     */
    public void configure(ValueParser values)
    {
        String loggerName = values.getString(LOGGER_NAME_KEY);
        if (loggerName == null)
        {
            loggerName = "Velocity";
        }
        LOG = LoggerFactory.getLogger(loggerName);
    }
    
    public void error(String message)
    {
        LOG.error(message);
    }

    public void warn(String message)
    {
        LOG.warn(message);
    }

    public void info(String message)
    {
        LOG.info(message);
    }

    public void debug(String message)
    {
        LOG.debug(message);
    }

    public void trace(String message)
    {
        LOG.trace(message);
    }
}

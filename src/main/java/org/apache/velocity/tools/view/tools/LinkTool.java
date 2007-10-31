package org.apache.velocity.tools.view.tools;

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

import java.util.Map;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ViewContext;

/**
 * Use {@link org.apache.velocity.tools.view.LinkTool}
 */
@Deprecated
public class LinkTool extends org.apache.velocity.tools.view.LinkTool
{
    @Deprecated
    public static final String SELF_ABSOLUTE_KEY = "self-absolute";

    @Deprecated
    public static final String SELF_INCLUDE_PARAMETERS_KEY = "self-include-parameters";

    @Deprecated
    public void configure(Map params)
    {
        ValueParser parser = new ValueParser(params);
        Boolean selfAbsolute = parser.getBoolean(SELF_ABSOLUTE_KEY);
        if (selfAbsolute != null)
        {
            setSelfAbsolute(selfAbsolute.booleanValue());
        }
        Boolean selfParams = parser.getBoolean(SELF_INCLUDE_PARAMETERS_KEY);
        if (selfParams != null)
        {
            setSelfIncludeParameters(selfParams.booleanValue());
        }
    }

    @Deprecated
    public void init(Object obj)
    {
        if (obj instanceof ViewContext)
        {
            ViewContext ctx = (ViewContext)obj;
            setRequest(ctx.getRequest());
            setResponse(ctx.getResponse());
            setServletContext(ctx.getServletContext());
            if (ctx.getVelocityEngine() != null)
            {
                setLog(ctx.getVelocityEngine().getLog());
            }
        }
    }

    @Deprecated
    public void setXhtml(boolean useXhtml)
    {
        setXHTML(useXhtml);
    }
}

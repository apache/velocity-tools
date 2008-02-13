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
import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.tools.view.ViewContextTool;

/**
 * Use {@link org.apache.velocity.tools.view.ViewContextTool}
 */
@Deprecated
public class ContextTool extends ViewContextTool
{
    @Deprecated
    public static final String OLD_SAFE_MODE_KEY = "safe-mode";

    @Deprecated
    public void init(Object obj)
    {
        if (obj instanceof ViewContext)
        {
            ViewContext ctx = (ViewContext)obj;
            this.context = ctx.getVelocityContext();
            this.request = ctx.getRequest();
            this.session = request.getSession(false);
            this.application = ctx.getServletContext();
        }
    }

    @Override
    public void configure(Map params)
    {
        if (params != null)
        {
            // if we find a param under the old key
            Object oldSafeMode = params.get(OLD_SAFE_MODE_KEY);
            if (oldSafeMode != null)
            {
                // copy it under the new one
                params.put(SAFE_MODE_KEY, oldSafeMode);
            }
            super.configure(params);
        }
    }

}

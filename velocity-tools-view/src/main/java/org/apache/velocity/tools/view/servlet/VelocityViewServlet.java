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

import org.apache.velocity.tools.view.VelocityView;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @deprecated This class has moved to {@link org.apache.velocity.tools.view.VelocityViewServlet}
 * @version $Id: VelocityViewServlet.java 511959 2007-02-26 19:24:39Z nbubna $
 */
@Deprecated
public class VelocityViewServlet
    extends org.apache.velocity.tools.view.VelocityViewServlet
{
    /**
     * @deprecated toolbox key is now managed by {@link VelocityView}
     */
    protected static final String TOOLBOX_KEY =
        VelocityView.DEPRECATED_TOOLS_KEY;

    /**
     * @deprecated Default path is now managed by {@link VelocityView}
     */
    protected static final String DEFAULT_TOOLBOX_PATH =
        VelocityView.DEPRECATED_USER_TOOLS_PATH;

    private transient VelocityView view;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        getLog().debug(this.getClass().getName() +
                       " has been deprecated. Use " +
                       super.getClass().getName() + " instead.");
    }

    /**
     * Overrides parent to ensure each VVS instance has
     * it's own separate configuration, just like in Tools 1.x.
     */
    @Override
    protected VelocityView getVelocityView()
    {
        if (this.view == null)
        {
            this.view = new VelocityView(getServletConfig());
        }
        return this.view;
    }
}

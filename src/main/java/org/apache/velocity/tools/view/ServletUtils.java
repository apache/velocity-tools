package org.apache.velocity.tools.view;

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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>A set of utility methods for the servlet environment.</p>
 *
 * @version $Id: ServletUtils.java 471244 2006-11-04 18:34:38Z henning $
 */
public class ServletUtils
{
    public static final String VELOCITY_VIEW_KEY =
        VelocityView.class.getName();

    /**
     * Retrieves the path for the specified request regardless of
     * whether this is a direct request or an include by the
     * RequestDispatcher.
     */
    public static String getPath(HttpServletRequest request)
    {
        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following special
        // attribute holds the correct path.  See section 8.3 of the Servlet
        // 2.3 specification.
        String path = (String)request.getAttribute("javax.servlet.include.servlet_path");
        // also take into account the PathInfo stated on SRV.4.4 Request Path Elements
        String info = (String)request.getAttribute("javax.servlet.include.path_info");
        if (path == null)
        {
            path = request.getServletPath();
            info = request.getPathInfo();
        }
        if (info != null)
        {
            path += info;
        }
        return path;
    }

    /**
     * Returns the shared {@link VelocityView} for the specified
     * {@link ServletConfig}.  If one has not yet been created, it
     * will create, store it for future access, and then return it.
     */
    public static VelocityView getVelocityView(ServletConfig config)
    {
        ServletContext application = config.getServletContext();

        // check for an already initialized VelocityView to use
        VelocityView view =
            (VelocityView)application.getAttribute(VELOCITY_VIEW_KEY);
        if (view == null)
        {
            // only create a new one if we don't already have one
            view = new VelocityView(config);

            // and store it in the application attributes, so other
            // servlets, filters, or tags can use it
            application.setAttribute(VELOCITY_VIEW_KEY, view);
        }
        return view;
    }

}

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

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * General-purpose text-importing tool for templates.
 * <p>Usage: just call $import.read("http://www.foo.com/bleh.jsp?sneh=bar") to insert the contents of the named
 * resource into the template.
 * </p>
 * <p><pre>
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.ImportTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @since VelocityTools 3.0
 * @version $Id$
 */

@DefaultKey("import")
@ValidScope(Scope.REQUEST)
public class ImportTool extends SafeConfig
{
    /**
     * ImportSupport utility which provides underlying i/o
     */
    protected ImportSupport importSupport = null;

    /**
     * Importsupport initialization
     * @param config
     */
    protected void initializeImportSupport(ValueParser config)
    {
        importSupport = new ImportSupport();
        importSupport.configure(config);
    }

    /**
     * Configuration
     * @param values
     */
    protected void configure(ValueParser values)
    {
        initializeImportSupport(values);
    }

    /**
     * Returns the supplied resource rendered as a String.
     *
     * @param resource the URL to import
     * @return the URL as a string
     */
    public String read(String resource)
    {
        if (resource == null)
        {
            getLog().warn("resource is null!");
            return null;
        }
        if (resource.length() == 0)
        {
            getLog().warn("resource is empty string!");
            return null;
        }
        try
        {
            return importSupport.getResourceString(resource);
        }
        catch (Exception ex)
        {
            getLog().error("Exception while getting '{}'", resource, ex);
            return null;
        }
    }

    /**
     * Returns the supplied URL rendered as a String.
     *
     * @param url the URL to import
     * @return the URL as a string
     */
    public String fetch(String url)
    {
        if (url == null)
        {
            getLog().warn("URL is null!");
            return null;
        }
        if (url.length() == 0)
        {
            getLog().warn("URL is empty string!");
            return null;
        }
        try
        {
            return importSupport.acquireString(url);
        }
        catch (Exception ex)
        {
            getLog().error("Exception while acquiring '{}'", url, ex);
            return null;
        }
    }

}

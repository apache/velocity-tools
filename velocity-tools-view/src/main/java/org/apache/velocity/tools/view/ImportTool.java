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

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * General-purpose text-importing view tool for templates.
 * <p>Usage:<br>
 * Just call $import.read("http://www.foo.com/bleh.jsp?sneh=bar") to insert the contents of the named
 * resource into the template.
 * </p>
 * <pre>
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.ImportTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @since VelocityTools 2.0
 * @version $Revision$ $Date$
 */

@DefaultKey("import")
@ValidScope(Scope.REQUEST)
public class ImportTool extends org.apache.velocity.tools.generic.ImportTool
{
    protected synchronized void initializeImportSupport(ValueParser config)
    {
        if (importSupport == null)
        {
            importSupport = new ViewImportSupport();
            importSupport.configure(config);
        }
    }

    protected void configure(ValueParser values)
    {
        super.configure(values);
    }

    /**
     * Returns the supplied URL rendered as a String.
     *
     * @param obj the URL to import
     * @return the URL as a string
     */
    public String read(Object obj) {
        if (obj == null)
        {
            getLog().warn("URL is null!");
            return null;
        }
        String url = String.valueOf(obj).trim();
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

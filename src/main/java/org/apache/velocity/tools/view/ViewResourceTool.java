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

import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.ResourceTool;

/**
 * <p>Tool for accessing ResourceBundles and formatting messages therein.</p>
 * <p><pre>
 * Template example(s):
 *   $text.foo                      ->  bar
 *   $text.hello.world              ->  Hello World!
 *   #set( $otherText = $text.bundle('otherBundle') )
 *   $otherText.foo                 ->  woogie
 *   $otherText.bar                 ->  The args are {0} and {1}.
 *   $otherText.bar.insert(4)       ->  The args are 4 and {1}.
 *   $otherText.bar.insert(4,true)  ->  The args are 4 and true.
 *
 * Toolbox configuration example:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.ViewResourceTool"
 *              bundles="resources,com.foo.moreResources"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This comes in very handy when internationalizing templates.
 *    Note that the default resource bundle baseName is "resources", and
 *    the default locale is the result of HttpServletRequest.getLocale().
 *    The default bundle baseName can be overridden as shown above.
 * </p>
 * <p>Also, be aware that very few performance considerations have been made
 *    in this initial version.  It should do fine, but if you have performance
 *    issues, please report them to dev@velocity.apache.org, so we can make
 *    improvements.
 * </p>
 *
 * <p>This tool is NOT meant to be used in either application or
 * session scopes of a servlet environment.</p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date: 2006-11-27 10:49:37 -0800 (Mon, 27 Nov 2006) $
 * @since VelocityTools 2.0
 */
@InvalidScope({Scope.APPLICATION,Scope.SESSION})
public class ViewResourceTool extends ResourceTool
{
    /**
     * Sets the current {@link HttpServletRequest}
     *
     * @param request the {@link HttpServletRequest} to retrieve the default Locale from
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request != null)
        {
            setDefaultLocale(request.getLocale());
        }
    }

}

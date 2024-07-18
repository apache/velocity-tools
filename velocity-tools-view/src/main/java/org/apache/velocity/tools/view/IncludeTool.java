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

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.ServletContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * Allows for transparent content negotiation in a manner mimicking
 * Apache httpd's <a
 * href="http://httpd.apache.org/docs-2.0/content-negotiation.html">MultiViews</a>.
 *
 * <p>Reads the default language out of the ViewToolContext as
 * <code>org.apache.velocity.tools.view.i18n.defaultLanguage</code>.
 * See {@link #find(String, String)}, {@link
 * #find(String, Locale)} and {@link #exists(String)} for usage.</p>
 *
 * <p>This is the successor to the MultiViewsTool in VelocityTools 1.x.
 * Please note that it does NOT do the actual #include or #parse for
 * you, but is merely to aid in include content negotiation.</p>
 *
 * @version $Id$
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 */

@DefaultKey("include")
@InvalidScope(Scope.APPLICATION)
public class IncludeTool extends SafeConfig implements Serializable
{
    private static final long serialVersionUID = -1547421197271245152L;

    /**
     * The key used to search initialization, context, and JVM
     * parameters for the default language to use.
     */
    protected static final String DEFAULT_LANGUAGE_KEY =
        "org.apache.velocity.tools.view.i18n.defaultLanguage";

    /**
     * The two character abbreviation for the request's default
     * language.
     */
    protected String defaultLanguage;
    protected VelocityEngine engine;

    /**
     * Extracts the default language from the specified
     * <code>ViewContext</code>, looking first at the Velocity
     * context, then the servlet context, then lastly at the JVM
     * default.  This "narrow scope to wide scope" pattern makes it
     * easy to setup language overrides at different levels within
     * your application.
     *
     * @param params the {@link Map} of configuration parameters
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    protected void configure(ValueParser params)
    {
        configure((ViewToolContext)params.get(ViewToolContext.CONTEXT_KEY));
    }

    protected void configure(ViewToolContext ctx)
    {
        defaultLanguage = (String) ctx.get(DEFAULT_LANGUAGE_KEY);
        if (defaultLanguage == null || defaultLanguage.trim().equals(""))
        {
            ServletContext sc = ctx.getServletContext();
            defaultLanguage = (String) sc.getAttribute(DEFAULT_LANGUAGE_KEY);
            if (defaultLanguage == null || defaultLanguage.trim().equals(""))
            {
                // Use JVM default.
                defaultLanguage = Locale.getDefault().getLanguage();
            }
        }

        this.engine = ctx.getVelocityEngine();
    }

    /**
     * Calls {@link #find(String, String)} using the
     * language extracted from <code>locale</code>.
     *
     * @param name resource filename
     * @param locale locale
     * @return locallized resource filename
     * @see #find(String, String)
     */
    public String find(String name, Locale locale)
    {
        if (locale == null)
        {
            return null;
        }
        return find(name, locale.getLanguage());
    }

    /**
     * Calls {@link #find(String, String)} using the
     * default language.
     *
     * @param name resource filename
     * @return locallized resource filename
     * @see #find(String, String)
     */
    public String find(String name)
    {
        return find(name, defaultLanguage);
    }

    /**
     * <p>Finds the a localized version of the requested Velocity
     * resource (such as a file or template) which is most appropriate
     * for the locale of the current request.  Use in conjuction with
     * Apache httpd's <code>MultiViews</code>, or by itself.</p>
     *
     * <p>Usage from a template would be something like the following:</p>
     * <pre><code>
     * #parse( $include.find('header.vm', 'en') )
     * #include( $include.find('my_page.html', 'en') )
     * #parse( $include.find('footer.vm', 'en') )
     * </code></pre>
     *
     * <p>You might also wrap this method using another pull/view tool
     * which does internationalization/localization/content negation
     * for a single point of access.</p>
     *
     * @param name The unlocalized name of the file to find.
     * @param language The language to find localized context for.
     * @return The localized file name, or <code>name</code> if it is
     * not localizable.
     */
    public String find(String name, String language)
    {
        String localizedName = name + '.' + language;
        if (!exists(localizedName))
        {
            // Fall back to the default lanaguage.
            String defaultLangSuffix = '.' + defaultLanguage;
            if (localizedName.endsWith(defaultLangSuffix))
            {
                // Assume no localized version of the resource.
                localizedName = name;
            }
            else
            {
                localizedName = name + defaultLangSuffix;
                if (!exists(localizedName))
                {
                    localizedName = name;
                }
            }
        }
        return localizedName;
    }

    /**
     * <p>Checks to see whether a #parse-able template or
     * #include-able resource exists under the specified name/path.</p>
     *
     * <p>Usage from a template would be something like the following:</p>
     * <pre>
     * #if( $include.exists('header.vm') )
     *   #parse( 'header.vm' )
     * #end
     * </pre>
     *
     * @param name resource filename
     * @return whether resource exists
     * @see VelocityEngine#resourceExists
     */
    public boolean exists(String name)
    {
        try
        {
            // checks for both templates and static content
            return engine.resourceExists(name);
        }
        // make sure about this...
        catch (ResourceNotFoundException rnfe)
        {
            return false;
        }
    }

    /**
     * Checks to see whether a localized version of the
     * named template exists for the specified language.
     *
     * @param name resource filename
     * @param language asked language
     * @return whether resource exists
     * @see #exists(String)
     */
    public boolean exists(String name, String language)
    {
        String localizedName = name + '.' + language;
        return exists(localizedName);
    }

}

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.velocity.tools.view.i18n;

import java.util.Locale;
import javax.servlet.ServletContext;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ServletViewTool;

/**
 * <p>Allows for transparent content negotiation in a manner mimicking
 * Apache httpd's <a
 * href="http://httpd.apache.org/docs-2.0/content-negotiation.html">MultiViews</a></p>.
 *
 * <p>Reads the default language out of the ViewContext as
 * <code>org.apache.velocity.tools.view.i18n.defaultLanguage</code>.
 * See {@link #findLocalizedResource(String, String)} and {@link
 * #findLocalizedResource(String, Locale)} for usage.</p>
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
public class MultiViewsTool
    implements ServletViewTool
{
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

    /**
     * Creates a new uninitialized instance.  Call {@link
     * #init(ViewContext)} to initialize it.
     */
    public MultiViewsTool()
    {
    }

    /**
     * Extracts the default language from the specified
     * <code>ViewContext</code>, looking first at the Velocity
     * context, then the servlet context, then lastly at the JVM
     * default.  This "narrow scope to wide scope" pattern makes it
     * easy to setup language overrides at different levels within
     * your application.
     *
     * @param context The context to use.
     */
    protected MultiViewsTool(ViewContext context)
    {
        Context vc = context.getVelocityContext();
        defaultLanguage = (String) vc.get(DEFAULT_LANGUAGE_KEY);
        if (defaultLanguage == null || defaultLanguage.trim().equals(""))
        {
            ServletContext sc = context.getServletContext();
            defaultLanguage = (String) sc.getAttribute(DEFAULT_LANGUAGE_KEY);
            if (defaultLanguage == null || defaultLanguage.trim().equals(""))
            {
                // Use JVM default.
                defaultLanguage = Locale.getDefault().getLanguage();
            }
        }
    }

    /**
     * Calls {@link #findLocalizedResource(String, String)} using the
     * language extracted from <code>locale</code>.
     *
     * @see #findLocalizedResource(String, String)
     */
    public String findLocalizedResource(String name, Locale locale)
    {
        return findLocalizedResource(name, locale.getLanguage());
    }

    /**
     * Calls {@link #findLocalizedResource(String, String)} using the
     * default language.
     *
     * @see #findLocalizedResource(String, String)
     */
    public String findLocalizedResource(String name)
    {
        return findLocalizedResource(defaultLanguage);
    }

    /**
     * <p>Finds the a localized version of the requested Velocity
     * resource (such as a file or template) which is most appropriate
     * for the locale of the current request.  Use in conjuction with
     * Apache httpd's <code>MultiViews</code>, or by itself.</p>
     *
     * <p>Usage from a template would be something like the following:
     * <blockquote><code><pre>
     * #parse ($multiviews.findLocalizedResource("header.vm", "en"))
     * #include ($multiviews.findLocalizedResource("my_page.html", "en"))
     * #parse ($multiviews.findLocalizedResource("footer.vm", "en"))
     * </pre></code></blockquote>
     *
     * You might also wrap this method using another pull/view tool
     * which does internationalization/localization/content negation
     * for a single point of access.</p>
     *
     * @param name The unlocalized name of the file to find.
     * @param language The language to find localized context for.
     * @return The localized file name, or <code>name</code> if it is
     * not localizable.
     */
    public String findLocalizedResource(String name, String language)
    {
        String localizedName = name + '.' + language;
        // templateExists() checks for static content as well
        if (!Velocity.templateExists(localizedName))
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
                if (!Velocity.templateExists(localizedName))
                {
                    localizedName = name;
                }
            }
        }
        return localizedName;
    }

    /**
     * Unneccessary cruft required by our interface.  Hopefully this
     * method will go away soon.
     *
     * @see org.apache.velocity.tools.view.tools.ServletViewTool#getInstance(ViewContext)
     */
    public Object getInstance(ViewContext context)
    {
        return new MultiViewsTool(context);
    }

    /**
     * Denotes the global/application scope of this tool.  Note that
     * this is merely to signify that it is thread-safe, and can
     * actually be used in any scope.
     *
     * @see org.apache.velocity.tools.view.tools.ServletViewTool#getDefaultLifecycle()
     */
    public String getDefaultLifecycle()
    {
        return APPLICATION;
    }
}

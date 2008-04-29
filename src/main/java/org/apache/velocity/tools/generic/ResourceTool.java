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

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.velocity.tools.config.DefaultKey;

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
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ResourceTool"
 *              bundles="resources,com.foo.moreResources"
 *              locale="en_US"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This comes in very handy when internationalizing templates.
 *    Note that the default resource bundle baseName is "resources", and
 *    the default locale is the system locale.  These may both be overridden
 *    in your toolbox config as demonstrated above.
 * </p>
 * <p>Also, be aware that very few performance considerations have been made
 *    in this initial version.  It should do fine, but if you have performance
 *    issues, please report them to dev@velocity.apache.org, so we can make
 *    improvements.
 * </p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date: 2006-11-27 10:49:37 -0800 (Mon, 27 Nov 2006) $
 * @since VelocityTools 1.3
 */
@DefaultKey("text")
public class ResourceTool extends LocaleConfig
{
    public static final String BUNDLES_KEY = "bundles";

    private String[] bundles = new String[] { "resources" };
    private boolean deprecationSupportMode = false;


    protected final void setDefaultBundle(String bundle)
    {
        if (bundle == null)
        {
            throw new NullPointerException("Default bundle cannot be null");
        }
        this.bundles = new String[] { bundle };
    }

    protected final String getDefaultBundle()
    {
        return this.bundles[0];
    }

    @Deprecated
    protected final void setDefaultLocale(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException("Default locale cannot be null");
        }
        super.setLocale(locale);
    }

    @Deprecated
    protected final Locale getDefaultLocale()
    {
        return super.getLocale();
    }

    @Deprecated
    public void setDeprecationSupportMode(boolean depMode)
    {
        this.deprecationSupportMode = depMode;
    }


    protected void configure(ValueParser parser)
    {
        String[] bundles = parser.getStrings(BUNDLES_KEY);
        if (bundles != null)
        {
            this.bundles = bundles;
        }

        super.configure(parser);
    }


    /**
     * Accepts objects and uses their string value as the key.
     */
    public Key get(Object k)
    {
        String key = k == null ? null : String.valueOf(k);
        return get(key);
    }

    public Key get(String key)
    {
        return new Key(key, this.bundles, getLocale(), null);
    }

    public Key bundle(String bundle)
    {
        return new Key(null, new String[] { bundle }, getLocale(), null);
    }

    public Key locale(Locale locale)
    {
        return new Key(null, this.bundles, locale, null);
    }

    public Key insert(Object[] args)
    {
        return new Key(null, this.bundles, getLocale(), args);
    }

    public Key insert(List args)
    {
        return insert(args.toArray());
    }

    public Key insert(Object arg)
    {
        return insert(new Object[] { arg });
    }

    public Key insert(Object arg0, Object arg1)
    {
        return insert(new Object[] { arg0, arg1 });
    }


    /**
     * Returns the value for the specified key in the ResourceBundle for
     * the specified basename and locale.  If no such resource can be
     * found, no errors are thrown and {@code null} is returned.
     */
    public Object get(Object k, String baseName, Locale locale)
    {
        if (baseName == null || k == null)
        {
            return null;
        }
        String key = k == null ? null : String.valueOf(k);
        if (locale == null)
        {
            locale = getLocale();
        }

        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        if (bundle != null)
        {
            try
            {
                return bundle.getObject(key);
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
        return null;
    }

    /**
     * Retrieve a resource for the specified key from the first of the
     * specified bundles in which a matching resource is found.
     * If no resource is found, no exception will be thrown and {@code null}
     * will be returned.
     */
    public Object get(Object k, String[] bundles, Locale locale)
    {
        String key = k == null ? null : String.valueOf(k);
        for (int i=0; i < bundles.length; i++)
        {
            Object resource = get(key, bundles[i], locale);
            if (resource != null)
            {
                return resource;
            }
        }
        return null;
    }

    /**
     * Renders the specified resource value and arguments as a String.
     * The resource is treated as a {@link MessageFormat} pattern which
     * is used for formatting along with any specified argument values.
     * If <code>deprecationSupportMode</code> is set to true, then this
     * will return the resource directly when there are no args (as it
     * did in 1.x versions).
     */
    public String render(Object resource, Object[] args)
    {
        String value = String.valueOf(resource);
        if (deprecationSupportMode && args == null)
        {
            return value;
        }
        return MessageFormat.format(value, args);
    }



    /**
     * Internal class used to enable an elegant syntax for accessing
     * resources.
     */
    public final class Key
    {
        // these are copied and/or altered when a mutator is called
        private final String[] bundles;
        private final String key;
        private final Locale locale;
        private final Object[] args;

        // these are not copied when a mutator is called
        private boolean cached = false;
        private Object rawValue;

        public Key(String key, String[] bundles, Locale locale, Object[] args)
        {
            this.key = key;
            this.bundles = bundles;
            this.locale = locale;
            this.args = args;
        }

        // ----- mutators (these return an altered duplicate) ---

        public Key get(Object k)
        {
            return get(String.valueOf(k));
        }

        public Key get(String key)
        {
            String newKey;
            if (this.key == null)
            {
                newKey = key;
            }
            else
            {
                newKey = this.key + '.' + key;
            }
            return new Key(newKey, this.bundles, this.locale, this.args);
        }

        public Key bundle(String bundle)
        {
            String[] newBundles = new String[] { bundle };
            return new Key(this.key, newBundles, this.locale, this.args);
        }

        public Key locale(Locale locale)
        {
            return new Key(this.key, this.bundles, locale, this.args);
        }

        public Key insert(Object[] args)
        {
            Object[] newargs;
            if (this.args == null)
            {
                // we can just use the new ones
                newargs = args;
            }
            else
            {
                // create a new array to hold both the new and old args
                newargs = new Object[this.args.length + args.length];
                // copy the old args into the newargs array
                System.arraycopy(this.args, 0, newargs, 0, this.args.length);
                // copy the args to be inserted into the newargs array
                System.arraycopy(args, 0, newargs, this.args.length, args.length);
            }
            return new Key(this.key, this.bundles, this.locale, newargs);
        }

        public Key insert(List args)
        {
            return insert(args.toArray());
        }

        public Key insert(Object arg)
        {
            return insert(new Object[] { arg });
        }

        public Key insert(Object arg0, Object arg1)
        {
            return insert(new Object[] { arg0, arg1 });
        }

        // ---  accessors (these do not return a new Key) ---

        public boolean getExists()
        {
            return (getRaw() != null);
        }

        public Object getRaw()
        {
            if (!this.cached)
            {
                this.rawValue =
                    ResourceTool.this.get(this.key, this.bundles, this.locale);
                this.cached = true;
            }
            return this.rawValue;
        }

        public String toString()
        {
            if (this.key == null)
            {
                return "";
            }
            if (!getExists())
            {
                return "???"+this.key+"???";
            }
            return ResourceTool.this.render(this.rawValue, this.args);
        }
    }

}

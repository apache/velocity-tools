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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>Tool for accessing ResourceBundles and formatting messages therein.</p>
 * <p>Template example(s):</p>
 * <pre>
 *   $text.foo                      -&gt;  bar
 *   $text.hello.world              -&gt;  Hello World!
 *   $text.keys                     -&gt;  [foo, hello.world, world]
 *   #set( $otherText = $text.bundle('otherBundle') )
 *   $otherText.foo                 -&gt;  woogie
 *   $otherText.bar                 -&gt;  The args are {0} and {1}.
 *   $otherText.bar.insert(4)       -&gt;  The args are 4 and {1}.
 *   $otherText.bar.insert(4,true)  -&gt;  The args are 4 and true.
 *
 * Toolbox configuration example:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ResourceTool"
 *              bundles="resources,com.foo.moreResources"
 *              locale="en_US"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>This comes in very handy when internationalizing templates.
 *    Note that the default resource bundle baseName is "resources", and
 *    the default locale is either:<p>
 * <ul>
 *    <li>the result of HttpServletRequest.getLocale() (if used in request scope
 *          of a VelocityView app)</li>
 *    <li>the configured locale for this tool (as shown above)<li>
 *    <li>the configured locale for the toolbox this tool is in<li>
 *    <li>the configured locale for the toolbox factory managing this tool</li>
 *    <li>the system locale, if none of the above</li>
 * </ul>
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
public class ResourceTool extends LocaleConfig implements Serializable
{
    public static final String BUNDLES_KEY = "bundles";

    private String[] bundles = new String[] { "resources" };

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

    public List<String> getKeys()
    {
        return getKeys(null, this.bundles, getLocale());
    }

    public Key bundle(String bundle)
    {
        return new Key(null, new String[] { bundle }, getLocale(), null);
    }

    public Key locale(Object locale)
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
     * Retrieves the {@link ResourceBundle} for the specified baseName
     * and locale, if such exists.  If the baseName or locale is null
     * or if the locale argument cannot be converted to a {@link Locale},
     * then this will return null.
     */
    protected ResourceBundle getBundle(String baseName, Object loc)
    {
        Locale locale = (loc == null) ? getLocale() : toLocale(loc);
        if (baseName == null || locale == null)
        {
            return null;
        }
        return ResourceBundle.getBundle(baseName, locale);
    }

    /**
     * Returns the value for the specified key in the ResourceBundle for
     * the specified basename and locale.  If no such resource can be
     * found, no errors are thrown and {@code null} is returned.
     *
     * @param key the key for the requested resource
     * @param baseName the base name of the resource bundle to search
     * @param loc the locale to use
     */
    public Object get(Object key, String baseName, Object loc)
    {
        ResourceBundle bundle = getBundle(baseName, loc);
        if (key == null || bundle == null)
        {
            return null;
        }
        try
        {
            return bundle.getObject(String.valueOf(key));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Retrieve a resource for the specified key from the first of the
     * specified bundles in which a matching resource is found.
     * If no resource is found, no exception will be thrown and {@code null}
     * will be returned.
     *
     * @param k the key for the requested resource
     * @param bundles the resource bundles to search
     * @param l the locale to use
     */
    public Object get(Object k, String[] bundles, Object l)
    {
        String key = k == null ? null : String.valueOf(k);
        for (int i=0; i < bundles.length; i++)
        {
            Object resource = get(key, bundles[i], l);
            if (resource != null)
            {
                return resource;
            }
        }
        return null;
    }

    /**
     * Returns a {@link List} of the key strings in the ResourceBundle
     * with the specified baseName and locale.  If the specified prefix
     * is not null, then this will skip any keys that do not begin with
     * that prefix and trim the prefix and any subsequent '.' off of the
     * remaining ones.  If the prefix is null, then no filtering or trimming
     * will be done.
     *
     * @param prefix the prefix for the requested keys
     * @param baseName the resource bundle base name
     * @param loc the locale to use
     */
    public List<String> getKeys(String prefix, String baseName, Object loc)
    {
        ResourceBundle bundle = getBundle(baseName, loc);
        if (bundle == null)
        {
            return null;
        }
        Enumeration<String> keys = bundle.getKeys();
        if (keys == null)
        {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            if (prefix == null)
            {
                list.add(key);
            }
            else if (key.startsWith(prefix))
            {
                key = key.substring(prefix.length(), key.length());
                if (key.charAt(0) == '.')
                {
                    key = key.substring(1, key.length());
                }
                list.add(key);
            }
        }
        return list;
    }

    /**
     * Returns a {@link List} of the key strings in the specified
     * ResourceBundles.  If the specified prefix
     * is not null, then this will skip any keys that do not begin with
     * that prefix and trim the prefix and any subsequent '.' off of the
     * remaining ones.  If the prefix is null, then no filtering or trimming
     * will be done.
     *
     * @param prefix the prefix for the requested keys
     * @param bundles the resource bundles to search
     * @param loc the locale to use
     * @see #getKeys(String,String,Object)
     */
    public List<String> getKeys(String prefix, String[] bundles, Object loc)
    {
        Locale locale = (loc == null) ? getLocale() : toLocale(loc);
        if (locale == null || bundles == null || bundles.length == 0)
        {
            return null;
        }

        List<String> master = new ArrayList<String>();
        for (String bundle : bundles)
        {
            List<String> sub = getKeys(prefix, bundle, locale);
            if (sub != null)
            {
                master.addAll(sub);
            }
        }
        return master;
    }

    /**
     * Renders the specified resource value and arguments as a String.
     * The resource is treated as a {@link MessageFormat} pattern which
     * is used for formatting along with any specified argument values.
     */
    public String render(Object resource, Object[] args)
    {
        String value = String.valueOf(resource);
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
        private final Object locale;
        private final Object[] args;

        // these are not copied when a mutator is called
        private boolean cached = false;
        private Object rawValue;

        public Key(String key, String[] bundles, Object locale, Object[] args)
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

        public Key locale(Object locale)
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

        public List<String> getKeys()
        {
            return ResourceTool.this.getKeys(this.key, this.bundles, this.locale);
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

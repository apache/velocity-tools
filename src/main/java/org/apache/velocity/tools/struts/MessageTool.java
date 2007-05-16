package org.apache.velocity.tools.struts;

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

import java.util.List;
import java.util.Locale;
import org.apache.struts.util.MessageResources;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <p>View tool that provides methods to render Struts
 * application resources for internationalized text.</p>
 *
 * <p><pre>
 * Template example(s):
 *   #if( $text.greeting.exists )
 *     $text.greeting
 *   #end
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;text&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.MessageTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @since VelocityTools 1.0
 * @version $Id$
 */
@DefaultKey("text")
@ValidScope("request")
public class MessageTool extends MessageResourcesTool
{

    /**
     * Default constructor. Tool must be initialized before use.
     */
    public MessageTool() {}


    /**
     * Looks up and returns the localized message for the specified key.
     * The user's locale is consulted to determine the language of the
     * message.
     *
     * <p><pre>Example use: $text.forms.profile.title</pre></p>
     *
     * @param key message key
     */
    public TextKey get(String key)
    {
        return new TextKey(key, null, null, getLocale());
    }


    /**
     * Looks up and returns the localized message for the specified key.
     * The user's locale is consulted to determine the language of the
     * message.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     *
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     * @since VelocityTools 1.1
     */
    public String get(String key, String bundle)
    {
        return get(key, bundle, (Object[])null);
    }


    /**
     * Looks up and returns the localized message for the specified key.
     * Replacement parameters passed with <code>args</code> are
     * inserted into the message. The user's locale is consulted to
     * determine the language of the message.
     *
     * @param key message key
     * @param args replacement parameters for this message
     *
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     */
    public String get(String key, Object args[])
    {
       return get(key, null, args);
    }

    /**
     * Looks up and returns the localized message for the specified key.
     * Replacement parameters passed with <code>args</code> are
     * inserted into the message. The user's locale is consulted to
     * determine the language of the message.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     * @param args replacement parameters for this message
     * @since VelocityTools 1.1
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     */
    public String get(String key, String bundle, Object args[])
    {
        return this.get(key, bundle, args, getLocale());
    }

    /**
     * Looks up and returns the localized message for the specified key.
     * Replacement parameters passed with <code>args</code> are
     * inserted into the message.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     * @param args replacement parameters for this message
     * @param locale The locale to use for this message.
     *
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     * @since VelocityTools 1.4
     */
    public String get(String key, String bundle, Object[] args, Locale locale)
    {
        MessageResources res = getResources(bundle);
        if (res == null)
        {
            return null;
        }

        // return the requested message
        if (args == null)
        {
            return res.getMessage(locale, key);
        }
        else
        {
            return res.getMessage(locale, key, args);
        }
    }

    /**
     * Same as {@link #get(String key, Object[] args)}, but takes a
     * <code>java.util.List</code> instead of an array. This is more
     * Velocity friendly.
     *
     * @param key message key
     * @param args replacement parameters for this message
     *
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     */
    public String get(String key, List args)
    {
        return get(key, null, args);
    }

    /**
     * Same as {@link #get(String key, Object[] args)}, but takes a
     * <code>java.util.List</code> instead of an array. This is more
     * Velocity friendly.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     * @param args replacement parameters for this message
     * @since VelocityTools 1.1
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     */
    public String get(String key, String bundle, List args)
    {
        return get(key, bundle, args.toArray());
    }

    /**
     * Looks up and returns the localized message for the specified key.
     * Replacement parameters passed with <code>args</code> are
     * inserted into the message.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     * @param args replacement parameters for this message
     * @param locale The locale to use for this message.
     *
     * @since VelocityTools 1.4
     * @return the localized message for the specified key or
     * <code>null</code> if no such message exists
     */
    public String get(String key, String bundle, List args, Locale locale)
    {
        return get(key, bundle, args.toArray(), locale);
    }

    /**
     * Checks if a message string for a specified message key exists
     * for the user's locale.
     *
     * @param key message key
     *
     * @return <code>true</code> if a message strings exists,
     * <code>false</code> otherwise
     */
    public boolean exists(String key)
    {
        return exists(key, null);
    }

    /**
     * Checks if a message string for a specified message key exists
     * for the user's locale.
     *
     * @param key message key
     * @param bundle The bundle name to look for.
     * @since VelocityTools 1.1
     * @return <code>true</code> if a message strings exists,
     * <code>false</code> otherwise
     */
    public boolean exists(String key, String bundle)
    {
        MessageResources res = getResources(bundle);
        if (res == null)
        {
            return false;
        }

        // Return the requested message presence indicator
        return res.isPresent(getLocale(), key);
    }


    /**
     * Helper class to simplify tool usage when retrieving
     * no-arg messages from the default bundle that have periods
     * in their key.
     *
     * <p>So instead of <code>$text.get("forms.profile.title")</code>,1
     * you can just type <code>$text.forms.profile.title</code>. Also,
     * this lets you do things like:
     * <pre>
     *   #if( $text.forms.profile.exists )
     *      #set( $profiletext = $text.forms.profile )
     *      &lt;h1&gt;$profiletext.title&lt;/h1&gt;
     *      &lt;h3&gt;$profiletext.subtitle&lt;/h3&gt;
     *   #end
     * </pre>
     * </p>
     *
     * @since VelocityTools 1.2
     */
    public class TextKey
    {
        private String key;
        private String bundle;
        private Object[] args;
        private Locale locale;

        /**
         * @deprecated This will be removed after VelocityTools 1.4
         */
        public TextKey(MessageTool tool, String key, String bundle, Object[] args)
        {
            this(key, bundle, args, MessageTool.this.getLocale());
        }

        /**
         * @since VelocityTools 1.4
         */
        public TextKey(String key, String bundle,
                       Object[] args, Locale locale)
        {
            this.key = key;
            this.bundle = bundle;
            this.args = args;
            this.locale = locale;
        }

        /**
         * Appends a period and the new key to the current
         * key and returns a new TextKey instance with the
         * combined result as its key.
         */
        public TextKey get(String appendme)
        {
            StringBuffer sb = new StringBuffer(this.key);
            sb.append('.');
            sb.append(appendme);
            return new TextKey(sb.toString(), this.bundle, this.args, this.locale);
        }

        /**
         * Returns a new TextKey with the specified resource bundle set.
         * @since VelocityTools 1.3
         */
        public TextKey bundle(String setme)
        {
            return new TextKey(this.key, setme, this.args, this.locale);
        }

        /**
         * Returns a new TextKey with the specified resource bundle set.
         * @since VelocityTools 1.4
         */
        public TextKey locale(Locale setme)
        {
            return new TextKey(this.key, this.bundle, this.args, setme);
        }

        /**
         * Returns a new TextKey with the specified argument
         * to be inserted into the text output.  If arguments already
         * exist for this TextKey, the new arguments will be appended
         * to the old ones in the new TextKey that is returned.
         *
         * @since VelocityTools 1.3
         */
        public TextKey insert(Object addme)
        {
            return insert(new Object[] { addme });
        }

        /**
         * Returns a new TextKey with the specified arguments
         * to be inserted into the text output.  If arguments already
         * exist for this TextKey, the new arguments will be appended
         * to the old ones in the new TextKey that is returned.
         *
         * @since VelocityTools 1.3
         */
        public TextKey insert(Object addme, Object metoo)
        {
            return insert(new Object[] { addme, metoo });
        }

        /**
         * Returns a new TextKey with the specified arguments
         * to be inserted into the text output.  If arguments already
         * exist for this TextKey, the new arguments will be appended
         * to the old ones in the new TextKey that is returned.
         *
         * @since VelocityTools 1.3
         */
        public TextKey insert(Object addme, Object metoo, Object methree)
        {
            return insert(new Object[] { addme, metoo, methree });
        }

        /**
         * Returns a new TextKey with the specified List of arguments
         * to be inserted into the text output.  If arguments already
         * exist for this TextKey, the new arguments will be appended
         * to the old ones in the new TextKey that is returned.
         *
         * @since VelocityTools 1.3
         */
        public TextKey insert(List addme)
        {
            // convert the list to an array
            Object[] newargs = ((List)addme).toArray();
            return insert(newargs);
        }

        /**
         * Returns a new TextKey with the specified array of arguments
         * to be inserted into the text output.  If arguments already
         * exist for this TextKey, the new arguments will be appended
         * to the old ones in the new TextKey that is returned.
         *
         * @since VelocityTools 1.3
         */
        public TextKey insert(Object[] addme)
        {
            Object[] newargs;
            if (this.args == null)
            {
                // we can just use the ones we're adding
                newargs = addme;
            }
            else
            {
                // create the new array to hold both the new and old ones
                newargs = new Object[this.args.length + addme.length];
                // copy the old args into the newargs array
                System.arraycopy(this.args, 0, newargs, 0, this.args.length);
                // copy the args to be inserted into the newargs array
                System.arraycopy(addme, 0, newargs, this.args.length, addme.length);
            }
            // return an new TextKey
            return new TextKey(this.key, this.bundle, newargs, this.locale);
        }

        /**
         * This will return a new TextKey that has <b>no</b> arguments to
         * be inserted into the text output.
         *
         * @since VelocityTools 1.3
         */
        public TextKey clearArgs()
        {
            return new TextKey(this.key, this.bundle, null, this.locale);
        }

        /**
         * Convenience method to allow <code>$text.key.exists</code> syntax.
         * @since VelocityTools 1.3
         */
        public boolean getExists()
        {
            return exists();
        }

        /**
         * Checks for the existence of the key that we've built up.
         * @since VelocityTools 1.3
         */
        public boolean exists()
        {
            return MessageTool.this.exists(key, bundle);
        }

        /**
         * Renders the text output according to the collected key value,
         * bundle, and arguments.
         */
        public String toString()
        {
            return MessageTool.this.get(key, bundle, args, locale);
        }
    }

}

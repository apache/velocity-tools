/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.tools.struts;

import java.util.List;
import java.util.Locale;
import org.apache.struts.util.MessageResources;

/**
 * <p>View tool that provides methods to render Struts
 * application resources for internationalized text.</p>
 * 
 * <p><pre>
 * Template example(s):
 *   #if( $text.exists('greeting') )
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
 * @version $Id: MessageTool.java,v 1.14 2004/05/28 18:51:16 nbubna Exp $
 */
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
        return new TextKey(this, key);
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
        MessageResources res = getResources(bundle);
        if (res == null)
        {
            return null;
        }

        // return the requested message
        if (args == null)
        {
            return res.getMessage(this.locale, key);
        }
        else
        {
            return res.getMessage(this.locale, key, args);
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
        return get(key, args.toArray());
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
        return res.isPresent(this.locale, key);
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
     *   #set( $profiletext = $text.forms.profile )
     *   <h1>$profiletext.title</h1>
     *   <h3>$profiletext.subtitle</h3>
     * </pre>
     * </p>
     *
     * @since VelocityTools 1.2
     */
    public class TextKey
    {
        private MessageTool tool;
        private String key;

        public TextKey(MessageTool tool, String key)
        {
            this.tool = tool;
            this.key = key;
        }

        /**
         * Appends a period and the new key to the current
         * key and returns a new TextKey instance with the
         * combined result as its key.
         */
        public TextKey get(String key)
        {
            StringBuffer sb = new StringBuffer(this.key);
            sb.append('.');
            sb.append(key);
            return new TextKey(this.tool, sb.toString());
        }

        public String toString()
        {
            // don't recurse infinitely
            return tool.get(key, (Object[])null);
        }
    }

}

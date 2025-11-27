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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * Tool for working with escaping in Velocity templates.
 * It provides methods to escape outputs for Velocity, Java, JavaScript, HTML, HTTP, XML and SQL.
 * Also provides methods to render VTL characters that otherwise needs escaping.
 *
 * <p>Example uses:</p>
 * <pre>
 *  $velocity                    -&gt; Please escape $ and #!
 *  $esc.velocity($velocity)     -&gt; Please escape ${esc.d} and ${esc.h}!
 *
 *  $java                        -&gt; He didn't say, "Stop!"
 *  $esc.java($java)             -&gt; He didn't say, \"Stop!\"
 *
 *  $javascript                  -&gt; He didn't say, "Stop!"
 *  $esc.javascript($javascript) -&gt; He didn\'t say, \"Stop!\"
 *
 *  $html                        -&gt; "bread" &amp; "butter"
 *  $esc.html($html)             -&gt; &amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;
 *
 *  $xml                         -&gt; "bread" &amp; "butter"
 *  $esc.xml($xml)               -&gt; &amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;
 *
 *  $sql                         -&gt; McHale's Navy
 *  $esc.sql($sql)               -&gt; McHale''s Navy
 *
 *  $url                         -&gt; hello here &amp; there
 *  $esc.url($url)               -&gt; hello+here+%26+there
 *  $esc.unurl($esc.url($url))   -&gt; hello here &amp; there
 *
 *  $esc.dollar                  -&gt; $
 *  $esc.d                       -&gt; $
 *
 *  $esc.hash                    -&gt; #
 *  $esc.h                       -&gt; #
 *
 *  $esc.backslash               -&gt; \
 *  $esc.b                       -&gt; \
 *
 *  $esc.quote                   -&gt; "
 *  $esc.q                       -&gt; "
 *
 *  $esc.singleQuote             -&gt; '
 *  $esc.s                       -&gt; '
 *
 *  $esc.newline                 -&gt;
 *
 *  $esc.n                       -&gt;
 *
 *
 *  $esc.exclamation             -&gt; !
 *  $esc.e                       -&gt; !
 * </pre>
 * <p>Example tools.xml config (if you want to use this with VelocityView):</p>
 * <pre>
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.EscapeTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>This tool is entirely threadsafe, and has no instance members.
 * It may be used in any scope (request, session, or application).
 * </p>
 *
 * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
 * @version $Id: $
 * @since VelocityTools 1.2
 * @see StringEscapeUtils
 */

@DefaultKey("esc")
@ValidScope(Scope.APPLICATION)
public class EscapeTool extends SafeConfig implements Serializable
{
    private static final long serialVersionUID = -6063849274118412139L;

    public static final String DEFAULT_KEY = "esc";

    private String key = DEFAULT_KEY;

    /**
     * Does the actual configuration. This is protected, so
     * subclasses may share the same ValueParser and call configure
     * at any time, while preventing templates from doing so when
     * configure(Map) is locked.
     */
    protected void configure(ValueParser values)
    {
        String altkey = values.getString("key");
        if (altkey != null)
        {
            setKey(altkey);
        }
    }

    /**
     * Sets the key under which this tool has been configured.
     * @param key tool key
     * @see #velocity
     */
    protected void setKey(String key)
    {
        if (key == null)
        {
            throw new NullPointerException("EscapeTool key cannot be null");
        }
        this.key = key;
    }

    /**
     * Should return the key under which this tool has been configured.
     * The default is 'esc'.
     * @return tool key
     * @see #velocity
     */
    public String getKey()
    {
        return this.key;
    }


    /**
     * <p>Escapes the characters in a <code>String</code> using "poor man's
     * escaping" for Velocity templates by replacing all '$' characters
     * with '${esc.d}' and all '#' characters with '${esc.h}'.  This form
     * of escaping is far more reliable and consistent than using '\' to
     * escape valid references, directives and macros, though it does require
     * that you have the EscapeTool available in the context when you later
     * go to process the result returned by this method.
     * </p><p>
     * <b>NOTE</b>: This will only work so long as the EscapeTool is placed
     * in the context using its default key 'esc' <i>or</i> you are using
     * VelocityTools 2.0+ and have put this tool in one of your toolboxes
     * under an alternate key (in which case the EscapeTool will automatically
     * be told what its new key is).  If for some strange reason you wish
     * to use an alternate key and are not using the tool management facilities
     * of VelocityTools 2.0+, you must subclass this tool and manually call
     * setKey(String) before using this method.
     * </p>
     *
     * @param obj the string value that needs escaping
     * @return String with escaped values, <code>null</code> if null string input
     */
    public String velocity(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        String string = String.valueOf(obj);
        // must escape $ first, so we don't escape our hash escapes!
        return string.replace("$", "${"+getKey()+".d}")
                     .replace("#", "${"+getKey()+".h}");
    }

    /**
     * The characters to escape for markdown.  Backslash must be first.
     */
    private static final String[] MARKDOWN_CHARS = charParser("\\`*_{}[]<>()#+-.!|");

    /**
     * The characters to escape for APT (Almost Plain Text). Backslash must be first.
     */
    private static final String[] APT_CHARS = charParser("\\~=-+*[]<>{}");

    /**
     * A method to convert a string int an array single character strings.
     * @param charText the string of characters to convert.
     * @return an array of single character strings.
     */
    private static String[] charParser(final String charText) {
        final char[] chars = charText.toCharArray();
        final String[] result = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = String.valueOf(chars[i]);
        }
        return result;
    }

    /**
     * Escapes a text string based on an array of strings.  Every matched pattern is
     * escaped with a "\".
     *
     * @param text The text to escape.
     * @param chars The characters (or strings) to escape.
     * @return the escaped string.
     */
    private String escape(final String text, final String[] chars) {
        if (text == null) {
            return "";
        }
        String result = text;
        for (final String chrStr : chars) {
            result = result.replace(chrStr, "\\" + chrStr);
        }
        return result;
    }

    /**
     * Escapes a string for markdown.
     *
     * @param text The text to escape.
     * @return the text with the markdown specific characters escaped.
     */
    public String markdown(final String text) {
        return escape(text, MARKDOWN_CHARS);
    }

    /**
     * Escapes a string for APT (almost plain text).
     *
     * @param text The text to escape.
     * @return the text with the APT specific characters escaped.
     */
    public String apt(final String text) {
        return escape(text, APT_CHARS);
    }

    /**
     * <p>Escapes the characters in a <code>String</code> using Java String rules.</p>
     * <p>Delegates the process to {@link StringEscapeUtils#escapeJava(String)}.</p>
     *
     * @param string the string to escape values, may be null
     * @return String with escaped values, <code>null</code> if null string input
     *
     * @see StringEscapeUtils#escapeJava(String)
     */
    public String java(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeJava(String.valueOf(string));
    }

    /**
     * Escapes the characters in a <code>String</code> using java.util.Properties rules for escaping property keys.
     *
     * @param string the string to escape values, may be null
     * @return String with escaped values, <code>null</code> if null string input
     * @see #dumpString(String, boolean)
     */
    public String propertyKey(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return dumpString(String.valueOf(string), true);
    }

    /**
     * Escapes the characters in a <code>String</code> using java.util.Properties rules for escaping property values.
     *
     * @param string the string to escape values, may be null
     * @return String with escaped values, <code>null</code> if null string input
     * @see #dumpString(String, boolean)
     */
    public String propertyValue(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return dumpString(String.valueOf(string), false);
    }

    /**
     * This code was pulled from the Apache Harmony project.  See
     * https://svn.apache.org/repos/asf/harmony/enhanced/classlib/trunk/modules/luni/src/main/java/java/util/Properties.java
     * @param string property key or property value
     * @param key <code>true</code> for a property key
     * @return escaped string
     */
    protected String dumpString(String string, boolean key) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            builder.append("\\ "); //$NON-NLS-1$
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\t':
                    builder.append("\\t"); //$NON-NLS-1$
                    break;
                case '\n':
                    builder.append("\\n"); //$NON-NLS-1$
                    break;
                case '\f':
                    builder.append("\\f"); //$NON-NLS-1$
                    break;
                case '\r':
                    builder.append("\\r"); //$NON-NLS-1$
                    break;
                default:
                    if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
                        builder.append('\\');
                    }
                    if (ch >= ' ' && ch <= '~') {
                        builder.append(ch);
                    } else {
                        String hex = Integer.toHexString(ch);
                        builder.append("\\u"); //$NON-NLS-1$
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            builder.append("0"); //$NON-NLS-1$
                        }
                        builder.append(hex);
                }
            }
        }
        return builder.toString();
    }

    /**
     * <p>Escapes the characters in a <code>String</code> using JavaScript String rules.</p>
     * <p>Delegates the process to {@link StringEscapeUtils#escapeEcmaScript(String)}.</p>
     *
     * @param string the string to escape values, may be null
     * @return String with escaped values, <code>null</code> if null string input
     *
     * @see StringEscapeUtils#escapeEcmaScript(String)
     */
    public String javascript(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeEcmaScript(String.valueOf(string));
    }

    /**
     * <p>Escapes the characters in a <code>String</code> using HTML entities.</p>
     * <p>Delegates the process to {@link StringEscapeUtils#escapeHtml4(String)}.</p>
     *
     * @param string the string to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     *
     * @see StringEscapeUtils#escapeHtml4(String)
     */
    public String html(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(String.valueOf(string));
    }

    /**
     * <p>Escape the characters in a <code>String</code> to be suitable to use as an HTTP parameter value.</p>
     * <p>Uses UTF-8 as default character encoding.</p>
     * @param string the string to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     *
     * See java.net.URLEncoder#encode(String,String)
     * @since VelocityTools 1.3
     */
    public String url(Object string) {
        if (string == null) {
            return null;
        }
        try {
            return URLEncoder.encode(String.valueOf(string),"UTF-8");
        } catch(UnsupportedEncodingException uee) {
            return null;
        }
    }

    /**
     * <p>Unscape the characters in a <code>String</code> encoded as an HTTP parameter value.</p>
     * <p>Uses UTF-8 as default character encoding.</p>
     * @param string the string to unescape, may be null
     * @return a new unescaped <code>String</code>, <code>null</code> if null string input
     *
     * @see java.net.URLDecoder#decode(String,String)
     * @since VelocityTools 3.0
     */
    public String unurl(Object string) {
        if (string == null) {
            return null;
        }
        try {
            return URLDecoder.decode(String.valueOf(string),"UTF-8");
        } catch(UnsupportedEncodingException uee) {
            return null;
        }
    }

    /**
     * <p>Escapes the characters in a <code>String</code> using XML entities.</p>
     * <p>Delegates the process to {@link StringEscapeUtils#escapeXml(java.lang.String)}.</p>
     *
     * @param string the string to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     *
     * @see StringEscapeUtils#escapeXml(String)
     */
    public String xml(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeXml10(String.valueOf(string));
    }

    /**
     * <p>Escapes the characters in a <code>String</code> to be suitable to pass to an SQL query.</p>
     * <p>It boils down to doubling single quotes.</p>
     *
     * @param string the string to escape, may be null
     * @return a new String, escaped for SQL, <code>null</code> if null string input
     *
     */
    public String sql(Object string)
    {
        if (string == null)
        {
            return null;
        }
        return String.valueOf(string).replace("'", "''");
    }

    /**
     * <p>Converts the specified Unicode code point and/or escape sequence into
     * the associated Unicode character.  This allows numeric
     * code points or String versions of the numeric code point to be correctly
     * translated within a template.  This is especially useful for those
     * creating unicode from a reference value, or injecting a unicode character
     * into a template with a version of Velocity prior to 1.6.</p>
     * @param code the code to be translated/escaped, may be null
     * @return the unicode character for that code, {@code null} if input was null
     * @see Character#toChars(int codePoint)
     */
    public String unicode(Object code)
    {
        if (code == null)
        {
            return null;
        }

        String s = String.valueOf(code);
        if (s.startsWith("\\u"))
        {
            s = s.substring(2, s.length());
        }
        int codePoint = Integer.valueOf(s, 16);
        return String.valueOf(Character.toChars(codePoint));
    }


    /**
     * Renders a dollar sign ($).
     * @return a dollar sign ($).
     * @see #getD()
     */
    public String getDollar()
    {
        return "$";
    }

    /**
     * Renders a dollar sign ($).
     * @return a dollar sign ($).
     * @see #getDollar()
     */
    public String getD()
    {
        return this.getDollar();
    }

    /**
     * Renders a hash (#).
     * @return a hash (#).
     * @see #getH()
     */
    public String getHash()
    {
        return "#";
    }

    /**
     * Renders a hash (#).
     * @return a hash (#).
     * @see #getHash()
     */
    public String getH()
    {
        return this.getHash();
    }

    /**
     * Renders a backslash (\).
     * @return a backslash (\).
     * @see #getB()
     */
    public String getBackslash()
    {
        return "\\";
    }

    /**
     * Renders a backslash (\).
     * @return a backslash (\).
     * @see #getBackslash()
     */
    public String getB()
    {
        return this.getBackslash();
    }

    /**
     * Renders a double quotation mark (").
     * @return a double quotation mark (").
     * @see #getQ()
     */
    public String getQuote()
    {
        return "\"";
    }

    /**
     * Renders a double quotation mark (").
     * @return a double quotation mark (").
     * @see #getQuote()
     */
    public String getQ()
    {
        return this.getQuote();
    }

    /**
     * Renders a single quotation mark (').
     * @return a single quotation mark (').
     * @see #getS()
     */
    public String getSingleQuote()
    {
        return "'";
    }

    /**
     * Renders a single quotation mark (').
     * @return a single quotation mark (').
     * @see #getSingleQuote()
     */
    public String getS()
    {
        return this.getSingleQuote();
    }

    /**
     * Renders a new line character appropriate for the
     * operating system ("\n" in java).
     * @return system newline string
     * @see #getN()
     */
    public String getNewline()
    {
        return "\n";
    }

    /**
     * Renders a new line character appropriate for the
     * operating system ("\n" in java).
     * @return system newline string
     * @see #getNewline()
     */
    public String getN()
    {
        return this.getNewline();
    }

    /**
     * Renders an exclamation mark (!).
     * @return an exclamation mark (!).
     * @see #getE()
     */
    public String getExclamation()
    {
        return "!";
    }

    /**
     * Renders an exclamation mark (!).
     * @return an exclamation mark (!).
     * @see #getExclamation()
     */
    public String getE()
    {
        return this.getExclamation();
    }

}

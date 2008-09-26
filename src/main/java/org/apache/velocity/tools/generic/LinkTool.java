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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <p>The LinkTool provides many methods to work with URIs and can help you:
 * <ul>
 *     <li>construct full URIs (opaque, absolute or relative)</li>
 *     <li>encode and decode URLs (part or whole)</li>
 *     <li>retrieve path info for the current request</li>
 *     <li>and more..</li>
 * </ul></p>
 *
 * <p>This GenericTools (i.e. non-servlet based) version of LinkTool
 * is largely based upon the same API and behavior as the older
 * VelocityView version, with a few differences, particularly in
 * internal representation and query handling.  You can expect that
 * in the future work will be done to more closely align the APIs.
 * It is likely that the VelocityView version will become a subclass
 * of this version that adds on servlet-awareness and related features.
 * For now, though, they are entirely separate but similar tools.
 * </p>
 *
 * <p>The LinkTool is somewhat special in that nearly all public methods return
 * a new instance of LinkTool. This facilitates greatly the repeated use
 * of the LinkTool in Velocity and leads to an elegant syntax.</p>
 * 
 * <p><pre>
 * Template example(s):
 *   #set( $base = $link.relative('MyPage.vm').anchor('view') )
 *   &lt;a href="$base.param('select','this')"&gt;this&lt;/a&gt;
 *   &lt;a href="$base.param('select','that')"&gt;that&lt;/a&gt;
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.LinkTool"
 *              uri="http://velocity.apache.org/tools/devel/"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id: LinkTool.java 601976 2007-12-07 03:50:51Z nbubna $
 */
@DefaultKey("link")
@SkipSetters
@ValidScope(Scope.REQUEST)
public class LinkTool extends SafeConfig implements Cloneable
{
    /** Standard HTML delimiter for query data ('&') */
    public static final String HTML_QUERY_DELIMITER = "&";

    /** XHTML delimiter for query data ('&amp;amp;') */
    public static final String XHTML_QUERY_DELIMITER = "&amp;";

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_SCHEME = "http";
    public static final String SECURE_SCHEME = "https";

    public static final String URI_KEY = "uri";
    public static final String SCHEME_KEY = "scheme";
    public static final String USER_KEY = "user";
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String PATH_KEY = ToolContext.PATH_KEY;
    public static final String QUERY_KEY = "params";
    public static final String FRAGMENT_KEY = "anchor";
    public static final String CHARSET_KEY = "charset";
    public static final String XHTML_MODE_KEY = "xhtml";

    protected Log LOG;
    protected String scheme;
    protected String user;
    protected String host;
    protected int port;
    protected String path;
    protected String query;
    protected String fragment;
    protected String charset;
    protected String queryDelim;

    private boolean opaque;
    private final LinkTool self;


    /**
     * Default constructor. Tool typically is configured before use.
     */
    public LinkTool()
    {
        scheme = null;
        user = null;
        host = null;
        port = -1;
        path = null;
        query = null;
        fragment = null;
        charset = DEFAULT_CHARSET;
        queryDelim = XHTML_QUERY_DELIMITER;
        opaque = false;
        self = this;
    }

    protected final void debug(String msg, Throwable t, Object... args)
    {
        if (LOG != null && LOG.isDebugEnabled())
        {
            LOG.debug("LinkTool: "+String.format(msg, args), t);
        }
    }


    // --------------------------------------- Setup Methods -------------

    protected void configure(ValueParser props)
    {
        this.LOG = (Log)props.getValue(ToolContext.LOG_KEY);

        String link = props.getString(URI_KEY);
        if (link != null)
        {
            setFromURI(link);
        }

        String schm = props.getString(SCHEME_KEY);
        if (schm != null)
        {
            setScheme(schm);
        }
        String info = props.getString(USER_KEY);
        if (info != null)
        {
            setUserInfo(info);
        }
        String hst = props.getString(HOST_KEY);
        if (hst != null)
        {
            setHost(hst);
        }
        Integer prt = props.getInteger(PORT_KEY);
        if (prt != null)
        {
            setPort(prt.intValue());
        }
        String pth = props.getString(PATH_KEY);
        if (pth != null)
        {
            setPath(pth);
        }
        String params = props.getString(QUERY_KEY);
        if (params != null)
        {
            setQuery(params);
        }
        String anchor = props.getString(FRAGMENT_KEY);
        if (anchor != null)
        {
            setFragment(anchor);
        }

        String chrst = props.getString(CHARSET_KEY);
        if (chrst != null)
        {
            setCharacterEncoding(chrst);
        }

        Boolean xhtml = props.getBoolean(XHTML_MODE_KEY);
        if (xhtml != null)
        {
            setXHTML(xhtml);
        }
    }

    /**
     * Equivalent to clone, but with no checked exceptions.
     * If for some unfathomable reason clone() doesn't work,
     * this will throw a RuntimeException.
     */
    protected LinkTool duplicate()
    {
        try
        {
            return (LinkTool)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            String msg = "Could not properly clone " + getClass();
            if (LOG != null)
            {
                LOG.error(msg, e);
            }
            throw new RuntimeException(msg, e);
        }
    }

    public void setCharacterEncoding(String chrst)
    {
        this.charset = chrst;
    }

    /**
     * <p>Controls the delimiter used for separating query data pairs.
     *    By default, the standard '&' character is used.</p>
     * <p>This is not exposed to templates as this decision is best not
     *    made at that level.</p>
     * <p>Subclasses may easily override the init() method to set this
     *    appropriately and then call super.init()</p>
     *
     * @param xhtml if true, the XHTML query data delimiter ('&amp;amp;')
     *        will be used.  if false, then '&' will be used.
     * @see <a href="http://www.w3.org/TR/xhtml1/#C_12">Using Ampersands in Attribute Values (and Elsewhere)</a>
     */
    public void setXHTML(boolean xhtml)
    {
        queryDelim = (xhtml) ? XHTML_QUERY_DELIMITER : HTML_QUERY_DELIMITER;
    }

    /**
     * This will treat empty strings like null values
     * and will trim any trailing ':' character.
     */
    public void setScheme(Object obj)
    {
        if (obj == null)
        {
            this.scheme = null;
        }
        else
        {
            this.scheme = String.valueOf(obj);
            if (scheme.length() == 0)
            {
                this.scheme = null;
            }
            if (scheme.endsWith(":"))
            {
                this.scheme = scheme.substring(0, scheme.length() - 1);
            }
        }
    }

    public void setUserInfo(Object obj)
    {
        this.user = obj == null ? null : String.valueOf(obj);
    }

    public void setHost(Object obj)
    {
        this.host = obj == null ? null : String.valueOf(obj);
    }

    /**
     * If the specified object is null, this will set the port value
     * to -1 to indicate that.  If it is non-null and cannot be converted
     * to an integer, then it will be set to -2 to indicate an error.
     */
    public void setPort(Object obj)
    {
        if (obj == null)
        {
            this.port = -1;
        }
        else if (obj instanceof Number)
        {
            this.port = ((Number)obj).intValue();
        }
        else
        {
            try
            {
                this.port = Integer.parseInt(String.valueOf(obj));
            }
            catch (NumberFormatException nfe)
            {
                debug("Could convert '%s' to int", nfe, obj);
                this.port = -2; // use this to mean error
            }
        }
    }

    /**
     * If this instance is not opaque and the specified value does
     * not start with a '/' character, then that will be prepended
     * automatically.
     */
    public void setPath(Object obj)
    {
        if (obj == null)
        {
            this.path = null;
        }
        else
        {
            this.path = String.valueOf(obj);
            if (!this.opaque && !path.startsWith("/"))
            {
                this.path = '/' + this.path;
            }
        }
    }

    /**
     * Uses {@link #combinePath} to add the specified value
     * to the current {@link #getPath} value.  If the specified
     * value is null or this instance is opaque, then this is
     * a no-op.
     */
    public void appendPath(Object obj)
    {
        if (obj != null && !this.opaque)
        {
            setPath(combinePath(getPath(), String.valueOf(obj)));
        }
    }

    /**
     * If end is null, this will return start and vice versa.
     * If neither is null, this will append the end to the start,
     * making sure that there is only one '/' character between
     * the two values.
     */
    protected String combinePath(String start, String end)
    {
        if (end == null)
        {
            return start;
        }
        if (start == null)
        {
            return end;
        }

        // make sure we don't get // or nothing between start and end
        boolean startEnds = start.endsWith("/");
        boolean endStarts = end.startsWith("/");
        if (startEnds ^ endStarts) //one
        {
            return start + end;
        }
        else if (startEnds & endStarts) //both
        {
            return start + end.substring(1, end.length());
        }
        else //neither
        {
            return start + '/' + end;
        }
    }

    /**
     * This will trim any '?' character at the start of the
     * specified value.  It will also check the value to see if
     * it already includes multiple query pairs by checking for
     * any '&amp;' characters in the string.  If there are some, then
     * the delimiters between the pairs will all be replaced
     * with this instance's configured query delimiter.
     */
    public void setQuery(Object obj)
    {
        if (obj == null)
        {
            this.query = null;
        }
        else
        {
            this.query = String.valueOf(obj);
            if (query.startsWith("?"))
            {
                this.query = query.substring(1, query.length());
            }
            // if we have multiple pairs...
            if (query.contains("&"))
            {
                // ensure the delimeters match the xhtml setting
                // this impl is not at all efficient, but it's easy
                this.query = query.replaceAll("&(amp;)?", queryDelim);
            }
        }
    }

    /**
     * Converts the map of keys to values into a query string
     * and uses {@link #appendQuery(Object)} to add it to the
     * current {@link #getQuery} value.
     */
    public void appendQuery(Map parameters)
    {
        StringBuilder query = new StringBuilder();
        for (Object e : parameters.entrySet())
        {
            Map.Entry entry = (Map.Entry)e;
            //add new pair to this LinkTool's query data
            if (query.length() > 0)
            {
                query.append(queryDelim);
            }
            query.append(toQuery(entry.getKey(), entry.getValue()));
        }
        appendQuery(query);
    }

    /**
     * Uses {@link #combineQuery} to append the specified value
     * to the current {@link #getQuery} value.
     */
    public void appendQuery(Object obj)
    {
        if (obj != null)
        {
            setQuery(combineQuery(getQuery(), String.valueOf(obj)));
        }
    }

    /**
     * If the second param is null or empty, this will simply return the first
     * and vice versa.  Otherwise, it will trim any '?'
     * at the start of the second param and any '&amp;' or '&amp;amp;' at the
     * end of the first one, then combine the two, making sure that they
     * are separated by only one delimiter.
     */
    protected String combineQuery(String current, String add)
    {
        if (add == null || add.length() == 0)
        {
            return current;
        }
        if (add.startsWith("?"))
        {
            add = add.substring(1, add.length());
        }
        if (current == null || current.length() == 0)
        {
            return add;
        }
        if (current.endsWith(queryDelim))
        {
            current = current.substring(0, current.length() - queryDelim.length());
        }
        else if (current.endsWith("&"))
        {
            current = current.substring(0, current.length() - 1);
        }
        if (add.startsWith(queryDelim))
        {
            return current + add;
        }
        else if (add.startsWith("&"))
        {
            // drop the html delim in favor of the xhtml one
            add = add.substring(1, add.length());
        }
        return current + queryDelim + add;
    }

    /**
     * Turns the specified key and value into a properly encoded
     * query pair string.  If the value is an array or List, then
     * this will create a delimited string of query pairs, reusing 
     * the same key for each of the values separately.
     */
    protected String toQuery(Object key, Object value)
    {
        StringBuilder out = new StringBuilder();
        if (value == null)
        {
            out.append(encode(key));
            out.append('=');
            /* Interpret null as "no value" */
        }
        else if (value instanceof List)
        {
            appendAsArray(out, key, ((List)value).toArray());
        }
        else if (value instanceof Object[])
        {
            appendAsArray(out, key, (Object[])value);
        }
        else
        {
            out.append(encode(key));
            out.append('=');
            out.append(encode(value));
        }
        return out.toString();
    }

    /* Utility method to avoid logic duplication in toQuery() */
    protected void appendAsArray(StringBuilder out, Object key, Object[] arr)
    {
        String encKey = encode(key);
        for (int i=0; i < arr.length; i++)
        {
            out.append(encKey);
            out.append('=');
            if (arr[i] != null)
            {
                out.append(encode(arr[i]));
            }
            if (i+1 < arr.length)
            {
                out.append(queryDelim);
            }
        }
    }

    /**
     * Uses {@link #parseQuery(String,String)} to parse the specified
     * query string using this instance's current query delimiter.
     */
    protected Map<String,Object> parseQuery(String query)
    {
        return parseQuery(query, this.queryDelim);
    }

    /**
     * This will use the specified query delimiter to parse the specified
     * query string into a map of keys to values.
     * If there are multiple query pairs in the string that have the same
     * key, then the values will be combined into a single List value
     * associated with that key.
     */
    protected Map<String,Object> parseQuery(String query, String queryDelim)
    {
        if (query.startsWith("?"))
        {
            query = query.substring(1, query.length());
        }
        String[] pairs = query.split(queryDelim);
        if (pairs.length == 0)
        {
            return null;
        }
        Map<String,Object> params = new LinkedHashMap<String,Object>(pairs.length);
        for (String pair : pairs)
        {
            String[] kv = pair.split("=");
            String key = kv[0];
            Object value = kv.length > 1 ? kv[1] : null;
            if (params.containsKey(kv[0]))
            {
                Object oldval = params.get(key);
                if (oldval instanceof List)
                {
                    ((List)oldval).add((String)value);
                    value = oldval;
                }
                else
                {
                    List<String> list = new ArrayList<String>();
                    list.add((String)oldval);
                    list.add((String)value);
                    value = list;
                }
            }
            params.put(key, value);
        }
        return params;
    }

    /**
     * Sets the anchor for this instance and treats empty strings like null.
     */
    public void setFragment(Object obj)
    {
        if (obj == null)
        {
            this.fragment = null;
        }
        else
        {
            this.fragment = String.valueOf(obj);
            if (this.fragment.length() == 0)
            {
                this.fragment = null;
            }
        }
    }

    /**
     * If the specified value is null, this will set the scheme, userInfo,
     * host, port, path, query, and fragment all to their null-equivalent
     * values.  Otherwise, this will
     * convert the specified object into a {@link URI}, then those same
     * values from the URI object to this instance.
     */
    protected boolean setFromURI(Object obj)
    {
        if (obj == null)
        {
            // clear everything out...
            setScheme(null);
            setUserInfo(null);
            setHost(null);
            setPort(null);
            setPath(null);
            setQuery(null);
            setFragment(null);
            return true;
        }

        URI uri;
        if (obj instanceof URI)
        {
            uri = (URI)obj;
        }
        else
        {
            try
            {
                uri = new URI(String.valueOf(obj));
            }
            catch (Exception e)
            {
                debug("Could convert '%s' to URI", e, obj);
                return false;
            }
        }
        setScheme(uri.getScheme());
        if (uri.isOpaque())
        {
            this.opaque = true;
            // path is used as scheme-specific part
            setPath(uri.getSchemeSpecificPart());
        }
        else
        {
            setUserInfo(uri.getUserInfo());
            setHost(uri.getHost());
            setPort(uri.getPort());
            String pth = uri.getPath();
            if (pth.equals("/") || pth.length() == 0)
            {
                pth = null;
            }
            setPath(pth);
            setQuery(uri.getQuery());
        }
        setFragment(uri.getFragment());
        return true;
    }

    /**
     * Tries to create a URI from the current port, opacity, scheme,
     * userInfo, host, path, query and fragment set for this instance,
     * using the {@link URI} constructor that is appropriate to the opacity.
     */
    protected URI createURI()
    {
        try
        {
            // fail if there was an error in setting the port
            if (port > -2)
            {
                String anchor = this.fragment;
                if (anchor != null)
                {
                    anchor = encode(anchor);
                }
                if (opaque)
                {
                    // path is used as scheme-specific part
                    return new URI(scheme, path, anchor);
                }
                else
                {
                    // only create the URI if we have some values besides a port
                    if (scheme == null && user == null && host == null
                        && path == null && query == null && fragment == null)
                    {
                        return null;
                    }
                    return new URI(scheme, user, host, port, path, query, fragment);
                }
            }
        }
        catch (Exception e)
        {
            debug("Could not create URI", e);
        }
        return null;
    }

    // --------------------------------------------- Template Methods -----------

    /**
     * Returns the configured charset used by the {@link #encode} and
     * {@link #decode} methods.
     */
    public String getCharacterEncoding()
    {
        return this.charset;
    }

    /**
     * Returns true if the query delimiter used by this instance is
     * using <code>&amp;amp;</code> as the delimiter for query data pairs
     * or just using <code>&amp;</code>.
     */
    public boolean isXHTML()
    {
        return queryDelim.equals(XHTML_QUERY_DELIMITER);
    }


    /**
     * Returns a new instance with the specified value set as its scheme.
     */
    public LinkTool scheme(Object scheme)
    {
        LinkTool copy = duplicate();
        copy.setScheme(scheme);
        return copy;
    }

    /**
     * Returns a new instance with the scheme set to "https".
     */
    public LinkTool secure()
    {
        return scheme(SECURE_SCHEME);
    }

    /**
     * Returns a new instance with the scheme set to "http".
     */
    public LinkTool insecure()
    {
        return scheme(DEFAULT_SCHEME);
    }

    /**
     * Return the scheme value for this instance.
     */
    public String getScheme()
    {
        return scheme;
    }

    /**
     * Returns true if this instance's scheme is "https".
     */
    public boolean isSecure()
    {
        return SECURE_SCHEME.equalsIgnoreCase(getScheme());
    }

    /**
     * Returns true if this instance has a scheme value.
     */
    public boolean isAbsolute()
    {
        if (this.scheme == null)
        {
            return false;
        }
        return true;
    }

    /**
     * Returns true if this instance represents an opaque URI.
     * @see URI
     */
    public boolean isOpaque()
    {
        return this.opaque;
    }

    /**
     * Returns a new instance with the specified value
     * set as its user info.
     */
    public LinkTool user(Object info)
    {
        LinkTool copy = duplicate();
        copy.setUserInfo(info);
        return copy;
    }

    /**
     * Returns the {@link URI#getUserInfo()} value for this instance.
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * Returns a new instance with the specified value set as its
     * host.  If no scheme has yet been set, the new instance will
     * also have its scheme set to the {@link #DEFAULT_SCHEME} (http).
     */
    public LinkTool host(Object host)
    {
        LinkTool copy = duplicate();
        copy.setHost(host);
        // if we have host but no scheme
        if (copy.getHost() != null && !copy.isAbsolute())
        {
            // use default scheme
            copy.setScheme(DEFAULT_SCHEME);
        }
        return copy;
    }

    /**
     * Return the host value for this instance.
     */
    public String getHost()
    {
        return this.host;
    }

    /**
     * Returns a new instance with the specified value set
     * as its port number.  If the value cannot be parsed into
     * an integer, the returned instance will always return
     * null for {@link #toString} and other
     * {@link #createURI}-dependent methods to alert the user
     * to the error.
     */
    public LinkTool port(Object port)
    {
        LinkTool copy = duplicate();
        copy.setPort(port);
        return copy;
    }

    /**
     * Returns the  port value, if any.
     */
    public Integer getPort()
    {
        if (this.port < 0)
        {
            return null;
        }
        return this.port;
    }

    /**
     * Returns a new instance with the specified value
     * set as its path.
     */
    public LinkTool path(Object pth)
    {
        LinkTool copy = duplicate();
        copy.setPath(pth);
        return copy;
    }

    /**
     * Returns the current path value for this instance.
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * Appends the given value to the end of the current
     * path value.
     */
    public LinkTool append(Object pth)
    {
        LinkTool copy = duplicate();
        copy.appendPath(pth);
        return copy;
    }

    /**
     * At this level, this method returns all "directories"
     * in the set {@link #getPath()} value, by just trimming
     * of the last "/" and all that follows.
     */
    public String getContextPath()
    {
        if (this.path == null || this.opaque)
        {
            return null;
        }
        int lastSlash = this.path.lastIndexOf('/');
        if (lastSlash <= 0)
        {
            return "";
        }
        return this.path.substring(0, lastSlash);
    }

    /**
     * At this level, this method returns the last section
     * of the path, from the final "/" onward.
     */
    public String getRequestPath()
    {
        if (this.path == null || this.opaque)
        {
            return null;
        }
        int lastSlash = this.path.lastIndexOf('/');
        if (lastSlash <= 0)
        {
            return this.path;
        }
        return this.path.substring(lastSlash, this.path.length());
    }

    /**
     * Returns the "root" for this URI, if it has one.
     * This does not stick close to URI dogma and will
     * try to insert the default scheme if there is none,
     * and will return null if there is no host or if there
     * was an error when the port value was last set. It will
     * return null for any opaque URLs as well, as those have
     * no host or port.
     */
    public String getRoot()
    {
        if (host == null || opaque || port == -2)
        {
            return null;
        }
        if (scheme == null)
        {
            scheme = DEFAULT_SCHEME;
        }
        StringBuilder out = new StringBuilder();
        out.append(scheme);
        out.append("://");
        out.append(host);
        // if we have a port that's not a default for the scheme
        if (port >= 0 &&
            ((scheme.equals(DEFAULT_SCHEME) && port != 80) ||
            (isSecure() && port != 443)))
        {
            out.append(':');
            out.append(port);
        }
        return out.toString();
    }
        

    /**
     * <p>Returns the URI that addresses the current "directory"
     * of this instance. This string does not end with a "/" and
     * is essentially a concatenation of {@link #getRoot} and
     * {@link #getContextPath}</p>
     */
    public String getContextURL()
    {
        String root = getRoot();
        if (root == null)
        {
            return null;
        }
        return combinePath(root, getContextPath());
    }

    /**
     * <p>Returns a copy of the link with the specified context-relative
     * URI reference converted to a server-relative URI reference. This
     * method will overwrite any previous URI reference settings but will
     * copy the query string.</p>
     *
     * Example:<br>
     * <code>&lt;a href='$link.relative("/templates/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like</br>
     * <code>&lt;a href="/myapp/templates/login/index.vm"&gt;Login Page&lt;/a&gt;</code><br>
     *
     * @param obj A context-relative URI reference. A context-relative URI
     *        is a URI that is relative to the root of this web application.
     * @return a new instance of LinkTool with the specified URI
     */
    public LinkTool relative(Object obj)
    {
        if (obj == null)
        {
            return path(getContextPath());
        }
        String pth = String.valueOf(obj);
        LinkTool copy = duplicate();
        // prepend relative paths with context path
        copy.setPath(combinePath(getContextPath(), pth));
        return copy;
    }

    /**
     * <p>Returns a copy of the link with the specified URI reference
     * either used as or converted to an absolute (non-relative)
     * URI reference. This method will overwrite any previous URI
     * reference settings but will copy the query string.</p>
     *
     * Example:<br>
     * <code>&lt;a href='$link.absolute("/templates/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like<br/>
     * <code>&lt;a href="http://myserver.net/myapp/templates/login/index.vm"&gt;Login Page&lt;/a&gt;</code><br>
     * and<br>
     * <code>&lt;a href='$link.absolute("http://theirserver.com/index.jsp")'&gt;Their, Inc.&lt;/a&gt;</code><br>
     * produces something like<br/>
     * <code>&lt;a href="http://theirserver.net/index.jsp"&gt;Their, Inc.&lt;/a&gt;</code><br>
     *
     * @param obj A context-relative URI reference or absolute URL.
     * @return a new instance of LinkTool with the specified URI
     * @see #uri(Object uri)
     * @since VelocityTools 1.3
     */
    public LinkTool absolute(Object obj)
    {
        if (obj == null)
        {
            // use uri's null handling
            return uri(obj);
        }
        String pth = String.valueOf(obj);
        if (pth.startsWith(DEFAULT_SCHEME))
        {
            // looks absolute already
            return uri(pth);
        }

        // assume it's relative and try to make it absolute
        String root = getRoot();
        if (root != null)
        {
            return uri(combinePath(root, pth));
        }
        // ugh. this is about all we can do here w/o a host
        LinkTool copy = duplicate();
        copy.setScheme(DEFAULT_SCHEME);
        copy.setPath(pth);
        return copy;
    }

    /**
     * <p>Returns a copy of the link with the given URI reference set.
     * Few changes are applied to the given URI reference. The URI
     * reference can be absolute, server-relative, relative and may
     * contain query parameters. This method will overwrite all previous
     * settings for scheme, host port, path, query and anchor.</p>
     *
     * @param uri URI reference to set
     * @return a new instance of LinkTool
     * @since VelocityTools 1.3
     */
    public LinkTool uri(Object uri)
    {
        LinkTool copy = duplicate();
        if (copy.setFromURI(uri))
        {
            return copy;
        }
        return null;
    }

    /**
     * If the tool is not in "safe mode"--which it is by default--
     * this will return the {@link URI} representation of this instance,
     * if any.
     * @see SafeConfig#isSafeMode()
     */
    public URI getUri()
    {
        if (!isSafeMode())
        {
            return createURI();
        }
        return null;
    }

    /**
     * Returns the full URI of this template without any query data.
     * e.g. <code>http://myserver.net/myapp/stuff/View.vm</code>
     * Note! The returned String will not represent any URI reference
     * or query data set for this LinkTool. A typical application of
     * this method is with the HTML base tag. For example:
     * <code>&lt;base href="$link.baseRef"&gt;</code>
     */
    public String getBaseRef()
    {
        LinkTool copy = duplicate();
        copy.setQuery(null);
        copy.setFragment(null);
        return copy.toString();
    }

    /**
     * Sets the specified value as the current query string,
     * after normalizing the pair delimiters.
     */
    public LinkTool query(Object query)
    {
        LinkTool copy = duplicate();
        copy.setQuery(query);
        return copy;
    }

    /**
     * Returns the current query string, if any.
     */
    public String getQuery()
    {
        return this.query;
    }

    /**
     * <p>Adds a key=value pair to the query data. This returns a new LinkTool
     * containing both a copy of this LinkTool's query data and the new data.
     * Query data is URL encoded before it is appended.</p>
     *
     * @param key key of new query parameter
     * @param value value of new query parameter
     *
     * @return a new instance of LinkTool
     * @since VelocityTools 1.3
     */
    public LinkTool param(Object key, Object value)
    {
        LinkTool copy = duplicate();
        copy.appendQuery(toQuery(key, value));
        return copy;
    }

    /**
     * <p>Adds multiple key=value pairs to the query data.
     * This returns a new LinkTool containing both a copy of
     * this LinkTool's query data and the new data.
     * Query data is URL encoded before it is appended.</p>
     *
     * @param parameters map of new query data keys to values
     * @return a new instance of LinkTool
     * @since VelocityTools 1.3
     */
    public LinkTool params(Map parameters)
    {
        // don't waste time with null/empty data
        if (parameters == null || parameters.isEmpty())
        {
            return this;
        }
        LinkTool copy = duplicate();
        copy.appendQuery(parameters);
        return copy;
    }

    public Map getParams()
    {
        if (this.query == null || this.query.length() == 0)
        {
            return null;
        }
        return parseQuery(this.query);
    }

    /**
     * <p>Returns a copy of the link with the specified anchor to be
     *    added to the end of the generated hyperlink.</p>
     *
     * Example:<br>
     * <code>&lt;a href='$link.setAnchor("foo")'&gt;Foo&lt;/a&gt;</code><br>
     * produces something like</br>
     * <code>&lt;a href="#foo"&gt;Foo&lt;/a&gt;</code><br>
     *
     * @param anchor an internal document reference
     * @return a new instance of LinkTool with the set anchor
     * @since VelocityTools 1.3
     */
    public LinkTool anchor(Object anchor)
    {
        LinkTool copy = duplicate();
        copy.setFragment(anchor);
        return copy;
    }

    /**
     * Returns the anchor (internal document reference) set for this link.
     */
    public String getAnchor()
    {
        return this.fragment;
    }

    public LinkTool getSelf()
    {
        // there are no self-params to bother with at this level,
        return self;
    }

    /**
     * Returns the full URI reference that's been built with this tool,
     * including the query string and anchor, e.g.
     * <code>http://myserver.net/myapp/stuff/View.vm?id=42&type=blue#foo</code>.
     * Typically, it is not necessary to call this method explicitely.
     * Velocity will call the toString() method automatically to obtain
     * a representable version of an object.
     */
    public String toString()
    {
        URI uri = createURI();
        if (uri == null)
        {
            return null;
        }
        return uri.toString();
    }

    /**
     * This instance is considered equal to any
     * LinkTool instance whose toString() method returns a
     * String equal to that returned by this instance's toString()
     * @see #toString()
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof LinkTool))
        {
            return false;
        }
        // string value is all that ultimately matters
        String that = obj.toString();
        if (that == null && toString() == null)
        {
            return true;
        }
        return that.equals(toString());
    }

    /**
     * Returns the hash code for the result of toString().
     * If toString() returns {@code null} (yes, we do break that contract),
     * this will return {@code -1}.
     */
    @Override
    public int hashCode()
    {
        String hashme = toString();
        if (hashme == null)
        {
            return -1;
        }
        return hashme.hashCode();
    }


    /**
     * Delegates encoding of the specified url content to
     * {@link URLEncoder#encode} using the configured character encoding.
     *
     * @return String - the encoded url.
     */
    public String encode(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        try
        {
            return URLEncoder.encode(String.valueOf(obj), charset);
        }
        catch (UnsupportedEncodingException uee)
        {
            debug("Character encoding '%s' is unsupported", uee, charset);
            return null;
        }
    }

    /**
     * Delegates decoding of the specified url content to
     * {@link URLDecoder#decode} using the configured character encoding.
     *
     * @return String - the decoded url.
     */
    public String decode(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        try
        {
            return URLDecoder.decode(String.valueOf(obj), charset);
        }
        catch (UnsupportedEncodingException uee)
        {
            debug("Character encoding '%s' is unsupported", uee, charset);
            return null;
        }
    }

}

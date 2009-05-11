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

    public static final String APPEND_PARAMS_KEY = "appendParameters";
    public static final String FORCE_RELATIVE_KEY = "forceRelative";
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
    protected Map query;
    protected String fragment;
    protected String charset;
    protected String queryDelim;
    protected boolean appendParams;
    protected boolean forceRelative;
    protected boolean opaque;
    protected final LinkTool self;


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
        appendParams = true;
        forceRelative = false;
        self = this;
    }

    protected final void debug(String msg, Object... args)
    {
        debug(msg, null, args);
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
        Boolean addParams = props.getBoolean(APPEND_PARAMS_KEY);
        if (addParams != null)
        {
            setAppendParams(addParams);
        }
        Boolean forceRelative = props.getBoolean(FORCE_RELATIVE_KEY);
        if (forceRelative != null)
        {
            setForceRelative(forceRelative);
        }
    }

    /**
     * Equivalent to clone, but with no checked exceptions.
     * If for some unfathomable reason clone() doesn't work,
     * this will throw a RuntimeException.
     */
    protected LinkTool duplicate()
    {
        return duplicate(false);
    }

    /**
     * Equivalent to clone, but with no checked exceptions.
     * If for some unfathomable reason clone() doesn't work,
     * this will throw a RuntimeException.  If doing a deep
     * clone, then the parameter Map will also be cloned.
     */
    protected LinkTool duplicate(boolean deep)
    {
        try
        {
            LinkTool that = (LinkTool)this.clone();
            if (deep && query != null)
            {
                that.query = new LinkedHashMap(query);
            }
            return that;
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
     * Sets whether or not the {@link #setParam} method
     * will override existing query values for the same key or simply append
     * the new value to a list of existing values.
     */
    public void setAppendParams(boolean addParams)
    {
        this.appendParams = addParams;
    }

    /**
     * Sets whether or not the {@link #createURI} method should ignore the
     * scheme, user, port and host values for non-opaque URIs, thus making
     * {@link #toString} print the link as a relative one, not an absolute
     * one.  NOTE: using {@link #absolute()}, {@link #absolute(Object)},
     * {@link #relative()}, or {@link #relative(Object)} will alter this
     * setting accordingly on the new instances they return.
     */
    public void setForceRelative(boolean forceRelative)
    {
        this.forceRelative = forceRelative;
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
     * If the specified value is null, it will set the query to null.
     * If a Map, it will copy all those values into a new LinkedHashMap and
     * replace any current query value with that. If it is a String,
     * it will use {@link #parseQuery(String)} to parse it into a map
     * of keys to values.
     */
    public void setQuery(Object obj)
    {
        if (obj == null)
        {
            this.query = null;
        }
        else if (obj instanceof Map)
        {
            this.query = new LinkedHashMap((Map)obj);
        }
        else
        {
            String qs = normalizeQuery(String.valueOf(obj));
            this.query = parseQuery(qs);
        }
    }

    protected String normalizeQuery(String qs)
    {
        // if we have multiple pairs...
        if (qs.indexOf('&') >= 0)
        {
            // ensure the delimeters match the xhtml setting
            // this impl is not at all efficient, but it's easy
            qs = qs.replaceAll("&(amp;)?", queryDelim);
        }
        return qs;
    }

    /**
     * Converts the map of keys to values into a query string.
     */
    public String toQuery(Map parameters)
    {
        if (parameters == null)
        {
            return null;
        }
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
        return query.toString();
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
     * If there is no existing value for this key in the query, it
     * will simply add it and its value to the query.  If the key
     * already is present in the query and append
     * is true, this will add the specified value to those
     * already under that key.  If {@link #appendParams} is
     * false, this will override the existing values with the
     * specified new value.
     */
    public void setParam(Object key, Object value, boolean append)
    {
        // use all keys as strings, even null -> "null"
        key = String.valueOf(key);
        if (this.query == null)
        {
            this.query = new LinkedHashMap();
            putParam(key, value);
        }
        else if (append)
        {
            appendParam((String)key, value);
        }
        else
        {
            putParam(key, value);
        }
    }

    private void appendParam(String key, Object value)
    {
        if (query.containsKey(key))
        {
            Object cur = query.get(key);
            if (cur instanceof List)
            {
                addToList((List)cur, value);
            }
            else
            {
                List vals = new ArrayList();
                vals.add(cur);
                addToList(vals, value);
                putParam(key, vals);
            }
        }
        else
        {
            putParam(key, value);
        }
    }

    private void putParam(Object key, Object value)
    {
        if (value instanceof Object[])
        {
            List vals = new ArrayList();
            for (Object v : ((Object[])value))
            {
                vals.add(v);
            }
            value = vals;
        }
        query.put(key, value);
    }

    private void addToList(List vals, Object value)
    {
        if (value instanceof List)
        {
            for (Object v : ((List)value))
            {
                vals.add(v);
            }
        }
        else if (value instanceof Object[])
        {
            for (Object v : ((Object[])value))
            {
                vals.add(v);
            }
        }
        else
        {
            vals.add(value);
        }
    }

    /**
     * If append is false, this simply delegates to {@link #setQuery}.
     * Otherwise, if the specified object is null, it does nothing.  If the object
     * is not a Map, it will turn it into a String and use {@link #parseQuery} to
     * parse it. Once it is a Map, it will iterate through the entries appending
     * each key/value to the current query data.
     */
    public void setParams(Object obj, boolean append)
    {
        if (!append)
        {
            setQuery(obj);
        }
        else if (obj != null)
        {
            if (!(obj instanceof Map))
            {
                obj = parseQuery(String.valueOf(obj));
            }
            if (obj != null)
            {
                if (query == null)
                {
                    this.query = new LinkedHashMap();
                }
                for (Object e : ((Map)obj).entrySet())
                {
                    Map.Entry entry = (Map.Entry)e;
                    String key = String.valueOf(entry.getKey());
                    appendParam(key, entry.getValue());
                }
            }
        }
    }

    /**
     * Removes the query pair(s) with the specified key from the
     * query data and returns the remove value(s), if any.
     */
    public Object removeParam(Object key)
    {
        if (query != null)
        {
            key = String.valueOf(key);
            return query.remove(key);
        }
        return null;
    }

    /**
     * In this class, this method ignores true values.  If passed a false value,
     * it will call {@link #setQuery} with a null value to clear all query data.
     */
    protected void handleParamsBoolean(boolean keep)
    {
        if (!keep)
        {
            setQuery(null);
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
     * Uses {@link #normalizeQuery} to make all delimiters in the
     * specified query string match the current query delimiter 
     * and then uses {@link #parseQuery(String,String)} to parse it
     * according to that same delimiter.
     */
    protected Map<String,Object> parseQuery(String query)
    {
        return parseQuery(normalizeQuery(query), this.queryDelim);
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

        URI uri = toURI(obj);
        if (uri == null)
        {
            return false;
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
     * Turns the specified object into a string and thereby a URI.
     */
    protected URI toURI(Object obj)
    {
        if (obj instanceof URI)
        {
            return (URI)obj;
        }
        else
        {
            try
            {
                return new URI(String.valueOf(obj));
            }
            catch (Exception e)
            {
                debug("Could convert '%s' to URI", e, obj);
                return null;
            }
        }
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
                if (opaque)
                {
                    // path is used as scheme-specific part
                    return new URI(scheme, path, fragment);
                }
                else if (forceRelative)
                {
                    if (path == null && query == null && fragment == null)
                    {
                        return null;
                    }
                    return new URI(null, null, null, -1, path, toQuery(query), fragment);
                }
                else
                {
                    // only create the URI if we have some values besides a port
                    if (scheme == null && user == null && host == null
                        && path == null && query == null && fragment == null)
                    {
                        return null;
                    }
                    return new URI(scheme, user, host, port, path, toQuery(query), fragment);
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
     * Returns true if {@link #param(Object,Object)} appends values;
     * false if the method overwrites existing value(s) for the specified key.
     */
    public boolean getAppendParams()
    {
        return this.appendParams;
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
     * Returns the directory stack
     * in the set {@link #getPath()} value, by just trimming
     * off all that follows the last "/".
     */
    public String getDirectory()
    {
        if (this.path == null || this.opaque)
        {
            return null;
        }
        int lastSlash = this.path.lastIndexOf('/');
        if (lastSlash < 0)
        {
            return "";
        }
        return this.path.substring(0, lastSlash + 1);
    }

    /**
     * Returns the last section of the path,
     * which is all that follows the final "/".
     */
    public String getFile()
    {
        if (this.path == null || this.opaque)
        {
            return null;
        }
        int lastSlash = this.path.lastIndexOf('/');
        if (lastSlash < 0)
        {
            return this.path;
        }
        return this.path.substring(lastSlash + 1, this.path.length());
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
        LinkTool root = root();
        if (root == null)
        {
            return null;
        }
        return root.toString();
    }

    /**
     * Returns a new LinkTool instance that represents
     * the "root" of the current one, if it has one.
     * This essentially calls {@link #absolute()} and
     * sets the path, query, and fragment to null on
     * the returned instance.
     * @see #getRoot()
     */
    public LinkTool root()
    {
        if (host == null || opaque || port == -2)
        {
            return null;
        }
        LinkTool copy = absolute();
        copy.setPath(null);
        copy.setQuery(null);
        copy.setFragment(null);
        return copy;
    }

    /**
     * Returns a new LinkTool instance with
     * the path set to the result of {@link #getDirectory()}
     * and the query and fragment set to null.
     */
    public LinkTool directory()
    {
        LinkTool copy = root();
        if (copy == null)
        {
            copy = duplicate();
            // clear query and fragment, since root() didn't
            copy.setQuery(null);
            copy.setFragment(null);
        }
        copy.setPath(getDirectory());
        return copy;
    }

    /**
     * Returns true if this instance is being forced to
     * return relative URIs or has a null scheme value.
     */
    public boolean isRelative()
    {
        return (this.forceRelative || this.scheme == null);
    }

    /**
     * Returns a copy of this LinkTool instance that has
     * {@link #setForceRelative} set to true.
     */
    public LinkTool relative()
    {
        LinkTool copy = duplicate();
        copy.setForceRelative(true);
        return copy;
    }

    /**
     * <p>Returns a copy of the link with the specified directory-relative
     * URI reference set as the end of the path and {@link #setForceRelative}
     * set to true. If the specified relative path is null, that is treated
     * the same as an empty path.</p>
     *
     * Example:<br>
     * <code>&lt;a href='$link.relative("/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like</br>
     * <code>&lt;a href="/myapp/login/index.vm"&gt;Login Page&lt;/a&gt;</code><br>
     *
     * @param obj A directory-relative URI reference (e.g. file path in current directory)
     * @return a new instance of LinkTool with the specified changes
     * @see #relative()
     */
    public LinkTool relative(Object obj)
    {
        LinkTool copy = relative();
        // prepend relative paths with the current directory
        String pth;
        if (obj == null)
        {
            pth = getContextPath();
        }
        else
        {
            pth = combinePath(getContextPath(), String.valueOf(obj));
        }
        copy.setPath(pth);
        return copy;
    }

    /**
     * At this level, this only returns the result of {@link #getDirectory}.
     * It is here as an extension hook for subclasses to change the
     * "context" for relative links.
     * @see #relative(Object)
     * @see #getDirectory
     */
    public String getContextPath()
    {
        return getDirectory();
    }

    /**
     * Returns true if this instance has a scheme value
     * and is not being forced to create relative URIs.
     */
    public boolean isAbsolute()
    {
        return (this.scheme != null && !this.forceRelative);
    }

    /**
     * Returns a copy of this LinkTool instance that has
     * {@link #setForceRelative} set to false and sets the
     * scheme to the "http" if no scheme has been set yet.
     */
    public LinkTool absolute()
    {
        LinkTool copy = duplicate();
        copy.setForceRelative(false);
        if (copy.getScheme() == null)
        {
            copy.setScheme(DEFAULT_SCHEME);
        }
        return copy;
    }

    /**
     * <p>Returns a copy of the link with the specified URI reference
     * either used as or converted to an absolute (non-relative)
     * URI reference. Unless the specified URI contains a query
     * or anchor, those values will not be overwritten when using
     * this method.</p>
     *
     * Example:<br>
     * <code>&lt;a href='$link.absolute("login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like<br/>
     * <code>&lt;a href="http://myserver.net/myapp/login/index.vm"&gt;Login Page&lt;/a&gt;</code>;<br>
     * <code>&lt;a href='$link.absolute("/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like<br/>
     * <code>&lt;a href="http://myserver.net/login/index.vm"&gt;Login Page&lt;/a&gt;</code>;<br>
     * and<br>
     * <code>&lt;a href='$link.absolute("http://theirserver.com/index.jsp")'&gt;Their, Inc.&lt;/a&gt;</code><br>
     * produces something like<br/>
     * <code>&lt;a href="http://theirserver.net/index.jsp"&gt;Their, Inc.&lt;/a&gt;</code><br>
     *
     * @param obj A root-relative or context-relative path or an absolute URI.
     * @return a new instance of LinkTool with the specified path or URI
     * @see #absolute()
     */
    public LinkTool absolute(Object obj)
    {
        // assume it's just a path value to go with current scheme/host/port
        LinkTool copy = absolute();
        String pth;
        if (obj == null)
        {
            // just use the current directory path, if any
            pth = getDirectory();
        }
        else
        {
            pth = String.valueOf(obj);
            if (pth.startsWith(DEFAULT_SCHEME))
            {
                // looks absolute already
                URI uri = toURI(pth);
                if (uri == null)
                {
                    return null;
                }
                copy.setScheme(uri.getScheme());
                copy.setUserInfo(uri.getUserInfo());
                copy.setHost(uri.getHost());
                copy.setPort(uri.getPort());
                // handle path, query and fragment with care
                pth = uri.getPath();
                if (pth.equals("/") || pth.length() == 0)
                {
                    pth = null;
                }
                copy.setPath(pth);
                if (uri.getQuery() != null)
                {
                    copy.setQuery(uri.getQuery());
                }
                if (uri.getFragment() != null)
                {
                    copy.setFragment(uri.getFragment());
                }
                return copy;
            }
            else if (!pth.startsWith("/"))
            {
                // paths that don't start with '/'
                // are considered relative to the current directory
                pth = combinePath(getDirectory(), pth);
            }
        }
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
     * Sets the specified value as the current query data,
     * after normalizing the pair delimiters.  This overrides
     * any existing query.
     */
    public LinkTool query(Object query)
    {
        LinkTool copy = duplicate();
        copy.setQuery(query);
        return copy;
    }

    /**
     * Returns the current query as a string, if any.
     */
    public String getQuery()
    {
        return toQuery(this.query);
    }

    /**
     * <p>Adds a key=value pair to the query data. Whether
     * this new query pair is appended to the current query
     * or overwrites any previous pair(s) with the same key
     * is controlled by the {@link #getAppendParams} value.
     * The default behavior is to append.</p>
     *
     * @param key key of new query parameter
     * @param value value of new query parameter
     * @return a new instance of LinkTool
     */
    public LinkTool param(Object key, Object value)
    {
        LinkTool copy = duplicate(true);
        copy.setParam(key, value, this.appendParams);
        return copy;
    }

    /**
     * Appends a new key=value pair to the existing query
     * data.
     *
     * @param key key of new query parameter
     * @param value value of new query parameter
     * @return a new instance of LinkTool
     */
    public LinkTool append(Object key, Object value)
    {
        LinkTool copy = duplicate(true);
        copy.setParam(key, value, true);
        return copy;
    }

    /**
     * Sets a new key=value pair to the existing query
     * data, overwriting any previous pair(s) that have
     * the same key.
     *
     * @param key key of new query parameter
     * @param value value of new query parameter
     * @return a new instance of LinkTool
     */
    public LinkTool set(Object key, Object value)
    {
        LinkTool copy = duplicate(true);
        copy.setParam(key, value, false);
        return copy;
    }

    /**
     * Returns a new LinkTool instance that has any
     * value(s) under the specified key removed from the query data.
     *
     * @param key key of the query pair(s) to be removed
     * @return a new instance of LinkTool
     */
    public LinkTool remove(Object key)
    {
        LinkTool copy = duplicate(true);
        copy.removeParam(key);
        return copy;
    }

    /**
     * This method can do two different things.  If you pass in a
     * boolean, it will create a new LinkTool duplicate and call
     * {@link #handleParamsBoolean(boolean)} on it. In this class, true
     * values do nothing (subclasses may have use for them), but false
     * values will clear out all params in the query for that instance.
     * If you pass in a query string or a Map of parameters, those
     * values will be added to the new LinkTool, either overwriting
     * previous value(s) with those keys or appending to them,
     * depending on the {@link #getAppendParams} value.
     *
     * @param parameters a boolean or new query data (either Map or query string)
     * @return a new instance of LinkTool
     */
    public LinkTool params(Object parameters)
    {
        // don't waste time with null/empty data
        if (parameters == null)
        {
            return this;
        }
        if (parameters instanceof Boolean)
        {
            Boolean action = ((Boolean)parameters).booleanValue();
            LinkTool copy = duplicate(true);
            copy.handleParamsBoolean(action);
            return copy;
        }
        if (parameters instanceof Map && ((Map)parameters).isEmpty())
        {
            return duplicate(false);
        }

        LinkTool copy = duplicate(this.appendParams);
        copy.setParams(parameters, this.appendParams);
        return copy;
    }

    public Map getParams()
    {
        if (this.query == null || this.query.isEmpty())
        {
            return null;
        }
        return this.query;
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
        if (query != null)
        {
            return decodeQueryPercents(uri.toString());
        }
        return uri.toString();
    }

    /**
     * This is an ugly (but fast) hack that's needed because URI encodes
     * things that we don't need encoded while not encoding things
     * that we do need encoded.  So, we have to encode query data
     * before creating the URI to ensure they are properly encoded,
     * but then URI encodes all the % from that encoding.  Here,
     * we isolate the query data and manually decode the encoded
     * %25 in that section back to %, without decoding anything else.
     */
    protected String decodeQueryPercents(String url)
    {
        StringBuilder out = new StringBuilder(url.length());
        boolean inQuery = false, havePercent = false, haveTwo = false;
        for (int i=0; i<url.length(); i++)
        {
            char c = url.charAt(i);
            if (inQuery)
            {
                if (havePercent)
                {
                    if (haveTwo)
                    {
                        out.append('%');
                        if (c != '5')
                        {
                            out.append('2').append(c);
                        }
                        havePercent = haveTwo = false;
                    }
                    else if (c == '2')
                    {
                        haveTwo = true;
                    }
                    else
                    {
                        out.append('%').append(c);
                        havePercent = false;
                    }
                }
                else if (c == '%')
                {
                    havePercent = true;
                }
                else
                {
                    out.append(c);
                }
                if (c == '#')
                {
                    inQuery = false;
                }
            }
            else
            {
                out.append(c);
                if (c == '?')
                {
                    inQuery = true;
                }
            }
        }
        // if things ended part way
        if (havePercent)
        {
            out.append('%');
            if (haveTwo)
            {
                out.append('2');
            }
        }
        return out.toString();
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

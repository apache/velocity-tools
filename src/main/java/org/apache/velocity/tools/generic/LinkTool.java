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
@ValidScope(Scope.REQUEST)
public class LinkTool extends AbstractLockConfig implements Cloneable
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
     * Default constructor. Tool must be initialized before use.
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

    private void logError(String msg, Throwable t)
    {
        if (this.LOG != null)
        {
            this.LOG.error("LinkTool: "+msg, t);
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
            this.charset = chrst;
        }

        Boolean xhtml = props.getBoolean(XHTML_MODE_KEY);
        if (xhtml != null)
        {
            setXHTML(xhtml);
        }
    }

    /**
     * <p>Controls the delimiter used for separating query data pairs.
     *    By default, the standard '&' character is used.</p>
     * <p>This is not exposed to templates as this decision is best not
     *    made at that level.</p>
     * <p>Subclasses may easily override the init() method to set this
     *    appropriately and then call super.init()</p>
     *
     * @param useXhtml if true, the XHTML query data delimiter ('&amp;amp;')
     *        will be used.  if false, then '&' will be used.
     * @see <a href="http://www.w3.org/TR/xhtml1/#C_12">Using Ampersands in Attribute Values (and Elsewhere)</a>
     */
    protected void setXHTML(boolean xhtml)
    {
        queryDelim = (xhtml) ? XHTML_QUERY_DELIMITER : HTML_QUERY_DELIMITER;
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
            logError(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    protected void setScheme(Object obj)
    {
        if (obj == null)
        {
            this.scheme = null;
        }
        else
        {
            this.scheme = String.valueOf(obj);
            if (scheme.endsWith(":"))
            {
                scheme = scheme.substring(0, scheme.length() - 1);
            }
        }
    }

    protected void setUserInfo(Object obj)
    {
        this.user = obj == null ? null : String.valueOf(obj);
    }

    protected void setHost(Object obj)
    {
        this.host = obj == null ? null : String.valueOf(obj);
    }

    protected void setPort(Object obj)
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
                logError("Could convert '"+obj+"' to int", nfe);
                this.port = -2; // use this to mean error
            }
        }
    }

    protected void setPath(Object obj)
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

    protected void appendPath(Object obj)
    {
        if (obj != null && !this.opaque)
        {
            setPath(combinePath(getPath(), String.valueOf(obj)));
        }
    }

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

    protected void setQuery(Object obj)
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

    protected void appendQuery(Object obj)
    {
        if (obj != null)
        {
            setQuery(combineQuery(getQuery(), String.valueOf(obj)));
        }
    }

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
        if (current == null)
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
    private void appendAsArray(StringBuilder out, Object key, Object[] arr)
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

    protected Map<String,Object> parseQuery(String query)
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

    protected void setFragment(Object obj)
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
                logError("Could convert '"+obj+"' to URI", e);
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
            logError("Could not create URI", e);
        }
        return null;
    }

    // --------------------------------------------- Template Methods -----------

    public LinkTool scheme(Object scheme)
    {
        LinkTool copy = duplicate();
        copy.setScheme(scheme);
        return copy;
    }

    public LinkTool secure()
    {
        return scheme(SECURE_SCHEME);
    }

    public LinkTool insecure()
    {
        return scheme(DEFAULT_SCHEME);
    }

    public String getScheme()
    {
        return scheme;
    }

    public boolean isSecure()
    {
        return SECURE_SCHEME.equalsIgnoreCase(getScheme());
    }

    public boolean isAbsolute()
    {
        if (this.scheme == null)
        {
            return false;
        }
        return true;
    }

    public boolean isOpaque()
    {
        return this.opaque;
    }

    public LinkTool user(Object info)
    {
        LinkTool copy = duplicate();
        copy.setUserInfo(info);
        return copy;
    }

    public String getUser()
    {
        return this.user;
    }

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

    public String getHost()
    {
        return this.host;
    }

    public LinkTool port(Object port)
    {
        LinkTool copy = duplicate();
        copy.setPort(port);
        return copy;
    }

    public Integer getPort()
    {
        if (this.port < 0)
        {
            return null;
        }
        return this.port;
    }

    public LinkTool path(Object pth)
    {
        LinkTool copy = duplicate();
        copy.setPath(pth);
        return copy;
    }

    public String getPath()
    {
        return this.path;
    }

    public LinkTool append(Object pth)
    {
        LinkTool copy = duplicate();
        copy.appendPath(pth);
        return copy;
    }

    /**
     * At this level, this method returns all "folders"
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
     * and will return null if there is no host. It
     * is also unfriendly to opaque URIs.
     */
    public String getRoot()
    {
        if (host == null)
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
     * <p>Returns the URI that addresses this web application. E.g.
     * <code>http://myserver.net/myapp</code>. This string does not end
     * with a "/".  Note! This will not represent any URI reference or
     * query data set for this LinkTool.</p>
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
     * @param path A context-relative URI reference. A context-relative URI
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
     * @param uri A context-relative URI reference or absolute URL.
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
     * No conversions are applied to the given URI reference. The URI
     * reference can be absolute, server-relative, relative and may
     * contain query parameters. This method will overwrite any
     * previous URI reference settings but will copy the query
     * string.</p>
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

    public LinkTool query(Object query)
    {
        LinkTool copy = duplicate();
        copy.setQuery(query);
        return copy;
    }

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
        copy.appendQuery(query);
        return copy;
    }

    public Map getParams()
    {
        if (this.query == null || this.query.isEmpty())
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
            logError("Character encoding '"+charset+"' is unsupported", uee);
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
            logError("Character encoding '"+charset+"' is unsupported", uee);
            return null;
        }
    }

}

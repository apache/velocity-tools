/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.velocity.tools.view.tools;


import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * <p>View tool to make building URIs pleasant and fun! :)</p>
 * 
 * <p>This class is equipped to be used with a toolbox manager, for example
 * the ServletToolboxManager included with VelocityViewServlet. This class 
 * implements interface ViewTool, which allows a toolbox manager to pass the
 * required context information.</p>
 *
 * <p>This class is not thread-safe by design. A new instance is needed for
 * the processing of every template request.  This means this tool should
 * only be used in the request scope, not application or session scopes.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: LinkTool.java,v 1.3 2003/03/22 20:33:09 nbubna Exp $
 */
public class LinkTool implements ViewTool, Cloneable
{


    /** Standard HTML delimiter for query data ('&') */ 
    public static final String HTML_QUERY_DELIMITER = "&";

    /** XHTML delimiter for query data ('&amp;amp;') */ 
    public static final String XHTML_QUERY_DELIMITER = "&amp;";


    /** A reference to the ServletContext */ 
    protected ServletContext application;

    /** A reference to the HttpServletRequest. */ 
    protected HttpServletRequest request;

    /** A reference to the HttpServletResponse. */ 
    protected HttpServletResponse response;


    /** The URI reference set for this link. */ 
    private String uri;

    /** A list of query string parameters. */ 
    private ArrayList queryData;

    /** The current delimiter for query data */
    private String queryDataDelim;


    /**
     * Default constructor. Tool must be initialized before use.
     */
    public LinkTool()
    {
        uri = null;
        queryData = null;
        queryDataDelim = HTML_QUERY_DELIMITER;
    }


    // --------------------------------------- Protected Methods -------------

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
    protected void setXhtml(boolean useXhtml)
    {
        queryDataDelim = 
            (useXhtml) ? XHTML_QUERY_DELIMITER : HTML_QUERY_DELIMITER;
    }


    /**
     * For internal use.
     *
     * Copies 'that' LinkTool into this one and adds the new query data.
     *
     * @param pair the query parameter to add
     */
    protected LinkTool copyWith(QueryPair pair)
    {
        LinkTool copy = duplicate();
        if (copy.queryData != null)
        {
            // set the copy's query data to a shallow clone of 
            // the current query data array
            copy.queryData = (ArrayList)this.queryData.clone();
        }
        else
        {
            copy.queryData = new ArrayList();
        }
        //add new pair to this LinkTool's query data
        copy.queryData.add(pair);
        return copy;
    }


    /**
     * For internal use.
     *
     * Copies 'that' LinkTool into this one and sets the new URI.
     *
     * @param uri uri string
     */
    protected LinkTool copyWith(String uri)
    {
        LinkTool copy = duplicate();
        copy.uri = uri;
        return copy;
    }


    /**
     * This is just to avoid duplicating this code for both copyWith() methods
     */
    private LinkTool duplicate()
    {
        try
        {
            return (LinkTool)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            Velocity.warn("LinkTool: could not clone " + getClass() + " - " + e);

            // "clone" manually
            LinkTool copy;
            try
            {
                // one last try for a subclass instance...
                copy = (LinkTool)getClass().newInstance();
            }
            catch (Exception ee)
            {
                // fine, we'll use the base class
                copy = new LinkTool();
            }
            copy.application = this.application;
            copy.request = this.request;
            copy.response = this.response;
            copy.uri = this.uri;
            copy.queryData = this.queryData;
            return copy;
        }
    }


    // --------------------------------------------- ViewTool Interface -------

    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        if (!(obj instanceof ViewContext))
        {
            throw new IllegalArgumentException("Tool can only be initialized with a ViewContext");
        }
 
        ViewContext context = (ViewContext)obj;
        this.request = context.getRequest();
        this.response = context.getResponse();
        this.application = context.getServletContext();    
    }


    // --------------------------------------------- Template Methods -----------

    /**
     * <p>Returns a copy of the link with the specified context-relative
     * URI reference converted to a server-relative URI reference. This 
     * method will overwrite any previous URI reference settings but will 
     * copy the query string.</p> 
     *
     * Example:<br>
     * <code>&lt;a href='$link.setRelative("/templates/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like</br>
     * <code>&lt;a href="/myapp/templates/login/index.vm"&gt;Login Page&lt;/a&gt;</code><br>
     *
     * @param uri A context-relative URI reference. A context-relative URI 
     * is a URI that is relative to the root of this web application.
     *
     * @return a new instance of LinkTool with the specified URI
     */
    public LinkTool setRelative(String uri)
    {
        if (uri.startsWith("/"))
        {
            return copyWith(request.getContextPath() + uri);
        }
        else
        {
            return copyWith(request.getContextPath() + '/' + uri);
        }        
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
     *
     * @return a new instance of LinkTool
     */
    public LinkTool setURI(String uri)
    {
        return copyWith(uri);
    }


    /**
     * <p>Returns the current URI of this link as set by the setURI(String) 
     * or setRelative(String) methods. Any conversions 
     * have been applied. The returned URI reference does not include query 
     * data that was added with method addQueryData().</p>
     */
    public String getURI()
    {
        return uri;
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
     */
    public LinkTool addQueryData(String key, Object value)
    {
        return copyWith(new QueryPair(key, value));
    }

    
    /**
     * <p>Returns this link's query data as a url-encoded string e.g. 
     * <cpde>key=value&foo=this+is+encoded</code>.</p>
     */
    public String getQueryData()
    {
        if (queryData != null && !queryData.isEmpty())
        {

            StringBuffer out = new StringBuffer();
            for(int i=0; i < queryData.size(); i++)
            {
                out.append(queryData.get(i));
                if (i+1 < queryData.size())
                {
                    out.append(queryDataDelim);
                }
            }
            return out.toString();
        }
        return null;
    }


    /**
     * <p>Returns the URI that addresses this web application. E.g. 
     * <code>http://myserver.net/myapp</code>. This string does not end 
     * with a "/".  Note! This will not represent any URI reference or 
     * query data set for this LinkTool.</p>
     */
    public String getContextURL()
    {
        String scheme = request.getScheme();
        int port = request.getServerPort();

        StringBuffer out = new StringBuffer();
        out.append(request.getScheme());
        out.append("://");
        out.append(request.getServerName());
        if ((scheme.equals("http") && port != 80) ||
            (scheme.equals("https") && port != 443))
        {
            out.append(':');
            out.append(port);
        }
        out.append(request.getContextPath());
        return out.toString();
    }


    /**
     * <p>Returns the context path that addresses this web 
     * application, e.g. <code>/myapp</code>. This string starts 
     * with a "/" but does not end with a "/" Note! This will not 
     * represent any URI reference or query data set for this 
     * LinkTool.</p>
     */
    public String getContextPath()
    {
        return request.getContextPath();
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
        StringBuffer out = new StringBuffer();
        out.append(getContextURL());
        out.append(request.getServletPath());
        return out.toString();
    }


    /** 
     * Returns the full URI reference that's been built with this tool, 
     * including the query string, e.g. 
     * <code>http://myserver.net/myapp/stuff/View.vm?id=42&type=blue</code>.
     * Typically, it is not necessary to call this method explicitely.
     * Velocity will call the toString() method automatically to obtain 
     * a representable version of an object.
     */
    public String toString()
    {
        StringBuffer out = new StringBuffer();

        if (uri != null)
        {
            out.append(uri);
        } 

        String query = getQueryData();
        if (query != null)
        {
            // Check if URI already contains query data
            if ( uri == null || uri.indexOf('?') < 0)
            {
                // no query data yet, start query data with '?'    
                out.append('?');
            }
            else
            {
                // there is already query data, use data delimiter
                out.append(queryDataDelim);
            }
            out.append(query);
        }
        // encode session ID into URL if sessions are used but cookies are
        // not supported
        return response.encodeURL(out.toString());
    }


  
    // --------------------------------------------- Internal Class -----------
 
     /**
     * Internal util class to handle representation and
     * encoding of key/value pairs in the query string
     */
    protected final class QueryPair
    {

        private final String key;
        private final Object value;


        /**
         * Construct a new query pair.
         *
         * @param key query pair
         * @param value query value
         */
        public QueryPair(String key, Object value) 
        {
            this.key = key;
            this.value = value;
        }

        /**
         * Return the URL-encoded query string.
         */ 
        public String toString()
        {
            StringBuffer out = new StringBuffer();
            out.append(URLEncoder.encode(key));
            out.append('=');
            out.append(URLEncoder.encode(String.valueOf(value)));
            return out.toString();
        }
    }


}

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

package org.apache.velocity.tools.struts;

import java.util.ArrayList;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.*;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.LogEnabledContextToolImpl;
import org.apache.velocity.tools.view.tools.ServletContextTool;


/**
 * <p>Context tool to work with URI links in Struts.</p> 
 * 
 * <p>This class is equipped to be used with a toolbox manager, for example
 * the ServletToolboxManager included with VelServlet. The class extends 
 * ServletContextToolLogger to profit from the logging facilities of that class.
 * Furthermore, this class implements interface ServletContextTool, which allows
 * a toolbox manager to pass the required context information.</p>
 *
 * <p>This class is not thread-safe by design. A new instance is needed for
 * the processing of every template request.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: LinkTool.java,v 1.3 2002/04/02 16:46:30 sidler Exp $
 * 
 */
public class LinkTool extends LogEnabledContextToolImpl 
    implements ServletContextTool
{

    // --------------------------------------------- Properties ---------------

    /**
     * A reference to the ServletContext
     */ 
    protected ServletContext application;


    /**
     * A reference to the HtttpServletRequest.
     */ 
    protected HttpServletRequest request;
    

    /**
     * A reference to the HtttpSession.
     */ 
    protected HttpSession session;


    /**
     * The URI reference set for this link.
     */ 
    protected final String uri;


    /**
     * A list of query string parameters.
     */ 
    protected final ArrayList queryData;

    
    // --------------------------------------------- Constructors -------------

    /**
     * Returns a factory for instances of this class. Use method 
     * {@link #getInstance(ViewContext context)} to obtain instances 
     * of this class. Do not use instance obtained from this method
     * in templates. They are not properly initialized.
     */
    public LinkTool()
    {
        request = null;
        session = null;
        application = null;
        
        uri = null;
        queryData = null;
    }
    
    
    /**
     * For internal use only! Use method {@link #getInstance(ViewContext context)} 
     * to obtain instances of this tool.
     */
    private LinkTool(ViewContext context)
    {
        this.request = context.getRequest();
        this.application = context.getServletContext();    

        this.uri = null;
        this.queryData = null;
    }
    

    /**
     * For internal use.
     *
     * Copies 'that' LinkTool into this one and adds the new query data.
     *
     * @param that a reference to a link tool
     * @param pair the query parameter to add
     */
    private LinkTool(LinkTool that, QueryPair pair)
    {
        this.request = that.request;
        this.application = that.application;    
        this.uri = that.uri;
        if (that.queryData != null)
        {
            //set this query data to a shallow clone of that query data
            this.queryData = (ArrayList)that.queryData.clone();
        }
        else
        {
            this.queryData = new ArrayList();
        }
        //add new pair to this LinkTool's query data
        this.queryData.add(pair);
    }


    /**
     * For internal use.
     *
     * Copies 'that' LinkTool into this one and sets the new URI.
     *
     * @param that a reference to a link tool
     * @param uri uri string
     */
    private LinkTool(LinkTool that, String uri)
    {
        this.request = that.request;
        this.application = that.application;    
        //set to new uri
        this.uri = uri;
        //we don't need to clone here, this was not changed
        this.queryData = that.queryData;
    }



    // ----------------------------------- Interface ServletContextTool -------

    /**
     * Returns an initialized instance of this context tool.
     */
    public Object getInstance(ViewContext context)
    {
        return new LinkTool(context);
    }


    /**
     * <p>Returns the default life cycle for this tool. This is 
     * {@link ServletContextTool#REQUEST}. Do not overwrite this
     * per toolbox configuration. No alternative life cycles are 
     * supported by this tool</p>
     */
    public String getDefaultLifecycle()
    {
        return ServletContextTool.REQUEST; 
    }

    

    
    // --------------------------------------------- View Helpers -------------


    /**
     * <p>Returns a copy of the link with the given URI reference set. 
     * No conversions are applied to the given URI reference. The URI 
     * reference can be absolute, server-relative, relative and may
     * contain query parameters. This method will overwrite any 
     * previous URI reference settings but will copy the query 
     * string.</p>
     *
     * <p>Note: It is Struts' recommended practice to forward to
     * actions or forwards, but not directly to templates as this
     * bypasses the Struts controller. Consider using setAction() 
     * or setForward() instead.</p>
     * 
     * @param uri URI reference to set
     *
     * @return a new instance of LinkTool
     */
    public LinkTool setURI(String uri)
    {
        return new LinkTool(this, uri);
    }


    /**
     * <p>Returns a copy of the link with the given action name
     * converted into a server-relative URI reference. This method 
     * does not check if the specified action really is defined. 
     * This method will overwrite any previous URI reference settings 
     * but will copy the query string.</p>
     *
     * @param action an action path as defined in struts-config.xml
     *
     * @return a new instance of LinkTool
     */
    public LinkTool setAction(String action)
    {
        return new LinkTool(this, 
            StrutsUtils.getActionMappingURL(application, request, action));
    }
    
    
    /**
     * <p>Returns a copy of the link with the given global forward name
     * converted into a server-relative URI reference. If the parameter 
     * does not map to an existing global forward name, <code>null</code> 
     * is returned. This method will overwrite any previous URI reference 
     * settings but will copy the query string.</p>
     *
     * @param forward a global forward name as defined in struts-config.xml
     *
     * @return a new instance of LinkTool
     */
    public LinkTool setForward(String forward)
    {
        ActionForward mapping = StrutsUtils.getActionForward(forward, application);
        
        if (mapping == null)
        {
            log(WARN, "In method setForward(" + forward + "): Parameter does not map to a valid forward.");
            return null;
        }

        String relPath = mapping.getPath();
        if (relPath.startsWith("/"))
        {
            return new LinkTool(this, request.getContextPath() + relPath);
        }
        else
        {
            return new LinkTool(this, request.getContextPath() + "/" + relPath);
        }        
        
    }
        

    /**
     * <p>Returns a copy of the link with the specified context-relative
     * URI reference converted to a server-relative URI reference. This 
     * method will overwrite any previous URI reference settings but will 
     * copy the query string.</p> 
     *
     * Example:<br>
     * <code>&lt;a href='$link.setAbsolute("/templates/login/index.vm")'&gt;Login Page&lt;/a&gt;</code><br>
     * produces something like</br>
     * <code>&lt;a href="/myapp/templates/login/index.vm"&gt;Login Page&lt;/a&gt;</code><br>
     *
     * @param uri A context-relative URI reference. A context-relative URI 
     * is a URI that is relative to the root of this web application.
     *
     * @return a new instance of LinkTool
     */
    public LinkTool setAbsolute(String uri)
    {
        if (uri.startsWith("/"))
        {
            return new LinkTool(this, request.getContextPath() + uri);
        }
        else
        {
            return new LinkTool(this, request.getContextPath() + "/" + uri);
        }        
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
        return new LinkTool(this, new QueryPair(key, value));
    }


    /**
     * <p>Returns the current URI of this link as set by one of the methods 
     * setURI(), setAction(), setForward() or setAbsolute(). Any conversions 
     * have been applied. The returned URI reference does not include query 
     * data that was added with method addQueryData().</p>
     */
    public String getURI()
    {
        return uri;
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
                    out.append('&');
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
            out.append(":");
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
                out.append("?");
            }
            else
            {
                // there is already query data, delimiter is '&'
                out.append("&");
            }
            out.append(query);
        }

        return out.toString();
    }


  
    // --------------------------------------------- Internal Class -----------
 
     /**
     * Internal util class to handle representation and
     * encoding of key/value pairs in the query string
     */
    final class QueryPair
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

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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


package org.apache.velocity.tools.view.servlet;


import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.dom4j.Element;
import org.dom4j.Node;

import org.apache.velocity.tools.view.DataInfo;
import org.apache.velocity.tools.view.ToolInfo;
import org.apache.velocity.tools.view.XMLToolboxManager;
import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ViewContext;


/**
 * <p>A toolbox manager for the servlet environment.</p>
 *
 * <p>A toolbox manager is responsible for automatically filling the Velocity
 * context with a set of view tools. This class provides the following 
 * features:</p>
 * <ul>
 *   <li>configurable through an XML-based configuration file</li>   
 *   <li>assembles a set of view tools (the toolbox) on request</li>
 *   <li>handles different tool scopes (request, session, application)</li>
 *   <li>supports any class with a public constructor without parameters 
 *     to be used as a view tool</li>
 *   <li>supports adding primitive data values to the context(String,Number,Boolean)</li>
 * </ul>
 * 
 *
 * <p><strong>Configuration</strong></p>
 * <p>The toolbox manager is configured through an XML-based configuration
 * file. The configuration file is passed to the {@link #load(java.io.InputStream input)}
 * method. The required format is shown in the following example:</p>
 * <pre> 
 * &lt;?xml version="1.0"?&gt;
 * 
 * &lt;toolbox&gt;
 *   &lt;tool&gt;
 *      &lt;key&gt;toolLoader&lt;/key&gt;
 *      &lt;scope&gt;application&lt;/scope&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.ToolLoader&lt;/class&gt;
 *   &lt;/tool&gt;
 *   &lt;tool&gt;
 *      &lt;key&gt;math&lt;/key&gt;
 *      &lt;scope&gt;application&lt;/scope&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.MathTool&lt;/class&gt;
 *   &lt;/tool&gt;
 *   &lt;data type="Number"&gt;
 *      &lt;key&gt;luckynumber&lt;/key&gt;
 *      &lt;value&gt;1.37&lt;/class&gt;
 *   &lt;/data&gt;
 *   &lt;data type="String"&gt;
 *      &lt;key&gt;greeting&lt;/key&gt;
 *      &lt;value&gt;Hello World!&lt;/class&gt;
 *   &lt;/data&gt;
 * &lt;/toolbox&gt;    
 * </pre>
 * <p>The recommended location for the configuration file is the WEB-INF directory of the
 * web application. 
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ServletToolboxManager.java,v 1.4 2003/01/24 05:04:51 nbubna Exp $
 * 
 */
public class ServletToolboxManager extends XMLToolboxManager
{

    // --------------------------------------------------- Properties ---------

    public static final String ELEMENT_SCOPE = "scope";
    public static final String ELEMENT_CREATE_SESSION = "create-session";

    public static final String VALUE_YES  = "yes";
    public static final String VALUE_NO   = "no";

    public static final String SESSION_TOOLS_KEY = "org.apache.velocity.tools.view.tools.ServletToolboxManager.SessionTools";

    private ServletContext servletContext;
    private Map appTools;
    private ArrayList sessionToolInfo;
    private ArrayList requestToolInfo;
    private boolean createSession;



    // --------------------------------------------------- Constructor --------

    /**
     * Default constructor
     */
    public ServletToolboxManager(ServletContext servletContext)
    {
        this.servletContext = servletContext;
        appTools = new HashMap();
        sessionToolInfo = new ArrayList();
        requestToolInfo = new ArrayList();
        createSession = true;
    }



    // --------------------------------------------------- Methods ------------

    /**
     * Sets whether or not to create a new session when none exists for the
     * current request and session-scoped tools have been defined for this
     * toolbox.
     *
     * If true, then a call to {@link getToolboxContext} will 
     * create a new session if none currently exists for this request and
     * the toolbox has one or more session-scoped tools designed.
     *
     * If false, then a call to {@link getToolboxContext} will never
     * create a new session for the current request.
     * This effectively means that no session-scoped tools will be added to 
     * the ToolboxContext for a request that does not have a session object.
     *
     * The default value is true.
     */
    public void setCreateSession(boolean b)
    {
        createSession = b;
    }


    /**
     * Overrides XMLToolboxManager to log to the servlet context
     * and to Velocity's main log
     */
    protected void log(String s) 
    {
        servletContext.log("ServletToolboxManager: " + s);
    }


    /**
     * Overrides XMLToolboxManager to handle the create-session element.
     */
    protected boolean readElement(Element e) throws Exception
    {
        String name = e.getName();

        ToolInfo info = null;

        if (name.equalsIgnoreCase(ELEMENT_TOOL))
        {
            info = readToolInfo(e);
        }
        else if (name.equalsIgnoreCase(ELEMENT_DATA)) 
        {
            info = readDataInfo(e);
        }
        else if (name.equalsIgnoreCase(ELEMENT_CREATE_SESSION))
        {
            readCreateSession(e);
            return true;
        }
        else
        {
            log("Could not read element: "+name);
            return false;
        }

        addTool(info);
        log("Added "+info.getClassname()+" as "+info.getKey());
        return true;
    }


    /**
     * Reads the value for create-session.
     *
     * @see setCreateSession(boolean)
     */
    protected boolean readCreateSession(Element e) throws Exception
    {
        String csValue = e.getText();
        if (VALUE_YES.equalsIgnoreCase(csValue))
        {
            setCreateSession(true);
        }
        else if (VALUE_NO.equalsIgnoreCase(csValue))
        {
            setCreateSession(false);
        }
        else
        {
            log("Unknown value for create-session.  Valid options are 'yes' or 'no'.");
            return false;
        }
        log("create-session is set to "+createSession);
        return true;
    }


    /**
     * Overrides XMLToolboxManager to read a {@link ServletToolInfo}
     * instead of a {@link org.apache.velocity.tools.view.ViewToolInfo}.
     */
    protected ToolInfo readToolInfo(Element e) throws Exception
    {
        Node n = e.selectSingleNode(ELEMENT_KEY);
        String key = n.getText();

        n = e.selectSingleNode(ELEMENT_CLASS);
        String classname = n.getText();
        
        String scope = ServletToolInfo.REQUEST_SCOPE;
        n = e.selectSingleNode(ELEMENT_SCOPE);
        if (n != null)
        {
            scope = n.getText();
        }

        return new ServletToolInfo(key, classname, scope);
    }


    /**
     * Overrides XMLToolboxManager to separate tools by scope.
     * For this to work, we obviously override getToolboxContext as well.
     */
    public void addTool(ToolInfo info)
    {
        if (info instanceof DataInfo)
        {
            //add static data to the appTools map
            appTools.put(info.getKey(), info.getInstance(null));
        }
        else if (info instanceof ServletToolInfo)
        {
            ServletToolInfo stInfo = (ServletToolInfo)info;
            
            if (stInfo.getScope().equalsIgnoreCase(ServletToolInfo.REQUEST_SCOPE))
            {
                requestToolInfo.add(stInfo);
            }
            else if (stInfo.getScope().equalsIgnoreCase(ServletToolInfo.SESSION_SCOPE))
            {
                sessionToolInfo.add(stInfo);
            }
            else if (stInfo.getScope().equalsIgnoreCase(ServletToolInfo.APPLICATION_SCOPE))
            {
                //add application scoped tools to appTools and
                //initialize them with the ServletContext
                appTools.put(stInfo.getKey(), stInfo.getInstance(servletContext));
            }
            else
            {
                log("Unknown scope: "+stInfo.getScope()+" "+stInfo.getKey()+" will be request scoped.");
                requestToolInfo.add(stInfo);
            }
        }
        else
        {
            //default is request scope
            requestToolInfo.add(info);
        }
    }


    /**
     * Overrides XMLToolboxManager to handle the separate
     * scopes.
     *
     * Application scope tools were initialized when the toolbox was loaded.
     * Session scope tools are initialized once per session and stored in a
     * map in the session attributes.
     * Request scope tools are initialized on every request.
     * 
     */
    public ToolboxContext getToolboxContext(Object initData)
    {
        //we know the initData is a ViewContext
        ViewContext ctx = (ViewContext)initData;
        
        //create the toolbox map with the application tools in it
        Map toolbox = new HashMap(appTools);

        if (!sessionToolInfo.isEmpty())
        {
            HttpSession session = ctx.getRequest().getSession(createSession);

            if (session != null) {
                //synchronize session tool initialization to avoid potential
                //conflicts from multiple simultaneous requests in the same session
                synchronized(session)
                {
                    //get the initialized session tools
                    Map stmap = (Map)session.getAttribute(SESSION_TOOLS_KEY);

                    //if session tools aren't initialized,
                    //do so and store them in the session
                    if (stmap == null)
                    {
                        stmap = new HashMap(sessionToolInfo.size());
                        Iterator i = sessionToolInfo.iterator();
                        while(i.hasNext())
                        {
                            ToolInfo info = (ToolInfo)i.next();
                            stmap.put(info.getKey(), info.getInstance(ctx));
                        }
                        session.setAttribute(SESSION_TOOLS_KEY, stmap);
                    }

                    //add the initialized session tools to the toolbox
                    toolbox.putAll(stmap);
                }
            }
        }

        //add and initialize request tools
        Iterator i = requestToolInfo.iterator();
        while(i.hasNext())
        {
            ToolInfo info = (ToolInfo)i.next();
            toolbox.put(info.getKey(), info.getInstance(ctx));
        }

        return new ToolboxContext(toolbox);
    }


}

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


import java.io.InputStream;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.apache.velocity.context.Context;

import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ServletViewTool;
import org.apache.velocity.tools.view.tools.LogEnabledViewTool;
import org.apache.velocity.tools.view.tools.ContextViewTool;
import org.apache.velocity.tools.view.tools.ThreadSafeViewTool;


/**
 * <p>A toolbox manager for the servlet environment.</p>
 *
 * <p>A toolbox manager is responsible for automatically filling the Velocity
 * context with a set of view tools. This class provides the following 
 * features:</p>
 * <ul>
 *   <li>configurable through an XML-based configuration file</li>   
 *   <li>assembles a set of view tools (the toolbox) on request</li>
 *   <li>handles view tools with different life cycles</li>
 *   <li>efficiently reuses view tool instances where possible</li>
 *   <li>provides special handling to known classes of view tools</li>
 *   <li>supports any class with a public constructor without parameters 
 *     to be used as a view tool</li>
 * </ul>
 * 
 *
 * <p><strong>Configuration</strong></p>
 * <p>The toolbox manager is configured through an XML-based configuration
 * file. The configuration file is passed to the {@link #load(InputStream input)}
 * method. The required format is show in the following example:</p>
 * <pre> 
 * &lt;?xml version="1.0"?&gt;
 * 
 * &lt;toolbox&gt;
 *   &lt;tool&gt;
 *      &lt;key&gt;toolLoader&lt;/key&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.ToolLoaderTool&lt;/class&gt;
 *   &lt;/tool&gt;
 *   &lt;tool&gt;
 *      &lt;key>math&lt;/key&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.MathTool&lt;/class&gt;
 *   &lt;/tool&gt;
 * &lt;/toolbox&gt;    
 * </pre>
 * <p>The recommended location for the configuration file is the WEB-INF directory of the
 * web application. Note that some classes of view tools may allow or
 * require additional configuration attributes. Please consult the documentation 
 * of the view tools for more details.
 * 
 *
 * <p><strong>Recognized Classes of View Tools</strong></p>
 * <p>ServletToolboxManager provides special support for the following classes
 * of view tools:
 * <dl>
 *   <dt>{@link LogEnabledViewTool}</dt>
 *   <dd>Receive a reference to a logger object that enables them to log error 
 *     conditions.</dd>
 *
 *   <dt>{@link ThreadSafeViewTool}</dt>
 *   <dd>Instances are considered to be thread-safe. One single instance of 
 *     the tool is re-used the entire runtime. This is much more efficient 
 *     than the default handling (see below)</dd>
 *
 *   <dt>{@link ServletViewTool}</dt>
 *   <dd>View tools that implement this interface support the additional 
 *     configuration attribute <i>lifecycle</i>. This allows an
 *     application developer to explicitely assign a <i>lifecycle</i> to the tool.
 *     Supported are the values <code>request</code>, <code>session</code> and 
 *     <code>application</code>. The <i>lifecycle</i> attribute is optional.
 *     If not specified, a tool-specific default lifecycle is used. In the 
 *     following configuration example a <i>lifecycle</i> of <code>session</code> 
 *     is assigned to tool instances:
 *     <pre>
 *       &lt;tool&gt;
 *          &lt;key&gt;xyz&lt;/key&gt;
 *          &lt;class&gt;org.apache.velocity.tools.tools.XYZ&lt;/class&gt;
 *          &lt;scope&gt;session&lt;/scope&gt;
 *       &lt;/tool&gt;
 *     </pre>    
 *     Furthermore, view tools of this class get access to the current 
 *     servlet request, the current session and the servlet context.</dd>
 *
 *   <dt>{@link ContextViewTool}</dt>
 *   <dd>View tools of this class receive a reference to the Velocity
 *     context.</dd>
 * </dl>
 *
 *
 * <p><strong>Default Handling</strong></p>
 * <p>Any object with a public constructor without parameters can be used
 * as a view tool. For classes of view tools that are not listed above,
 * the following default handling is applied:</p>
 * <ul>
 *   <li>instances are created using a constructor without parameters</li>
 *   <li>tools are assumed to be not thread-safe, a new instance is created
 *     for every template being processed</li>
 *   <li>tools have no access to logging</li>
 *   <li>tools have no access to contextual information, like the servlet
 *     environment or the Velocity context</li>
 * </ul>
 *
 *
 * <p><strong>Other Environments</strong></p>
 * <p>Note that while the implementation of this toolbox manager is specific
 * to the Servlet environment, it can be easily adapted to be used in other
 * environments, like the DVSL ant task, for example.</p>
 * 
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: ServletToolboxManager.java,v 1.2 2002/04/15 18:30:29 sidler Exp $
 * 
 */
public class ServletToolboxManager
{

    // --------------------------------------------------- Properties ---------

    public static final String TOOL_KEY = "key";
    public static final String TOOL_LIFECYCLE = "lifecycle";
    public static final String TOOL_CLASS = "class";

    public static final String SESSION_TOOLS_KEY = "org.apache.velocity.tools.view.tools.ServletToolboxManager.SessionTools";

    private ServletViewToolLogger logger;
    private ServletContext scontext;
 
    private SAXReader saxReader;
    private Map applicationTools;
    private ArrayList applicationToolsNotInitialized;
    private ArrayList sessionTools;
    private ArrayList requestTools;



    // --------------------------------------------------- Constructor --------

    /**
     * Default constructor
     */
    public ServletToolboxManager(ServletContext scontext)
    {
        this.scontext = scontext;

        saxReader = new SAXReader();
        applicationToolsNotInitialized = new ArrayList();
        sessionTools = new ArrayList();
        requestTools = new ArrayList();
        logger = new ServletViewToolLogger(scontext);
    }



    // --------------------------------------------------- Methods ------------

    private void log(String s) 
    {
        scontext.log("ServletToolboxManager: " + s);
    }


    /**
     * Reads an XML document from an {@link InputStream}
     * using <a href="http://dom4j.org">dom4j</a> and
     * sets up the toolbox for the servlet from that.
     * 
     * Assumes toolbox.xml has a format like
     *
     *  <toolbox>
     *    <tool>
     *      <key>foo</key>
     *      <lifecycle>request</lifecycle>
     *      <class>com.mycompany.tools.Foo</class>
     *    </tool>
     *    <tool>
     *      <key>bar</key>
     *      <class>org.yourorganization.tools.Bar</class>
     *    </tool>
     *  </toolbox>
     *
     * @param input the InputStream to read from
     */
    public void load(InputStream input) throws Exception
    {
        log("Initializing the toolbox...");
        Document document = saxReader.read(input);
        List tools = document.selectNodes("//toolbox/*");

        Iterator i = tools.iterator();
        while(i.hasNext())
        {
            //
            // Read definition of a tool in toolbox config file
            //
            Element e = (Element)i.next();
            String name = e.getName();
            log("Loading " + name);

            // Read tool's key
            Node n = e.selectSingleNode(TOOL_KEY);
            String key = n.getText();
            log("  Context key: " + key);

            // Read the tool's class
            n = e.selectSingleNode(TOOL_CLASS);
            String classname = n.getText();
            log("  Class: " + classname);
            
            // Create an instance of the tool
            Object obj;
            try
            {
                obj = Class.forName(classname).newInstance();
            }
            catch(Exception ex)
            {
                log("  Error creating instance for class: " + classname +
                    ". " + ex);
                log("  Tool not loaded.");    
                continue;
            }

            // Try to read the tool's lifecycle (specific to ServletViewTool)
            
            // First, check of a lifecycle has been configured. 
            n = e.selectSingleNode(TOOL_LIFECYCLE);
            String lifecycle;
            if (n != null)
            {
                lifecycle = n.getText();
            }
            else
            {
                // Secondly, try to read default lifecycle
                try 
                {
                    lifecycle = ((ServletViewTool)obj).getDefaultLifecycle();
                }
                catch (ClassCastException cce)
                {
                    lifecycle = "";
                }
            }
            log("  Life cycle: " + lifecycle);
            
            //
            // Pass a logger to the tools that implement interface LogEnabledViewTool
            //
            if (obj instanceof LogEnabledViewTool)
            {
                ((LogEnabledViewTool)obj).setLogger(logger);
                log("  Known interface: LogEnabledViewTool");
            }
            
            //
            // Store the tool instance in the appropriate list
            //
            
            // First, handle tools that implement interface ServletViewTool
            // (the interface ThreadSafeViewTool is not considered in this
            // case because the 'lifecycle' attribute takes precedence over the 
            // 'thread safe' attribute)
            if (obj instanceof ServletViewTool)
            {
                log("  Known interface: ServletViewTool");
                if (lifecycle.equalsIgnoreCase(ServletViewTool.REQUEST))
                {
                    requestTools.add(new ToolInfo(key, obj, classname));
                }
                else if (lifecycle.equalsIgnoreCase(ServletViewTool.SESSION))
                {
                    sessionTools.add(new ToolInfo(key, obj, classname));
                }
                else if (lifecycle.equalsIgnoreCase(ServletViewTool.APPLICATION))
                {
                    applicationToolsNotInitialized.add(new ToolInfo(key, obj, classname));
                }
                else
                {
                    log("  Error: Unknown lifecycle: \""+ lifecycle +"\".");
                    log("  Tool not loaded.");
                }
                continue;
            }

            // Secondly, handle tools that implement interface ContextViewTool
            // (The interface ThreadSafeViewTool is not considered in this case
            // because it doesn't matter. A new tool instance is created for every 
            // request anyway since the Velocity context needs to be passed.)
            if (obj instanceof ContextViewTool)
            {
                log("  Known interface: ContextViewTool");
                // These tools always have a life cycle of 'request'
                requestTools.add(new ToolInfo(key, obj, classname));
                continue;
            }
            
            // Third, handle tools that implement interface ThreadSafeViewTool.
            // In this case, the one and only instance of the tool is reused the 
            // entire runtime.
            if (obj instanceof ThreadSafeViewTool)
            {
                log("  Known interface: ThreadSafeViewTool");
                applicationToolsNotInitialized.add(new ToolInfo(key, obj, classname));
                continue;
            }
            
            // Fourth, handle tools that implement no known interface.
            // Unknown view tools are considered not thread-safe and 
            // therefore a new instance is created for every template processing
            // request. 
            log("  Known interface: None. Apply default handling.");
            requestTools.add(new ToolInfo(key, obj, classname));
        }

        log("Done initializing the toolbox.");
    }


    /**
     * Creates a {@link ToolboxContext} from the tools loaded
     * in this manager. It uses the given {@link ViewContext}
     * to create instances of session and request tools. Request
     * tools are created on every call to this method. Session
     * tools are created once per session. Application tool instances
     * are re-used for the entire runtime.
     *
     * @param vcontext the current Velocity context
     * @return the created ToolboxContext
     */
    public ToolboxContext getToolboxContext(ViewContext vcontext)
    {
        // Only on first request, initialize tools with a life 
        // cycle of 'application'. This cannot be done earlier because 
        // some tools may need access to data that is only available 
        // with a template processing request.
        if (applicationTools == null)
        {
            synchronized (applicationToolsNotInitialized)
            {
                if (applicationTools == null)
                {
                    applicationTools = new HashMap(applicationToolsNotInitialized.size());
                    Iterator i = applicationToolsNotInitialized.iterator();
                    while (i.hasNext())
                    {
                        ToolInfo info = (ToolInfo)i.next();
                        Object tool = info.getTool();

                        // First, handle tools that implement ServletViewTool and have
                        // a defined life cycle of 'application'
                        if (tool instanceof ServletViewTool)
                        {
                            applicationTools.put(info.getKey(), 
                                ((ServletViewTool)tool).getInstance(vcontext));
                            continue;
                        }

                        // Secondly, handle ContextViewTools
                        // => they never have an 'application' life cycle, => skip

                        // Third, handle tools that implement ThreadSafeViewTool
                        if (tool instanceof ThreadSafeViewTool)
                        {
                            applicationTools.put(info.getKey(), tool);
                            continue;
                        }

                        // There shouldn't be any tool left, otherwise it's an error
                        log("Error trying to load unknown tool with a life cycle " +
                            "of 'application': key=" + info.getKey() + " class=" + 
                            tool.getClass() + ". Tool not loaded.");
                    }
                }
            }
        }
        
        //
        // Assemble toolbox
        //
        
        // First, add tools with an 'application' life cycle, if any.
        Map toolbox = new HashMap(applicationTools);
        
        // Secondly, add tools with a 'session' life cycle, if any.
        if (!sessionTools.isEmpty())
        {
            HttpSession session = vcontext.getRequest().getSession();

            // get the initialized session tools
            Map stmap = (Map)session.getAttribute(SESSION_TOOLS_KEY);

            // if session tools aren't initialized, do so and store 
            // them in the session attributes.
            if (stmap == null)
            {
                synchronized (session)
                {
                    if (stmap == null)
                    {
                        stmap = new HashMap(sessionTools.size());
                        Iterator i = sessionTools.iterator();
                        while(i.hasNext())
                        {
                            ToolInfo info = (ToolInfo)i.next();
                            Object tool = info.getTool();

                            // Only tools of class ServletViewTool can
                            // have a life cycle of session.
                            try
                            {
                            stmap.put(info.getKey(), 
                                ((ServletViewTool)tool).getInstance(vcontext));
                            }
                            catch(ClassCastException cce)
                            {
                                log("Error trying to load unknown tool with" +
                                    " a life cycle of 'session': key=" + 
                                    info.getKey() + " class=" + tool.getClass() + 
                                    ". Tool not loaded.");
                            }
                            
                        }
                        session.setAttribute(SESSION_TOOLS_KEY, stmap);
                    }
                }
            }
            //add the initialized session tools to the toolbox
            toolbox.putAll(stmap);
        }
        
        // Thirdly, add tools with a 'request' life cycle, if any.
        Iterator i = requestTools.iterator();
        while(i.hasNext())
        {
            ToolInfo info = (ToolInfo)i.next();
            Object tool = info.getTool();

            // First, handle tools that implement ServletViewTool.
            // They are initialized with an instance of ViewContext.
            if (tool instanceof ServletViewTool)
            {
                toolbox.put(info.getKey(), 
                        ((ServletViewTool)(info.getTool())).getInstance(vcontext));
                continue;
            }
            
            // Secondly, handle tools that implement ContextViewTool.
            // They are initialized with an instance of Context.
            if (tool instanceof ContextViewTool)
            {
                toolbox.put(info.getKey(), 
                        ((ContextViewTool)(info.getTool())).getInstance((Context)vcontext));
                continue;
            }
            
            // Third, handle any other tool that does not implement any known
            // interface. It is required that these tools have a public 
            // contructor with no parameters.
            try
            {
                tool = Class.forName(info.getClassname()).newInstance();
                toolbox.put(info.getKey(), tool);
                continue;
            }
            catch(Exception e)
            {
               log("Error creating instance for class: " + info.getClassname() +
                ". " + e);
               continue;
            }
        }
        
        return new ToolboxContext(toolbox);
    }


    /**
     * This class holds a view tool's key and original instance.
     */
    protected final class ToolInfo
    {
        private String key;
        private Object tool;
        private String classname;
        
        ToolInfo(String key, Object tool, String classname)
        {
            this.key = key;
            this.tool = tool;
            this.classname = classname;
        }
        
        String getKey()
        {
            return key;
        }
        
        Object getTool()
        {
            return tool;
        }

        String getClassname()
        {
            return classname;
        }
        
    }


}

package org.apache.velocity.tools.view.jsp;

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

import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.VelocityView;

/**
 * <p>JSP tag implementation to allow use of Velocity and VelocityTools.</p>
 *
 * @author Nathan Bubna
 * @version $Id: VelocityViewTag.java,v 1.1 2001/08/14 00:07:39 geirm Exp $
 * @since VelocityTools 2.0
 */
public class VelocityViewTag extends BodyTagSupport
{
    public static final String DEFAULT_BODY_CONTENT_KEY = "bodyContent";
    public static final String DEFAULT_NAME =
        VelocityViewTag.class.getSimpleName();

    protected static int count = 0;

    protected VelocityView view;
    protected ViewToolContext context;
    protected String var;
    protected String scope;
    protected String template;
    protected String bodyContentKey = DEFAULT_BODY_CONTENT_KEY;

    public VelocityViewTag()
    {
        // always try to have some sort of unique id set, since
        // this serves as a log tag and may serve as a cache name later
        setId(DEFAULT_NAME + count++);
    }

    public void setId(String id)
    {
        // always try to have some sort of id set because
        // this is the log tag and cache name
        if (id == null)
        {
            throw new NullPointerException("id cannot be null");
        }
        super.setId(id);
    }

    public void setVar(String var)
    {
        this.var = var;
    }

    public String getVar()
    {
        return this.var;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getScope()
    {
        return this.scope;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public String getTemplate()
    {
        return this.template;
    }

    public void setBodyContentKey(String key)
    {
        this.bodyContentKey = DEFAULT_BODY_CONTENT_KEY;
    }

    public String getBodyContentKey()
    {
        return this.bodyContentKey;
    }

    public VelocityView getVelocityView()
    {
        return this.view;
    }

    public void setVelocityView(VelocityView view)
    {
        this.view = view;
    }

    public ViewToolContext getViewToolContext()
    {
        return this.context;
    }

    public void setViewToolContext(ViewToolContext context)
    {
        this.context = context;
    }


    public int doStartTag() throws JspException
    {
        initializeView();

        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException
    {
        if (hasContent())
        {
            try
            {
                // check for a var attribute
                String varname = getVar();
                if (varname == null)
                {
                    // if none, render into the pageout
                    renderContent(this.pageContext.getOut());
                }
                else
                {
                    // if we have a var, render into a string
                    StringWriter out = new StringWriter();
                    renderContent(out);

                    // and insert the string into the specified scope
                    // if none specified, default is PAGE_SCOPE
                    this.pageContext.setAttribute(varname,
                                                  out.toString(),
                                                  toScopeInt(getScope()));
                }
            }
            catch (Exception e)
            {
                new JspException("Failed to render " + getClass() +
                                 ": "+getId(), e);
            }
        }
        return EVAL_PAGE;
    }


    protected void initializeView()
    {
        // get the VelocityView for this app
        VelocityView view =
            ServletUtils.getVelocityView(this.pageContext.getServletConfig());

        // now make a Context
        ViewToolContext context =
            new JspToolContext(view.getVelocityEngine(), this.pageContext);

        view.prepareToolboxes((HttpServletRequest)this.pageContext.getRequest());
        view.prepareContext(context);

        setVelocityView(view);
        setViewToolContext(context);
    }

    protected boolean hasContent()
    {
        return (getBodyContent() != null || getTemplate() != null);
    }

    protected void renderContent(Writer out) throws Exception
    {
        VelocityView view = getVelocityView();
        VelocityEngine engine = view.getVelocityEngine();

        if (getTemplate() != null)
        {
            ViewToolContext context = getViewToolContext();

            // get the actual Template
            Template template = view.getTemplate(getTemplate());

            if (getBodyContent() != null)
            {
                context.put(getBodyContentKey(), getRenderedBody());
            }

            // render the template into the writer
            template.merge(context, out);
        }
        else
        {
            // render the body into the writer
            renderBody(out);
        }
    }

    protected String getRenderedBody() throws Exception
    {
        // render the body into a string
        StringWriter out = new StringWriter();
        renderBody(out);
        return out.toString();
    }

    protected void renderBody(Writer out) throws Exception
    {
        VelocityEngine engine = getVelocityView().getVelocityEngine();

        /*
         * Eventually, it'd be nice to utilize some AST caching here.
         * But that will likely need to wait until the StringResourceLoader
         * is ready for general use (Velocity 1.6), unless we want to
         * duplicate that minimally here in Tools 2.0
         */
        engine.evaluate(getViewToolContext(), out, getId(),
                        getBodyContent().getReader());
    }

    protected static int toScopeInt(String scope)
    {
        if (scope == null)
        {
            return PageContext.PAGE_SCOPE;
        }
        if (scope.equalsIgnoreCase("request"))
        {
            return PageContext.REQUEST_SCOPE;
        }
        if (scope.equalsIgnoreCase("session"))
        {
            return PageContext.SESSION_SCOPE;
        }
        if (scope.equalsIgnoreCase("application"))
        {
            return PageContext.APPLICATION_SCOPE;
        }
        if (scope.equalsIgnoreCase("page"))
        {
            return PageContext.PAGE_SCOPE;
        }
        throw new IllegalArgumentException("Unknown scope: "+scope);
    }

}

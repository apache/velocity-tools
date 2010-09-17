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
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.VelocityView;

/**
 * <p>This tag enables use of Velocity and VelocityTools within JSP files and tags.
 * This makes it trivial to render embedded VTL (Velocity Template Language)
 * or include a separate Velocity template within a JSP using the current
 * page context.  This also automatically provides the typical 
 * {@link VelocityView} toolbox support, much like the VelocityViewServlet
 * and VelocityLayoutServlets have.  In fact, this will by default share
 * the {@link VelocityView} instance used with those servlets.  This allows
 * for consistent configuration and shared resources (better performance).
 * </p>
 *
 * @author Nathan Bubna
 * @version $Id: VelocityViewTag.java,v 1.1 2001/08/14 00:07:39 geirm Exp $
 * @since VelocityTools 2.0
 */
public class VelocityViewTag extends BodyTagSupport
{
    public static final String DEFAULT_BODY_CONTENT_KEY = "bodyContent";
    private static final long serialVersionUID = -3329444102562079189L;

    protected transient VelocityView view;
    protected transient ViewToolContext context;
    protected transient StringResourceRepository repository;

    protected String var;
    protected String scope;
    protected String template;
    protected String bodyContentKey = DEFAULT_BODY_CONTENT_KEY;
    private boolean cache = false;

    /**
     * Release any per-invocation resources, resetting any resources or state
     * that should be cleared between successive invocations of
     * {@link javax.servlet.jsp.tagext.Tag#doEndTag()} and
     * {@link javax.servlet.jsp.tagext.Tag#doStartTag()}.
     */
    protected void reset()
    {
        super.setId(null);
        var = null;
        scope = null;
        template = null;
        bodyContentKey = DEFAULT_BODY_CONTENT_KEY;
        cache = false;
    }

    public void setId(String id)
    {
        if (id == null)
        {
            throw new NullPointerException("id cannot be null");
        }
        super.setId(id);
        // assume they want this cached
        cache = true;
    }

    protected String getLogId()
    {
        String id = super.getId();
        if (id == null)
        {
            id = getClass().getSimpleName();
        }
        return id;
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

    public void setCache(String s)
    {
        this.cache = "true".equalsIgnoreCase(s);
    }

    public String getCache()
    {
        return String.valueOf(this.cache);
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

    public StringResourceRepository getRepository()
    {
        if (this.repository == null)
        {
            setRepository(StringResourceLoader.getRepository());
        }
        return this.repository;
    }

    public void setRepository(StringResourceRepository repo)
    {
        this.repository = repo;
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
                throw new JspException("Failed to render " + getClass() +
                                       ": "+getLogId(), e);
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

        view.prepareContext(context, (HttpServletRequest)this.pageContext.getRequest());

        setVelocityView(view);
        setViewToolContext(context);
    }

    protected boolean hasContent()
    {
        return (getBodyContent() != null || getTemplate() != null);
    }

    protected void renderContent(Writer out) throws Exception
    {
        if (getTemplate() != null)
        {
            VelocityView view = getVelocityView();
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

    protected boolean isCached()
    {
        return getRepository().getStringResource(getId()) != null;
    }

    protected void renderBody(Writer out) throws Exception
    {
        String name = getId();
        // if it hasn't been cached, try that
        if (cache && !isCached())
        {
            String template = getBodyContent().getString();
            // if no id was set, use the template as the id
            if (name == null)
            {
                name = template;
            }
            cache(name, template);
        }
        // if it can't be cached, eval it
        if (!cache)
        {
            evalBody(out);
        }
        else
        {
            // load template from cache
            Template template = getVelocityView().getTemplate(name);
            template.merge(getViewToolContext(), out);
        }
    }

    protected void evalBody(Writer out) throws Exception
    {
        VelocityEngine engine = getVelocityView().getVelocityEngine();
        engine.evaluate(getViewToolContext(), out, getLogId(),
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

    protected void cache(String name, String template)
    {
        try
        {
            getRepository().putStringResource(name, template);
        }
        catch (Exception cnfe)
        {
            getVelocityView().getLog()
                .error("Could not cache body in a StringResourceRepository", cnfe);
            cache = false;
        }
    }

    /**
     * Release any per-instance resources, releasing any resources or state
     * before this tag instance is disposed.
     *
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release()
    {
        super.release();
        reset();
    }

}

package org.apache.velocity.tools.view.tools;

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

import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.context.ViewContext;

/**
 * @deprecated Use {@link org.apache.velocity.tools.view.LinkTool}
 */
@Deprecated
public class LinkTool extends org.apache.velocity.tools.view.LinkTool
{
    @Deprecated
    public static final String SELF_ABSOLUTE_KEY = "self-absolute";
    @Deprecated
    public static final String SELF_INCLUDE_PARAMETERS_KEY = "self-include-parameters";
    @Deprecated
    public static final String AUTO_IGNORE_PARAMETERS_KEY = "auto-ignore-parameters";

    @Deprecated
    protected ServletContext application;

    private HashSet<String> parametersToIgnore;
    private boolean autoIgnore = true;

    @Override
    protected void configure(ValueParser parser)
    {
        Boolean selfAbsolute = parser.getBoolean(SELF_ABSOLUTE_KEY);
        if (selfAbsolute != null)
        {
            setForceRelative(!selfAbsolute.booleanValue());
        }
        Boolean selfParams = parser.getBoolean(SELF_INCLUDE_PARAMETERS_KEY);
        if (selfParams != null)
        {
            setIncludeRequestParams(selfParams.booleanValue());
        }
        Boolean autoIgnoreParams = parser.getBoolean(AUTO_IGNORE_PARAMETERS_KEY);
        if (autoIgnoreParams != null)
        {
            setAutoIgnoreParameters(autoIgnoreParams.booleanValue());
        }

        super.configure(parser);
    }

    @Deprecated
    public void init(Object obj)
    {
        if (obj instanceof ViewContext)
        {
            ViewContext ctx = (ViewContext)obj;
            setRequest(ctx.getRequest());
            setResponse(ctx.getResponse());
            this.application = ctx.getServletContext();
            if (ctx.getVelocityEngine() != null)
            {
                this.LOG = ctx.getVelocityEngine().getLog();
            }
        }
    }


    // --------------------------------------- Protected Methods -------------

    @Deprecated
    public void setXhtml(boolean useXhtml)
    {
        setXHTML(useXhtml);
    }

    /**
     * @deprecated use {@link #setForceRelative} as <b>reversed</b>
     *      replacement
     */
    @Deprecated
    public void setSelfAbsolute(boolean selfAbsolute)
    {
        setForceRelative(!selfAbsolute);
    }

    /**
     * @deprecated use {@link #setIncludeRequestParams} instead
     */
    @Deprecated
    public void setSelfIncludeParameters(boolean selfParams)
    {
        setIncludeRequestParams(selfParams);
    }

    @Deprecated
    public void setAutoIgnoreParameters(boolean autoIgnore)
    {
        this.autoIgnore = autoIgnore;
    }

    @Deprecated
    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
        setFromRequest(request);
    }

    @Deprecated
    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
        if (response != null)
        {
            setCharacterEncoding(response.getCharacterEncoding());
        }
    }


    // --------------------------------------------- Template Methods -----------

    /**
     * @deprecated use {@link #anchor(Object)} instead
     */
    @Deprecated
    public LinkTool setAnchor(String anchor)
    {
        return (LinkTool)anchor(anchor);
    }

    /**
     * @deprecated use {@link #relative(Object)}
     */
    @Deprecated
    public LinkTool setRelative(String uri)
    {
        return (LinkTool)relative(uri);
    }

    /**
     * @deprecated use {@link #absolute(Object)}
     */
    @Deprecated
    public LinkTool setAbsolute(String uri)
    {
        return (LinkTool)absolute(uri);
    }

    /**
     * @deprecated use {@link #uri(Object)}
     */
    @Deprecated
    public LinkTool setURI(String uri)
    {
        return (LinkTool)uri(uri);
    }

    /**
     * @deprecated use {@link #getPath}
     */
    @Deprecated
    public String getURI()
    {
        return path;
    }

    /**
     * @deprecated use {@link #append(Object,Object)}
     */
    @Deprecated
    public LinkTool addQueryData(String key, Object value)
    {
        return (LinkTool)append(key, value);
    }

    /**
     * @deprecated use {@link #params(Object)}
     */
    @Deprecated
    public LinkTool addQueryData(Map parameters)
    {
        return (LinkTool)params(parameters);
    }

    /**
     * @deprecated use {@link #getQuery}
     */
    @Deprecated
    public String getQueryData()
    {
        return getQuery();
    }

    /**
     * @deprecated use {@link #encode(Object)}
     */
    @Deprecated
    public String encodeURL(String url)
    {
        return encode(url);
    }

    /**
     * If you do use this, then you must use {@link #addAllParameters()}
     * and not the replacements for it, or these will not be honored.
     *
     * @deprecated use {@link #addRequestParams(String...)}
     *             or {@link #addRequestParamsExcept(String...)}
     *             or {@link #addMissingRequestParams(String...)}
     */
    @Deprecated
    public LinkTool addIgnore(String parameterName)
    {
        LinkTool copy = (LinkTool)duplicate();
        if (copy.parametersToIgnore == null)
        {
            copy.parametersToIgnore = new HashSet<String>(1);
        }
        copy.parametersToIgnore.add(parameterName);
        return copy;
    }

    /**
     * @deprecated use {@link #addRequestParams(String...)}
     *             or {@link #addRequestParamsExcept(String...)}
     *             or {@link #addMissingRequestParams(String...)}
     */
    @Deprecated
    public LinkTool addAllParameters()
    {
        if (this.parametersToIgnore != null)
        {
            String[] ignoreThese = new String[parametersToIgnore.size()];
            return (LinkTool)addRequestParamsExcept(parametersToIgnore.toArray(ignoreThese));
        }
        else if (autoIgnore)
        {
            return (LinkTool)addMissingRequestParams();
        }
        else
        {
            return (LinkTool)addRequestParams();
        }
    }

    @Override
    public void setParam(Object key, Object value, boolean append)
    {
        super.setParam(key, value, append);
        if (autoIgnore)
        {
            if (parametersToIgnore == null)
            {
                parametersToIgnore = new HashSet<String>(1);
            }
            parametersToIgnore.add(String.valueOf(key));
        }
    }

    @Override
    public void setParams(Object obj, boolean append)
    {
        super.setParams(obj, append);
        if (autoIgnore && obj instanceof Map)
        {
            Map params = (Map)obj;
            if (!params.isEmpty())
            {
                if (parametersToIgnore == null)
                {
                    parametersToIgnore = new HashSet<String>(params.size());
                }
                for (Object e : ((Map)obj).entrySet())
                {
                    Map.Entry entry = (Map.Entry)e;
                    String key = String.valueOf(entry.getKey());
                    parametersToIgnore.add(key);
                }
            }
        }
    }

}

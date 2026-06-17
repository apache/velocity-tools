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
package org.apache.velocity.tools.spring;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.ViewToolContext;
import org.apache.velocity.tools.view.ViewToolManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 * Spring MVC {@link org.springframework.web.servlet.View View} rendering a Velocity template
 * with a Velocity Tools toolbox in scope.
 *
 * <p>The render context is built by the {@link ViewToolManager} of the single
 * {@link VelocityConfig} bean (so all configured tools are available), then the Spring model is
 * layered on top, then the template named by this view's URL is merged to the response.</p>
 */
public class VelocityToolboxView extends AbstractTemplateView
{
    private String encoding;

    /** Template input encoding; {@code null} uses the engine default. */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return this.encoding;
    }

    @Override
    protected void renderMergedTemplateModel(@NonNull Map<String, Object> model,
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception
    {
        VelocityConfig config = autodetectVelocityConfig();
        VelocityEngine engine = config.getVelocityEngine();
        ViewToolManager toolManager = config.getToolManager();

        ViewToolContext context = toolManager.createContext(request, response);
        for (Map.Entry<String, Object> entry : model.entrySet())
        {
            context.put(entry.getKey(), entry.getValue());
        }

        Template template = (this.encoding != null)
                ? engine.getTemplate(getUrl(), this.encoding)
                : engine.getTemplate(getUrl());
        template.merge(context, response.getWriter());
    }

    /** Locate the single {@link VelocityConfig} bean in the (ancestor-inclusive) context. */
    protected VelocityConfig autodetectVelocityConfig() throws BeansException
    {
        try
        {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    obtainApplicationContext(), VelocityConfig.class, true, false);
        }
        catch (NoSuchBeanDefinitionException ex)
        {
            throw new ApplicationContextException(
                    "Must define a single VelocityConfig bean (e.g. VelocityConfigurer) in this "
                    + "web application context", ex);
        }
    }
}

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

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * Convenience {@link org.springframework.web.servlet.ViewResolver} for {@link VelocityToolboxView},
 * resolving view names to {@code prefix + name + suffix} templates.
 */
public class VelocityViewResolver extends AbstractTemplateViewResolver
{
    private String encoding;

    public VelocityViewResolver()
    {
        setViewClass(requiredViewClass());
    }

    @NonNull
    @Override
    protected Class<?> requiredViewClass()
    {
        return VelocityToolboxView.class;
    }

    /** Template input encoding propagated to resolved views. */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    @NonNull
    @Override
    protected AbstractUrlBasedView buildView(@NonNull String viewName) throws Exception
    {
        VelocityToolboxView view = (VelocityToolboxView) super.buildView(viewName);
        if (this.encoding != null)
        {
            view.setEncoding(this.encoding);
        }
        return view;
    }
}

package org.apache.velocity.tools.view.jsp.jspimpl;

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

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.JspFragment;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTBlock;

/**
 * Exposes a directive body block as a {@link JspFragment}, i.e. the body of SimpleTag.
 *
 */
public class VelocityJspFragment extends JspFragment
{

    /**
     * The JSP page context.
     */
    private PageContext pageContext;

    /**
     * The block to wrap.
     */
    private ASTBlock block;

    /**
     * The directive context.
     */
    private InternalContextAdapter context;

    /**
     * Constructor.
     *
     * @param pageContext The page context to use.
     * @param block The block to wrap.
     * @param context The directive context.
     */
    public VelocityJspFragment(PageContext pageContext, ASTBlock block,
            InternalContextAdapter context)
    {
        this.pageContext = pageContext;
        this.block = block;
        this.context = context;
    }

    @Override
    public void invoke(Writer out) throws JspException, IOException
    {
        block.render(context, out);
    }

    @Override
    public JspContext getJspContext()
    {
        return pageContext;
    }

}

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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.junit.Test;

/**
 * Tests {@link VelocityJspFragment}.
 *
 */
public class VelocityJspFragmentTest
{

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityJspFragment#invoke(java.io.Writer)}.
     * @throws IOException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     * @throws JspException If something goes wrong.
     */
    @Test
    public void testInvokeWriter() throws MethodInvocationException, ResourceNotFoundException, ParseErrorException, IOException, JspException
    {
        PageContext pageContext = createMock(PageContext.class);
        ASTBlock block = createMock(ASTBlock.class);
        InternalContextAdapter context = createMock(InternalContextAdapter.class);
        Writer writer = createMock(Writer.class);
        expect(block.render(context, writer)).andReturn(true);

        replay(pageContext, block, context, writer);
        VelocityJspFragment fragment = new VelocityJspFragment(pageContext, block, context);
        fragment.invoke(writer);
        verify(pageContext, block, context, writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityJspFragment#getJspContext()}.
     */
    @Test
    public void testGetJspContext()
    {
        PageContext pageContext = createMock(PageContext.class);
        ASTBlock block = createMock(ASTBlock.class);
        InternalContextAdapter context = createMock(InternalContextAdapter.class);

        replay(pageContext, block, context);
        VelocityJspFragment fragment = new VelocityJspFragment(pageContext, block, context);
        assertSame(pageContext, fragment.getJspContext());
        verify(pageContext, block, context);
    }

}

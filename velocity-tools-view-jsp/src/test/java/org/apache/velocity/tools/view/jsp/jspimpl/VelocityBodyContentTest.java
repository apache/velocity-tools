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
import java.io.StringWriter;
import java.io.Writer;

import jakarta.servlet.jsp.JspWriter;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link VelocityBodyContent}.
 *
 */
public class VelocityBodyContentTest
{

    /**
     * The object to test.
     */
    private VelocityBodyContent content;

    private JspWriter jspWriter;

    private ASTBlock block;

    private InternalContextAdapter context;

    /**
     * Sets up the test.
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        jspWriter = createMock(JspWriter.class);
        block = createMock(ASTBlock.class);
        context = createMock(InternalContextAdapter.class);
        content = new VelocityBodyContent(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#write(char[], int, int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteCharArrayIntInt() throws IOException
    {
        jspWriter.write(aryEq("hello".toCharArray()), eq(1), eq(3));
        replay(jspWriter, block, context);
        content.write("hello".toCharArray(), 1, 3);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#close()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testClose() throws IOException
    {
        jspWriter.close();
        replay(jspWriter, block, context);
        content.close();
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#newLine()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testNewLine() throws IOException
    {
        jspWriter.newLine();
        replay(jspWriter, block, context);
        content.newLine();
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(boolean)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintBoolean() throws IOException
    {
        jspWriter.print(true);
        replay(jspWriter, block, context);
        content.print(true);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(char)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintChar() throws IOException
    {
        jspWriter.print('c');
        replay(jspWriter, block, context);
        content.print('c');
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintInt() throws IOException
    {
        jspWriter.print(42);
        replay(jspWriter, block, context);
        content.print(42);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(long)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintLong() throws IOException
    {
        jspWriter.print(42l);
        replay(jspWriter, block, context);
        content.print(42l);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(float)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintFloat() throws IOException
    {
        jspWriter.print(0.2f);
        replay(jspWriter, block, context);
        content.print(0.2f);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(double)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintDouble() throws IOException
    {
        jspWriter.print(0.2d);
        replay(jspWriter, block, context);
        content.print(0.2d);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(char[])}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintCharArray() throws IOException
    {
        jspWriter.print(aryEq("hello".toCharArray()));
        replay(jspWriter, block, context);
        content.print("hello".toCharArray());
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintln() throws IOException
    {
        jspWriter.println();
        replay(jspWriter, block, context);
        content.println();
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(boolean)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnBoolean() throws IOException
    {
        jspWriter.println(true);
        replay(jspWriter, block, context);
        content.println(true);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(char)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnChar() throws IOException
    {
        jspWriter.println('c');
        replay(jspWriter, block, context);
        content.println('c');
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnInt() throws IOException
    {
        jspWriter.println(42);
        replay(jspWriter, block, context);
        content.println(42);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(long)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnLong() throws IOException
    {
        jspWriter.println(42l);
        replay(jspWriter, block, context);
        content.println(42l);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(float)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnFloat() throws IOException
    {
        jspWriter.println(0.2f);
        replay(jspWriter, block, context);
        content.println(0.2f);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(double)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnDouble() throws IOException
    {
        jspWriter.println(0.2d);
        replay(jspWriter, block, context);
        content.println(0.2d);
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(char[])}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnCharArray() throws IOException
    {
        jspWriter.println(aryEq("hello".toCharArray()));
        replay(jspWriter, block, context);
        content.println("hello".toCharArray());
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#clear()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testClear() throws IOException
    {
        jspWriter.clear();
        replay(jspWriter, block, context);
        content.clear();
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#clearBuffer()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testClearBuffer() throws IOException
    {
        jspWriter.clearBuffer();
        replay(jspWriter, block, context);
        content.clearBuffer();
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#getRemaining()}.
     */
    @Test
    public void testGetRemaining()
    {
        expect(jspWriter.getRemaining()).andReturn(42);
        replay(jspWriter, block, context);
        assertEquals(42, content.getRemaining());
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#getReader()}.
     * @throws IOException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     */
    @Test
    public void testGetReader() throws MethodInvocationException, ResourceNotFoundException, ParseErrorException, IOException
    {
        expect(block.render(eq(context), isA(StringWriter.class))).andReturn(true);
        replay(jspWriter, block, context);
        assertNotNull(content.getReader());
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#getString()}.
     * @throws IOException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     */
    @Test
    public void testGetString() throws MethodInvocationException, ResourceNotFoundException, ParseErrorException, IOException
    {
        expect(block.render(eq(context), isA(StringWriter.class))).andReturn(true);
        replay(jspWriter, block, context);
        assertEquals("", content.getString());
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#writeOut(java.io.Writer)}.
     * @throws IOException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     */
    @Test
    public void testWriteOutWriter() throws MethodInvocationException, ResourceNotFoundException, ParseErrorException, IOException
    {
        Writer writer = createMock(Writer.class);
        expect(block.render(context, writer)).andReturn(true);
        replay(jspWriter, block, context, writer);
        content.writeOut(writer);
        verify(jspWriter, block, context, writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintString() throws IOException
    {
        jspWriter.print("hello");
        replay(jspWriter, block, context);
        content.print("hello");
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#print(java.lang.Object)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintObject() throws IOException
    {
        jspWriter.print((Object) new Integer(42));
        replay(jspWriter, block, context);
        content.print((Object) new Integer(42));
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnString() throws IOException
    {
        jspWriter.println("hello");
        replay(jspWriter, block, context);
        content.println("hello");
        verify(jspWriter, block, context);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityBodyContent#println(java.lang.Object)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnObject() throws IOException
    {
        jspWriter.println((Object) new Integer(42));
        replay(jspWriter, block, context);
        content.println((Object) new Integer(42));
        verify(jspWriter, block, context);
    }

}

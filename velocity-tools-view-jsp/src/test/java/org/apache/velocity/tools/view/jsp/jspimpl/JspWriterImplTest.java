package org.apache.velocity.tools.view.jsp.jspimpl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link JspWriterImpl}.
 *
 */
public class JspWriterImplTest
{

    private Writer writer;

    private JspWriterImpl jspWriter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        writer = createMock(Writer.class);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#write(int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteInt() throws IOException
    {
        writer.write(10);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.write(10);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#write(char[])}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteCharArray() throws IOException
    {
        writer.write(aryEq("hello".toCharArray()), eq(0), eq(5));

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.write("hello".toCharArray());
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#write(char[], int, int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteCharArrayIntInt() throws IOException
    {
        writer.write(aryEq("hello".toCharArray()), eq(2), eq(3));

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.write("hello".toCharArray(), 2, 3);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#flush()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testFlush() throws IOException
    {
        writer.flush();

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.flush();
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#close()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testClose() throws IOException
    {
        writer.close();

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.close();
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#newLine()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testNewLine() throws IOException
    {
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.newLine();
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(boolean)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintBoolean() throws IOException
    {
        writer.write("true", 0, 4);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print(true);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(char)}.
     * @throws IOException
     */
    @Test
    public void testPrintChar() throws IOException
    {
        writer.write("x", 0, 1);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print('x');
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintInt() throws IOException
    {
        writer.write("152", 0, 3);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print(152);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(long)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintLong() throws IOException
    {
        writer.write("152", 0, 3);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print(152l);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(float)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintFloat() throws IOException
    {
        writer.write("0.5", 0, 3);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print(0.5f);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(double)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintDouble() throws IOException
    {
        writer.write("0.5", 0, 3);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print(0.5d);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(char[])}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintCharArray() throws IOException
    {
        writer.write(aryEq("hello".toCharArray()), eq(0), eq(5));

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print("hello".toCharArray());
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintln() throws IOException
    {
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println();
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(boolean)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnBoolean() throws IOException
    {
        writer.write("true", 0, 4);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println(true);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(char)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnChar() throws IOException
    {
        writer.write("x", 0, 1);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println('x');
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnInt() throws IOException
    {
        writer.write("152", 0, 3);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println(152);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(long)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnLong() throws IOException
    {
        writer.write("152", 0, 3);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println(152l);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(float)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnFloat() throws IOException
    {
        writer.write("0.5", 0, 3);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println(0.5f);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(double)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnDouble() throws IOException
    {
        writer.write("0.5", 0, 3);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println(0.5d);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(char[])}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnCharArray() throws IOException
    {
        writer.write(aryEq("hello".toCharArray()), eq(0), eq(5));
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println("hello".toCharArray());
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#clear()}.
     * @throws IOException If something goes wrong.
     */
    @Test(expected = IllegalStateException.class)
    public void testClear() throws IOException
    {
        jspWriter = new JspWriterImpl(writer);
        jspWriter.clear();
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#clearBuffer()}.
     * @throws IOException If something goes wrong.
     */
    @Test(expected = IllegalStateException.class)
    public void testClearBuffer() throws IOException
    {
        jspWriter = new JspWriterImpl(writer);
        jspWriter.clearBuffer();
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#getRemaining()}.
     */
    @Test
    public void testGetRemaining()
    {
        jspWriter = new JspWriterImpl(writer);
        assertEquals(0, jspWriter.getRemaining());
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#write(java.lang.String, int, int)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteStringIntInt() throws IOException
    {
        writer.write("hello", 0, 3);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.write("hello", 0, 3);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#write(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testWriteString() throws IOException
    {
        writer.write("hello", 0, 5);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.write("hello");
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintString() throws IOException
    {
        writer.write("hello", 0, 5);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print("hello");
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#print(java.lang.Object)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintObject() throws IOException
    {
        writer.write("null", 0, 4);

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.print((Object) null);
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnString() throws IOException
    {
        writer.write("hello", 0, 5);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println("hello");
        verify(writer);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspWriterImpl#println(java.lang.Object)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testPrintlnObject() throws IOException
    {
        writer.write("null", 0, 4);
        writer.write(JspWriterImpl.lineSeparator, 0, JspWriterImpl.lineSeparator.length());

        replay(writer);
        jspWriter = new JspWriterImpl(writer);
        jspWriter.println((Object) null);
        verify(writer);
    }

}

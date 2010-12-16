package org.apache.velocity.tools.view.jsp.taglib.jspimpl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTBlock;

public class VelocityBodyContent extends BodyContent
{

    private ASTBlock block;

    private InternalContextAdapter context;

    public VelocityBodyContent(JspWriter jspWriter, ASTBlock block,
            InternalContextAdapter context)
    {
        super(jspWriter);
        this.block = block;
        this.context = context;
    }

    @Override
    public Reader getReader()
    {
        return new StringReader(getString());
    }

    @Override
    public String getString()
    {
        StringWriter stringWriter = new StringWriter();
        try
        {
            block.render(context, stringWriter);
            stringWriter.close();
            return stringWriter.toString();
        } catch (MethodInvocationException e)
        {
            throw new VelocityToolsJspException("Cannot invoke a method while rendering a body", e);
        } catch (ResourceNotFoundException e)
        {
            throw new VelocityToolsJspException("Cannot find a resource while rendering a body", e);
        } catch (ParseErrorException e)
        {
            throw new VelocityToolsJspException("Cannot parse while rendering a body", e);
        } catch (IOException e)
        {
            throw new VelocityToolsJspException("I/O exception while rendering a body", e);
        }
    }

    @Override
    public void writeOut(Writer out) throws IOException
    {
        block.render(context, out);
    }

    @Override
    public void newLine() throws IOException
    {
        getEnclosingWriter().newLine();
    }

    @Override
    public void print(boolean b) throws IOException
    {
        getEnclosingWriter().print(b);
    }

    @Override
    public void print(char c) throws IOException
    {
        getEnclosingWriter().print(c);
    }

    @Override
    public void print(int i) throws IOException
    {
        getEnclosingWriter().print(i);
    }

    @Override
    public void print(long l) throws IOException
    {
        getEnclosingWriter().print(l);
    }

    @Override
    public void print(float f) throws IOException
    {
        getEnclosingWriter().print(f);
    }

    @Override
    public void print(double d) throws IOException
    {
        getEnclosingWriter().print(d);
    }

    @Override
    public void print(char[] s) throws IOException
    {
        getEnclosingWriter().print(s);
    }

    @Override
    public void print(String s) throws IOException
    {
        getEnclosingWriter().print(s);
    }

    @Override
    public void print(Object obj) throws IOException
    {
        getEnclosingWriter().print(obj);
    }

    @Override
    public void println() throws IOException
    {
        getEnclosingWriter().println();
    }

    @Override
    public void println(boolean x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(char x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(int x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(long x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(float x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(double x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(char[] x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(String x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void println(Object x) throws IOException
    {
        getEnclosingWriter().println(x);
    }

    @Override
    public void clear() throws IOException
    {
        getEnclosingWriter().clear();
    }

    @Override
    public void clearBuffer() throws IOException
    {
        getEnclosingWriter().clearBuffer();
    }

    @Override
    public void close() throws IOException
    {
        getEnclosingWriter().close();
    }

    @Override
    public int getRemaining()
    {
        return getEnclosingWriter().getRemaining();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        getEnclosingWriter().write(cbuf, off, len);
    }

}

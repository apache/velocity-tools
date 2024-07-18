package org.apache.velocity.tools.view.jsp.jspimpl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.JspTag;
import jakarta.servlet.jsp.tagext.SimpleTag;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagAdapter;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;
import org.junit.Test;

/**
 * Tests {@link JspUtils}.
 *
 */
public class JspUtilsTest
{
    /**
     * The attribute name under which the latest encountered tag is put.
     */
    private final static String LATEST_TAG_ATTRIBUTE_NAME = "org.apache.velocity.tools.view.jsp.taglib.jspimpl.LATEST_TAG";

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspUtils#setLatestTag(org.apache.velocity.context.Context, jakarta.servlet.jsp.tagext.JspTag)}.
     */
    @Test
    public void testSetLatestTag()
    {
        Context context = createMock(Context.class);
        JspTag tag = createMock(JspTag.class);

        expect(context.put(LATEST_TAG_ATTRIBUTE_NAME, tag)).andReturn(null);

        replay(context, tag);
        JspUtils.setLatestTag(context, tag);
        verify(context, tag);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspUtils#getLatestJspTag(org.apache.velocity.context.Context)}.
     */
    @Test
    public void testGetLatestJspTag()
    {
        Context context = createMock(Context.class);
        JspTag tag = createMock(JspTag.class);

        expect(context.get(LATEST_TAG_ATTRIBUTE_NAME)).andReturn(tag);

        replay(context, tag);
        assertSame(tag, JspUtils.getLatestJspTag(context));
        verify(context, tag);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspUtils#wrapTag(jakarta.servlet.jsp.tagext.JspTag)}.
     */
    @Test
    public void testWrapTag()
    {
        Tag tag = createMock(Tag.class);
        SimpleTag simpleTag = createMock(SimpleTag.class);

        replay(tag, simpleTag);
        assertSame(tag, JspUtils.wrapTag(tag));
        assertSame(simpleTag, ((TagAdapter) JspUtils.wrapTag(simpleTag)).getAdaptee());
        verify(tag, simpleTag);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspUtils#executeSimpleTag(org.apache.velocity.context.InternalContextAdapter, org.apache.velocity.runtime.parser.node.Node, jakarta.servlet.jsp.PageContext, jakarta.servlet.jsp.tagext.SimpleTag)}.
     * @throws IOException If something goes wrong.
     * @throws JspException If something goes wrong.
     */
    @Test
    public void testExecuteSimpleTag() throws JspException, IOException
    {
        InternalContextAdapter context = createMock(InternalContextAdapter.class);
        Node node = createMock(Node.class);
        PageContext pageContext = createMock(PageContext.class);
        SimpleTag tag = createMock(SimpleTag.class);
        ASTBlock block = createMock(ASTBlock.class);

        tag.setJspBody(isA(VelocityJspFragment.class));
        expect(node.jjtGetChild(1)).andReturn(block);
        tag.doTag();

        replay(context, node, pageContext, block, tag);
        JspUtils.executeSimpleTag(context, node, pageContext, tag);
        verify(context, node, pageContext, block, tag);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.JspUtils#executeTag(org.apache.velocity.context.InternalContextAdapter, org.apache.velocity.runtime.parser.node.Node, jakarta.servlet.jsp.PageContext, jakarta.servlet.jsp.tagext.Tag)}.
     * @throws JspException If something goes wrong.
     * @throws IOException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     */
    @Test
    public void testExecuteTag() throws JspException, MethodInvocationException, ResourceNotFoundException, ParseErrorException, IOException
    {
        InternalContextAdapter context = createMock(InternalContextAdapter.class);
        Node node = createMock(Node.class);
        PageContext pageContext = createMock(PageContext.class);
        BodyTag tag = createMock(BodyTag.class);
        ASTBlock block = createMock(ASTBlock.class);
        JspWriter writer = createMock(JspWriter.class);

        expect(tag.doStartTag()).andReturn(BodyTag.EVAL_BODY_BUFFERED);
        tag.setBodyContent(isA(VelocityBodyContent.class));
        tag.doInitBody();
        expect(node.jjtGetChild(1)).andReturn(block);
        expect(tag.doAfterBody()).andReturn(BodyTag.SKIP_BODY);
        expect(tag.doEndTag()).andReturn(BodyTag.EVAL_PAGE);
        expect(pageContext.getOut()).andReturn(writer);
        expect(block.render(eq(context), isA(StringWriter.class))).andReturn(true);

        replay(context, node, pageContext, block, tag, writer);
        JspUtils.executeTag(context, node, pageContext, tag);
        verify(context, node, pageContext, block, tag, writer);
    }

}

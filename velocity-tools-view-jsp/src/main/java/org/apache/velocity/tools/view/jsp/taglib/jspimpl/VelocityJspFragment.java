package org.apache.velocity.tools.view.jsp.taglib.jspimpl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTBlock;

public class VelocityJspFragment extends JspFragment
{

    private PageContext pageContext;

    private ASTBlock block;

    private InternalContextAdapter context;

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

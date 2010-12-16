package org.apache.velocity.tools.view.jsp.taglib.jspimpl;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagAdapter;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;

public class JspUtils
{
    private final static String LATEST_TAG_ATTRIBUTE_NAME = "org.apache.velocity.tools.view.jsp.taglib.jspimpl.LATEST_TAG";

    public static void setLatestTag(Context context, JspTag tag) {
        context.put(LATEST_TAG_ATTRIBUTE_NAME, tag);
    }

    public static JspTag getLatestJspTag(Context context) {
        return (JspTag) context.get(LATEST_TAG_ATTRIBUTE_NAME);
    }

    public static Tag wrapTag(JspTag tag) {
        if (tag == null) {
            return null;
        }
        if (tag instanceof Tag)
        {
            return (Tag) tag;
        }
        if (tag instanceof SimpleTag)
        {
            return new TagAdapter((SimpleTag) tag);
        }
        throw new VelocityToolsJspException(
                "Cannot get tag that is not a Tag nor a SimpleTag, class "
                        + tag.getClass().getCanonicalName());
    }

    public static void executeSimpleTag(InternalContextAdapter context,
            Node node, PageContext pageContext, SimpleTag tag)
            throws JspException, IOException
    {
        tag.setJspBody(new VelocityJspFragment(pageContext, (ASTBlock) node
                .jjtGetChild(1), context));
        tag.doTag();
    }

    public static void executeTag(InternalContextAdapter context, Node node,
            PageContext pageContext, Tag tag) throws JspException
    {
        int result = tag.doStartTag();
        if (tag instanceof BodyTag)
        {
            BodyTag bodyTag = (BodyTag) tag;
            BodyContent bodyContent = new VelocityBodyContent(
                    pageContext.getOut(), (ASTBlock) node.jjtGetChild(1),
                    context);
            switch (result)
            {
            case BodyTag.EVAL_BODY_BUFFERED:
                bodyTag.setBodyContent(bodyContent);
                bodyTag.doInitBody();
            case BodyTag.EVAL_BODY_INCLUDE:
                bodyContent.getString();
            default:
                break;
            }
            while (bodyTag.doAfterBody() == BodyTag.EVAL_BODY_AGAIN) {
                bodyContent.getString();
            }
        }
        tag.doEndTag();
    }

}

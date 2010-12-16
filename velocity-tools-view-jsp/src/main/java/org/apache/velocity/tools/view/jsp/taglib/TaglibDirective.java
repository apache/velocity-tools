package org.apache.velocity.tools.view.jsp.taglib;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.PageContext;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.tools.view.jsp.taglib.jspimpl.VelocityPageContext;

public class TaglibDirective extends Directive
{

	private static final String PAGE_CONTEXT_ATTRIBUTE_NAME = "org.apache.velocity.tools.view.jsp.taglib.PAGE_CONTEXT";

	@Override
    public String getName()
    {
	    return "taglib";
    }

	@Override
    public int getType()
    {
	    return LINE;
    }

	@Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException,
            MethodInvocationException
    {
		ViewContext viewContext = (ViewContext) context
		        .getInternalUserContext();
		PageContext pageContext = (PageContext) context.get(PAGE_CONTEXT_ATTRIBUTE_NAME);
		if (pageContext == null) {
			pageContext = new VelocityPageContext(context, writer, viewContext);
			context.put(PAGE_CONTEXT_ATTRIBUTE_NAME, pageContext);
		}
	    return true;
    }

}

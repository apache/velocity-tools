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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.JspTag;
import jakarta.servlet.jsp.tagext.SimpleTag;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagAdapter;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Some utilities to work with JSP.
 */
public class JspUtils
{
    /**
     * The attribute name under which the latest encountered tag is put.
     */
    private final static String LATEST_TAG_ATTRIBUTE_NAME = "org.apache.velocity.tools.view.jsp.taglib.jspimpl.LATEST_TAG";

    /**
     * Sets the latest tag encountered.
     *
     * @param context The Velocity context.
     * @param tag The tag.
     */
    public static void setLatestTag(Context context, JspTag tag) {
        context.put(LATEST_TAG_ATTRIBUTE_NAME, tag);
    }

    /**
     * Returns the latest tag encountered.
     *
     * @param context The Velocity context.
     * @return The latest tag.
     */
    public static JspTag getLatestJspTag(Context context) {
        return (JspTag) context.get(LATEST_TAG_ATTRIBUTE_NAME);
    }

    /**
     * If necessary, wraps a {@link SimpleTag} into a {@link Tag}.
     *
     * @param tag The tag to (possibly) wrap.
     * @return The wrapped tag, or the tag passed as parameter if it was not necessary.
     */
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

    /**
     * Executes a {@link SimpleTag}.
     *
     * @param context The directive context.
     * @param node The main node of the directive.
     * @param pageContext The page context.
     * @param tag The tag to execute.
     * @throws JspException If something goes wrong.
     * @throws IOException If something goes wrong.
     */
    public static void executeSimpleTag(InternalContextAdapter context,
            Node node, PageContext pageContext, SimpleTag tag)
            throws JspException, IOException
    {
        tag.setJspBody(new VelocityJspFragment(pageContext, (ASTBlock) node
                .jjtGetChild(1), context));
        tag.doTag();
    }

    /**
     * Executes a {@link Tag}.
     *
     * @param context The directive context.
     * @param node The main node of the directive.
     * @param pageContext The page context.
     * @param tag The tag to execute.
     * @throws JspException If something goes wrong.
     */
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

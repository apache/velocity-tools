/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.view.servlet;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.velocity.tools.view.ToolboxRuleSet;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;
import org.apache.velocity.tools.view.servlet.ServletToolInfo;

/**
 * <p>The set of Digester rules required to parse a toolbox
 * configuration file (<code>toolbox.xml</code>) for the
 * ServletToolboxManager class.</p> 
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @version $Id: ServletToolboxRuleSet.java,v 1.2 2003/08/02 20:43:10 nbubna Exp $
 */
public class ServletToolboxRuleSet extends ToolboxRuleSet
{

    /**
     * Overrides {@link ToolboxRuleSet} to add create-session rule.
     * 
     * <p>These rules assume that an instance of
     * <code>org.apache.velocity.tools.view.ServletToolboxManager</code> is
     * pushed onto the evaluation stack before parsing begins.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester)
    {
        digester.addRule("toolbox/create-session", new CreateSessionRule());
        digester.addRule("toolbox/xhtml", new XhtmlRule());
        super.addRuleInstances(digester);
    }


    /**
     * Overrides {@link ToolboxRuleSet} to add rule for scope element.
     */
    protected void addToolRules(Digester digester)
    {
        super.addToolRules(digester);
        digester.addBeanPropertySetter("toolbox/tool/scope", "scope");
    }


    /**
     * Overrides {@link ToolboxRuleSet} to use ServletToolInfo class.
     */
    protected Class getToolInfoClass()
    {
        return ServletToolInfo.class;
    }


    /****************************** Custom Rules *****************************/

    /**
     * Abstract rule for configuring boolean options on the parent 
     * object/element of the matching element.
     */
    protected abstract class BooleanConfigRule extends Rule
    {
        public void body(String ns, String name, String text) throws Exception
        {
            Object parent = digester.peek();
            if ("yes".equalsIgnoreCase(text))
            {
                setBoolean(parent, Boolean.TRUE);
            }
            else
            {
                setBoolean(parent, Boolean.valueOf(text));
            }
        }

        /**
         * Takes the parent object and boolean value in order to
         * call the appropriate method on the parent for the
         * implementing rule.
         *
         * @param parent the parent object/element in the digester's stack
         * @param value the boolean value contained in the current element
         */
        public abstract void setBoolean(Object parent, Boolean value) 
            throws Exception;
    }


    /**
     * Rule that sets <code>setCreateSession()</code> for the top object
     * on the stack, which must be a
     * <code>org.apache.velocity.tools.ServletToolboxManager</code>.
     */
    protected final class CreateSessionRule extends BooleanConfigRule
    {
        public void setBoolean(Object obj, Boolean b) throws Exception
        {
            ((ServletToolboxManager)obj).setCreateSession(b.booleanValue());
        }
    }


    /**
     * Rule that sets <code>setCreateSession()</code> for the top object
     * on the stack, which must be a
     * <code>org.apache.velocity.tools.ServletToolboxManager</code>.
     */
    protected final class XhtmlRule extends BooleanConfigRule
    {
        public void setBoolean(Object obj, Boolean b) throws Exception
        {
            ((ServletToolboxManager)obj).setXhtml(b);
        }
    }

}

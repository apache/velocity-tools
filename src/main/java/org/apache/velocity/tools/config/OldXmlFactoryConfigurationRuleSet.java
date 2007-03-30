package org.apache.velocity.tools.config;

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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;

/**
 * <p>The set of Digester rules required to parse a old toolbox
 * configuration file (<code>toolbox.xml</code>).</p>
 *
 * @author Nathan Bubna
 * @deprecated This is provided merely for 1.x compatibility.
 * @version $Id: OldXmlFactoryConfigurationRuleSet.java 511959 2007-02-26 19:24:39Z nbubna $
 */
class OldXmlFactoryConfigurationRuleSet extends RuleSetBase
{
    public void addRuleInstances(Digester digester)
    {
        digester.addRule("toolbox/create-session", new CreateSessionRule());
        digester.addRule("toolbox/xhtml", new XhtmlRule());

        digester.addObjectCreate("toolbox", ToolboxConfiguration.class);
        digester.addSetNext("toolbox", "addToolbox");

        digester.addObjectCreate("toolbox/tool", ToolConfiguration.class);
        digester.addBeanPropertySetter("toolbox/tool/key", "key");
        digester.addBeanPropertySetter("toolbox/tool/class", "classname");
        digester.addBeanPropertySetter("toolbox/tool/request-path", "restrictTo");
        digester.addRule("toolbox/tool/scope", new ScopeRule());
        digester.addRule("toolbox/tool/parameter", new ParameterRule());
        digester.addSetNext("toolbox/tool", "addTool");

        digester.addObjectCreate("toolbox/data", Data.class);
        digester.addSetProperties("toolbox/data");
        digester.addBeanPropertySetter("toolbox/data/key", "key");
        digester.addBeanPropertySetter("toolbox/data/value", "value");
        digester.addRule("toolbox/data", new SetNextDataRule());
    }


    protected class ScopeRule extends Rule
    {
        public void body(String namespace, String element, String value)
            throws Exception
        {
            ToolConfiguration tool = (ToolConfiguration)digester.peek(0);
            ToolboxConfiguration toolbox = (ToolboxConfiguration)digester.peek(1);
            // if the scope is different than that of the current toolbox
            if (value != null && !value.equals(toolbox.getScope()))
            {
                // add the old ToolboxConfiguration to the FactoryConfiguration
                FactoryConfiguration factory = (FactoryConfiguration)digester.peek(2);
                factory.addToolbox(toolbox);

                // pop off the old toolbox and the tool
                digester.pop();
                digester.pop();

                // and push a new toolbox on the stack with the new scope
                ToolboxConfiguration newbox = new ToolboxConfiguration();
                newbox.setScope(value);
                digester.push(newbox);

                // push the tool back on the stack
                digester.push(tool);
            }
        }
    }

    protected class ParameterRule extends Rule
    {
        public void begin(String ns, String ln, Attributes attributes)
            throws Exception
        {
            ToolConfiguration config = (ToolConfiguration)digester.peek();
            String name = attributes.getValue("name");
            String value = attributes.getValue("value");
            config.setProperty(name, value);
        }
    }

    protected class SetNextDataRule extends Rule
    {
        public void end() throws Exception
        {
            Data data = (Data)digester.peek(0);
            FactoryConfiguration factory = (FactoryConfiguration)digester.getRoot();
            factory.addData(data);
        }
    }

    protected abstract class BooleanConfigRule extends Rule
    {
        public void body(String ns, String name, String text) throws Exception
        {
            FactoryConfiguration factory =
                (FactoryConfiguration)digester.getRoot();
            if ("yes".equalsIgnoreCase(text))
            {
                setBoolean(factory, Boolean.TRUE);
            }
            else
            {
                setBoolean(factory, Boolean.valueOf(text));
            }
        }

        public abstract void setBoolean(FactoryConfiguration parent, Boolean value);
    }

    protected class CreateSessionRule extends BooleanConfigRule
    {
        public void setBoolean(FactoryConfiguration factory, Boolean b)
        {
            factory.setProperty("createSession", b);
        }
    }

    protected class XhtmlRule extends BooleanConfigRule
    {
        public void setBoolean(FactoryConfiguration factory, Boolean b)
        {
            // for templates
            factory.setProperty("XHTML", b);
            // for tools
            factory.setProperty("Xhtml", b);
        }
    }

}

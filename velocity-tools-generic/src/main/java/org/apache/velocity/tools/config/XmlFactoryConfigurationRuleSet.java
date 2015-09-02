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
 * <p>This provides set of {@link Rule}s used by 
 * Commons-{@link Digester} to process configuration info
 * formatted as XML.  This is the default RuleSet used by
 * {@link XmlFactoryConfiguration}.</p>
 * <p>Here is a short example XML:
 * <pre><code>
 * &lt;tools&gt; 
 *     &lt;data type="number" key="version" value="1.1"/&gt;
 *     &lt;data key="isConvertedProp" value="false" class="java.lang.Boolean" converter="org.apache.commons.beanutils.converters.BooleanConverter"/&gt;
 *     &lt;data type="boolean" key="isKnownType" value="true"/&gt;
 *     &lt;data key="isAutoType" value="true"/&gt;
 *     &lt;data key="foo" value="this is foo."/&gt;
 *     &lt;data key="bar"&gt;this is bar.&lt;/data&gt;
 *     &lt;toolbox scope="request" xhtml="true"&gt;
 *         &lt;tool key="toytool" class="ToyTool" restrictTo="index.vm"/&gt;
 *     &lt;/toolbox&gt;
 *     &lt;toolbox scope="session"&gt;
 *         &lt;property name="createSession" value="true" type="boolean"/&gt;
 *         &lt;tool key="map" class="java.util.HashMap"/&gt;
 *     &lt;/toolbox&gt;
 *     &lt;toolbox scope="application"&gt;
 *         &lt;tool class="org.apache.velocity.tools.generic.DateTool"/&gt;
 *     &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </code></pre></p>
 *
 * @author Nathan Bubna
 * @version $Id: XmlConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class XmlFactoryConfigurationRuleSet extends RuleSetBase
{
    protected Class toolboxConfigurationClass = ToolboxConfiguration.class;
    protected Class toolConfigurationClass = ToolConfiguration.class;
    protected Class dataClass = Data.class;
    protected Class propertyClass = Property.class;

    public void setToolboxConfigurationClass(Class clazz)
    {
        this.toolboxConfigurationClass = clazz;
    }

    public void setToolConfigurationClass(Class clazz)
    {
        this.toolConfigurationClass = clazz;
    }

    public void setDataClass(Class clazz)
    {
        this.dataClass = clazz;
    }

    public void setPropertyClass(Class clazz)
    {
        this.propertyClass = clazz;
    }

    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.  These rules assume that an instance of
     * <code>org.apache.velocity.tools.view.ToolboxManager</code> is pushed
     * onto the evaluation stack before parsing begins.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *        should be added.
     */
    public void addRuleInstances(Digester digester)
    {
        // create the config objects
        digester.addObjectCreate("tools/property", propertyClass);
        digester.addObjectCreate("tools/*/property", propertyClass);
        digester.addObjectCreate("tools/data", dataClass);
        digester.addObjectCreate("tools/toolbox", toolboxConfigurationClass);
        digester.addObjectCreate("tools/toolbox/tool", toolConfigurationClass);

        // to apply matching attributes to specific setters of config objects
        digester.addSetProperties("tools/property");
        digester.addSetProperties("tools/*/property");
        digester.addSetProperties("tools");
        digester.addSetProperties("tools/data");
        digester.addSetProperties("tools/toolbox");
        digester.addSetProperties("tools/toolbox/tool");

        // to add all attributes to config via setProperty(name,value)
        digester.addRule("tools", new PropertyAttributeRule());
        digester.addRule("tools/toolbox", new PropertyAttributeRule());
        digester.addRule("tools/toolbox/tool", new PropertyAttributeRule());

        // for config data & properties whose values are in the body of the tag
        digester.addRule("tools/data", new DataValueInBodyRule());
        digester.addRule("tools/*/property", new DataValueInBodyRule());

        // to finish a config and move on to the next
        digester.addSetNext("tools/property", "addProperty");
        digester.addSetNext("tools/*/property", "addProperty");
        digester.addSetNext("tools/data", "addData");
        digester.addSetNext("tools/toolbox", "addToolbox");
        digester.addSetNext("tools/toolbox/tool", "addTool");
    }


    /****************************** Custom Rules *****************************/

    /**
     * Rule for adding configuration properties
     */
    public static class DataValueInBodyRule extends Rule
    {
        public void body(String namespace, String element, String value)
            throws Exception
        {
            Data data = (Data)digester.peek();
            if (data.getValue() == null)
            {
                data.setValue(value);
            }
        }
    }

    public static class PropertyAttributeRule extends Rule
    {
        public void begin(String namespace, String element, Attributes attributes)
            throws Exception
        {
            Configuration config = (Configuration)digester.peek();

            for (int i=0; i < attributes.getLength(); i++)
            {
                String name = attributes.getLocalName(i);
                if ("".equals(name))
                {
                    name = attributes.getQName(i);
                }

                // don't treat "class" as a property
                if (!"class".equals(name))
                {
                    String value = attributes.getValue(i);
                    config.setProperty(name, value);
                }
            }
        }
    }

}

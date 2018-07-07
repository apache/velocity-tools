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

import java.io.IOException;
import java.net.URL;
import org.xml.sax.SAXException;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSet;

/**
 * <p>This reads in configuration info formatted as an XML file
 * using Commons-{@link Digester}.  This uses 
 * {@link XmlFactoryConfigurationRuleSet} as the default set of rules
 * for processing the XML.  However, you may always change this by
 * passing a new {@link RuleSet} to the {@link #setRuleSet} method.
 * See the configuration documentation on the main web site for 
 * instructions on the XML format supported by the default rules.</p>
 * <p>Example usage:</p>
 * <pre>
 * FactoryConfiguration cfg = new XmlFactoryConfiguration("Dev Tools");
 * cfg.read("devtools.xml");
 * ToolboxFactory factory = cfg.createFactory();
 * </pre>
 *
 * @author Nathan Bubna
 * @version $Id: XmlFactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class XmlFactoryConfiguration extends FileFactoryConfiguration
{
    private RuleSet ruleSet;

    /**
     * Creates an instance
     *
     * @see FactoryConfiguration#setSource(String)
     */
    public XmlFactoryConfiguration()
    {
        this("");
    }

    /**
     * Creates an instance using the specified string
     * as an identifier to distinguish this instance when debugging
     *
     * @param id the name of the "source" of this instance
     * @see FactoryConfiguration#setSource(String)
     */
    public XmlFactoryConfiguration(String id)
    {
        super(XmlFactoryConfiguration.class, id);
        setRuleSet(new XmlFactoryConfigurationRuleSet());
    }

    /**
     * Sets the {@link RuleSet} this loader will use to
     * digest the xml toolbox.
     */
    public void setRuleSet(RuleSet rules)
    {
        this.ruleSet = rules;
    }

    /**
     * <p>Retrieves the rule set Digester should use to parse and load
     * the toolbox for this manager.</p>
     */
    public RuleSet getRuleSet()
    {
        return ruleSet;
    }


    /**
     * <p>Reads an XML document from an {@link URL}
     * and uses it to configure this {@link FactoryConfiguration}.</p>
     * 
     * @param url the URL to read from
     */
    protected void readImpl(URL url) throws IOException
    {
        Digester digester = new Digester();
        digester.setNamespaceAware(true);
        digester.setXIncludeAware(true);
        digester.setValidating(false);
        digester.setUseContextClassLoader(true);
        digester.push(this);
        digester.addRuleSet(getRuleSet());
        try
        {
            digester.parse(url);
        }
        catch (SAXException saxe)
        {
            throw new RuntimeException("There was an error while parsing the InputStream", saxe);
        }
    }

}

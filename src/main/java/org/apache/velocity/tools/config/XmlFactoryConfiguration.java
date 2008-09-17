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

import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;

/**
 * <p>This reads in configuration info formatted as an XML file
 * using Commons-{@link Digester}.  This uses 
 * {@link XmlFactoryConfigurationRuleSet} as the default set of rules
 * for processing the XML.  However, you may always change this by
 * passing a new {@link RuleSet} to the {@link #setRuleSet} method.
 * See the configuration documentation on the main web site for 
 * instructions on the XML format supported by the default rules.</p>
 * <p>Example usage:
 * <pre>
 * FactoryConfiguration cfg = new XmlFactoryConfiguration("Dev Tools");
 * cfg.read("devtools.xml");
 * ToolboxFactory factory = cfg.createFactory();
 * </pre></p>
 *
 * @author Nathan Bubna
 * @version $Id: XmlFactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class XmlFactoryConfiguration extends FileFactoryConfiguration
{
    private RuleSet ruleSet;
    private boolean supportOldXml;

    public XmlFactoryConfiguration()
    {
        this(false, "");
    }

    public XmlFactoryConfiguration(boolean supportOldConfig)
    {
        this(supportOldConfig, String.valueOf(supportOldConfig));
    }

    /**
     * Creates an instance using the specified string
     * as an identifier to distinguish this instance when debugging.
     *
     * @param id the name of the "source" of this instance
     * @see FactoryConfiguration#setSource(String)
     */
    public XmlFactoryConfiguration(String id)
    {
        this(false, id);
    }

    /**
     * Creates an instance using the specified string
     * as an identifier to distinguish this instance when debugging
     * and using the specified setting for supporting the old toolbox.xml
     * format from VelocityTools 1.x.
     *
     * @param supportOldConfig whether the old toolbox.xml format should be supported
     * @param id the name of the "source" of this instance
     * @see FactoryConfiguration#setSource(String)
     */
    public XmlFactoryConfiguration(boolean supportOldConfig, String id)
    {
        super(XmlFactoryConfiguration.class, id);
        setRuleSet(new XmlFactoryConfigurationRuleSet());
        this.supportOldXml = supportOldConfig;
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
     * <p>Reads an XML document from an {@link InputStream}
     * and uses it to configure this {@link FactoryConfiguration}.</p>
     * 
     * @param input the InputStream to read from
     */
    public void read(InputStream input) throws IOException
    {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setUseContextClassLoader(true);
        digester.push(this);
        digester.addRuleSet(getRuleSet());
        if (supportOldXml)
        {
            digester.addRuleSet(new OldXmlFactoryConfigurationRuleSet());
        }
        try
        {
            digester.parse(input);
        }
        catch (SAXException saxe)
        {
            throw new RuntimeException("There was an error while parsing the InputStream", saxe);
        }
    }

}

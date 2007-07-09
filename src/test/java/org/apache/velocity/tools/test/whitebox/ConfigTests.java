package org.apache.velocity.tools.test.whitebox;

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

import java.util.Locale;
import org.junit.*;
import static org.junit.Assert.*;
import org.apache.velocity.tools.view.ViewRenderTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.config.*;

/**
 * <p>Configuration tests.</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class ConfigTests {

    private static final String XML_PATH = "@test.conf.dir@/tools.test.xml";
    private static final String OLD_XML_PATH = "@test.conf.dir@/toolbox.test.xml";
    private static final String PROPS_PATH = "@test.conf.dir@/tools.test.properties";

    protected FactoryConfiguration getBaseConfig()
    {
        FactoryConfiguration base = new FactoryConfiguration();

        Data datum = new Data();
            datum.setKey("version");
            datum.setType("number");
            datum.setValue("2.0");
        base.addData(datum);

        ToolboxConfiguration toolbox = new ToolboxConfiguration();
        toolbox.setScope("request");
        toolbox.setProperty("locale", Locale.US);
            ToolConfiguration tool = new ToolConfiguration();
                tool.setClass(ViewRenderTool.class);
            toolbox.addTool(tool);
        base.addToolbox(toolbox);

        toolbox = new ToolboxConfiguration();
        toolbox.setScope("application");
            tool = new ToolConfiguration();
                tool.setKey("calc");
                tool.setClass(MathTool.class);
            toolbox.addTool(tool);

            tool = new ToolConfiguration();
                tool.setClass(NumberTool.class);
                tool.setProperty("locale", Locale.FRENCH);
            toolbox.addTool(tool);
        base.addToolbox(toolbox);

        return base;
    }


    public @Test void testXmlConfig()
    {
        FileFactoryConfiguration xml = new XmlFactoryConfiguration();
        xml.read(XML_PATH);

        assertConfigEquals(getBaseConfig(), xml);
    }

    public @Test void testOldConfig()
    {
        FileFactoryConfiguration old = new XmlFactoryConfiguration(true);
        old.read(OLD_XML_PATH);

        FactoryConfiguration base = getBaseConfig();
        // remove the request toolbox property locale=en_US manually,
        // because the old format provide no means to set properties
        // on a whole toolbox
        base.getToolbox("request").removeProperty("locale");
        // add the expected deprecationSupportMode property
        base.setProperty("deprecationSupportMode", "true");

        assertConfigEquals(base, old);
    }

    public @Test void testPropsConfig()
    {
        FileFactoryConfiguration props = new PropertiesFactoryConfiguration();
        props.read(PROPS_PATH);

        assertConfigEquals(getBaseConfig(), props);
    }

    public @Test void testEasyConfig()
    {
        EasyFactoryConfiguration easy = new EasyFactoryConfiguration();
        easy.number("version", 2.0);
        easy.toolbox("request")
                .property("locale", Locale.US)
                .tool(ViewRenderTool.class);
        easy.toolbox("application")
                .tool("calc", MathTool.class)
                .tool(NumberTool.class)
                    .property("locale", Locale.FRENCH);

        assertConfigEquals(getBaseConfig(), easy);
    }

    protected void assertConfigEquals(Configuration one, Configuration two)
    {
        assertNotNull(one);
        assertNotNull(two);
if (!one.toString().equals(two.toString()))
System.out.println("\n"+one.getClass().getName()+":\n"+one+"\n"+two.getClass().getName()+":\n"+two+"\n");

        // for now, just compare the toString() output
        assertEquals(one.toString(), two.toString());
    }

}

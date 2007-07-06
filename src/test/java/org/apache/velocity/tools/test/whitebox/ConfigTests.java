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
import org.apache.velocity.tools.generic.DateTool;
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
    private static final String PROPS_PATH = "@test.conf.dir@/tools.test.properties";

    private static final FactoryConfiguration baseline;
    static
    {
        baseline = new FactoryConfiguration();
            ToolboxConfiguration toolbox = new ToolboxConfiguration();
            toolbox.setScope("request");
            toolbox.setProperty("locale", Locale.US);
                ToolConfiguration tool = new ToolConfiguration();
                tool.setClassname(DateTool.class.getName());
                toolbox.addTool(tool);

                tool = new ToolConfiguration();
                tool.setKey("calc");
                tool.setClassname(MathTool.class.getName());
                toolbox.addTool(tool);
            baseline.addToolbox(toolbox);

            toolbox = new ToolboxConfiguration();
            toolbox.setScope("application");
                tool = new ToolConfiguration();
                tool.setClassname(NumberTool.class.getName());
                tool.setProperty("locale", Locale.FRENCH);
                toolbox.addTool(tool);
            baseline.addToolbox(toolbox);
    }

    public @Test void testXmlConfig()
    {
        FileFactoryConfiguration xml = new XmlFactoryConfiguration();
        xml.read(XML_PATH);

        assertConfigEquals(baseline, xml);
    }

    public @Test void testPropsConfig()
    {
        FileFactoryConfiguration props = new PropertiesFactoryConfiguration();
        props.read(PROPS_PATH);

        assertConfigEquals(baseline, props);
    }

    public @Test void testEasyConfig()
    {
        EasyFactoryConfiguration easy = new EasyFactoryConfiguration();
        easy.toolbox("request")
            .property("locale", Locale.US)
            .tool(DateTool.class)
            .tool("calc", MathTool.class);
        easy.toolbox("application")
            .tool(NumberTool.class).property("locale", Locale.FRENCH);

        assertConfigEquals(baseline, easy);
    }

    protected void assertConfigEquals(Configuration one, Configuration two)
    {
        assertNotNull(one);
        assertNotNull(two);

        // for now, just compare the toString() output
        assertEquals(one.toString(), two.toString());
    }

}

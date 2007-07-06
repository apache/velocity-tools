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
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.*;
import static org.junit.Assert.*;

import org.apache.velocity.tools.generic.Alternator;
import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.*;

/**
 * <p>Configuration tests.</p>
 *
 * @author Nathan Bubna
 * @since Velocity Tools 2.0
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
System.out.println("baseline: \n"+baseline.toString()+"\n\n");
    }

    /*public static @BeforeClass void initConfigTests() throws Exception {
        ToolManager manager = new ToolManager(false);
        manager.configure(TOOLBOX_PATH);
        toolbox = manager.createContext();
    }*/

    public @Test void testXmlConfig()
    {
        XmlFactoryConfiguration xml = new XmlFactoryConfiguration();
        xml.read(XML_PATH);
System.out.println("xml: \n"+xml.toString()+"\n\n");

        assertConfigEquals(baseline, xml);
    }

    public @Test void testPropsConfig()
    {
        PropertiesFactoryConfiguration props =
            new PropertiesFactoryConfiguration();
        props.read(PROPS_PATH);
System.out.println("props: \n"+props.toString()+"\n\n");

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
System.out.println("easy: \n"+easy.toString()+"\n\n");

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

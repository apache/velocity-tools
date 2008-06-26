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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.*;
import static org.junit.Assert.*;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.*;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.ResourceTool;

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
        toolbox.setScope(Scope.REQUEST);
        toolbox.setProperty("locale", Locale.US);
            ToolConfiguration tool = new ToolConfiguration();
                tool.setClass(ResourceTool.class);
            toolbox.addTool(tool);
        base.addToolbox(toolbox);

        toolbox = new ToolboxConfiguration();
        toolbox.setScope(Scope.APPLICATION);
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


    public @Test void testBaseConfig()
    {
        assertValid(getBaseConfig());
    }

    public @Test void testXmlConfig()
    {
        FileFactoryConfiguration xml = new XmlFactoryConfiguration();
        xml.read(XML_PATH);

        assertValid(xml);
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

        assertValid(old);
        assertConfigEquals(base, old);
    }

    public @Test void testPropsConfig()
    {
        FileFactoryConfiguration props = new PropertiesFactoryConfiguration();
        props.read(PROPS_PATH);

        assertValid(props);
        assertConfigEquals(getBaseConfig(), props);
    }

    /**
     * Tests that adding two equal configs produces one that is equal
     * to the originals.
     */
    public @Test void testPropsPlusXmlConfig()
    {
        FileFactoryConfiguration props = new PropertiesFactoryConfiguration();
        props.read(PROPS_PATH);

        FileFactoryConfiguration xml = new XmlFactoryConfiguration();
        xml.read(XML_PATH);
        // make sure they're equal
        assertConfigEquals(props, xml);
        // now add them and make sure the result is equal to the original
        xml.addConfiguration(props);
        assertValid(xml);
        assertConfigEquals(xml, props);
    }

    public @Test void testEasyConfig()
    {
        EasyFactoryConfiguration easy = new EasyFactoryConfiguration();
        easy.number("version", 2.0);
        easy.toolbox("request")
                .property("locale", Locale.US)
                .tool(ResourceTool.class);
        easy.toolbox("application")
                .tool("calc", MathTool.class)
                .tool(NumberTool.class)
                    .property("locale", Locale.FRENCH);

        assertValid(easy);
        assertConfigEquals(getBaseConfig(), easy);
    }

    public @Test void testToolsClassConfig()
    {
        FactoryConfiguration java = ConfigurationUtils.getFromClass("tools");

        assertValid(java);
        assertConfigEquals(getBaseConfig(), java);
    }

    public @Test void testDefaultConfig()
    {
        FactoryConfiguration def = ConfigurationUtils.getDefaultTools();
        assertValid(def);
    }

    public @Test void testAutoConfig()
    {
        FactoryConfiguration autoMinusDef = ConfigurationUtils.getAutoLoaded(false);
        assertValid(autoMinusDef);

        assertValid(autoMinusDef);
        assertConfigEquals(getBaseConfig(), autoMinusDef);

        FactoryConfiguration auto = ConfigurationUtils.getAutoLoaded();
        assertValid(auto);

        // get the default tools
        FactoryConfiguration def = ConfigurationUtils.getDefaultTools();
        assertValid(def);
        // add the autoloaded ones (sans defaults) onto the default
        def.addConfiguration(autoMinusDef);
        // and see that it comes out the same
        assertConfigEquals(auto, def);
    }

    public @Test void testBadData()
    {
        Data datum = new Data();
        // a fresh datum should be invalid
        assertInvalid(datum);
        // setting a key is not enough to be valid
        datum.setKey("test");
        assertInvalid(datum);

        // set type to number value to a non-number
        datum.setValue("true");
        datum.setType("number");
        assertInvalid(datum);

        // should fail to convert a decimal string to an integer
        datum.setValue("0.1");
        datum.convertWith(new IntegerConverter());
        assertInvalid(datum);
    }

    public @Test void testData()
    {
        Data datum = new Data();
        datum.setKey("test");
        datum.setValue("true");
        assertValid(datum);

        // check that the default type is "auto"
        assertEquals(datum.getType(), "auto");

        // check that "true" auto-converted to boolean
        assertSame(datum.getConvertedValue(), Boolean.TRUE);

        // check that "1" auto-converts to integer
        datum.setValue("1");
        assertEquals(datum.getConvertedValue(), Integer.valueOf(1));

        // check that a really big number converts to a Long
        datum.setValue(""+Integer.MAX_VALUE+"0");
        assertEquals(datum.getConvertedValue().getClass(), Long.class);

        // check that "1.2" auto-converts to double
        datum.setValue("1.2");
        assertEquals(datum.getConvertedValue(), 1.2);
        assertEquals(datum.getConvertedValue().getClass(), Double.class);

        // check that java.lang.Integer.MIN_VALUE auto-converts to the field
        datum.setValue("java.lang.Integer.MIN_VALUE");
        assertEquals(datum.getConvertedValue(), Integer.MIN_VALUE);

        // check that "yes" also auto-converts to boolean
        datum.setValue("yes");
        assertSame(datum.getConvertedValue(), Boolean.TRUE);

        // check boolean type
        datum.setType("boolean");
        assertValid(datum);
        assertSame(datum.getConvertedValue(), Boolean.TRUE);

        // check valid field type
        datum.setValue("java.lang.Boolean.TRUE");
        datum.setType("field");
        assertValid(datum);
        assertSame(datum.getConvertedValue(), Boolean.TRUE);

        // check invalid field value
        datum.setValue("blahblah");
        assertInvalid(datum);

        // check number type
        datum.setValue("3.16");
        datum.setType("number");
        assertValid(datum);
        assertEquals(datum.getConvertedValue(), new Double(3.16));

        // check string type
        datum.setType("string");
        assertValid(datum);
        assertEquals(datum.getConvertedValue(), "3.16");

        // check list type (singleton)
        datum.setType("list");
        assertValid(datum);
        assertEquals(datum.getConvertedValue(), Collections.singletonList("3.16"));

        // ok, try a three item list
        datum.setValue("1,2,3");
        assertValid(datum);
        List three = new ArrayList(3);
        three.add("1");
        three.add("2");
        three.add("3");
        assertEquals(datum.getConvertedValue(), three);

        // turn that into a list of numbers
        datum.setType("list.number");
        assertValid(datum);
        three.set(0, new Integer(1));
        three.set(1, new Integer(2));
        three.set(2, new Integer(3));
        assertEquals(datum.getConvertedValue(), three);

        // and a list of booleans
        datum.setType("list.boolean");
        datum.setValue("true,false");
        List two = new ArrayList(2);
        two.add(Boolean.TRUE);
        two.add(Boolean.FALSE);
        assertEquals(datum.getConvertedValue(), two);

        // and a list of fields
        datum.setType("list.field");
        datum.setValue("java.lang.Boolean.TRUE,java.lang.Boolean.FALSE");
        assertEquals(datum.getConvertedValue(), two);

        //TODO: test converter/target class stuff
    }

    public @Test void testConfiguration()
    {
        Configuration conf = new Configuration();
        // a fresh config should be valid
        assertValid(conf);

        // add and retrieve a simple string property
        conf.setProperty("string", "whatever");
        assertValid(conf);
        assertEquals("whatever", conf.getProperty("string").getValue());

        // add and retrieve a simple boolean property
        conf.setProperty("boolean", "true");
        assertValid(conf);
        assertEquals(Boolean.TRUE, conf.getProperty("boolean").getConvertedValue());

        // add and retrieve an arbitrary object property
        conf.setProperty("testclass", this);
        assertValid(conf);
        assertSame(this, conf.getProperty("testclass").getValue());

        // test addConfiguration
        Configuration cfg = new Configuration();
        cfg.setProperty("string", "whoever");
        conf.addConfiguration(cfg);
        assertEquals("whoever", conf.getProperty("string").getValue());

        //TODO: test adding convertable properties
    }

    //TODO: public @Test void testCompoundConfiguration()

    public @Test void testBadToolConfig()
    {
        ToolConfiguration tool = new ToolConfiguration();
        // a fresh tool config should be invalid
        assertInvalid(tool);

        // set a fake class name and confirm it is invalid
        tool.setClassname("no.such.Class");
        assertInvalid(tool);
    }

    public @Test void testToolConfig()
    {
        ToolConfiguration tool = new ToolConfiguration();

        // set a real class, confirm it is valid
        tool.setClass(OldTool.class);
        assertValid(tool);
        // and confirm the default key
        assertEquals("old", tool.getKey());

        // the toString() should describe this as old, due
        // to the non-deprecated init() method
        assertTrue((tool.toString().indexOf("Old") >= 0));

        // change to a more modern class, confirm it's valid
        tool.setClassname(FakeTool.class.getName());
        assertValid(tool);
        // and confirm the default key annotation works
        assertEquals("test", tool.getKey());

        // change the key and ensure it overrides the default
        tool.setKey("fake");
        assertEquals("fake", tool.getKey());

        // test that adding A to B copies A's class to B when B has no class
        ToolConfiguration alt = new ToolConfiguration();
        assertNull(alt.getClassname());
        alt.addConfiguration((Configuration)tool);
        assertNotNull(alt.getClassname());
        assertEquals(FakeTool.class.getName(), alt.getClassname());

        // test that adding A to B doesn't copy A's class to B when A has no class
        alt = new ToolConfiguration();
        assertNull(alt.getClassname());
        tool.addConfiguration(alt);
        assertEquals(FakeTool.class.getName(), tool.getClassname());
    }

    //TODO: add tests for ToolboxConfiguration
    //TODO: add tests for FactoryConfiguration



    /************* Support classes and methods ******************/


    public static class OldTool
    {
        public void init(Object foo)
        {
            // does nothing
        }

        // exists only to keep the testrunner happy
        public @Test @Ignore void foo() {}
    }

    @DefaultKey("test")
    public static class FakeTool
    {
        // exists only to keep the testrunner happy
        public @Test @Ignore void foo() {}
    }


    protected void assertConfigEquals(Configuration one, Configuration two)
    {
        assertNotNull(one);
        assertNotNull(two);

        // for now, just compare the toString() output
        assertEquals(one.toString(), two.toString());
    }

    protected void assertConfigEquals(FactoryConfiguration one, FactoryConfiguration two)
    {
        assertNotNull(one);
        assertNotNull(two);

        // for now, just compare the toString() output without source info
        assertEquals(one.toString(false), two.toString(false));
    }

    protected void assertValid(Data valid)
    {
        assertNotNull(valid);
        try
        {
            valid.validate();
            // if we get past the call above, then the test passed
        }
        catch (ConfigurationException e)
        {
            // then the data was not valid
            fail("\n**** Unexpected Invalid Data ****\n" + valid
                 + "\n" + e);
        }
    }

    protected void assertValid(Configuration valid)
    {
        assertNotNull(valid);
        try
        {
            valid.validate();
            // if we get past the call above, then the test passed
        }
        catch (ConfigurationException e)
        {
            // then the config was not valid
            fail("\n**** Unexpected Invalid Configuration ****\n" + valid
                 + "\n" + e);
        }
    }

    protected void assertInvalid(Data invalid)
    {
        try
        {
            invalid.validate();
            // if we get past the call above, then the test failed
            fail("\n**** Unexpected Valid Data ****\n" + invalid);
        }
        catch (ConfigurationException e)
        {
            // then the data was invalid, as it ought to be
        }
    }

    protected void assertInvalid(Configuration invalid)
    {
        try
        {
            invalid.validate();
            // if we get past the call above, then the test failed
            fail("\n**** Unexpected Valid Configuration ****\n" + invalid);
        }
        catch (ConfigurationException e)
        {
            // then the config was invalid, as it ought to be
        }
    }

}

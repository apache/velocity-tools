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
import java.util.List;
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
import org.apache.velocity.tools.generic.FieldTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.ToolContext;

/**
 * <p>Generic tools whitebox tests.</p>
 *
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @author Nathan Bubna
 * @since Velocity Tools 1.3
 * @version $Id$
 */

public class GenericToolsTests {

    private static final String TOOLBOX_PATH = "@test.conf.dir@/whiteboxtest-toolbox.xml";

    private static ToolContext toolbox = null;

    public static @BeforeClass void initGenericToolsTests() throws Exception {
        ToolManager manager = new ToolManager(false);
        manager.configure(TOOLBOX_PATH);
        toolbox = manager.createContext();
    }

    protected void assertStringEquals(String expected, Object testThis) {
        assertEquals(expected, String.valueOf(testThis));
    }

    public @Test void testDateTool() { /* TODO still incomplete */
        DateTool dateTool = (DateTool)toolbox.get("date");
        assertNotNull(dateTool);
        Date date = new GregorianCalendar(2007,0,2).getTime();
        String disp = "2007-01-02";
        String disp2 = "2007/01/02";
        /* check configured format */
        assertEquals("yyyy-MM-dd",dateTool.getFormat());
        /* test formatting */
        assertEquals(disp,dateTool.format(date));
        assertEquals(disp2,dateTool.format("yyyy/MM/dd",date));
        /* test parsing */
        assertEquals(2007,dateTool.getYear(disp));
        assertEquals(0,dateTool.getMonth(disp));
        assertEquals(2,dateTool.getDay(disp));
    }

    public @Test void testEscapeTool() {
        EscapeTool escapeTool = (EscapeTool)toolbox.get("esc");
        assertNotNull(escapeTool);
        assertEquals("${esc.d}foo ${esc.h}bar()",escapeTool.velocity("$foo #bar()"));
        /* propertyKey */
        assertEquals("\\ C\\:\\\\Program\\ Files",escapeTool.propertyKey(" C:\\Program Files"));
        /* propertyValue */
        assertEquals("\\ C\\:\\\\Program Files",escapeTool.propertyValue(" C:\\Program Files"));
        /* java */
        assertEquals("\\uFFFF\\b\\n\\t\\f\\r\\\"\\\\",escapeTool.java("\uFFFF\b\n\t\f\r\"\\"));
        /* javascript */
        assertEquals("\\uFFFF\\b\\n\\t\\f\\r\\\"\\'\\\\",escapeTool.javascript("\uFFFF\b\n\t\f\r\"'\\"));
        /* html */
        assertEquals("&quot;&amp;&lt;&gt;&nbsp;",escapeTool.html("\"&<>"+(char)160));
        /* url */
        assertEquals("%40%2F%3F%3D+%26",escapeTool.url("@/?= &"));
        /* sql */
        assertEquals("''",escapeTool.sql("'"));
        /* xml */
        assertEquals("&quot;&amp;&lt;&gt;",escapeTool.html("\"&<>"));
        /* unicode */
        assertEquals("\uf00b", escapeTool.unicode("f00b"));
        assertEquals("\u1010", escapeTool.unicode("\\u1010"));
        assertEquals("\u1111", escapeTool.unicode(1111));
    }

    public static String MUTABLE_FIELD = "foo";

    public @Test void testFieldTool() {
        FieldTool fieldTool = (FieldTool)toolbox.get("field");
        assertNotNull(fieldTool);

        // read a constant from the configured included Class
        assertEquals(Integer.MAX_VALUE, fieldTool.get("MAX_VALUE"));

        // read a constant from java.lang.Boolean and make sure it is the same
        assertSame(Boolean.TRUE, fieldTool.in("java.lang.Boolean").get("TRUE"));

        // tell it to read constants from a non-existant class
        // (which should return null)
        assertNull(fieldTool.in("no.such.Class"));

        // tell field tool to read constants from this instance's Class
        // (which should NOT return null)
        assertNotNull(fieldTool.in(this));
        assertEquals(MUTABLE_FIELD, fieldTool.get("MUTABLE_FIELD"));
        // grab the mutable field
        String foo = MUTABLE_FIELD;
        // change it
        MUTABLE_FIELD = MUTABLE_FIELD + MUTABLE_FIELD;
        // make sure it changed
        assertFalse(foo.equals(MUTABLE_FIELD));
        // make sure the fieldtool recognized that it changed
        assertEquals(MUTABLE_FIELD, fieldTool.get("MUTABLE_FIELD"));

        // pass a full field path to the get() method
        assertEquals(Long.MIN_VALUE, fieldTool.get("java.lang.Long.MIN_VALUE"));
    }

    public @Test void testMathTool() {
        MathTool mathTool = (MathTool)toolbox.get("math");
        assertNotNull(mathTool);
        assertEquals(1,mathTool.abs(-1));
        assertEquals(2,mathTool.add(1,1));
        assertEquals(3,mathTool.ceil(2.5));
        assertEquals(4,mathTool.div(8,2));
        assertEquals(5,mathTool.floor(5.1));
        assertEquals(6,mathTool.getAverage(new long[] {5,6,7}));
        /* getTotal() watches the type of its first argument, so assertEquals needs a long */
        assertEquals((long)7,mathTool.getTotal(new long[] {2,2,3}));
        assertEquals(8,mathTool.idiv(130,16));
        assertEquals(9,mathTool.max(9,-10));
        assertEquals(10,mathTool.min(10,20));
        assertEquals(11,mathTool.mod(37,13));
        assertEquals(12,mathTool.mul(3,4));
        assertEquals(13,mathTool.round(12.8));
        assertEquals(14.2,mathTool.roundTo(1,14.18));
        assertEquals(-5.0,mathTool.roundTo(2,-4.999));
        assertEquals(15,mathTool.sub(30,15));
        assertEquals(16,mathTool.pow(4,2));
        assertEquals(17,mathTool.toInteger("17"));
        assertEquals(18.1,mathTool.toDouble("18.1"));
    }

    public @Test void testNumberTool() {
        NumberTool numberTool = (NumberTool)toolbox.get("number");
        assertNotNull(numberTool);
//        assertEquals()
    }

    public @Test void testResourceTool() {
        ResourceTool textTool = (ResourceTool)toolbox.get("text");
        assertNotNull(textTool);

        List<String> keys = textTool.getKeys();
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("hello.whoever"));
        assertTrue(keys.contains("world"));

        keys = textTool.get("hello").getKeys();
        assertTrue(keys.contains("whoever"));
        assertFalse(keys.contains("foo"));

        ResourceTool.Key foo = textTool.get("foo");
        assertStringEquals("bar", foo);

        ResourceTool.Key frenchFoo = foo.locale(Locale.FRENCH);
        assertStringEquals("barre", frenchFoo);

        ResourceTool.Key otherFoo = foo.bundle("resources2");
        assertStringEquals("woogie", otherFoo);

        ResourceTool.Key helloWhoever = textTool.get("hello").get("whoever");
        assertStringEquals("Hello {0}!", helloWhoever);

        ResourceTool.Key helloWorld = helloWhoever.insert(textTool.get("world"));
        assertStringEquals("Hello World!", helloWorld);

        ResourceTool.Key halfFrenchHelloWorld = helloWorld.locale(Locale.FRENCH);
        assertStringEquals("Bonjour World!", halfFrenchHelloWorld);

        ResourceTool.Key frenchTool = textTool.locale("fr");
        ResourceTool.Key frenchHelloWorld =
            frenchTool.get("hello.whoever").insert(frenchTool.get("world"));
        assertStringEquals("Bonjour Monde!", frenchHelloWorld);
    }

    public @Test void testComparisonDateTool() { /* TODO still incomplete */
        ComparisonDateTool dateTool = (ComparisonDateTool)toolbox.get("date");
        assertNotNull(dateTool);
        Calendar date1 = new GregorianCalendar(2007,0,2);
        Calendar date2 = new GregorianCalendar(2007,1,15);
        /* test comparing */
        ComparisonDateTool.Comparison whenIs = dateTool.whenIs(date1, date2);
        assertEquals(0l, whenIs.getYears());
        assertEquals(1l, whenIs.getMonths());
        assertEquals(44l, whenIs.getDays());
        // the toolbox config says to skip months, so this should be in weeks
        assertStringEquals("6 weeks later", whenIs);
    }
}

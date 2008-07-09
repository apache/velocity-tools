package org.apache.velocity.tools.generic;

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

import org.junit.*;
import static org.junit.Assert.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.velocity.tools.generic.ValueParser;
import org.dom4j.Node;

/**
 * <p>Tests for XmlTool</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class XmlToolTests {

    private static final String XML_FILE = "@test.file.dir@/file.xml";

    private static final String XML_STRING =
"<foo>\n  <bar name=\"a\"/>\n  <baz>woogie</baz>\n  <baz>wiggie</baz>\n</foo>";

    public @Test void ctorXmlTool() throws Exception
    {
        try
        {
            new XmlTool();
        }
        catch (Exception e)
        {
            fail("Constructor 'XmlTool()' failed due to: " + e);
        }
    }

    private XmlTool stringBased() throws Exception
    {
        XmlTool xml = new XmlTool();
        xml.parse(XML_STRING);
        return xml;
    }

    private XmlTool fileBased() throws Exception
    {
        XmlTool xml = new XmlTool();
        xml.read(XML_FILE);
        return xml;
    }

    public @Test void testStringFileEquals() throws Exception
    {
        String string = stringBased().toString();
        String file = fileBased().toString();
        //System.out.println("string:\n"+string+"\nfile:\n"+file);
        assertEquals(string, file);
    }

    public @Test void methodAttr_Object() throws Exception
    {
        XmlTool xml = stringBased();
        assertNull(xml.attr("href"));
        xml = xml.find("bar");
        assertNotNull(xml.attr("name"));
        assertEquals("a", xml.attr("name"));
    }

    public @Test void methodAttributes() throws Exception
    {
        XmlTool xml = stringBased();
        Map<String,String> result = xml.attributes();
        assertTrue(result.isEmpty());
        xml = xml.find("bar");
        result = xml.attributes();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("a", result.get("name"));
    }

    public @Test void methodChildren() throws Exception
    {
        XmlTool xml = stringBased();
        assertEquals(1, xml.size());
        XmlTool result = xml.children();
        assertEquals(3, result.size());
    }

    public @Test void methodGetParent() throws Exception
    {
        XmlTool xml = stringBased().find("bar");
        assertNotNull(xml);
        XmlTool foo = xml.getParent();
        assertNotNull(foo);
        assertEquals(1, foo.size());
        assertNull(foo.getParent());
    }

    public @Test void methodParents() throws Exception
    {
        XmlTool foo = stringBased();
        XmlTool xml = foo.find("bar");
        assertEquals(foo.toString(), xml.parents().toString());
        xml = foo.children();
        assertEquals(3, xml.size());
        foo = xml.parents();
        assertEquals(1, foo.size());
    }

    public @Test void methodConfigure_ValueParser() throws Exception
    {
        XmlTool xml = new XmlTool();
        Map<String,String> params = new HashMap<String,String>();
        assertEquals("file", XmlTool.FILE_KEY);
        params.put(XmlTool.FILE_KEY, XML_FILE);
        xml.configure(params);
        assertEquals(1, xml.size());
        assertEquals("foo", xml.getName());
    }

    public @Test void methodFind_Object() throws Exception
    {
        XmlTool xml = stringBased();
        XmlTool result = xml.find((Object)null);
        assertNull(result);
        assertEquals(xml.find("bar").toString(), xml.find("//bar").toString());
        //TODO: test more xpath expressions?
        //TODO: test expressions with no results
    }

    public @Test void methodGetFirst() throws Exception
    {
        XmlTool xml = stringBased();
        assertSame(xml, xml.getFirst());
        xml = xml.children();
        assertEquals(3, xml.size());
        xml = xml.getFirst();
        assertEquals(1, xml.size());
        assertEquals("a", xml.getName());
    }

    public @Test void methodGetLast() throws Exception
    {
        XmlTool xml = stringBased();
        assertSame(xml, xml.getLast());
        xml = xml.children();
        assertEquals(3, xml.size());
        xml = xml.getLast();
        assertEquals(1, xml.size());
        assertEquals("baz", xml.getName());
        assertEquals("wiggie", xml.getText());
    }

    public @Test void methodGetName() throws Exception
    {
        XmlTool xml = stringBased();
        assertEquals(1, xml.size());
        assertEquals("foo", xml.getName());
        xml = xml.find("bar");
        assertEquals("a", xml.getName());
    }

    public @Test void methodGetNodeName() throws Exception
    {
        XmlTool xml = stringBased();
        assertEquals("foo", xml.getNodeName());
        xml = xml.find("baz");
        assertEquals("baz", xml.getNodeName());
    }

    public @Test void methodGetText() throws Exception
    {
        XmlTool xml = stringBased();
        //TODO: prepare the instance for testing
        String result = xml.getText();
        assertEquals((String)null, result);
    }

    public @Test void methodGet_Number() throws Exception
    {
        XmlTool xml = stringBased();
        assertEquals(xml.toString(), xml.get(0).toString());
        xml = xml.children();
        assertEquals("bar", xml.get(0).getNodeName());
        assertEquals("baz", xml.get(1).getName());
        assertEquals("baz", xml.get(2).getName());
        assertNull(xml.get(3));
        assertNull(xml.get(-1));
    }

    public @Test void methodGet_Object() throws Exception
    {
        XmlTool xml = stringBased();
        assertNull(xml.get(null));
        assertNull(xml.get(""));
        assertNull(xml.get("null"));
        Object result = xml.get("bar");
        assertNotNull(result);
        assertTrue(result instanceof XmlTool);
        xml = (XmlTool)result;
        result = null;
        assertNull(result);
        result = xml.get("0");
        assertNotNull(result);
        assertEquals(result.toString(), xml.toString());
        result = null;
        assertNull(result);
        result = xml.get("name");
        assertNotNull(result);
        assertEquals("a", result);
    }

    public @Test void methodIsEmpty() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertTrue(xml.isEmpty());
        xml.parse(XML_STRING);
        assertFalse(xml.isEmpty());
    }

    public @Test void methodIterator() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertNull(xml.iterator());
        xml.parse(XML_STRING);
        Iterator<XmlTool> i = xml.iterator();
        assertNotNull(i);
        XmlTool foo = i.next();
        assertNotNull(foo);
        assertEquals(foo.toString(), xml.toString());
        xml = xml.children();
        i = xml.iterator();
        assertEquals("a", i.next().attr("name"));
        assertEquals("baz", i.next().getName());
        assertEquals("wiggie", i.next().getText());
        assertFalse(i.hasNext());
    }

    public @Test void methodNode() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertNull(xml.node());
        xml.parse(XML_STRING);
        Node n = xml.node();
        assertNotNull(n);
    }

    public @Test void methodParse_Object() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertNull(xml.parse((Object)null));
        assertNull(xml.parse((Object)"><S asdf8 ~$"));
        assertNotNull(xml.parse((Object)XML_STRING));
        //TODO: test other strings?
    }

    public @Test void methodSize() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertEquals(0, xml.size());
        xml.parse(XML_STRING);
        assertEquals(1, xml.size());
        xml = xml.children();
        assertEquals(3, xml.size());
        xml = xml.getLast();
        assertEquals(1, xml.size());
    }

    public @Test void methodToString() throws Exception
    {
        XmlTool xml = new XmlTool();
        assertTrue(xml.toString().startsWith(XmlTool.class.getName()));
        xml.read(XML_FILE);
        assertTrue(xml.toString().startsWith("<foo>"));
        assertTrue(xml.toString().endsWith("</foo>"));
        XmlTool bar = xml.find("bar");
        assertEquals("<bar name=\"a\"/>", bar.toString());
        XmlTool baz = (XmlTool)xml.get("baz");
        assertEquals("<baz>woogie</baz><baz>wiggie</baz>", baz.toString());
    }


}
        
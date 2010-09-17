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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.tools.generic.Alternator;

/**
 * <p>Tests for AlternatorTool</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class AlternatorToolTests {

    public @Test void ctorAlternatorTool() throws Exception
    {
        try
        {
            new AlternatorTool();
        }
        catch (Exception e)
        {
            fail("Constructor 'AlternatorTool()' failed due to: " + e);
        }
    }

    public @Test void methodConfigure_Map() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        Map<String,Object> conf = new HashMap<String,Object>();
        conf.put(AlternatorTool.AUTO_ALTERNATE_DEFAULT_KEY, false);
        tool.configure(conf);
        assertFalse(tool.getAutoAlternateDefault());
    }

    public @Test void methodsGetSetAutoAlternateDefault() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        assertTrue(tool.getAutoAlternateDefault());
        tool.setAutoAlternateDefault(false);
        assertFalse(tool.getAutoAlternateDefault());
        tool.setAutoAlternateDefault(true);
        assertTrue(tool.getAutoAlternateDefault());
    }

    public @Test void methodMake_ObjectVarArgs() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        assertNull(tool.make());
        Alternator result = tool.make(new Object[] { true });
        assertTrue((Boolean)result.getNext());
        assertTrue((Boolean)result.getNext());
        result = tool.make("hi", true, false, 0);
        assertEquals("hi", result.getNext());
        assertTrue((Boolean)result.getNext());
        assertFalse((Boolean)result.getNext());
        assertEquals(0, result.getNext());
        assertEquals("hi", result.getNext());
        result = tool.make(new Object[] { "red", "blue" });
        result.shift();
        assertEquals("blue", result.toString());
    }

    public @Test void methodMake_booleanObjectVarArgs() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        Alternator result = tool.make(false, new Object[] { "red", "blue" });
        assertEquals("red", result.toString());
        assertEquals("red", result.toString());
        result.shift();
        assertEquals("blue", result.toString());
    }

    public @Test void methodAuto_ObjectVarArgs() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        Alternator result = tool.auto(-1,0,null,1);
        assertEquals("-1", result.toString());
        assertEquals(0, result.getCurrent());
        assertEquals("0", result.toString());
        assertEquals(null, result.toString());
        assertEquals("1", result.toString());
        assertEquals("-1", result.toString());
    }

    public @Test void methodManual_ObjectVarArgs() throws Exception
    {
        AlternatorTool tool = new AlternatorTool();
        Alternator result = tool.manual(new Object[] { true, false });
        assertTrue((Boolean)result.getCurrent());
        assertEquals("true", result.toString());
        assertTrue((Boolean)result.getNext());
        assertEquals("false", result.toString());
        assertFalse((Boolean)result.getNext());
        assertEquals("true", result.toString());
    }

}
        

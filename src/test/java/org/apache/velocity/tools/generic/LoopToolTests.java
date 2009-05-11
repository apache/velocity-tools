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
import java.util.*;

/**
 * <p>Tests for LoopTool</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class LoopToolTests {

    public static final String[] ARRAY = { "foo", "bar", "woogie" };

    public @Test void ctorLoopTool() throws Exception
    {
        try
        {
            new LoopTool();
        }
        catch (Exception e)
        {
            fail("Constructor 'LoopTool()' failed due to: " + e);
        }
    }

    public @Test void methodSkip_int() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        // skip nothing
        loop.skip(0);
        assertEquals(i.next(), ARRAY[0]);
        // skip one (should be on 2 now)
        loop.skip(1);
        assertEquals(i.next(), ARRAY[2]);
        // end this
        loop.pop();
        // start over to skip 2
        i = loop.watch(ARRAY);
        loop.skip(2);
        assertEquals(i.next(), ARRAY[2]);
    }

    public @Test void methodSkip_intString() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        Iterator j = loop.watch(ARRAY, "j");
        Iterator k = loop.watch(ARRAY, "k");
        // these should not do anything
        loop.skip(1, null);
        loop.skip(1, "");
        loop.skip(1, "test");
        // these should work
        loop.skip(1, "i");
        loop.skip(1, "j");
        loop.skip(1, "k");
        assertEquals(i.next(), ARRAY[1]);
        assertEquals(j.next(), ARRAY[1]);
        assertEquals(k.next(), ARRAY[1]);
    }

    public @Test void methodStop() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        assertTrue(i.hasNext());
        loop.stop();
        assertFalse(i.hasNext());
    }

    public @Test void methodStopAll() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        Iterator j = loop.watch(ARRAY);
        Iterator k = loop.watch(j);
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());
        loop.stopAll();
        assertFalse(i.hasNext() || j.hasNext() || k.hasNext());
    }

    public @Test void methodStopTo_String() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        Iterator j = loop.watch(ARRAY, "j");
        Iterator k = loop.watch(ARRAY, "k");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());

        // these shouldn't stop anything
        loop.stopTo(null);
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());
        loop.stopTo("");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());
        loop.stopTo("test");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());

        // this should only stop j and k
        loop.stopTo("j");
        assertTrue(i.hasNext());
        assertFalse(j.hasNext() || k.hasNext());
    }

    public @Test void methodStop_String() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        Iterator j = loop.watch(ARRAY, "j");
        Iterator k = loop.watch(ARRAY, "k");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());

        // these shouldn't stop anything
        loop.stop(null);
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());
        loop.stop("");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());
        loop.stop("test");
        assertTrue(i.hasNext() && j.hasNext() && k.hasNext());

        // this should only stop j
        loop.stop("j");
        assertTrue(i.hasNext() && k.hasNext());
        assertFalse(j.hasNext());
    }

    public @Test void methodWatch_Object() throws Exception
    {
        LoopTool loop = new LoopTool();
        // try to watch unwatchable things
        Iterator i = loop.watch(null);
        assertNull(i);
        assertNull(loop.watch(new Object()));
        // watch an array
        assertNotNull(loop.watch(ARRAY));
        // watch an iterator
        assertNotNull(loop.watch(loop.watch(ARRAY)));
        //TODO: watch enumeration
        // watch collection
        assertNotNull(loop.watch(LIST));
        // watch map
        assertNotNull(loop.watch(new HashMap()));
        // watch iterable
        assertNotNull(loop.watch(new MyIterable()));
        // watch object w/iterator method
        assertNotNull(loop.watch(new HasIteratorMethod()));
    }

    static final Collection LIST = Arrays.asList(ARRAY);
    public static class HasIteratorMethod
    {
        public Iterator iterator()
        {
            return LoopToolTests.LIST.iterator();
        }
    }
    public static class MyIterable extends HasIteratorMethod implements Iterable
    {
    }

    public @Test void methodWatch_ObjectString() throws Exception
    {
        LoopTool loop = new LoopTool();
        // null names are invalid
        Iterator i = loop.watch(ARRAY, null);
        assertNull(i);
        // empty names are ok
        assertNotNull(loop.watch(ARRAY, ""));
        // so are real ones
        assertNotNull(loop.watch(ARRAY, "name"));
    }

    public @Test void methodIsFirst() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        assertTrue(loop.isFirst());
        i.next();
        assertTrue(loop.isFirst());
        i.next();
        assertFalse(loop.isFirst());
    }

    public @Test void methodIsFirst_String() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        assertTrue(loop.isFirst());
        i.next();
        Iterator j = loop.watch(ARRAY, "j");
        assertTrue(loop.isFirst());
        assertTrue(loop.isFirst("i"));
        assertTrue(loop.isFirst("j"));
        // check short syntax too
        assertTrue((Boolean)loop.get("first_i"));
        assertTrue((Boolean)loop.get("first_j"));
        i.next();
        assertFalse(loop.isFirst("i"));
        assertFalse((Boolean)loop.get("first_i"));
        j.next();
        assertTrue(loop.isFirst());
        assertTrue(loop.isFirst("j"));
        assertTrue((Boolean)loop.get("first_j"));
        j.next();
        assertFalse(loop.isFirst("j"));
        assertFalse((Boolean)loop.get("first_j"));
    }

    public @Test void methodIsLast() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        assertFalse(loop.isLast());
        i.next();
        assertFalse(loop.isLast());
        i.next();
        assertFalse(loop.isLast());
        i.next();
        assertTrue(loop.isLast());
    }

    public @Test void methodIsLast_String() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        assertFalse(loop.isLast());
        i.next();
        i.next();
        i.next();
        assertTrue(loop.isLast());
        Iterator j = loop.watch(ARRAY, "j");
        assertFalse(loop.isLast());
        assertTrue(loop.isLast("i"));
        assertFalse(loop.isLast("j"));
        // check short syntax too
        assertTrue((Boolean)loop.get("last_i"));
        assertFalse((Boolean)loop.get("last_j"));
        j.next();
        j.next();
        j.next();
        assertTrue(loop.isLast());
        assertTrue(loop.isLast("j"));
        assertTrue((Boolean)loop.get("last_j"));
    }

    public @Test void methodGet() throws Exception
    {
        LoopTool loop = new LoopTool();
        // sync an array with itself
        Iterator i = loop.watch(ARRAY).sync(ARRAY, "twin");
        while (i.hasNext())
        {
            // make sure they match
            assertEquals(i.next(), loop.get("twin"));
        }

        // sync a shorter array with a longer one to be
        // sure the values turn to null once they're gone
        int[] little = { 10, 20, 30 };
        Integer[] big = { 1, 2, 3, 4, 5 };
        i = loop.watch(big).sync(little, "little");
        while (i.hasNext())
        {
            Integer val = (Integer)i.next();
            if (val < 4)
            {
                assertEquals(val * 10, loop.get("little"));
            }
            else
            {
                assertNull(loop.get("little"));
            }
        }
    }

    public @Test void methodGet_StringString() throws Exception
    {
        LoopTool loop = new LoopTool();
        int[] other = { 1, 2, 3 };

        // sync arrays with nested loops using default names
        // the way we iterate over both i and j together below
        // is, of course, impossible in a template, but it
        // makes writing a reasonable test for this method a lot
        // easier.
        //NOTE: this reliese on the default name for synced iterators
        //      being "synced", for i being "loop0", and for j being "loop1"
        Iterator i = loop.watch(ARRAY).sync(other);
        Iterator j = loop.watch(other).sync(ARRAY);
        while (i.hasNext() && j.hasNext())
        {
            // i and loop.synced (aka loop.get("loop1","synced")) should match
            assertEquals(i.next(), loop.get("synced"));
            // j and loop.get("loop0","synced") should match
            assertEquals(j.next(), loop.get("loop0", "synced"));
        }
    }

    public @Test void methodGetCountOrGetIndex() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        assertEquals(0, loop.getCount());
        assertNull(loop.getIndex());
        i.next();
        assertEquals(1, loop.getCount());
        assertEquals(0, loop.getIndex());
        i.next();
        assertEquals(2, loop.getCount());
        assertEquals(1, loop.getIndex());
        i.next();
        assertEquals(3, loop.getCount());
        assertEquals(2, loop.getIndex());
        loop.pop();
        // test that skipped iterations are still included
        i = loop.watch(ARRAY);
        loop.skip(2);
        assertEquals(2, loop.getCount());
        assertEquals(1, loop.getIndex());
    }

    public @Test void methodGetCountOrGetIndex_String() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY, "i");
        assertEquals(0, loop.getCount("i"));
        assertNull(loop.getIndex("i"));
        i.next();
        assertEquals(1, loop.getCount("i"));
        assertEquals(0, loop.getIndex("i"));
        Iterator j = loop.watch(ARRAY, "j");
        loop.skip(2);
        assertEquals(2, loop.getCount("j"));
        assertEquals(1, loop.getIndex("j"));
        assertEquals(1, loop.getCount("i"));
        assertEquals(0, loop.getIndex("i"));
        // check short syntax too
        assertEquals(2, loop.get("count_j"));
        assertEquals(1, loop.get("index_j"));
        assertEquals(1, loop.get("count_i"));
        assertEquals(0, loop.get("index_i"));
    }

    public @Test void aliasMethods() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY);
        assertEquals(loop.isFirst(), loop.getFirst());
        assertEquals(loop.isLast(), loop.getLast());
        i.next();
        assertEquals(loop.isFirst(), loop.getFirst());
        assertEquals(loop.isLast(), loop.getLast());
        i.next();
        assertEquals(loop.isFirst(), loop.getFirst());
        assertEquals(loop.isLast(), loop.getLast());
        i.next();
        assertEquals(loop.isFirst(), loop.getFirst());
        assertEquals(loop.isLast(), loop.getLast());
    }

    public @Test void watchAndExclude() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY).exclude("bar");
        assertEquals(i.next(), "foo");
        assertEquals(i.next(), "woogie");
        assertTrue(loop.isLast());
        assertFalse(i.hasNext());
    }

    public @Test void watchAndStop() throws Exception
    {
        LoopTool loop = new LoopTool();
        Iterator i = loop.watch(ARRAY).stop("bar");
        assertEquals(i.next(), "foo");
        assertTrue(loop.isLast());
        assertFalse(i.hasNext());
    }

    public @Test void method_getDepth() throws Exception
    {
        LoopTool loop = new LoopTool();
        assertEquals(0, loop.getDepth());
        loop.watch(ARRAY);
        assertEquals(1, loop.getDepth());
        loop.watch(ARRAY);
        assertEquals(2, loop.getDepth());
        loop.pop();
        assertEquals(1, loop.getDepth());
        loop.pop();
        assertEquals(0, loop.getDepth());
    }

}
        

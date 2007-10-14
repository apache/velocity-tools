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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * <p>Tests for {@link ClassTool}</p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
public class ClassToolTests {

    public @Test void ctorClassTool() throws Exception
    {
        try
        {
            new ClassTool();
        }
        catch (Exception e)
        {
            fail("Default constructor failed");
        }
    }

    public @Test void ctorClassTool_ClassToolClass() throws Exception
    {
        // null parent should fail
        try
        {
            new ClassTool(null, ClassTool.class);
            fail("Constructor 'ClassTool(null, Class)' worked but shouldn't have.");
        }
        catch (Exception e)
        {
        }

        ClassTool parent = new ClassTool();
        // null class should fail
        try
        {
            new ClassTool(parent, null);
            fail("Constructor 'ClassTool(ClassTool, null)' worked but shouldn't have.");
        }
        catch (Exception e)
        {
        }

        // this one should work
        try
        {
            new ClassTool(parent, ClassToolTests.class);
        }
        catch (Exception e)
        {
            fail("Constructor 'ClassTool(ClassTool, Class)' failed due to: " + e);
        }
    }

    public @Test void methodConfigure_Map() throws Exception
    {
        ClassTool classTool = new ClassTool();
        assertEquals(Object.class, classTool.getType());

        // change the inspected type to Map
        Map<String,Object> conf = new HashMap<String,Object>();
        conf.put(ClassTool.INSPECT_KEY, "java.util.Map");
        classTool.configure(conf);
        assertEquals(Map.class, classTool.getType());
        //TODO: test other configuration settings
    }

    public @Test void methodGetConstructors() throws Exception
    {
        ClassTool classTool = new ClassTool();
        List result = classTool.getConstructors();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        //TODO: test contents of list?
    }

    //TODO: add ConstructorSub tests

    public @Test void methodGetFields() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        List result = classTool.getFields();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        //TODO: test a class that does have fields
    }

    //TODO: add FieldSub tests

    public @Test void methodGetMethods() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        List result = classTool.getMethods();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        //TODO: test contents of list?
    }

    //TODO: add MethodSub tests

    public @Test void methodGetTypes() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        Set result = classTool.getTypes();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        //TODO: test contents of set?
    }

    public @Test void methodGetFullName() throws Exception
    {
        ClassTool classTool = new ClassTool();
        String result = classTool.getFullName();
        assertEquals(result, "java.lang.Object");
    }

    public @Test void methodGetName() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        String result = classTool.getName();
        assertEquals(classTool.getName(), "Object");
    }

    public @Test void methodGetPackage() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertEquals(classTool.getPackage(), "java.lang");
    }

    public @Test void methodGetType() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        Class result = classTool.getType();
        assertEquals(result, Object.class);
        classTool.setType(ClassTool.class);

        result = classTool.getType();
        assertEquals(result, ClassTool.class);
    }

    public @Test void methodInspectSuper() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object which has no super
        assertNull(classTool.inspectSuper());
        classTool.setType(ClassTool.class);
        assertEquals(classTool.inspectSuper().getType(), AbstractLockConfig.class);
    }

    public @Test void methodInspect_Class() throws Exception
    {
        ClassTool classTool = new ClassTool();
        ClassTool result = classTool.inspect(ClassTool.class);
        assertEquals(result.getType(), ClassTool.class);
    }

    public @Test void methodInspect_Object() throws Exception
    {
        ClassTool classTool = new ClassTool();
        ClassTool result = classTool.inspect(classTool);
        assertEquals(result.getType(), ClassTool.class);
    }

    public @Test void methodInspect_String() throws Exception
    {
        ClassTool classTool = new ClassTool();
        assertNull(classTool.inspect((String)null));
        assertNull(classTool.inspect(""));
        assertNull(classTool.inspect("bad"));
        assertEquals(Map.class, classTool.inspect("java.util.Map").getType());
    }

    public @Test void methodIsAbstract() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isAbstract());
        classTool.setType(AbstractLockConfig.class);
        assertTrue(classTool.isAbstract());
    }

    public @Test void methodIsFinal() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isFinal());
        classTool.setType(String.class);
        assertTrue(classTool.isFinal());
    }

    public @Test void methodIsInterface() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isInterface());
        classTool.setType(Map.class);
        assertTrue(classTool.isInterface());
    }

    public @Test void methodIsPrivate() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isPrivate());
        classTool.setType(PrivateStrictStatic.class);
        assertTrue(classTool.isPrivate());
    }

    public @Test void methodIsProtected() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isProtected());
        classTool.setType(ProtectedNoDefaultCtor.class);
        assertTrue(classTool.isProtected());
    }

    public @Test void methodIsPublic() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertTrue(classTool.isPublic());
        classTool.setType(PrivateStrictStatic.class);
        assertFalse(classTool.isPublic());
    }

    public @Test void methodIsStatic() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isStatic());
        classTool.setType(PrivateStrictStatic.class);
        assertTrue(classTool.isStatic());
    }

    public @Test void methodIsStrict() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type is java.lang.Object
        assertFalse(classTool.isStrict());
        classTool.setType(PrivateStrictStatic.class);
        assertTrue(classTool.isStrict());
    }

    public @Test void methodSetClass_Class() throws Exception
    {
        ClassTool classTool = new ClassTool();
        assertEquals(Object.class, classTool.getType());
        classTool.setType(ClassTool.class);
        assertEquals(ClassTool.class, classTool.getType());
    }

    public @Test void methodSupportsNewInstance() throws Exception
    {
        ClassTool classTool = new ClassTool();
        // default type class is java.lang.Object
        assertEquals(classTool.supportsNewInstance(), true);
        classTool.setType(ProtectedNoDefaultCtor.class);
        assertEquals(classTool.supportsNewInstance(), false);
    }

    private static strictfp class PrivateStrictStatic {}

    protected static class ProtectedNoDefaultCtor
    {
        public ProtectedNoDefaultCtor(String foo)
        {
        }
    }

}
        

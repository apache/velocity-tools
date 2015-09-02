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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.DefaultKey;

/**
 * <p>
 * This tool is meant to simplify reflective lookup of information about
 * a {@link Class} and its {@link Field}s, {@link Method}s, and {@link Constructor}s.
 * This is ideally aimed at those wishing to generate documentation, demo code, or
 * other content based on runtime reflection of a specified Class or Classes. It was not
 * designed with reflective execution of code in mind and thus provides no facilities
 * for code execution, nor direct access to the actual methods, constructors or fields
 * of the class being inspected.
 * </p>
 *
 * <p>
 * <pre>
 * Example tools.xml config:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.ClassTool"
 *              inspect="com.org.Foo"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 * <p>
 * If no Class to be inspected is specified, the default is java.lang.Object.
 * </p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id: ClassTool.java 463298 2006-10-12 16:10:32Z henning $
 */
@DefaultKey("class")
public class ClassTool extends SafeConfig
{
    public static final String INSPECT_KEY = "inspect";
    public static final String SHOW_DEPRECATED_KEY = "showDeprecated";

    protected Log log;
    protected Class type;
    protected List<MethodSub> methods;
    protected List<ConstructorSub> constructors;
    protected List<FieldSub> fields;

    private boolean showDeprecated = false;

    /**
     * Creates an instance with target type of {@link Object}.
     */
    public ClassTool()
    {
        setType(Object.class);
    }

    /**
     * Creates a new instance that inspects the specified type
     * and otherwise shares the configuration values of the specified "parent"
     * ClassTool instance.
     */
    protected ClassTool(ClassTool tool, Class type)
    {
        setType(type);
        if (tool == null)
        {
            throw new IllegalArgumentException("parent tool must not be null");
        }

        // manually duplicate configuration of the parent tool
        this.log = tool.log;
        this.showDeprecated = tool.showDeprecated;
        setSafeMode(tool.isSafeMode());
        setLockConfig(tool.isConfigLocked());
    }

    protected void configure(ValueParser values)
    {
        this.log = (Log)values.getValue("log");
        this.showDeprecated =
            values.getBoolean(SHOW_DEPRECATED_KEY, showDeprecated);

        String classname = values.getString(INSPECT_KEY);
        if (classname != null)
        {
            setType(toClass(classname));
        }
    }

    private Class toClass(String name)
    {
        try
        {
            return ClassUtils.getClass(name);
        }
        catch (Exception e)
        {
            if (this.log != null)
            {
                this.log.error("Could not load Class for "+name);
            }
            return null;
        }
    }

    protected void setType(Class type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("target type is null or invalid");
        }
        this.type = type;
    }

    protected static boolean isDeprecated(AnnotatedElement element)
    {
        return (element.getAnnotation(Deprecated.class) != null);
    }

    /**
     * Returns the current showDeprecated setting.
     */
    public boolean getShowDeprecated()
    {
        return this.showDeprecated;
    }

    /**
     * Returns the {@link Class} being inspected by this instance.
     */
    public Class getType()
    {
        return this.type;
    }

    /**
     * Returns a new ClassTool instance that is inspecting the
     * Class with the specified name.  If the specified Class cannot
     * be found, then this will return {@code null}. All other
     * configuration settings will be copied to the new instance.
     */
    public ClassTool inspect(String name)
    {
        if (name == null)
        {
            return null;
        }
        return inspect(toClass(name));
    }

    /**
     * Returns a new ClassTool instance that is inspecting the
     * Class of the specified {@link Object}.  If the specified object
     * is null, then this will return {@code null}. All other
     * configuration settings will be copied to the new instance.
     */
    public ClassTool inspect(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        return inspect(obj.getClass());
    }

    /**
     * Returns a new ClassTool instance that is inspecting the
     * superclass of the Class being inspected by this instance.
     * If the current inspectee has no super class,
     * then this will return {@code null}. All other
     * configuration settings will be copied to the new instance.
     */
    public ClassTool getSuper()
    {
        Class sup = getType().getSuperclass();
        if (sup == null)
        {
            return null;
        }
        return inspect(sup);
    }

    /**
     * Returns a new ClassTool instance that is inspecting the
     * the specified {@link Class}.  If the specified class
     * is null, then this will return {@code null}. All other
     * configuration settings will be copied to the new instance.
     * If {@link #isSafeMode()} is {@code true} and the specified Class
     * is not declared {@code public}, then this will return
     * {@code null}.
     */
    public ClassTool inspect(Class type)
    {
        if (type == null)
        {
            return null;
        }
        // create the new tool, but only return it if
        // it is public or isSafeMode() is off
        ClassTool tool = new ClassTool(this, type);
        if (isSafeMode() && !tool.isPublic())
        {
            return null;
        }
        return tool;
    }

    /**
     * Returns the name of the package to which the inspected Class belongs.
     */
    public String getPackage()
    {
        return getType().getPackage().getName();
    }

    /**
     * Returns the simple name (i.e. full name with package name removed) of
     * the inspected Class.
     */
    public String getName()
    {
        return getType().getSimpleName();
    }

    /**
     * Returns the fully-qualified name for the inspected Class.
     */
    public String getFullName()
    {
        return getType().getName();
    }

    /**
     * Returns true if a call to newInstance() on the Class being
     * inspected is successful; otherwise returns false.  Unlike calling
     * newInstance() directly from a template, this will not throw an
     * Exception if it fails, as all Exceptions are caught.
     */
    public boolean supportsNewInstance()
    {
        try
        {
            type.newInstance();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Returns true if the inspected Class has been deprecated.
     */
    public boolean isDeprecated()
    {
        return isDeprecated(getType());
    }

    /**
     * Returns true if the inspected Class is declared public.
     */
    public boolean isPublic()
    {
        return Modifier.isPublic(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is declared protected.
     */
    public boolean isProtected()
    {
        return Modifier.isProtected(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is declared private.
     */
    public boolean isPrivate()
    {
        return Modifier.isPrivate(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is an inner class
     * that has been declared static or is a standard outer class..
     */
    public boolean isStatic()
    {
        return Modifier.isStatic(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is declared final.
     */
    public boolean isFinal()
    {
        return Modifier.isFinal(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is an interface.
     */
    public boolean isInterface()
    {
        return Modifier.isInterface(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is declared strictfp
     * (uses strict floating point math).
     */
    public boolean isStrict()
    {
        return Modifier.isStrict(getType().getModifiers());
    }

    /**
     * Returns true if the inspected Class is declared abstract.
     */
    public boolean isAbstract()
    {
        return Modifier.isAbstract(getType().getModifiers());
    }

    /**
     * Returns a {@link List} of {@link MethodSub}s for each
     * method declared method in the inspected class. However,
     * in safe mode (which *is* the default), this will only return
     * the public methods.  You must configure safe mode to be off
     * to receive a list of all methods.
     */
    public List<MethodSub> getMethods()
    {
        if (methods == null)
        {
            Method[] declared = getType().getDeclaredMethods();
            List<MethodSub> subs = new ArrayList<MethodSub>(declared.length);
            for (Method method : declared)
            {
                MethodSub sub = new MethodSub(method);
                if ((!isSafeMode() || sub.isPublic()) &&
                    (showDeprecated || !sub.isDeprecated()))
                {
                    subs.add(sub);
                }
            }
            Collections.sort(subs);
            methods = Collections.unmodifiableList(subs);
        }
        return methods;
    }

    /**
     * Returns a {@link List} of {@link ConstructorSub}s for each
     * constructor declared constructor in the inspected class. However,
     * in safe mode (which *is* the default), this will only return
     * the public constructors.  You must configure safe mode to be off
     * to receive a list of all constructors.
     */
    public List<ConstructorSub> getConstructors()
    {
        if (constructors == null)
        {
            Constructor[] declared = getType().getDeclaredConstructors();
            List<ConstructorSub> subs = new ArrayList<ConstructorSub>(declared.length);
            for (Constructor constructor : declared)
            {
                ConstructorSub sub = new ConstructorSub(constructor);
                if ((!isSafeMode() || sub.isPublic()) &&
                    (showDeprecated || !sub.isDeprecated()))
                {
                    subs.add(sub);
                }
            }
            Collections.sort(subs);
            constructors = Collections.unmodifiableList(subs);
        }
        return constructors;
    }

    /**
     * Returns a {@link List} of {@link FieldSub}s for each
     * field declared field in the inspected class. However,
     * in safe mode (which *is* the default), this will only return
     * the public fields.  You must configure safe mode to be off
     * to receive a list of all fields.
     */
    public List<FieldSub> getFields()
    {
        if (fields == null)
        {
            Field[] declared = getType().getDeclaredFields();
            List<FieldSub> subs = new ArrayList<FieldSub>(declared.length);
            for (Field field : declared)
            {
                FieldSub sub = new FieldSub(field);
                if ((!isSafeMode() || sub.isPublic()) &&
                    (showDeprecated || !sub.isDeprecated()))
                {
                    subs.add(sub);
                }
            }
            Collections.sort(subs);
            fields = Collections.unmodifiableList(subs);
        }
        return fields;
    }

    /**
     * Returns a {@link Set} of all {@link Class}es that are
     * part of the signatures (i.e. parameters or return types)
     * of the inspected Class's methods, constructors and fields.
     */
    public Set<Class> getTypes()
    {
        Set<Class> types = new HashSet<Class>();
        for (MethodSub method : getMethods())
        {
            if (!isSafeMode() || method.isPublic())
            {
                if (!method.isVoid())
                {
                    addType(types, method.getReturns());
                }
                for (Class type : method.getParameters())
                {
                    addType(types, type);
                }
            }
        }
        for (ConstructorSub constructor : getConstructors())
        {
            if (!isSafeMode() || constructor.isPublic())
            {
                for (Class type : constructor.getParameters())
                {
                    addType(types, type);
                }
            }
        }
        for (FieldSub field : getFields())
        {
            if (!isSafeMode() || field.isPublic())
            {
                addType(types, field.getType());
            }
        }
        return types;
    }

    private void addType(Set<Class> types, Class type)
    {
        if (type.isArray())
        {
            type = type.getComponentType();
        }
        if (!type.isPrimitive())
        {
            types.add(type);
        }
    }

    /**
     * Returns the {@link Annotation}s of the Class being inspected.
     */
    public List<Annotation> getAnnotations()
    {
        return Arrays.asList(getType().getAnnotations());
    }

    public String toString()
    {
        return getType().toString();
    }



    /**
     * A simplified wrapping interface for inspecting features
     * of a {@link Field} in an inspected Class.
     */
    public static class FieldSub extends Sub<FieldSub>
    {
        protected Field field;

        public FieldSub(Field field)
        {
            this.field = field;
        }

        protected AnnotatedElement getElement()
        {
            return field;
        }

        public String getName()
        {
            return field.getName();
        }

        /**
         * Simply returns the name of the field, since field names
         * cannot be overloaded.
         */
        public String getUniqueName()
        {
            // field names can't be overloaded
            return field.getName();
        }

        /**
         * Simply returns the name of the field.
         */
        public String getJavadocRef()
        {
            return field.getName();
        }

        public Class getType()
        {
            return field.getType();
        }

        /**
         * Returns the value of the field if and only if
         * it is a static field that has no access restrictions
         * set by the security manager.
         */
        public Object getStaticValue()
        {
            if (isStatic())
            {
                try
                {
                    return field.get(null);
                }
                catch(IllegalAccessException iae)
                {
                    //ignore
                }
            }
            return null;
        }

        protected int getModifiers()
        {
            return field.getModifiers();
        }

        protected String getSubType()
        {
            return "field";
        }
    }

    /**
     * A simplified wrapping interface for inspecting features
     * of a {@link Constructor} in an inspected Class.
     */
    public static class ConstructorSub extends CallableSub<ConstructorSub>
    {
        protected Constructor constructor;

        public ConstructorSub(Constructor constructor)
        {
            this.constructor = constructor;
        }

        protected AnnotatedElement getElement()
        {
            return constructor;
        }

        public String getName()
        {
            return constructor.getDeclaringClass().getSimpleName();
        }

        public Class[] getParameters()
        {
            return constructor.getParameterTypes();
        }

        /**
         * Returns true if the final parameter for the constructor was declared
         * as a vararg.
         */
        public boolean isVarArgs()
        {
            return constructor.isVarArgs();
        }

        protected int getModifiers()
        {
            return constructor.getModifiers();
        }

        protected String getSubType()
        {
            return "constructor";
        }
    }

    /**
     * A simplified wrapping interface for inspecting features
     * of a {@link Method} in an inspected Class.
     */
    public static class MethodSub extends CallableSub<MethodSub>
    {
        protected Method method;

        public MethodSub(Method method)
        {
            this.method = method;
        }

        protected AnnotatedElement getElement()
        {
            return method;
        }

        public String getName()
        {
            return method.getName();
        }

        /**
         * If this method can be treated as a bean property in Velocity
         * (which does not exactly follow the javabean spec for such things)
         * then it will return the "bean property" equivalent of the method name.
         * (e.g. for getFoo(), isFoo() or setFoo(foo) it will return "foo")
         */
        public String getPropertyName()
        {
            String name = getName();
            switch (getParameterCount())
            {
                case 0:
                    if (name.startsWith("get") && name.length() > 3)
                    {
                        return uncapitalize(name.substring(3, name.length()));
                    }
                    else if (name.startsWith("is") && name.length() > 2)
                    {
                        return uncapitalize(name.substring(2, name.length()));
                    }
                    break;
                case 1:
                    if (name.startsWith("set") && name.length() > 3)
                    {
                        return uncapitalize(name.substring(3, name.length()));
                    }
                default:
            }
            return null;
        }

        private String uncapitalize(String string)
        {
            if (string.length() > 1)
            {
                StringBuilder out = new StringBuilder(string.length());
                out.append(string.substring(0,1).toLowerCase());
                out.append(string.substring(1, string.length()));
                return out.toString();
            }
            else
            {
                return string.toLowerCase();
            }
        }

        /**
         * Returns true if the final parameter for the method was declared
         * as a vararg.
         */
        public boolean isVarArgs()
        {
            return method.isVarArgs();
        }

        /**
         * Returns true if the return type of this method is void.
         */
        public boolean isVoid()
        {
            return (getReturns() == Void.TYPE);
        }

        public Class getReturns()
        {
            return method.getReturnType();
        }

        public Class[] getParameters()
        {
            return method.getParameterTypes();
        }

        protected int getModifiers()
        {
            return method.getModifiers();
        }

        protected String getSubType()
        {
            return "method";
        }
    }

    public abstract static class CallableSub<T extends CallableSub> extends Sub<T>
    {
        protected String uniqueName;
        protected String javadocRef;
        protected String signature;

        public abstract Class[] getParameters();
        public abstract boolean isVarArgs();

        public boolean takesParameters()
        {
            return (getParameterCount() > 0);
        }

        /**
         * Returns the number of expected parameters. If this method or
         * constructor is declared with varargs, the vararg only counts as one.
         */
        public int getParameterCount()
        {
            return getParameters().length;
        }

        /**
         * Build a unique method/ctor name by appending the simple names of
         * the expected parameter types, thereby distinguishing constructors
         * and overloaded methods with a useful name that would still be a
         * valid method name.  This is particularly useful for generating
         * JUnit test method names.
         */
        public String getUniqueName()
        {
            if (uniqueName == null)
            {
                Class[] params = getParameters();
                if (params.length == 0)
                {
                    uniqueName = getName();
                }
                else
                {
                    StringBuilder out = new StringBuilder(30);
                    out.append(getName());
                    out.append('_');
                    for (int i=0; i < params.length; i++)
                    {
                        Class param = params[i];
                        if (param.isArray())
                        {
                            out.append(param.getComponentType().getSimpleName());
                            // check for vararg on last param
                            if (i == params.length - 1 && isVarArgs())
                            {
                                out.append("VarArgs");
                            }
                            else
                            {
                                out.append("Array");
                            }
                        }
                        else
                        {
                            out.append(param.getSimpleName());
                        }
                    }
                    uniqueName = out.toString();
                }
            }
            return uniqueName;
        }

        public String getSignature()
        {
            if (signature == null)
            {
                signature = signature(false);
            }
            return signature;
        }

        public String getJavadocRef()
        {
            if (javadocRef == null)
            {
                javadocRef = signature(true);
            }
            return javadocRef;
        }

        protected String signature(boolean fullNames)
        {
            Class[] params = getParameters();
            if (params.length == 0)
            {
                return getName() + "()";
            }
            else
            {
                StringBuilder out = new StringBuilder(30);
                out.append(getName());
                out.append('(');
                boolean first = true;
                for (int i=0; i < params.length; i++)
                {
                    Class param = params[i];
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        out.append(',');
                    }
                    if (param.isArray())
                    {
                        if (fullNames)
                        {
                            out.append(param.getComponentType().getName());
                        }
                        else
                        {
                            out.append(param.getComponentType().getSimpleName());
                        }
                        if (i == params.length - 1 && isVarArgs())
                        {
                            out.append("...");
                        }
                        else
                        {
                            out.append("[]");
                        }
                    }
                    else
                    {
                        if (fullNames)
                        {
                            out.append(param.getName());
                        }
                        else
                        {
                            out.append(param.getSimpleName());
                        }
                    }
                }
                out.append(')');
                return out.toString();
            }
        }
    }

    public abstract static class Sub<T extends Sub> implements Comparable<T>
    {
        protected abstract AnnotatedElement getElement();

        protected abstract int getModifiers();

        protected abstract String getSubType();

        public abstract String getName();

        public abstract String getUniqueName();

        public abstract String getJavadocRef();

        /**
         * Returns the {@link Annotation}s of the element being inspected.
         */
        public List<Annotation> getAnnotations()
        {
            return Arrays.asList(getElement().getAnnotations());
        }

        public boolean isDeprecated()
        {
            return ClassTool.isDeprecated(getElement());
        }

        public boolean isPublic()
        {
            return Modifier.isPublic(getModifiers());
        }

        public boolean isProtected()
        {
            return Modifier.isProtected(getModifiers());
        }

        public boolean isPrivate()
        {
            return Modifier.isPrivate(getModifiers());
        }

        public boolean isStatic()
        {
            return Modifier.isStatic(getModifiers());
        }

        public boolean isFinal()
        {
            return Modifier.isFinal(getModifiers());
        }

        public boolean isInterface()
        {
            return Modifier.isInterface(getModifiers());
        }

        public boolean isNative()
        {
            return Modifier.isNative(getModifiers());
        }

        public boolean isStrict()
        {
            return Modifier.isStrict(getModifiers());
        }

        public boolean isSynchronized()
        {
            return Modifier.isSynchronized(getModifiers());
        }

        public boolean isTransient()
        {
            return Modifier.isTransient(getModifiers());
        }

        public boolean isVolatile()
        {
            return Modifier.isVolatile(getModifiers());
        }

        public boolean isAbstract()
        {
            return Modifier.isAbstract(getModifiers());
        }

        public int compareTo(T that)
        {
            return this.getUniqueName().compareTo(that.getUniqueName());
        }

        public int hashCode()
        {
            return this.getUniqueName().hashCode();
        }

        public boolean equals(Object obj)
        {
            if (obj instanceof Sub)
            {
                Sub that = (Sub)obj;
                return this.getUniqueName().equals(that.getUniqueName());
            }
            return false;
        }

        public String toString()
        {
            return getSubType() + ' ' + getJavadocRef();
        }
    }

}

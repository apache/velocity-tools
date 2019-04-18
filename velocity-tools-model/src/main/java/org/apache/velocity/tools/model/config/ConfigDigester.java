package org.apache.velocity.tools.model.config;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.config.ConfigurationException;
import org.apache.velocity.tools.model.Attribute;
import org.apache.velocity.tools.model.Entity;
import org.apache.velocity.tools.model.Model;
import org.apache.velocity.tools.model.impl.AttributeHolder;
import org.apache.velocity.tools.model.impl.BaseAttribute;
import org.apache.velocity.tools.model.util.TypeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * <p>A tailored minimalistic digester for XML configuration reading of the model tree.</p>
 *
 * @author Claude Brisson
 */

// TODO use annotations

public class ConfigDigester
{
    public ConfigDigester(Element doc, Object bean)
    {
        xmlPath.push(doc);
        beanStack.push(bean);
    }

    public void process() throws Exception
    {
        initReflection();
        useModelNamespace = isDeclaringNamespace(xmlPath.peek(), "model");
        recurseProcessing();
    }

    private void initReflection() throws Exception
    {
        addAttributeQueryPart = BaseAttribute.class.getDeclaredMethod("addQueryPart", String.class);
        addAttributeQueryPart.setAccessible(true);
        addAttributeParameter = BaseAttribute.class.getDeclaredMethod("addParameter", String.class);
        addAttributeParameter.setAccessible(true);
        addAttribute = AttributeHolder.class.getDeclaredMethod("addAttribute", Attribute.class);
        addAttribute.setAccessible(true);
    }

    private void recurseProcessing() throws Exception
    {
        Element element = xmlPath.peek();
        String tag = element.getTagName();
        Object bean = beanStack.peek();

        Map<String, Object> attrMap = getAttributesMap(element);
        setProperties(bean, attrMap);

        NodeList children = element.getChildNodes();
        for (int i = 0; i <children.getLength(); ++i)
        {
            Node child = children.item(i);
            Object childBean = null;

            if (bean instanceof Attribute)
            {
                Attribute attribute = (Attribute)bean;
                addAttributePart(attribute, child);
            }
            else if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                handleChildElement(bean, (Element)child);
            }
        }
        xmlPath.pop();
        beanStack.pop();
    }

    private void addAttributePart(Attribute attribute, Node child) throws Exception
    {
        switch (child.getNodeType())
        {
            case Node.TEXT_NODE:
            {
                String queryPart = child.getNodeValue();
                addAttributeQueryPart.invoke(attribute, queryPart);
                break;
            }
            case Node.ELEMENT_NODE:
            {
                String paramName = child.getLocalName();
                addAttributeParameter.invoke(attribute, paramName);
                break;
            }
        }
    }

    private void handleChildElement(Object parentBean, Element childElement) throws Exception
    {
        String childName = childElement.getLocalName();
        Object childBean;
        if (isAttributeResult(childElement))
        {
            String attributeClassName = modelPackage + "." + StringUtils.capitalize(childName);
            Class attributeClass = null;
            try
            {
                // first try with exact child name capitalized
                attributeClass = ClassUtils.getClass(attributeClassName);
            }
            catch (ClassNotFoundException cnfe)
            {
                // second try with 'Attribute' postfix
                attributeClassName += "Attribute";
                attributeClass = ClassUtils.getClass(attributeClassName);
            }
            String attributeName = childElement.getAttribute("name");
            if (attributeName == null || attributeName.length() == 0)
                throw new ConfigurationException("attribute without name:" + childElement.toString());
            childElement.removeAttribute("name");
            childBean = createChildInstance(attributeName, attributeClass);
            addAttribute.invoke(parentBean, childBean);
        }
        else
        {
            childBean = createChildInstance(childName, Entity.class);
            ((Model)parentBean).addEntity((Entity)childBean);
        }

        xmlPath.push(childElement);
        beanStack.push(childBean);
        recurseProcessing();
    }

    private Map<String, Object> getAttributesMap(Element element)
    {
        Map<String, Object> attrMap = new TreeMap<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Attr attribute = (Attr)attributes.item(i);
            String attributeName = attribute.getName();
            // filter out xmlns: etc
            if (attributeName.contains(":")) // the only ':' used in model DTD are for tags
            {
                // not for us
                continue;
            }
            String value = attribute.getValue();

            // handle subproperties
            int dot = attributeName.indexOf('.');
            if (dot > 0)
            {
                String prop = attributeName.substring(0, dot);
                Map<String, Object> subProps = null;
                Object sub = attrMap.get(prop);
                if (sub == null)
                {
                    subProps = new TreeMap<String, Object>();
                    attrMap.put(prop, subProps);
                }
                else if (sub instanceof Map)
                {
                    subProps = (Map<String, Object>)sub;
                }
                else
                {
                    throw new ConfigurationException("cannot mix values and subproperties for property: " + prop);
                }
                subProps.put(attributeName.substring(dot + 1), value);
            }
            else
            {
                attrMap.put(attributeName, value);
            }
        }
        return attrMap;
    }

    public static void setProperties(Object bean, Map properties) throws Exception
    {
        Map<String, Map<String, Object>> subProps = new TreeMap<>();
        for (Map.Entry entry : (Set<Map.Entry>)properties.entrySet())
        {
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            int dot;
            if ((dot = key.indexOf(".")) != -1)
            {
                String subkey = key.substring(0, dot);
                Map<String, Object> submap = subProps.get(subkey);
                if (submap == null)
                {
                    submap = new TreeMap<String, Object>();
                    subProps.put(subkey, submap);
                }
                submap.put(key.substring(dot + 1), value);
            }
            else
            {
                setProperty(bean, (String)entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, Map<String, Object>> entry : subProps.entrySet())
        {
            setProperty(bean, entry.getKey(), entry.getValue());
        }
    }

    public static void setProperty(Object bean, String name, Object value) throws Exception
    {
        if (value == null)
        {
            return;
        }

        // search for a getter
        if (value instanceof Map)
        {
            String getterName = getGetterName(name);
            Method getter = ClassUtils.findGetter(getterName, bean.getClass(), false);
            if (getter != null)
            {
                Object subBean = getter.invoke(bean);
                setProperties(subBean, (Map)value);
                return;
            }
        }

        // search for a setter
        String setterName = getSetterName(name);
        Method setter = ClassUtils.findSetter(setterName, bean.getClass(), ConfigDigester::isScalarType, false);
        if (setter == null)
        {
            // search for a map-like put() method
            Class clazz = bean.getClass();
            do
            {
                for (Method method : clazz.getDeclaredMethods())
                {
                    // prefix matching: we allow a method name like setWriteAccess for a parameter like write="..."
                    if (method.getParameterCount() == 2 && method.getName().equals("put") && method.getParameterTypes()[0] == String.class)
                    {
                        setter = method;
                    }
                }
                clazz = clazz.getSuperclass();
            }
            while (setter == null && clazz != Object.class);
        }
        if (setter == null)
        {
            throw new ConfigurationException("no setter for preperty " + name + " on class " + bean.getClass());
        }
        setter.setAccessible(true);
        Object argument;
        Class paramClass = setter.getParameterTypes()[setter.getParameterCount() - 1];
        if (paramClass == String.class)
        {
            argument = value;
        }
        else if (paramClass == Boolean.TYPE)
        {
            argument = TypeUtils.toBoolean(value);
        }
        else if (Enum.class.isAssignableFrom(paramClass) && value instanceof String)
        {
            argument = Enum.valueOf(paramClass, ((String)value).toUpperCase());
        }
        else
        {
            throw new ConfigurationException("cannot convert value to setter argument: " + setterName + "(" + paramClass + ")");
        }
        switch (setter.getParameterCount())
        {
            case 1:
                setter.invoke(bean, argument);
                break;
            case 2:
                setter.invoke(bean, name, argument);
                break;
            default:
                throw new ConfigurationException("oops, unhandled case");
        }

    }

    private Object createChildInstance(String name, Class clazz) throws Exception
    {
        Object parent = beanStack.peek();
        Class parentClass = parent.getClass();
        Constructor constructor = null;
        do
        {
            try
            {
                constructor = clazz.getConstructor(String.class, parentClass);
            }
            catch (NoSuchMethodException nsme)
            {
            }
            parentClass = parentClass.getSuperclass();
        }
        while (parentClass != null && constructor == null);
        if (constructor == null)
        {
            throw new ConfigurationException("no appropriate constructor for class " + clazz.getName());
        }
        constructor.setAccessible(true);
        return constructor.newInstance(name, parent);
    }

    private Object createChildInstance(String name, String className) throws Exception
    {
        return createChildInstance(name, ClassUtils.getClass(className));
    }

    private boolean isAttributeResult(Element element)
    {
        if (useModelNamespace)
        {
            return Constants.MODEL_NAMESPACE_URI.equals(element.getNamespaceURI()) && attributeResults.contains(element.getLocalName());
        }
        else
        {
            return attributeResults.contains(element.getTagName());
        }
    }

    private boolean isDeclaringNamespace(Element root, String namespace)
    {
        namespace = "xmlns:" + namespace;
        NamedNodeMap attributes = root.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Attr attribute = (Attr)attributes.item(i);
            if (namespace.equals(attribute.getNodeName())) return true;
        }
        return false;
    }

    public static String getSetterName(String name)
    {
        String[] parts = StringUtils.split(name, "_-.");
        StringBuilder builder = new StringBuilder("set");
        for (String part : parts)
        {
            builder.append(StringUtils.capitalize(part));
        }
        return builder.toString();
    }

    public static String getGetterName(String name)
    {
        String[] parts = StringUtils.split(name, "_-");
        StringBuilder builder = new StringBuilder("get");
        for (String part : parts)
        {
            builder.append(StringUtils.capitalize(part));
        }
        return builder.toString();
    }

    private static Set<Class> scalarTypes = new HashSet<>(Arrays.asList(
        String.class, Boolean.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
    ));

    public static boolean isScalarType(Class typeClass)
    {
        return scalarTypes.contains(typeClass) || Enum.class.isAssignableFrom(typeClass);
    }

    private Stack<Element> xmlPath = new Stack<>();
    private Stack<Object> beanStack = new Stack<>();
    private boolean useModelNamespace = false;
    private Method addAttributeQueryPart = null, addAttributeParameter = null, addAttribute = null;

    private static final String modelPackage = "org.apache.velocity.tools.model";
    // for now, explicit types as tags (like <int name="...">) aren't taken into account
    private static final Set<String> attributeResults = new HashSet<>(Arrays.asList("scalar", /* "string", "boolean", "int", "long", "float", "double",*/ "row", "rowset", "action", "transaction"));
}

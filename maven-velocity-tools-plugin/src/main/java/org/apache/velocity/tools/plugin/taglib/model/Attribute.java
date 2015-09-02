package org.apache.velocity.tools.plugin.taglib.model;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetTop;

/**
 * It represents a tag attribute, in a tag library descriptor.
 */
@ObjectCreate(pattern = "taglib/tag/attribute")
public class Attribute
{

    /**
     * Maps primitive types to their wrapped equivalents.
     */
    private static Map<String, String> primitive2wrapped;

    static {
        primitive2wrapped = new HashMap<String, String>();
        primitive2wrapped.put("byte", Byte.class.getName());
        primitive2wrapped.put("short", Short.class.getName());
        primitive2wrapped.put("int", Integer.class.getName());
        primitive2wrapped.put("long", Long.class.getName());
        primitive2wrapped.put("float", Float.class.getName());
        primitive2wrapped.put("double", Double.class.getName());
        primitive2wrapped.put("char", Character.class.getName());
        primitive2wrapped.put("boolean", Boolean.class.getName());
    }

    /**
     * The name of the attribute.
     */
    @BeanPropertySetter(pattern = "taglib/tag/attribute/name")
    private String name;

    /**
     * The description of the attribute.
     */
    @BeanPropertySetter(pattern = "taglib/tag/attribute/description")
    private String description;

    /**
     * It tells if the attribute is required.
     */
    @BeanPropertySetter(pattern = "taglib/tag/attribute/required")
    private boolean required = false;

    /**
     * It tells if the attribute can have a value that is the result of runtime expression.
     */
    @BeanPropertySetter(pattern = "taglib/tag/attribute/rtexprvalue")
    private boolean rtexprvalue = false;

    /**
     * The type of the attribute.
     */
    @BeanPropertySetter(pattern = "taglib/tag/attribute/type")
    private String type;

    /**
     * The containing tag.
     */
    private Tag tag;

    /**
     * Returns the name of the attribute.
     *
     * @return The name of the attribute.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the attribute.
     *
     * @param name The name of the attribute.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the description of the attribute.
     *
     * @return The description of the attribute.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the attribute.
     *
     * @param description The description of the attribute.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns <code>true</code> if the attribute is required.
     *
     * @return <code>true</code> if the attribute is required.
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * It tells if the attribute is required.
     *
     * @param required <code>true</code> if the attribute is required.
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * Returns <code>true</code> if the attribute accepts runtime expression.
     *
     * @return <code>true</code> if the attribute accepts runtime expression.
     */
    public boolean isRtexprvalue()
    {
        return rtexprvalue;
    }

    /**
     * It tells ifthe attribute accepts runtime expression.
     *
     * @param rtexprvalue <code>true</code> if the attribute accepts runtime expression.
     */
    public void setRtexprvalue(boolean rtexprvalue)
    {
        this.rtexprvalue = rtexprvalue;
    }

    /**
     * Returns the type of the attribute.
     *
     * @return The type of the attribute.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of the attribute.
     *
     * @param type The type of the attribute.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Sets the tag that contains this attribute.
     *
     * @param tag The container tag.
     */
    @SetTop(pattern="taglib/tag/attribute")
    public void setJTag(Tag tag)
    {
        this.tag = tag;
    }

    /**
     * Returns the tag that contains this attribute.
     *
     * @return The container tag.
     */
    public Tag getTag()
    {
        return tag;
    }

    /**
     * Returns the type that should be used in the Velocity directive for this attribute.
     *
     * @return The type to use in Velocity directive.
     */
    public String getWrappedType() {
        if (type == null) {
            return String.class.getName();
        }
        String retValue = primitive2wrapped.get(type);
        if (retValue == null) {
            retValue = type;
        }
        return retValue;
    }

    /**
     * Returns the name of the setter method.
     *
     * @return The name of the setter method.
     */
    public String getSetterName() {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public String toString()
    {
        return "Attribute [name=" + name + ", description=" + description
                + ", required=" + required + ", rtexprvalue=" + rtexprvalue
                + ", type=" + type + "]";
    }

}

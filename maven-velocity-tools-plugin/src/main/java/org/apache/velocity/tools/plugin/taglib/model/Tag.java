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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.SimpleTag;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetNext;
import org.apache.velocity.tools.view.jsp.taglib.jspimpl.VelocityToolsJspException;

/**
 * It represents a tag in a tag library descriptor.
 */
@ObjectCreate(pattern = "taglib/tag")
public class Tag
{

    /**
     * The name of the tag.
     */
    @BeanPropertySetter(pattern = "taglib/tag/name")
    private String name;

    /**
     * The description of the tag.
     */
    @BeanPropertySetter(pattern = "taglib/tag/description")
    private String description;

    /**
     * The Java class of the tag.
     */
    @BeanPropertySetter(pattern = "taglib/tag/tag-class")
    private String tagClass;

    /**
     * The type of the body content.
     */
    @BeanPropertySetter(pattern = "taglib/tag/body-content")
    private String bodyContent;

    /**
     * The list of supported attributes.
     */
    private List<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * Returns the name of the tag.
     *
     * @return The name of the tag.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the tag.
     *
     * @param name The name of the tag.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the description of the tag.
     *
     * @return The description of the tag.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the tag.
     *
     * @param description The description of the tag.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the class of the tag.
     *
     * @return The class of the tag.
     */
    public String getTagClass()
    {
        return tagClass;
    }

    /**
     * Sets the class of the tag.
     *
     * @param tagClass The class of the tag.
     */
    public void setTagClass(String tagClass)
    {
        this.tagClass = tagClass;
    }

    /**
     * Returns the body content of the tag.
     *
     * @return The body content of the tag.
     */
    public String getBodyContent()
    {
        return bodyContent;
    }

    /**
     * Sets the body content of the tag.
     *
     * @param bodyContent The body content of the tag.
     */
    public void setBodyContent(String bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    /**
     * Returns the contained attributes.
     *
     * @return The attributes.
     */
    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    /**
     * Adds an attribute to the supported attributes.
     *
     * @param attribute A new supported attribute.
     */
    @SetNext
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
    }

    /**
     * It tells if this tag implements {@link SimpleTag}.
     *
     * @return <code>true</code> if it is a SimpleTag.
     */
    public boolean isSimpleTag() {
        return SimpleTag.class.isAssignableFrom(getReflectedTagClass());
    }

    /**
     * It tells if this tag has a body.
     *
     * @return <code>true</code> if this tag has a body.
     */
    public boolean hasBody() {
        return !"empty".equals(bodyContent);
    }

    /**
     * Returns the reflected tag class.
     *
     * @return The real tag class.
     */
    public Class<?> getReflectedTagClass()
    {
        Class<?> clazz;
        try
        {
            clazz = Class.forName(tagClass);
        } catch (ClassNotFoundException e)
        {
            throw new VelocityToolsJspException("Problems obtaining class: " + tagClass, e);
        }
        return clazz;
    }

    @Override
    public String toString()
    {
        return "Tag [name=" + name + ", description=" + description
                + ", tagClass=" + tagClass + ", bodyContent=" + bodyContent
                + ", attributes=" + attributes + "]";
    }

}

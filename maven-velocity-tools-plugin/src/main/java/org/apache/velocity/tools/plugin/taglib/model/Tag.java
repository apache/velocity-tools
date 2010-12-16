package org.apache.velocity.tools.plugin.taglib.model;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.SimpleTag;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetNext;
import org.apache.velocity.tools.view.jsp.taglib.jspimpl.VelocityToolsJspException;

@ObjectCreate(pattern = "taglib/tag")
public class Tag
{

    @BeanPropertySetter(pattern = "taglib/tag/name")
    private String name;

    @BeanPropertySetter(pattern = "taglib/tag/description")
    private String description;

    @BeanPropertySetter(pattern = "taglib/tag/tag-class")
    private String tagClass;

    @BeanPropertySetter(pattern = "taglib/tag/body-content")
    private String bodyContent;

    private List<Attribute> attributes = new ArrayList<Attribute>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTagClass()
    {
        return tagClass;
    }

    public void setTagClass(String tagClass)
    {
        this.tagClass = tagClass;
    }

    public String getBodyContent()
    {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    @SetNext
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
    }

    public boolean isSimpleTag() {
        return SimpleTag.class.isAssignableFrom(getReflectedTagClass());
    }

    public boolean hasBody() {
        return !"empty".equals(bodyContent);
    }

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

package org.apache.velocity.tools.plugin.taglib.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetTop;

@ObjectCreate(pattern = "taglib/tag/attribute")
public class Attribute
{

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

    @BeanPropertySetter(pattern = "taglib/tag/attribute/name")
    private String name;

    @BeanPropertySetter(pattern = "taglib/tag/attribute/description")
    private String description;

    @BeanPropertySetter(pattern = "taglib/tag/attribute/required")
    private boolean required = false;

    @BeanPropertySetter(pattern = "taglib/tag/attribute/rtexprvalue")
    private boolean rtexprvalue = false;

    @BeanPropertySetter(pattern = "taglib/tag/attribute/type")
    private String type;

    private Tag tag;

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

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isRtexprvalue()
    {
        return rtexprvalue;
    }

    public void setRtexprvalue(boolean rtexprvalue)
    {
        this.rtexprvalue = rtexprvalue;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @SetTop(pattern="taglib/tag/attribute")
    public void setTag(Tag tag)
    {
        this.tag = tag;
    }

    public Tag getTag()
    {
        return tag;
    }

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

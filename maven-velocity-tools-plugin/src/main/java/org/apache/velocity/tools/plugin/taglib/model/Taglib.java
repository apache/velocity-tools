package org.apache.velocity.tools.plugin.taglib.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetNext;

@ObjectCreate(pattern = "taglib")
public class Taglib
{
    @BeanPropertySetter(pattern = "taglib/description")
    private String description;

    @BeanPropertySetter(pattern = "taglib/tlib-version")
    private String tlibVersion;

    @BeanPropertySetter(pattern = "taglib/short-name")
    private String shortName;

    @BeanPropertySetter(pattern = "taglib/uri")
    private String uri;

    private List<Tag> tags = new ArrayList<Tag>();

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTlibVersion()
    {
        return tlibVersion;
    }

    public void setTlibVersion(String tlibVersion)
    {
        this.tlibVersion = tlibVersion;
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public List<Tag> getTags()
    {
        return tags;
    }

    @SetNext
    public void addTag(Tag tag)
    {
        tags.add(tag);
    }

    @Override
    public String toString()
    {
        return "Taglib [description=" + description + ", tlibVersion="
                + tlibVersion + ", shortName=" + shortName + ", uri=" + uri
                + ", tags=" + tags + "]";
    }

}

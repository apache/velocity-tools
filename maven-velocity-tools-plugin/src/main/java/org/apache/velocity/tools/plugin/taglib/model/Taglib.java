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

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester.annotations.rules.ObjectCreate;
import org.apache.commons.digester.annotations.rules.SetNext;

/**
 * It represents a Tag Library, as read from a Tag Library Descriptor.
 */
@ObjectCreate(pattern = "taglib")
public class Taglib
{
    /**
     * The description of the taglib.
     */
    @BeanPropertySetter(pattern = "taglib/description")
    private String description;

    /**
     * The version of this taglib.
     */
    @BeanPropertySetter(pattern = "taglib/tlib-version")
    private String tlibVersion;

    /**
     * The short name of the taglib.
     */
    @BeanPropertySetter(pattern = "taglib/short-name")
    private String shortName;

    /**
     * The normalized URI to access this taglib.
     */
    @BeanPropertySetter(pattern = "taglib/uri")
    private String uri;

    /**
     * The contained tags.
     */
    private List<Tag> tags = new ArrayList<Tag>();

    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the taglib version.
     *
     * @return The taglib version.
     */
    public String getTlibVersion()
    {
        return tlibVersion;
    }

    /**
     * Sets the taglib version.
     *
     * @param tlibVersion The taglib version.
     */
    public void setTlibVersion(String tlibVersion)
    {
        this.tlibVersion = tlibVersion;
    }

    /**
     * Returns the short name of the taglib.
     *
     * @return The short name of the taglib.
     */
    public String getShortName()
    {
        return shortName;
    }

    /**
     * Sets the short name of the taglib.
     *
     * @param shortName The short name of the taglib.
     */
    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    /**
     * Returns the URI of the taglib.
     *
     * @return The URI of the taglib.
     */
    public String getUri()
    {
        return uri;
    }

    /**
     * Sets the URI of the taglib.
     *
     * @param uri The URI of the taglib.
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }

    /**
     * Returns the tags of this taglib.
     *
     * @return The tags.
     */
    public List<Tag> getTags()
    {
        return tags;
    }

    /**
     * Adds a new tag to the contained tag.
     *
     * @param tag The tag to add.
     */
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

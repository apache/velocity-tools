package org.apache.velocity.tools.config;

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

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class manages a {@link SortedSet} of child {@link Configuration}s
 * as well as being a {@link Configuration} itself.
 *
 * @author Nathan Bubna
 * @version $Id: Configuration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class CompoundConfiguration<C extends Configuration>
    extends Configuration
{
    private final SortedSet<C> children = new TreeSet<C>();

    protected void addChild(C newKid)
    {
        // check if we already have a matching child
        C child = getChild(newKid);
        if (child != null)
        {
            // compound children can just have the new props and kids
            // added to the old ones, we don't need to replace the old
            if (child instanceof CompoundConfiguration)
            {
                ((CompoundConfiguration)child)
                    .addConfiguration((CompoundConfiguration)newKid);
            }
            else
            {
                // add newKid's values to childs (overwriting any dupes)
                child.addConfiguration(newKid);
            }
        }
        else
        {
            // simply adopt the new kid
            children.add(newKid);
        }
    }

    protected boolean removeChild(C config)
    {
        return children.remove(config);
    }

    protected boolean hasChildren()
    {
        return !children.isEmpty();
    }

    protected Collection<C> getChildren()
    {
        return children;
    }

    protected void setChildren(Collection<C> kids)
    {
        for (C kid : kids)
        {
            addChild(kid);
        }
    }

    protected C getChild(C kid)
    {
        for (C child : children)
        {
            if (child.equals(kid))
            {
                return child;
            }
        }
        return null;
    }

    public void addConfiguration(CompoundConfiguration<C> config)
    {
        // add config's children to our own
        setChildren(config.getChildren());

        // add config's properties to ours
        super.addConfiguration(config);
    }

    @Override
    public void validate()
    {
        super.validate();

        for (C child : children)
        {
            child.validate();
        }
    }

    protected void appendChildren(StringBuilder out, String childrenName, String childDelim)
    {
        if (hasChildren())
        {
            if (hasProperties())
            {
                out.append(" and ");
            }
            else
            {
                out.append(" with ");
            }
            out.append(children.size());
            out.append(' ');
            out.append(childrenName);
            for (C child : children)
            {
                out.append(child);
                out.append(childDelim);
            }
        }
    }

    @Override
    public int hashCode()
    {
        // add the super's and the kid's
        return super.hashCode() + children.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        // must be of this type and have super.equals() be true
        if (!(obj instanceof CompoundConfiguration) || !super.equals(obj))
        {
            return false;
        }
        else
        {
            // they're of the same type
            CompoundConfiguration<C> that = (CompoundConfiguration<C>)obj;
            // if their children are equal, they're equal
            return this.children.equals(that.children);
        }
    }

}

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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Configuration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class CompoundConfiguration<C extends Configuration>
    extends Configuration<C>
{
    private List<C> children = new ArrayList<C>();

    protected void addChild(C config)
    {
        children.add(config);
    }

    protected boolean hasChildren()
    {
        return !children.isEmpty();
    }

    protected List<C> getChildren()
    {
        return children;
    }

    protected C findMatchingChild(C child)
    {
        // no matches in this implementation
        return null;
    }

    public void addConfiguration(CompoundConfiguration<C> config)
    {
        // add config's properties to ours
        super.addConfiguration(config);

        for (C newKid : config.getChildren())
        {
            C child = findMatchingChild(newKid);
            if (child == null)
            {
                // directly adopt the new kid
                addChild(newKid);
            }
            else
            {
                // newKid isn't that new, just add new config
                child.addConfiguration(newKid);
            }
        }
    }

    @Override
    public void validate()
    {
        super.validate();

        for (C child : getChildren())
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
                out.append("with ");
            }
            out.append(getChildren().size());
            out.append(' ');
            out.append(childrenName);
            for (C child : getChildren())
            {
                out.append(child);
                out.append(childDelim);
            }
        }
    }

}

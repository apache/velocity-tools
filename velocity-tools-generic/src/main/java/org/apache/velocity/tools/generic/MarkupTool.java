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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.SafeConfig;

/**
 * <b>NOTE: This tools is considered "alpha" quality due to lack of testing
 * and a generally unpolished API.  Feel free to use but expect changes.
 * Also, this is not automatically provided via the default tools.xml file.
 * </b>
 *
 * <p>
 * A tool to make it easy to generate XML or HTML on the fly.  It uses a CSS-type
 * syntax with a vaguely jQuery-ish API to help you generate the markup you need.
 * <p>
 * <pre>
 * Example uses in a template:
 *   #set( $foospan = $markup.span.id($foo.id).body($foo) )
 *   $markup.tag('table tr.bar td').body("This is $foospan")
 *
 * Output:
 *   <table>
 *     <tr class="bar">
 *       <td>This is <span id="foo1">my first foo.</span></td>
 *     </tr>
 *   </table>
 *
 *
 * Example tools.xml config:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.alpha.MarkupTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * @author Nathan Bubna
 * @since VelocityTools 2.0
 * @version $Id$
 */
@DefaultKey("mark")
public class MarkupTool extends SafeConfig
{
    public static final String DEFAULT_TAB = "  ";
    public static final String DEFAULT_DELIMITER = " ";

    private String tab = DEFAULT_TAB;
    private String delim = DEFAULT_DELIMITER;

    public void setTab(String tab)
    {
        if (isConfigLocked())
        {
            //TODO: log setTab failure
        }
        else
        {
            this.tab = tab;
        }
    }

    public String getTab()
    {
        return this.tab;
    }

    public Tag get(String tag)
    {
        return tag(tag);
    }

    public Tag tag(String definition)
    {
        String[] tags = split(definition);
        Tag last = null;
        for (int i=0; i < tags.length; i++)
        {
            Tag tag = parse(tags[i]);
            if (last != null)
            {
                last.append(tag);
            }
            last = tag;
        }
        return last;
    }

    protected String[] split(String me)
    {
        //TODO: fix escaped delimiters
        return me.split(delim);
    }

    private static enum Mode { NAME, ID, CLASS, ATTR }
    protected Tag parse(String definition)
    {
        StringBuilder store = new StringBuilder();
        Tag tag = new Tag(this);
        Mode mode = Mode.NAME;
        for (int i=0; i < definition.length(); i++)
        {
            char c = definition.charAt(i);
            if (c == '#')
            {
                store = clear(mode, tag, store, true);
                mode = Mode.ID;
            }
            else if (c == '.')
            {
                store = clear(mode, tag, store, true);
                mode = Mode.CLASS;
            }
            else if (c == '[')
            {
                store = clear(mode, tag, store, true);
                mode = Mode.ATTR;
            }
            else if (c == ']')
            {
                store = clear(mode, tag, store, true);
                mode = Mode.NAME;
            }
            else
            {
                store.append(c);
            }
        }
        clear(mode, tag, store, false);
        return tag;
    }
    private StringBuilder clear(Mode mode, Tag tag, StringBuilder val, boolean emptyStore)
    {
        if (val.length() > 0)
        {
            String s = val.toString();
            switch (mode)
            {
                case NAME:
                    tag.name(s);
                    break;
                case ID:
                    tag.id(s);
                    break;
                case CLASS:
                    tag.addClass(s);
                    break;
                case ATTR:
                    if (s.indexOf('=') > 0)
                    {
                        String[] kv = s.split("=");
                        tag.attr(kv[0], kv[1]);
                    }
                    else
                    {
                        tag.attr(s, null);
                    }
                    break;
            }
            if (emptyStore)
            {
                return new StringBuilder();
            }
            return val;
        }
        else
        {
            // already is clean
            return val;
        }
    }

    public static class Tag
    {
        private MarkupTool tool;
        private Tag parent;
        private Object name;
        private Object id;
        private List<Object> classes;
        private Map<Object,Object> attributes;
        private List<Object> children;

        public Tag(MarkupTool tool)
        {
            this.tool = tool;
        }

        public Tag name(Object name)
        {
            this.name = name;
            return this;
        }

        public Tag id(Object id)
        {
            this.id = id;
            return this;
        }

        public Tag addClass(Object c)
        {
            if (c == null)
            {
                return null;
            }

            if (classes == null)
            {
                classes = new ArrayList<Object>();
            }
            classes.add(c);
            return this;
        }

        public Tag attr(Object k, Object v)
        {
            if (k == null)
            {
                return null;
            }
            if (attributes == null)
            {
                attributes = new HashMap<Object,Object>();
            }
            attributes.put(k, v);
            return this;
        }

        public Tag body(Object o)
        {
            if (children == null)
            {
                children = new ArrayList<Object>();
            }
            else
            {
                children.clear();
            }
            children.add(o);
            return this;
        }

        public Tag append(Object o)
        {
            if (children == null)
            {
                children = new ArrayList<Object>();
            }
            children.add(o);
            if (o instanceof Tag)
            {
                ((Tag)o).parent(this);
            }
            return this;
        }

        public Tag prepend(Object o)
        {
            if (children == null)
            {
                children = new ArrayList<Object>();
                children.add(o);
            }
            else
            {
                children.add(0, o);
            }
            if (o instanceof Tag)
            {
                ((Tag)o).parent(this);
            }
            return this;
        }

        public Tag wrap(String tag)
        {
            // make new tag
            Tag prnt = tool.tag(tag);
            // give root of new tag our parent
            prnt.root().parent(parent());
            // make new tag our parent
            parent(prnt);
            return this;
        }

        public Tag orphan()
        {
            return parent(null);
        }

        public Tag parent(Tag parent)
        {
            this.parent = parent;
            return this;
        }

        public Tag parent()
        {
            return this.parent;
        }

        public Tag root()
        {
            if (isOrphan())
            {
                return this;
            }
            return this.parent.root();
        }

        public List<Object> children()
        {
            return children;
        }

        public boolean isOrphan()
        {
            return (parent == null);
        }

        public boolean isEmpty()
        {
            return (children == null || children().isEmpty());
        }

        public boolean matches(Tag tag)
        {
            if (missed(name, tag.name) ||
                missed(id, tag.id) ||
                missed(classes, tag.classes))
            {
                return false;
            }
            //TODO: match attributes
            return true;
        }

        protected boolean missed(Object target, Object arrow)
        {
            // no arrow, no miss
            if (arrow == null)
            {
                return false;
            }
            // otherwise, the arrow must hit the target
            return !arrow.equals(target);
        }

        protected boolean missed(List<Object> targets, List<Object> arrows)
        {
            // no arrows, no miss
            if (arrows == null)
            {
                return false;
            }
            // no targets, always miss
            if (targets == null)
            {
                return true;
            }
            for (Object o : arrows)
            {
                if (!targets.contains(o))
                {
                    return true;
                }
            }
            return false;
        }


        /************* rendering methods **************/

        protected void render(String indent, StringBuilder s)
        {
            if (render_start(indent, s))
            {
                render_body(indent, s);
                render_end(indent, s);
            }
        }

        protected boolean render_start(String indent, StringBuilder s)
        {
            if (indent != null)
            {
                s.append(indent);
            }
            s.append('<');
            render_name(s);
            render_id(s);
            render_classes(s);
            render_attributes(s);
            if (isEmpty())
            {
                s.append("/>");
                return false;
            }
            else
            {
                s.append('>');
                return true;
            }
        }

        protected void render_name(StringBuilder s)
        {
            s.append(name == null ? "div" : name);
        }

        protected void render_id(StringBuilder s)
        {
            if (id != null)
            {
                s.append(" id=\"").append(id).append('"');
            }
        }

        protected void render_classes(StringBuilder s)
        {
            if (classes != null)
            {
                s.append(" class=\"");
                for (int i=0; i < classes.size(); i++)
                {
                    s.append(classes.get(i));
                    if (i + 1 != classes.size())
                    {
                        s.append(' ');
                    }
                }
                s.append('"');
            }
        }

        protected void render_attributes(StringBuilder s)
        {
            if (attributes != null)
            {
                for (Map.Entry<Object,Object> entry : attributes.entrySet())
                {
                    s.append(' ').append(entry.getKey()).append("=\"");
                    if (entry.getValue() != null)
                    {
                        s.append(entry.getValue());
                    }
                    s.append('"');
                }
            }
        }

        protected void render_body(String indent, StringBuilder s)
        {
            String kidIndent = indent + tool.getTab();
            for (Object o : children)
            {
                if (o instanceof Tag)
                {
                    ((Tag)o).render(kidIndent, s);
                }
                else
                {
                    s.append(kidIndent);
                    s.append(o);
                }
            }
        }

        protected void render_end(String indent, StringBuilder s)
        {
            if (indent != null)
            {
                s.append(indent);
            }
            s.append("</").append(name).append('>');
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            root().render("\n", s);
            return s.toString();
        }
    }
}

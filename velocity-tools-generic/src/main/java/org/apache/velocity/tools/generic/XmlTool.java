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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.tools.XmlUtils;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * <p>Tool for reading/navigating XML files, with XPath expressions support.</p>
 *
 * <p>Configuration parameters:</p>
 * <ul>
 *     <li><code>resource</code>=<i>file or classpath resource</i></li>
 *     <li><code>source</code>=<i>URL</i></li>
 * </ul>
 *
 * <p>Here's a short example:</p>
 * <pre>
 * XML file:
 *   &lt;foo&gt;&lt;bar&gt;woogie&lt;/bar&gt;&lt;a name="test"/&gt;&lt;/foo&gt;
 *
 * Template:
 *   $foo.bar.text
 *   $foo.find('a')
 *   $foo.a.name
 *
 * Output:
 *   woogie
 *   &lt;a name="test"/&gt;
 *   test
 *
 * Configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="application"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.XmlTool"
 *              key="foo" source="doc.xml"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 * <p>Note that this tool is included in the default GenericTools configuration
 * under the key "xml". You can read  but unless you set safeMode="false" for it, you will
 * only be able to parse XML strings.  Safe mode is on by default and blocks
 * access to the {@link #read(String)} method.</p>
 *
 * @author Nathan Bubna
 * @author Claude Brisson
 * @version $Revision: 1769055 $ $Date: 2006-11-27 10:49:37 -0800 (Mon, 27 Nov 2006) $
 * @since VelocityTools 2.0
 */

@DefaultKey("xml")
public class XmlTool extends SafeConfig implements Serializable
{
    private static final long serialVersionUID = 8217744660691416595L;

    /**
     * ImportSupport utility which provides underlying i/o
     */
    protected transient ImportSupport importSupport = null;

    /**
     * ImportSupport initialization
     * @param config configuration values
     */
    protected synchronized void initializeImportSupport(ValueParser config)
    {
        if (importSupport == null)
        {
            importSupport = new ImportSupport();
            importSupport.configure(config);
        }
    }

    /**
     * Configuration.
     * @param values configuration values
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);
        initializeImportSupport(values);
        String resource = values.getString(ImportSupport.RESOURCE_KEY);
        if (resource != null)
        {
            read(resource);
        }
        else
        {
            String url = values.getString(ImportSupport.URL_KEY);
            if (url != null)
            {
                /* temporary disable safe mode */
                boolean safeMode = importSupport.isSafeMode();
                importSupport.setSafeMode(false);
                fetch(url);
                importSupport.setSafeMode(safeMode);
            }
        }
    }

    /**
     * Content nodes.
     */
    private List<Node> nodes = null;

    /**
     * Default constructor.
     */
    public XmlTool() {}

    /**
     * Builds an XmlTool around a node.
     * @param node target node
     */
    public XmlTool(Node node)
    {
        this(Collections.singletonList(node));
    }

    /**
     * Builds an XmlTool around a nodes list.
     * @param nodes nodes list
     */
    public XmlTool(List<Node> nodes)
    {
        this.nodes = nodes;
    }

    /**
     * Sets a singular root {@link Node} for this instance.
     * @param node new root node
     */
    protected void setRoot(Node node)
    {
        if (node == null)
        {
            this.nodes = null;
        }
        else
        {
            if (node instanceof Document)
            {
                node = ((Document) node).getDocumentElement();
            }
            this.nodes = new ArrayList<Node>(1);
            this.nodes.add(node);
        }
    }

    /**
     * Parses the given XML string and uses the resulting {@link Document}
     * as the root {@link Node}.
     * @param xml XML string
     * @return new XmlTool
     */
    public XmlTool parse(String xml)
    {
        try
        {
            if (xml != null)
            {
                setRoot(XmlUtils.parse(xml));
            }
        }
        catch (Exception e)
        {
            getLog().error("could not parse given XML string", e);
        }
        return this;
    }

    /**
     * Reads and parses a local resource file
     * @param resource resource path
     * @return new  XmlTool
     */
    public XmlTool read(String resource)
    {
        Reader reader = null;
        try
        {
            if (importSupport == null)
            {
                initializeImportSupport(new ValueParser());
            }
            reader = importSupport.getResourceReader(resource);
            if (reader != null)
            {
                setRoot(XmlUtils.parse(reader));
            }
        }
        catch (Exception e)
        {
            getLog().error("could not read XML resource {}", resource, e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ioe) {}
            }
        }
        return this;
    }

    /**
     * Reads and parses a remote or local URL
     * @param url resource URL
     * @return new XmlTool
     */
    public XmlTool fetch(String url)
    {
        Reader reader = null;
        try
        {
            if (importSupport == null)
            {
                initializeImportSupport(new ValueParser());
            }
            reader = importSupport.acquireReader(url);
            if (reader != null)
            {
                setRoot(XmlUtils.parse(reader));
            }
        }
        catch (Exception e)
        {
            getLog().error("could not fetch XML content from URL {}", url, e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ioe) {}
            }
        }
        return this;
    }

    /**
     * This will first attempt to find an attribute with the
     * specified name and return its value.  If no such attribute
     * exists or its value is {@code null}, this will attempt to convert
     * the given value to a {@link Number} and get the result of
     * {@link #get(Number)}.  If the number conversion fails,
     * then this will convert the object to a string. If that string
     * does not contain a '/', it appends the result of {@link #getPath()}
     * and a '/' to the front of it.  Finally, it delegates the string to the
     * {@link #find(String)} method and returns the result of that.
     * @param o attribute name, number, xpath relative expression
     * @return found value or null
     */
    public Object get(Object o)
    {
        if (isEmpty() || o == null)
        {
            return null;
        }
        String attr = attr(o);
        if (attr != null && attr.length() > 0)
        {
            return attr;
        }
        Number i = ConversionUtils.toNumber(o);
        if (i != null)
        {
            return get(i);
        }
        String s = String.valueOf(o);
        if (s.length() == 0)
        {
            return null;
        }
        if (s.indexOf('/') < 0)
        {
            s = getPath()+'/'+s;
        }
        return find(s);
    }


    /**
     * Asks {@link #get(Object)} for a "name" result.
     * If none, this will return the result of {@link #getNodeName()}.
     * @return found value or null
     */
    public Object getName()
    {
        // give attributes and child elements priority
        Object name = get("name");
        if (name != null && !"".equals(name))
        {
            return name;
        }
        return getNodeName();
    }

    /**
     * Returns the name of the root node. If the internal {@link Node}
     * list has more than one {@link Node}, it will only return the name
     * of the first node in the list.
     * @return node name
     */
    public String getNodeName()
    {
        if (isEmpty())
        {
            return null;
        }
        return node().getNodeName();
    }

    /**
     * Returns the XPath that identifies the first/sole {@link Node}
     * represented by this instance.
     * @return xpath node path
     */
    public String getPath()
    {
        if (isEmpty())
        {
            return null;
        }
        return XmlUtils.nodePath(node());
    }

    /**
     * Returns the value of the specified attribute for the first/sole
     * {@link Node} in the internal Node list for this instance, if that
     * Node is an {@link Element}.  If it is a non-Element node type or
     * there is no value for that attribute in this element, then this
     * will return {@code null}.
     * @param o attribute name
     * @return attribute value or null
     */
    public String attr(Object o)
    {
        if (o == null)
        {
            return null;
        }
        String key = String.valueOf(o);
        Node node = node();
        if (node instanceof Element)
        {
            String value = ((Element)node).getAttribute(key);
            if (value.length() > 0)
            {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns a {@link Map} of all attributes for the first/sole
     * {@link Node} held internally by this instance.  If that Node is
     * not an {@link Element}, this will return null.
     * @return attributes map
     */
    public Map<String,String> attributes()
    {
        Node node = node();
        if (node instanceof Element)
        {
            Map<String,String> attrs = new HashMap<String,String>();
            NamedNodeMap map = node.getAttributes();
            for (int i = 0; i < map.getLength(); ++i)
            {
                Attr attr = (Attr)map.item(i);
                attrs.put(attr.getName(), attr.getValue());
            }
            return attrs;
        }
        return null;
    }


    /**
     * Returns {@code true} if there are no {@link Node}s internally held
     * by this instance.
     * @return whether the set of current nodes is empty
     */
    public boolean isEmpty()
    {
        return (nodes == null || nodes.isEmpty());
    }

    /**
     * Returns the number of {@link Node}s internally held by this instance.
     * @return the size of the set of current nodes
     */
    public int size()
    {
        if (isEmpty())
        {
            return 0;
        }
        return nodes.size();
    }

    /**
     * Returns an {@link Iterator} that returns new {@link XmlTool}
     * instances for each {@link Node} held internally by this instance.
     * @return an iterator on wrappers of current nodes or null
     */
    public Iterator<XmlTool> iterator()
    {
        if (isEmpty())
        {
            return null;
        }
        return new NodeIterator(nodes.iterator());
    }

    /**
     * Returns an {@link XmlTool} that wraps only the
     * first {@link Node} from this instance's internal Node list.
     * @return a wrapper on the first node or null
     */
    public XmlTool getFirst()
    {
        if (size() == 1)
        {
            return this;
        }
        else if (isEmpty())
        {
            return null;
        }
        return new XmlTool(node());
    }

    /**
     * Returns an {@link XmlTool} that wraps only the
     * last {@link Node} from this instance's internal Node list.
     * @return a wrapper on the last node or null
     */
    public XmlTool getLast()
    {
        if (size() == 1)
        {
            return this;
        }
        else if (isEmpty())
        {
            return null;
        }
        return new XmlTool(nodes.get(size() - 1));
    }

    /**
     * Returns an {@link XmlTool} that wraps the specified
     * {@link Node} from this instance's internal Node list.
     * @param n node index in current nodes list
     * @return a wrapper on the nth node or null
     */
    public XmlTool get(Number n)
    {
        if (n == null || isEmpty())
        {
            return null;
        }
        int i = n.intValue();
        if (i < 0 || i > size() - 1)
        {
            return null;
        }
        return new XmlTool(nodes.get(i));
    }

    /**
     * Returns the first/sole {@link Node} from this
     * instance's internal Node list, if any.
     * @return the unwrapped first node or null
     */
    public Node node()
    {
        if (isEmpty())
        {
            return null;
        }
        return nodes.get(0);
    }


    /**
     * Converts the specified object to a String and calls
     * {@link #find(String)} with that.
     * @param o xpath to search
     * @return a wrapper on found nodes or null
     */
    public XmlTool find(Object o)
    {
        if (o == null || isEmpty())
        {
            return null;
        }
        return find(String.valueOf(o));
    }

    /**
     * Performs an XPath selection on the current set of
     * {@link Node}s held by this instance and returns a new
     * {@link XmlTool} instance that wraps those results.
     * If the specified value is null or this instance does
     * not currently hold any nodes, then this will return
     * {@code null}.  If the specified value, when converted
     * to a string, does not contain a '/' character, then
     * it has "//" prepended to it.  This means that a call to
     * {@code $xml.find("a")} is equivalent to calling
     * {@code $xml.find("//a")}.  The full range of XPath
     * selectors is supported here.
     * @param xpath xpath to search
     * @return a wrapper on found nodes or null
     */
    public XmlTool find(String xpath)
    {
        if (xpath == null || xpath.length() == 0 || isEmpty())
        {
            return null;
        }
        if (xpath.indexOf('/') < 0)
        {
            xpath = "//"+xpath;
        }
        List<Node> found = new ArrayList<Node>();
        for (Node n : nodes)
        {
            NodeList lst = XmlUtils.search(xpath, n);
            if (lst != null)
            {
                for (int i = 0; i < lst.getLength(); ++i)
                {
                    found.add(lst.item(i));
                }
            }
        }
        if (found.isEmpty())
        {
            return null;
        }
        return new XmlTool(found);
    }

    /**
     * Returns a new {@link XmlTool} instance that wraps
     * the parent {@link Element} of the first/sole {@link Node}
     * being wrapped by this instance.
     * @return wrapper on the parent of the first node or null
     */
    public XmlTool getParent()
    {
        if (isEmpty())
        {
            return null;
        }
        Node parent = node().getParentNode();
        if (parent == null || parent instanceof Document)
        {
            return null;
        }
        return new XmlTool(parent);
    }

    /**
     * Returns a new {@link XmlTool} instance that wraps
     * the parent {@link Element}s of each of the {@link Node}s
     * being wrapped by this instance.  This does not return
     * all ancestors, just the immediate parents.
     * @return a wrapper on the set of parents nodes or null
     */
    public XmlTool parents()
    {
        if (isEmpty())
        {
            return null;
        }
        if (size() == 1)
        {
            return getParent();
        }
        List<Node> parents = new ArrayList<Node>(size());
        for (Node n : nodes)
        {
            Element parent = null;
            if (n instanceof Element)
            {
                parent = (Element)n.getParentNode();
            }
            else if (n instanceof Attr)
            {
                parent = ((Attr) n).getOwnerElement();
            }
            if (parent != null && !parents.contains(parent))
            {
                parents.add(parent);
            }
        }
        if (parents.isEmpty())
        {
            return null;
        }
        return new XmlTool(parents);
    }

    /**
     * Returns a new {@link XmlTool} instance that wraps all the
     * child {@link Element}s of all the current internally held nodes
     * that are {@link Element}s themselves.
     * @return a wrapper on the children of current nodes
     */
    public XmlTool children()
    {
        if (isEmpty())
        {
            return null;
        }
        List<Node> kids = new ArrayList<Node>();
        for (Node n : nodes)
        {
            if (n instanceof Element)
            {
                NodeList lst = n.getChildNodes();
                for (int i = 0; i < lst.getLength(); ++i)
                {
                    Node child = lst.item(i);
                    if (child instanceof Text)
                    {
                        String value = child.getNodeValue().trim();
                        if (value.length() == 0)
                        {
                            continue;
                        }
                    }
                    kids.add(child);
                }
            }
        }
        return new XmlTool(kids);
    }

    /**
     * Returns the concatenated text content of all the internally held
     * nodes.  Obviously, this is most useful when only one node is held.
     * @return text of current nodes or null
     */
    public String getText()
    {
        if (isEmpty())
        {
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (Node n : nodes)
        {
            String text = n.getTextContent().trim();
            if (text != null && text.length() > 0)
            {
                out.append(text);
            }
        }
        String result = out.toString().trim();
        if (result.length() > 0)
        {
            return result;
        }
        return null;
    }


    /**
     * If this instance has no XML {@link Node}s, then this
     * returns the result of {@code super.toString()}.  Otherwise, it
     * returns the XML (as a string) of all the internally held nodes
     * that are not {@link Attr}ibutes. For attributes, only the value
     * is used.
     * @return XML string of current nodes or null
     */
    public String toString()
    {
        if (isEmpty())
        {
            return super.toString();
        }
        StringBuilder out = new StringBuilder();
        for (Node n : nodes)
        {
            if (n instanceof Attr)
            {
                out.append(((Attr)n).getValue().trim());
            }
            else
            {
                out.append(XmlUtils.nodeToString(n));
            }
        }
        return out.toString().trim();
    }


    /**
     * Iterator implementation that wraps a Node list iterator
     * to return new XmlTool instances for each item in the wrapped
     * iterator.s
     */
    public static class NodeIterator implements Iterator<XmlTool>
    {
        private Iterator<Node> i;

        public NodeIterator(Iterator<Node> i)
        {
            this.i = i;
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public XmlTool next()
        {
            return new XmlTool(i.next());
        }

        public void remove()
        {
            i.remove();
        }
    }
}

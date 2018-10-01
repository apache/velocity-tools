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
package org.apache.velocity.tools;

import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * <p>Utility class for simplifying parsing of xml documents. Documents are not validated, and
 * loading of external files (xinclude, external entities, DTDs, etc.) are disabled.</p>
 *
 * @author Claude Brisson
 * @since 3.0
 * @version $$
 */
public final class XmlUtils
{
    /* several pieces of code were borrowed from the Apache Shindig XmlUtil class.*/

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

    /**
     * Handles xml errors so that they're not logged to stderr.
     */
    private static final ErrorHandler errorHandler = new ErrorHandler()
    {
        public void error(SAXParseException exception) throws SAXException
        {
            throw exception;
        }
        public void fatalError(SAXParseException exception) throws SAXException
        {
            throw exception;
        }
        public void warning(SAXParseException exception)
        {
            LOGGER.info("warning during parsing", exception);
        }
    };

    private static boolean canReuseBuilders = false;

    private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    private static final ThreadLocal<DocumentBuilder> reusableBuilder
        = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try
            {
                LOGGER.trace("Created a new document builder");
                return builderFactory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    static
    {
        // Namespace support is required for <os:> elements
        builderFactory.setNamespaceAware(true);

        // Disable various insecure and/or expensive options.
        builderFactory.setValidating(false);

        // Can't disable doctypes entirely because they're usually harmless. External entity
        // resolution, however, is both expensive and insecure.
        try
        {
            builderFactory.setAttribute("http://xml.org/sax/features/external-general-entities", false);
        }
        catch (IllegalArgumentException e)
        {
            // Not supported by some very old parsers.
            LOGGER.info("Error parsing external general entities: ", e);
        }

        try
        {
            builderFactory.setAttribute("http://xml.org/sax/features/external-parameter-entities", false);
        }
        catch (IllegalArgumentException e)
        {
            // Not supported by some very old parsers.
            LOGGER.info("Error parsing external parameter entities: ", e);
        }

        try
        {
            builderFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        catch (IllegalArgumentException e)
        {
            // Only supported by Apache's XML parsers.
            LOGGER.info("Error parsing external DTD: ", e);
        }

        try
        {
            builderFactory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        }
        catch (IllegalArgumentException e)
        {
            // Not supported by older parsers.
            LOGGER.info("Error parsing secure XML: ", e);
        }

        try
        {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.reset();
            canReuseBuilders = true;
            LOGGER.trace("reusing document builders");
        }
        catch (UnsupportedOperationException e)
        {
            // Only supported by newer parsers (xerces 2.8.x+ for instance).
            canReuseBuilders = false;
            LOGGER.trace("not reusing document builders");
        }
        catch (ParserConfigurationException e)
        {
            // Only supported by newer parsers (xerces 2.8.x+ for instance).
            canReuseBuilders = false;
            LOGGER.trace("not reusing document builders");
        }
    }

    private static LinkedBlockingDeque<SoftReference<DocumentBuilder>> builderPool = new LinkedBlockingDeque<SoftReference<DocumentBuilder>>(); // contains only idle builders
    private static int maxBuildersCount = 100;
    private static int currentBuildersCount = 0;
    private static final String BUILDER_MAX_INSTANCES_KEY = "velocity.tools.xml.documentbuilder.max.instances";

    static
    {
        /* We're in a static code portion, so use a system property so that the DocumentBuilder pool size
        * remains configurable. */
        try
        {
            String configuredMax = System.getProperty(BUILDER_MAX_INSTANCES_KEY);
            if (configuredMax != null)
            {
                maxBuildersCount = Integer.parseInt(configuredMax);
            }
        }
        catch(Exception e)
        {
            LOGGER.error("could not configure XML document builder max instances count", e);
        }
    }

    /**
     * Get a document builder
     * @return document builder
     */
    private static synchronized DocumentBuilder getDocumentBuilder()
    {
        DocumentBuilder builder = null;
        if (canReuseBuilders && builderPool.size() > 0)
        {
            builder = builderPool.pollFirst().get();
        }
        if (builder == null)
        {
            if(!canReuseBuilders || currentBuildersCount < maxBuildersCount)
            {
                try
                {
                    builder = builderFactory.newDocumentBuilder();
                    builder.setErrorHandler(errorHandler);
                    ++currentBuildersCount;
                }
                catch(Exception e)
                {
                    /* this is a fatal error */
                    throw new RuntimeException("could not create a new XML DocumentBuilder instance", e);
                }
            }
            else
            {
                try
                {
                    LOGGER.warn("reached XML DocumentBuilder pool size limit, current thread needs to wait; you can increase pool size with the {} system property", BUILDER_MAX_INSTANCES_KEY);
                    builder = builderPool.takeFirst().get();
                }
                catch(InterruptedException ie)
                {
                    LOGGER.warn("caught an InterruptedException while waiting for a DocumentBuilder instance");
                }
            }
        }
        return builder;
    }

    /**
     * Release the given document builder
     * @param document builder
     */
    private static synchronized void releaseBuilder(DocumentBuilder builder)
    {
        builder.reset();
        builderPool.addLast(new SoftReference<DocumentBuilder>(builder));
    }

    private XmlUtils() {}

    /**
     * Extracts an attribute from a node.
     *
     * @param node target node
     * @param attr attribute name
     * @param def default value
     * @return The value of the attribute, or def
     */
    public static String getAttribute(Node node, String attr, String def)
    {
        NamedNodeMap attrs = node.getAttributes();
        Node val = attrs.getNamedItem(attr);
        if (val != null)
        {
            return val.getNodeValue();
        }
        return def;
    }

    /**
     * @param node target node
     * @param attr attribute name
     * @return The value of the given attribute, or null if not present.
     */
    public static String getAttribute(Node node, String attr)
    {
        return getAttribute(node, attr, null);
    }

    /**
     * Retrieves an attribute as a boolean.
     *
     * @param node target node
     * @param attr attribute name
     * @param def default value
     * @return True if the attribute exists and is not equal to "false"
     *    false if equal to "false", and def if not present.
     */
    public static boolean getBoolAttribute(Node node, String attr, boolean def)
    {
        String value = getAttribute(node, attr);
        if (value == null)
        {
            return def;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * @param node target node
     * @param attr attribute name
     * @return True if the attribute exists and is not equal to "false"
     *    false otherwise.
     */
    public static boolean getBoolAttribute(Node node, String attr)
    {
        return getBoolAttribute(node, attr, false);
    }

    /**
     * @param target node
     * @param attr attribute name
     * @param def default value
     * @return An attribute coerced to an integer.
     */
    public static int getIntAttribute(Node node, String attr, int def)
    {
        String value = getAttribute(node, attr);
        if (value == null)
        {
            return def;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

    /**
     * @param node target node
     * @param attr attribute name
     * @return An attribute coerced to an integer.
     */
    public static int getIntAttribute(Node node, String attr)
    {
        return getIntAttribute(node, attr, 0);
    }

    /**
     * Attempts to parse the input xml into a single element.
     * @param xml xml stream reader
     * @return The document object
     */
    public static Element parse(Reader xml)
    {
        Element ret = null;
        DocumentBuilder builder = getDocumentBuilder();
        try
        {
            ret = builder.parse(new InputSource(xml)).getDocumentElement();
            releaseBuilder(builder);
            return ret;
        }
        catch(Exception e)
        {
            LOGGER.error("could not parse given xml", e);
        }
        finally
        {
            releaseBuilder(builder);
        }
        return ret;
    }

    /**
     * Attempts to parse the input xml into a single element.
     * @param xml xml string
     * @return The document object
     */
    public static Element parse(String xml)
    {
        return parse(new StringReader(xml));
    }

    public static NodeList search(String xpath, Node context)
    {
        NodeList ret = null;
        try
        {
            XPath xp = XPathFactory.newInstance().newXPath();
            XPathExpression exp = xp.compile(xpath);
            ret = (NodeList)exp.evaluate(context, XPathConstants.NODESET);
        }
        catch (XPathExpressionException xpe)
        {
            LOGGER.error("could not process xpath expression {}", xpath, xpe);
        }
        return ret;
    }

    /**
     * <p>Builds the xpath expression for a node (tries to use id/name nodes when possible to get a unique path)</p>
     * @param n target node
     * @return node xpath
     */
    // (borrow from http://stackoverflow.com/questions/5046174/get-xpath-from-the-org-w3c-dom-node )
    public static String nodePath(Node n)
    {
        // abort early
        if (null == n)
            return null;

        // declarations
        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();
        StringBuffer buffer = new StringBuffer('/');

        // push element on stack
        hierarchy.push(n);

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }

        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element e = (Element) node;

                // is this the root element?
                if (buffer.length() == 1)
                {
                    // root element - simply append element name
                    buffer.append(node.getNodeName());
                }
                else
                {
                    // child element - append slash and element name
                    buffer.append("/");
                    buffer.append(node.getNodeName());

                    if (node.hasAttributes())
                    {
                        // see if the element has a name or id attribute
                        if (e.hasAttribute("id"))
                        {
                            // id attribute found - use that
                            buffer.append("[@id='" + e.getAttribute("id") + "']");
                            handled = true;
                        }
                        else if (e.hasAttribute("name"))
                        {
                            // name attribute found - use that
                            buffer.append("[@name='" + e.getAttribute("name") + "']");
                            handled = true;
                        }
                    }

                    if (!handled)
                    {
                        // no known attribute we could use - get sibling index
                        int prev_siblings = 1;
                        Node prev_sibling = node.getPreviousSibling();
                        while (null != prev_sibling)
                        {
                            if (prev_sibling.getNodeType() == node.getNodeType())
                            {
                                if (prev_sibling.getNodeName().equalsIgnoreCase(
                                    node.getNodeName()))
                                {
                                    prev_siblings++;
                                }
                            }
                            prev_sibling = prev_sibling.getPreviousSibling();
                        }
                        buffer.append("[" + prev_siblings + "]");
                    }
                }
            }
            else if (node.getNodeType() == Node.ATTRIBUTE_NODE)
            {
                buffer.append("/@");
                buffer.append(node.getNodeName());
            }
        }
        // return buffer
        return buffer.toString();
    }

    /**
     * XML Node to string
     * @param node XML node
     * @return XML node string representation
     */
    public static String nodeToString(Node node)
    {
        StringWriter sw = new StringWriter();
        try
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            /* CB - Since everything is already stored as strings in memory why shoud an encoding be required here? */
            t.setOutputProperty(OutputKeys.ENCODING, RuntimeConstants.ENCODING_DEFAULT);
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch (TransformerException te)
        {
            LOGGER.error("could not convert XML node to string", te);
        }
        return sw.toString();
    }

    /**
     * Checkes whether the given mime type is an XML format
     * @param mimeType
     * @return <code>true</code> if this mime type is an XML format
     */
    public static boolean isXmlMimeType(String mimeType)
    {
        return mimeType != null &&
            (
                "text/xml".equals(mimeType) ||
                "application/xml".equals(mimeType) ||
                mimeType.endsWith("+xml")
            );
    }
}

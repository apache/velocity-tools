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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.XmlUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <p>Tool which can parse a JSON file.</o>
 * <p>Usage:</p>
 * <p>
 *     <ul>
 *         <li>$json.parse(<i>json string</i>)</li>
 *         <li>$json.read(<i>file or classpath resource</i>)</li>
 *         <li>$json.fetch(<i>URL</i>)</li>
 *     </ul>
 * </p>
 * <p>Configuration parameters:</p>
 * <p>
 *     <ul>
 *         <li><code>resource</code>=<i>file or classpath resource</i></li>
 *         <li><code>source</code>=<i>URL</i></li>
 *     </ul>
 * </p>
 * <p>
 *     <pre>
 * Example configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.generic.JsonTool"
 *              key="foo" resource="doc.xml"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 *
 *     </pre>
 * </p>
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Id:$
 */

// JSONObject isn't (yet?) Serializable, so session scope is invalid
@DefaultKey("json")
@InvalidScope(Scope.SESSION)
public class JsonTool extends ImportSupport implements Iterable
{
    /**
     * ImportSupport utility which provides underlying i/o
     */
    protected ImportSupport importSupport = null;

    /**
     * ImportSupport initialization
     * @param config
     */
    protected void initializeImportSupport(ValueParser config)
    {
        importSupport = new ImportSupport();
        importSupport.configure(config);
    }

    /**
     * JSONObject content
     */
    private JSONObject jsonObject = null;

    /**
     * JSONArray content
     */
    private JSONArray jsonArray = null;

    /**
     * Looks for the "file" parameter and automatically uses
     * {@link #initJSON(String)} to parse the file (searched in filesystem current path and classpath) and set the
     * resulting JSON object as the root node for this instance.
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
                fetch(url);
            }
        }
    }

    /**
     * Initialize JSON content from a string.
     * @param json
     */
    protected void initJSON(String json)
    {
        if (json != null)
        {
            initJSON(new StringReader(json));
        }
    }

    /**
     * Initialize JSON content from a reader.
     * @param reader
     */
    protected void initJSON(Reader reader)
    {
        try
        {
            final int lookahead = 100;
            int jsonType = 0; // 1 = object, 2 = array
            if (!reader.markSupported())
            {
                reader = new BufferedReader(reader);
            }
            reader.mark(lookahead);
            char buffer[] = new char[lookahead];
            int read = reader.read(buffer);
            reader.reset();
            for (int i = 0; i < read; ++i)
            {
                switch (buffer[i])
                {
                    case '{':
                        jsonType = 1;
                        break;
                    case '[':
                        jsonType = 2;
                        break;
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        break;
                    default:
                    {
                        String msg = "could not pase JSON: invalid character at position " + i + ": '" + buffer[i] + "'";
                        throw new Exception(msg);
                    }
                }
                if (jsonType != 0)
                {
                    break;
                }
            }
            switch (jsonType)
            {
                case 0:
                {
                    String msg = "could not pase JSON: did not find any '{' or '[' in the first " + lookahead + " characters";
                    throw new Exception(msg);
                }
                case 1:
                    jsonArray = null;
                    jsonObject = new JSONObject(new JSONTokener(reader));
                    break;
                case 2:
                    jsonObject = null;
                    jsonArray = new JSONArray(new JSONTokener(reader));
            }
        }
        catch (Exception e)
        {
            getLog().error("error while setting up JSON source", e);
        }
    }

    /**
     * Parses the given JSON string and uses the resulting {@link Document}
     * as the root {@link Node}.
     */
    public void parse(String xml) throws Exception
    {
        if (xml != null)
        {
            try
            {
                initJSON(xml);
            }
            catch (Exception e)
            {
                getLog().error("could not parse given JSON string", e);
            }
        }
    }

    /**
     * Reads and parses a local JSON resource file
     */
    public void read(String resource)
    {
        if (resource != null)
        {
            try
            {
                Reader reader = importSupport.getResourceReader(resource);
                if (reader != null)
                {
                    initJSON(reader);
                }
            }
            catch (Exception e)
            {
                getLog().error("could not read JSON resource {}", resource, e);
            }
        }
    }

    /**
     * Reads and parses a remote or local URL
     */
    public void fetch(String url)
    {
        if (url != null)
        {
            try
            {
                Reader reader = importSupport.acquireReader(url);
                if (reader != null)
                {
                    initJSON(reader);
                }
            }
            catch (Exception e)
            {
                getLog().error("could not fetch JSON content from URL {}", url, e);
            }
        }
    }

    /**
     * Get JSON root object.
     * @return root object or array
     */
    public Object root()
    {
        return jsonObject != null ? jsonObject : jsonArray ;
    }

    /**
     * Get nth element from root json array.
     * @param index n
     * @return nth element, or null if root object is null or not an array
     */
    public Object get(int index)
    {
        return jsonArray == null ? null : jsonArray.get(index);
    }

    /**
     * Get a property from root object
     * @param key
     * @return property value, or null
     */
    public Object get(String key)
    {
        return jsonObject == null ? null : jsonObject.get(key);
    }

    /**
     * Iterate keys of root object.
     * @return iterator
     */
    public Iterator<String> keys()
    {
        return jsonObject == null ? null : jsonObject.keys();
    }

    /**
     * Get set of root object keys.
     * @return
     */
    public Set<String> keySet()
    {
        return jsonObject == null ? null : jsonObject.keySet();
    }

    /**
     * Get an iterator. For a root object, returns an iterator over key names. For a root array, returns an iterator
     * over contained objects.
     * @return iterator
     */
    public Iterator iterator()
    {
        if (jsonObject != null)
        {
            return jsonObject. keys();
        }
        else if (jsonArray != null)
        {
            return jsonArray.iterator();
        }
        return null;
    }

    /**
     * Get size of root object or array.
     * @return size
     */
    public int length()
    {
        return jsonObject == null ? jsonArray == null ? null : jsonArray.length() : jsonObject.length();
    }

    /**
     * Get array of root object keys.
     * @return array of keys
     */
    public JSONArray names()
    {
        return jsonObject == null ? null : jsonObject.names();
    }

    /**
     * Query root object or array using a JSON pointer
     * @param jsonPointer
     * @return result
     */
    public Object query(String jsonPointer)
    {
        return jsonObject == null ? jsonArray == null ? null : jsonArray.query(jsonPointer) : jsonObject.query(jsonPointer);
    }

    /**
     * Convert JSON object or array into string
     * @return JSON representation of the root object or array
     */
    public String toString()
    {
        return jsonObject == null ? jsonArray == null ? "null" : jsonArray.toString() : jsonObject.toString();
    }
}

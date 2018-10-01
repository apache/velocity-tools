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

import java.util.Iterator;
import java.util.Set;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * Container for *either* an array *or* an object
 */

public class JsonContent
{
    /**
     * JSONObject content
     */
    private JsonObject jsonObject = null;

    /**
     * JSONArray content
     */
    private JsonArray jsonArray = null;

    /**
     * wraps the object into an hybrid JSON container if necessary
     */
    private static Object wrapIfNeeded(Object obj)
    {
        if (obj == null)
        {
            return obj;
        }
        else if (obj instanceof JsonArray)
        {
            return new JsonContent((JsonArray)obj);
        }
        else if (obj instanceof JsonObject)
        {
            return new JsonContent((JsonObject)obj);
        }
        else
        {
            return obj;
        }
    }

    /**
     * wraps an object
     * @param object JsonObject to wrap
     */
    public JsonContent(JsonObject object)
    {
        jsonObject = object;
    }

    /**
     * wraps an array
     * @param array JsonArray to wrap
     */
    public JsonContent(JsonArray array)
    {
        jsonArray = array;
    }

    /**
     * Get a value from root array
     * @param index array index
     * @return value, or null
     */
    public Object get(int index)
    {
        Object ret = null;
        if (jsonArray != null)
        {
            ret = wrapIfNeeded(jsonArray.get(index));
        }
        else if (jsonObject != null)
        {
            ret = wrapIfNeeded(jsonObject.get(String.valueOf(index)));
        }
        return ret;
    }

    /**
     * Get a property from root object
     * @param key map key
     * @return property value, or null
     */
    public Object get(String key)
    {
        Object ret = null;
        if (jsonArray != null)
        {
            try
            {
                ret = wrapIfNeeded(jsonArray.get(Integer.parseInt(key)));
            }
            catch (NumberFormatException nfe) {}
        }
        else if (jsonObject != null)
        {
            ret = wrapIfNeeded(jsonObject.get(key));
        }
        return ret;
    }

    /**
     * Iterate keys of root object.
     * @return iterator
     */
    public Iterator<String> keys()
    {
        return jsonObject == null ? null : jsonObject.keySet().iterator();
    }

    /**
     * Get set of root object keys.
     * @return keys set
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
            return jsonObject.keySet().iterator();
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
    public int size()
    {
        return jsonObject == null ? jsonArray == null ? null : jsonArray.size() : jsonObject.size();
    }

    /**
     * Convert JSON object or array into string
     * @return JSON representation of the root object or array
     */
    public String toString()
    {
        return jsonObject == null ? jsonArray == null ? "null" : jsonArray.toString() : jsonObject.toString();
    }

    /**
     * Check if wrapped object is null
     * @return true if wrapped object is null
     */
    public boolean isNull()
    {
        return jsonArray == null && jsonObject == null;
    }

    /**
     * Check if wrapped object is a JSONObject
     * @return true if wrapped object is a JSONObject
     */
    public boolean isObject()
    {
        return jsonObject != null;
    }

    /**
     * Check if wrapped object is a JSONArray
     * @return true if wrapped object is a JSONArray
     */
    public boolean isArray()
    {
        return jsonArray != null;
    }
}

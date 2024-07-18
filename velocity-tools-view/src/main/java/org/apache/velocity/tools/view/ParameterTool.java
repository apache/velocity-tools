package org.apache.velocity.tools.view;

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

import java.lang.reflect.Array;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.ServletRequest;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * <p>Utility class for easy parsing of {@link ServletRequest} parameters.</p>
 * <pre>
 * Template example(s):
 *   $params.foo                -&gt;  bar
 *   $params.getNumber('baz')   -&gt;  12.6
 *   $params.getInt('baz')      -&gt;  12
 *   $params.getNumbers('baz')  -&gt;  [12.6]
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.view.ParameterTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre>
 *
 * <p>When used as a view tool, this should only be used in the request scope.
 * This class is, however, quite useful in your application's controller, filter,
 * or action code as well as in templates.</p>
 *
 * @author Nathan Bubna
 * @version $Revision$ $Date$
 * @since VelocityTools 2.0
 */
@DefaultKey("params")
@ValidScope(Scope.REQUEST)
public class ParameterTool extends ValueParser
{
    private ServletRequest request;

    /**
     * Constructs a new instance
     */
    public ParameterTool()
    {}

    /**
     * Constructs a new instance using the specified request.
     *
     * @param request the {@link ServletRequest} to be parsed
     */
    public ParameterTool(ServletRequest request)
    {
        setRequest(request);
    }

    @Override
    protected void configure(ValueParser values)
    {
        super.configure(values);

        ServletRequest req = (ServletRequest)values.getValue(ViewContext.REQUEST);
        setRequest(req);
    }

    /**
     * Sets the current {@link ServletRequest}
     *
     * @param request the {@link ServletRequest} to be parsed
     */
    public void setRequest(ServletRequest request)
    {
        this.request = request;
    }

    /**
     * Returns the current {@link ServletRequest} for this instance.
     *
     * @return the current {@link ServletRequest}
     * @throws UnsupportedOperationException if the request is null
     */
    protected ServletRequest getRequest()
    {
        if (request == null)
        {
            throw new UnsupportedOperationException("Request is null. ParameterTool must be initialized first!");
        }
        return request;
    }

    /**
     * Overrides ValueParser.getValue(String key) to retrieve the
     * value from the ServletRequest instead of an arbitrary Map.
     *
     * @param key the parameter's key
     * @return parameter matching the specified key or
     *         <code>null</code> if there is no matching
     *         parameter
     */
    @Override
    public Object getValue(String key)
    {
        return getRequest().getParameter(key);
    }


    /**
     * Overrides ValueParser.getValues(String key) to retrieve
     * Strings from the ServletRequest instead of an arbitrary Map.
     *
     * @param key the key for the desired parameter
     * @return an array of String objects containing all of the values
     *         the given request parameter has, or <code>null</code>
     *         if the parameter does not exist
     */
    @Override
    public Object[] getValues(String key)
    {
        String[] strings = getRequest().getParameterValues(key);
        if (strings == null || strings.length == 0)
        {
            return null;
        }
        else if (strings.length == 1)
        {

            return parseStringList(strings[0]);
        }
        return strings;
    }

    /**
     * Overrides ValueParser.setSource(Map source) to throw an
     * UnsupportedOperationException, because this class uses
     * a servlet request as its source, not a Map.
     */
    @Override
    protected void setSource(Map source)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Overrides ValueParser.getSource() to return the result
     * of getRequest().getParameterMap() and expand singleton
     * arrays within it first.
     */
    @Override
    protected Map getSource()
    {
        Map source = super.getSource(false);
        if (source == null)
        {
            source = expandSingletonArrays(getRequest().getParameterMap());
            super.setSource(source);
        }
        return source;
    }

    /**
     * @return the map of all parameters available for the current request.
     */
    public Map getAll()
    {
        return getSource();
    }

    private boolean isSingletonArray(Object value)
    {
        return (value != null &&
                value.getClass().isArray() &&
                Array.getLength(value) == 1);
    }

    private Map<String,Object> expandSingletonArrays(Map<String,String[]> original)
    {
        Map<String,Object> expanded = new HashMap<String,Object>(original);
        for (Map.Entry<String,Object> entry : expanded.entrySet())
        {
            Object value = entry.getValue();
            if (isSingletonArray(value))
            {
                entry.setValue(Array.get(value, 0));
            }
        }
        return expanded;
    }

}

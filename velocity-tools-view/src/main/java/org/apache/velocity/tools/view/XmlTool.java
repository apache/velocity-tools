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

import org.apache.velocity.tools.XmlUtils;
import org.apache.velocity.tools.generic.ValueParser;

import jakarta.servlet.ServletRequest;

/**
 * View version of {@link org.apache.velocity.tools.generic.XmlTool}. It adds an automatic parsing of the HTTP query
 * body content, if it is found to be of JSON type.
 *
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Id:$
 */

public class XmlTool extends org.apache.velocity.tools.generic.XmlTool
{
    private static final long serialVersionUID = 8975478801777360304L;

    /**
     * ImportSupport initialization.
     * @param config configuration values
     */
    @Override
    protected synchronized void initializeImportSupport(ValueParser config)
    {
        if (importSupport == null)
        {
            importSupport = new ViewImportSupport();
            importSupport.configure(config);
        }
    }

    /**
     * Configuration. Parses request body if appropriate.
     * @param values configuration values
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);
        if (isEmpty())
        {
            ServletRequest request = (ServletRequest)values.get(ViewContext.REQUEST);
            if (request.getContentLength() > 0)
            {
                String mimeType = request.getContentType();
                if (XmlUtils.isXmlMimeType(mimeType))
                {
                    try
                    {
                        setRoot(XmlUtils.parse(request.getReader()));
                    }
                    catch (Exception e)
                    {
                        getLog().error("could not parse given XML string", e);
                    }
                }
            }
        }
    }
}

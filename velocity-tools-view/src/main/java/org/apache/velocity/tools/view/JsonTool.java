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

import jakarta.servlet.ServletRequest;

import org.apache.velocity.tools.generic.ValueParser;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * View version of {@link org.apache.velocity.tools.generic.JsonTool}. It adds an automatic parsing of the HTTP query
 * body content, if it is found to be of JSON type.
 *
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Id:$
 */

@DefaultKey("json")
@ValidScope(Scope.REQUEST)
public class JsonTool extends org.apache.velocity.tools.generic.JsonTool
{

    private static final long serialVersionUID = -4305396131049004070L;

    /**
     * Importsupport initialization
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
     * Check if a given mime type is of JSON type
     * @param mimeType mime type
     * @return whether given mime type is of JSON type
     */
    protected static boolean isJsonMimeType(String mimeType)
    {
        return mimeType != null &&
            (
                "text/json".equals(mimeType) ||
                "application/json".equals(mimeType) ||
                mimeType.endsWith("+json")
            );
    }

    /**
     * Check if a given content type is of JSON type
     * @param contentType content type
     * @return whether given content type is of JSON type
     */
    protected static boolean isJsonContentType(String contentType)
    {
        if (contentType != null)
        {
            int sep = contentType.indexOf(';');
            String mimeType = sep == -1
                ? contentType
                : contentType.substring(0, sep).trim();
            return isJsonMimeType(mimeType);
        }
        return false;
    }

    /**
     * Configuration. Parses request body if appropriate.
     * @param parser configuration values
     */
    protected void configure(ValueParser parser)
    {
        super.configure(parser);
        if (root() == null)
        {
            ServletRequest request = (ServletRequest)parser.get(ViewContext.REQUEST);
            if (request.getContentLength() > 0 && isJsonContentType(request.getContentType()))
            {
                try
                {
                    initJSON(request.getReader());
                }
                catch (Exception e)
                {
                    getLog().error("could not parse JSON object", e);
                }
            }
        }
    }
}

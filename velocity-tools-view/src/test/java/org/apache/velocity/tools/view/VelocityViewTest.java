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

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Test;

/**
 * Tests {@link VelocityView}.
 *
 */
public class VelocityViewTest
{

    /**
     * Test method for {@link org.apache.velocity.tools.view.VelocityView#getTemplate(javax.servlet.http.HttpServletRequest)}.
     * Tests VELTOOLS-119
     * @throws IOException If something goes wrong.
     * @throws MethodInvocationException If something goes wrong.
     * @throws ParseErrorException If something goes wrong.
     * @throws ResourceNotFoundException If something goes wrong.
     */
    @Test
    public void testGetTemplateHttpServletRequestHttpServletResponse() throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException
    {
        JeeConfig config = createMock(JeeConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        Context context = createMock(Context.class);

        expect(config.getServletContext()).andReturn(servletContext);
        expect(config.findInitParameter(VelocityView.DEPRECATION_SUPPORT_MODE_KEY)).andReturn("false");
        expect(config.findInitParameter(VelocityView.USER_OVERWRITE_KEY)).andReturn(null);
        expect(config.findInitParameter(VelocityView.LOAD_DEFAULTS_KEY)).andReturn("false");
        expect(servletContext.getInitParameter(VelocityView.PROPERTIES_KEY)).andReturn(null);
        expect(config.getInitParameter(VelocityView.PROPERTIES_KEY)).andReturn(null);
        expect(config.findInitParameter(VelocityView.CLEAN_CONFIGURATION_KEY)).andReturn(null);
        expect(servletContext.getInitParameter(VelocityView.TOOLS_KEY)).andReturn(null);
        expect(config.getInitParameter(VelocityView.TOOLS_KEY)).andReturn(null);
        expect(servletContext.getAttribute(ServletUtils.CONFIGURATION_KEY)).andReturn(null);
        expect(servletContext.getResourceAsStream(VelocityView.USER_TOOLS_PATH))
            .andReturn(getClass().getResourceAsStream("/org/apache/velocity/tools/view/tools.xml"));

        expect(request.getAttribute("javax.servlet.include.servlet_path")).andReturn("/charset-test.vm");
        expect(request.getAttribute("javax.servlet.include.path_info")).andReturn(null);

        // This was necessary to verify the bug, now it is not called at all.
        // expect(response.getCharacterEncoding()).andReturn("ISO-8859-1");

        replay(config, servletContext, request, response, context);
        VelocityView view = new VelocityView(config);
        Template template = view.getTemplate(request);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        writer.close();
        assertTrue(writer.toString().startsWith("Questo è il momento della verità"));

        verify(config, servletContext, request, response, context);
    }

}

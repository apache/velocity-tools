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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.easymock.IAnswer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link VelocityView}.
 *
 */
public class VelocityViewTest extends BaseWebappMockTest
{

    static Logger logger = LoggerFactory.getLogger(VelocityViewTest.class);

    /**
     * Test method for {@link org.apache.velocity.tools.view.VelocityView#getTemplate(jakarta.servlet.http.HttpServletRequest)}.
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

        expect(config.getServletContext()).andAnswer(eval(servletContext));
        expect(config.findInitParameter(VelocityView.USER_OVERWRITE_KEY)).andAnswer(eval(null));
        expect(config.findInitParameter(VelocityView.LOAD_DEFAULTS_KEY)).andAnswer(eval("false"));
        expect(servletContext.getInitParameter(VelocityView.PROPERTIES_KEY)).andAnswer(eval(null));
        expect(servletContext.getResourceAsStream(VelocityView.USER_PROPERTIES_PATH))
            .andAnswer(eval(getClass().getResourceAsStream("/WEB-INF/velocity.properties")));
        String root = new File(getClass().getResource("/").getFile()).getAbsolutePath();
        expect(config.getInitParameter(VelocityView.PROPERTIES_KEY)).andAnswer(eval(null));
        expect(config.findInitParameter(VelocityView.CLEAN_CONFIGURATION_KEY)).andAnswer(eval(null));
        expect(servletContext.getInitParameter(VelocityView.TOOLS_KEY)).andAnswer(eval(null));
        expect(config.getInitParameter(VelocityView.TOOLS_KEY)).andAnswer(eval(null));
        expect(servletContext.getAttribute(ServletUtils.CONFIGURATION_KEY)).andAnswer(eval((String)null));
        expect(servletContext.getResource(VelocityView.USER_TOOLS_PATH)).andAnswer(eval(null));
        expect(request.getAttribute("jakarta.servlet.include.servlet_path")).andAnswer(eval("/charset-test.vm"));
        expect(request.getAttribute("jakarta.servlet.include.path_info")).andAnswer(eval((String)null));

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

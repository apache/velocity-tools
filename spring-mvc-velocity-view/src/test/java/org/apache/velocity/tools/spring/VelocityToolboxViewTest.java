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
package org.apache.velocity.tools.spring;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Renders a template through {@link VelocityToolboxView} in a mock Spring web context,
 * asserting that both the Spring model and the Velocity Tools toolbox reach the template.
 */
public class VelocityToolboxViewTest
{
    private MockServletContext servletContext;
    private GenericWebApplicationContext wac;

    private void configure(boolean createSession)
    {
        servletContext = new MockServletContext();

        // Inject a ready engine (classpath loader) so the test exercises the view + toolbox,
        // not the engine-side factory (already covered by spring-velocity-support).
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "class");
        engine.setProperty("resource.loader.class.class", ClasspathResourceLoader.class.getName());
        engine.init();

        wac = new GenericWebApplicationContext(servletContext);
        wac.registerBean("velocityConfigurer", VelocityConfigurer.class, () -> {
            VelocityConfigurer configurer = new VelocityConfigurer();
            configurer.setVelocityEngine(engine);
            configurer.setCreateSession(createSession);
            return configurer;
        });
        wac.refresh();
        // What DispatcherServlet would do: make the context discoverable to RequestContext.
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
    }

    @Test
    public void rendersModelAndToolboxWithoutCreatingSession() throws Exception
    {
        configure(false);
        VelocityToolboxView view = new VelocityToolboxView();
        view.setApplicationContext(wac);
        view.setUrl("hello.vm");
        view.setContentType("text/html;charset=UTF-8");

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, Object> model = new HashMap<>();
        model.put("name", "World");
        view.render(model, request, response);

        String out = response.getContentAsString();
        assertTrue("Spring model var should resolve, got: " + out, out.contains("Hello World!"));
        assertTrue("toolbox $esc should resolve, got: " + out, out.contains("&lt;x&gt;"));
        assertNull("no HttpSession should be created with createSession=false (default)",
                request.getSession(false));
    }

    @Test
    public void createSessionTrueAllowsSessionScopedTools() throws Exception
    {
        configure(true);
        VelocityToolboxView view = new VelocityToolboxView();
        view.setApplicationContext(wac);
        view.setUrl("hello.vm");
        view.setContentType("text/html;charset=UTF-8");

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(new HashMap<>(), request, response);

        HttpSession session = request.getSession(false);
        assertNotNull("an HttpSession should be created for session-scoped tools when createSession=true",
                session);
    }
}

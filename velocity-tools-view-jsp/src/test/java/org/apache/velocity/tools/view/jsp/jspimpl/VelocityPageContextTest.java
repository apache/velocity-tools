package org.apache.velocity.tools.view.jsp.jspimpl;

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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ViewContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link VelocityPageContext}.
 *
 */
public class VelocityPageContextTest
{

    private VelocityPageContext pageContext;

    private Context velocityContext;

    private Writer velocityWriter;

    private ViewContext viewContext;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ServletContext servletContext;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        velocityContext = createMock(Context.class);
        velocityWriter = createMock(Writer.class);
        viewContext = createMock(ViewContext.class);
        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
        servletContext = createMock(ServletContext.class);

        expect(viewContext.getRequest()).andReturn(request);
        expect(viewContext.getResponse()).andReturn(response);
        expect(viewContext.getServletContext()).andReturn(servletContext).anyTimes();
        expect(velocityContext.put(eq("out"), isA(JspWriterImpl.class))).andReturn(null);

        replay(viewContext, velocityContext);
        pageContext = new VelocityPageContext(velocityContext, velocityWriter, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getSession()}.
     */
    @Test
    public void testGetSession()
    {
        HttpSession session = createMock(HttpSession.class);

        expect(request.getSession(false)).andReturn(session);

        replay(velocityWriter, request, response, servletContext, session);
        assertSame(session, pageContext.getSession());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getPage()}.
     */
    @Test
    public void testGetPage()
    {
        replay(velocityWriter, request, response, servletContext);
        assertSame(viewContext, pageContext.getPage());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getRequest()}.
     */
    @Test
    public void testGetRequest()
    {
        replay(velocityWriter, request, response, servletContext);
        assertSame(request, pageContext.getRequest());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getResponse()}.
     */
    @Test
    public void testGetResponse()
    {
        replay(velocityWriter, request, response, servletContext);
        assertSame(response, pageContext.getResponse());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getException()}.
     */
    @Test
    public void testGetException()
    {
        replay(velocityWriter, request, response, servletContext);
        assertNull(pageContext.getException());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getServletConfig()}.
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testGetServletConfig()
    {
        replay(velocityWriter, request, response, servletContext);
        try {
            pageContext.getServletConfig();
        } finally {
            verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
        }
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getServletContext()}.
     */
    @Test
    public void testGetServletContext()
    {
        replay(velocityWriter, request, response, servletContext);
        assertSame(servletContext, pageContext.getServletContext());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#forward(java.lang.String)}.
     * @throws IOException If something goes wrong.
     * @throws ServletException If something goes wrong.
     */
    @Test
    public void testForwardString() throws ServletException, IOException
    {
        RequestDispatcher dispatcher = createMock(RequestDispatcher.class);

        expect(request.getRequestDispatcher("/my/path")).andReturn(dispatcher);
        dispatcher.forward(eq(request), isA(ExternalWriterHttpServletResponse.class));

        replay(velocityWriter, request, response, servletContext, dispatcher);
        pageContext.forward("/my/path");
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, dispatcher);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#include(java.lang.String)}.
     * @throws IOException If something goes wrong.
     * @throws ServletException If something goes wrong.
     */
    @Test
    public void testIncludeString() throws ServletException, IOException
    {
        RequestDispatcher dispatcher = createMock(RequestDispatcher.class);

        velocityWriter.flush();
        expect(request.getRequestDispatcher("/my/path")).andReturn(dispatcher);
        dispatcher.include(eq(request), isA(ExternalWriterHttpServletResponse.class));

        replay(velocityWriter, request, response, servletContext, dispatcher);
        pageContext.include("/my/path");
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, dispatcher);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#include(java.lang.String, boolean)}.
     * @throws IOException If something goes wrong.
     * @throws ServletException If something goes wrong.
     */
    @Test
    public void testIncludeStringBoolean() throws ServletException, IOException
    {
        RequestDispatcher dispatcher = createMock(RequestDispatcher.class);

        expect(request.getRequestDispatcher("/my/path")).andReturn(dispatcher);
        dispatcher.include(eq(request), isA(ExternalWriterHttpServletResponse.class));

        replay(velocityWriter, request, response, servletContext, dispatcher);
        pageContext.include("/my/path", false);
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, dispatcher);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#setAttribute(java.lang.String, java.lang.Object)}.
     */
    @Test
    public void testSetAttributeStringObject()
    {
        verify(velocityContext);
        reset(velocityContext);

        expect(velocityContext.put("name", "value")).andReturn(null);

        replay(velocityWriter, request, response, servletContext, velocityContext);
        pageContext.setAttribute("name", "value");
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#setAttribute(java.lang.String, java.lang.Object, int)}.
     */
    @Test
    public void testSetAttributeStringObjectInt()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.put("name", "value")).andReturn(null);
        request.setAttribute("name", "value");
        expect(request.getSession()).andReturn(session);
        session.setAttribute("name", "value");
        servletContext.setAttribute("name", "value");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        pageContext.setAttribute("name", "value", PageContext.PAGE_SCOPE);
        pageContext.setAttribute("name", "value", PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("name", "value", PageContext.SESSION_SCOPE);
        pageContext.setAttribute("name", "value", PageContext.APPLICATION_SCOPE);
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getAttribute(java.lang.String)}.
     */
    @Test
    public void testGetAttributeString()
    {
        verify(velocityContext);
        reset(velocityContext);

        expect(velocityContext.get("name")).andReturn("value");

        replay(velocityWriter, request, response, servletContext, velocityContext);
        assertEquals("value", pageContext.getAttribute("name"));
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getAttribute(java.lang.String, int)}.
     */
    @Test
    public void testGetAttributeStringInt()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.get("name")).andReturn("value");
        expect(request.getAttribute("name")).andReturn("value");
        expect(request.getSession(false)).andReturn(session);
        expect(session.getAttribute("name")).andReturn("value");
        expect(servletContext.getAttribute("name")).andReturn("value");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        assertEquals("value", pageContext.getAttribute("name", PageContext.PAGE_SCOPE));
        assertEquals("value", pageContext.getAttribute("name", PageContext.REQUEST_SCOPE));
        assertEquals("value", pageContext.getAttribute("name", PageContext.SESSION_SCOPE));
        assertEquals("value", pageContext.getAttribute("name", PageContext.APPLICATION_SCOPE));
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#findAttribute(java.lang.String)}.
     */
    @Test
    public void testFindAttributeString()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.get("name")).andReturn(null);
        expect(request.getAttribute("name")).andReturn(null);
        expect(request.getSession(false)).andReturn(session);
        expect(session.getAttribute("name")).andReturn(null);
        expect(servletContext.getAttribute("name")).andReturn("value");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        assertEquals("value", pageContext.findAttribute("name"));
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#removeAttribute(java.lang.String)}.
     */
    @Test
    public void testRemoveAttributeString()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.remove("name")).andReturn(null);
        request.removeAttribute("name");
        expect(request.getSession(false)).andReturn(session);
        session.removeAttribute("name");
        servletContext.removeAttribute("name");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        pageContext.removeAttribute("name");
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#removeAttribute(java.lang.String, int)}.
     */
    @Test
    public void testRemoveAttributeStringInt()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.remove("name")).andReturn(null);
        request.removeAttribute("name");
        expect(request.getSession(false)).andReturn(session);
        session.removeAttribute("name");
        servletContext.removeAttribute("name");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        pageContext.removeAttribute("name", PageContext.PAGE_SCOPE);
        pageContext.removeAttribute("name", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("name", PageContext.SESSION_SCOPE);
        pageContext.removeAttribute("name", PageContext.APPLICATION_SCOPE);
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getAttributesScope(java.lang.String)}.
     */
    @Test
    public void testGetAttributesScopeString()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);

        expect(velocityContext.get("name")).andReturn(null);
        expect(request.getAttribute("name")).andReturn(null);
        expect(request.getSession(false)).andReturn(session);
        expect(session.getAttribute("name")).andReturn(null);
        expect(servletContext.getAttribute("name")).andReturn("value");

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        assertEquals(PageContext.APPLICATION_SCOPE, pageContext.getAttributesScope("name"));
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getAttributeNamesInScope(int)}.
     */
    @Test
    public void testGetAttributeNamesInScopeInt()
    {
        verify(velocityContext);
        reset(velocityContext);
        HttpSession session = createMock(HttpSession.class);
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = createMock(Enumeration.class);

        expect(velocityContext.getKeys()).andReturn(new String[] {"one", "two", "three"});
        expect(request.getAttributeNames()).andReturn(keys);
        expect(request.getSession(false)).andReturn(session);
        expect(session.getAttributeNames()).andReturn(keys);
        expect(servletContext.getAttributeNames()).andReturn(keys);

        replay(velocityWriter, request, response, servletContext, velocityContext, session);
        assertNotNull(pageContext.getAttributeNamesInScope(PageContext.PAGE_SCOPE));
        assertSame(keys, pageContext.getAttributeNamesInScope(PageContext.REQUEST_SCOPE));
        assertSame(keys, pageContext.getAttributeNamesInScope(PageContext.SESSION_SCOPE));
        assertSame(keys, pageContext.getAttributeNamesInScope(PageContext.APPLICATION_SCOPE));
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext, session);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getOut()}.
     */
    @Test
    public void testGetOut()
    {
        replay(velocityWriter, request, response, servletContext);
        assertNotNull(pageContext.getOut());
        verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
    }

    /**
     * Test method for {@link org.apache.velocity.tools.view.jsp.jspimpl.VelocityPageContext#getELContext()}.
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testGetELContext()
    {
        replay(velocityWriter, request, response, servletContext);
        try {
            pageContext.getELContext();
        } finally {
            verify(velocityContext, velocityWriter, request, response, servletContext, viewContext);
        }
    }

}

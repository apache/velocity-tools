/*
 * $Id: ExternalWriterHttpServletResponseTest.java 892369 2009-12-18 20:21:36Z apetrelli $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.velocity.tools.view.jsp.jspimpl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * Tests {@link ExternalWriterHttpServletResponse}.
 */
public class ExternalWriterHttpServletResponseTest {

    /**
     * Test method for {@link org.apache.tiles.request.servlet.ExternalWriterHttpServletResponse#getWriter()}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testGetWriter() {
        HttpServletResponse wrappedResponse = createMock(HttpServletResponse.class);
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        replay(wrappedResponse);
        ExternalWriterHttpServletResponse response = new ExternalWriterHttpServletResponse(
                wrappedResponse, printWriter);
        assertEquals(printWriter, response.getWriter());
        verify(wrappedResponse);
    }

}

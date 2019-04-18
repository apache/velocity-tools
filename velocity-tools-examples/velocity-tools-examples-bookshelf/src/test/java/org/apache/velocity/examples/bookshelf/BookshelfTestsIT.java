package org.apache.velocity.examples.showcase;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import static org.junit.Assert.*;

/**
 * <p>View tools blackbox tests.</p>
 *
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @since Velocity Tools 1.3
 * @version $Id$
 */


public class BookshelfTestsIT
{

    private static final String ROOT_URL = "http://localhost:8080/velocity-tools-examples-bookshelf/";

    public static @BeforeClass void initBookshelfTests() throws Exception {
    }

    /******* Helpers **********/

    /**
     * Utility function to check the text content of an HTML element
     * @param resp web response
     * @param id HTML element id
     * @param text expected text
     * @throws Exception
     */
    private void checkText(WebResponse resp,String id,String text) throws Exception {
        HTMLElement element = resp.getElementWithID(id);
        assertNotNull(element);
        assertEquals(text,element.getText());
    }

    /**
     * Utility function to check the text content of an HTML element
     * @param resp web response
     * @param id HTML element id
     * @param text expected start of the text
     * @throws Exception
     */
    private void checkTextStart(WebResponse resp,String id,String text) throws Exception {
        HTMLElement element = resp.getElementWithID(id);
        assertNotNull(element);
        assertTrue(element.getText().startsWith(text));
    }

    /**
     * Utility function to check the text content of an HTML element
     * @param resp web response
     * @param id HTML element id
     * @param start expected start of the text
     * @param end expected end of the text
     * @throws Exception
     */
    private void checkTextStartEnd(WebResponse resp,String id,String start,String end) throws Exception {
        HTMLElement element = resp.getElementWithID(id);
        assertNotNull(element);
        assertTrue(element.getText().startsWith(start));
        assertTrue(element.getText().endsWith(end));
    }

    /**
     * Utility function to check the text content of an HTML element
     * @param resp web response
     * @param id HTML element id
     * @param text expected contained text
     * @throws Exception
     */
    private void checkTextContent(WebResponse resp,String id,String text) throws Exception {
        HTMLElement element = resp.getElementWithID(id);
        assertNotNull(element);
        assertTrue(element.getText().indexOf(text) != -1);
    }

    /**
     * Utility function to check the text content of an HTML element
     * @param resp web response
     * @param id HTML element id
     * @param regex expected regex
     * @throws Exception
     */
    private void checkTextRegex(WebResponse resp,String id,String regex) throws Exception {
        HTMLElement element = resp.getElementWithID(id);
        assertNotNull(element);
        Pattern pattern = Pattern.compile(regex);
        // strip new lines from string to be tested
        String text = element.getText().replace("\n","");
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches())
        {
            fail(element.getText()+" did not match "+regex);
        }
    }

    /**
     *
     * @param orig original web response
     * @param formname form name
     * @param paramname parameter name
     * @param value parameter value
     * @return new web response
     * @throws Exception
     */
    private WebResponse submitWithParam(WebResponse orig, String formname, String paramname, String value) throws Exception {
        WebForm form = orig.getFormWithName(formname);
        form.setParameter(paramname, value);
        return form.submit();
    }

    /**
     * Used for debugging testcases
     * @param resp webresponse
     */
    private void dump(WebResponse resp) {
        try {
            PrintWriter pw = new PrintWriter("/tmp/dump.html");
            pw.println(resp.getText());
            pw.flush();
            pw.close();
        } catch (IOException ioe) {

        }
    }


    /******* Tests **********/

    public @Test void testIndex() throws Exception
    {

        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"index.vhtml");
        WebResponse resp = conv.getResponse(req);

    }
}

package org.apache.velocity.tools.test.blackbox;

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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.PrintWriter;
import java.io.IOException;

import org.junit.*;

import static org.junit.Assert.*;

import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.HttpUnitOptions;


/**
 * <p>View tools blackbox tests.</p>
 *
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @since Velocity Tools 1.3
 * @version $Id$
 */


public class ViewToolsTests {

    private static final String ROOT_URL = "http://localhost:@test.webcontainer.port@/";

    public static @BeforeClass void initViewToolsTests() throws Exception {
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
        form.setParameter(paramname,value);
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

    public @Test void testBrowserSnifferTool() throws Exception {
        /* check we are identified as a Java (HttpUnit) client */
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"browser.vm");
        WebResponse resp = conv.getResponse(req);
        checkText(resp,"Java","true");

        /* check language */
        req.setHeaderField("Accept-Language","en");
        resp = conv.getResponse(req);
        checkText(resp,"preferredLanguage","en");
        req.setHeaderField("Accept-Language","en-US,en;q=0.8");
        resp = conv.getResponse(req);
        checkText(resp,"preferredLanguage","en");
    }

    public @Test void testContextTool() throws Exception {
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"context.vm");
        WebResponse resp = conv.getResponse(req);

        /* check that getThis() is a ViewToolContext instance */
        checkTextStart(resp,"getThis()","org.apache.velocity.tools.view.ViewToolContext");

        /* check contains('context') */
        resp = submitWithParam(resp,"contains_Object","contains_Object1","'context'");
        checkText(resp,"contains(java.lang.Object)","true");

        /* check get('context') */
        resp = submitWithParam(resp,"get_Object","get_Object1","'context'");
        checkTextStart(resp,"get(java.lang.Object)","org.apache.velocity.tools.view.ViewContextTool");

        /* check keys (the only expected uppercase is in 'velocityCount') */
        checkTextRegex(resp,"getKeys()","^\\[[a-z_A-Z]+(?:,\\s*[a-z_A-Z]+)*\\]$");

        /* check toolbox */
        checkTextRegex(resp,"getToolbox()","^\\{[a-z_A-Z]+=.*(?:,\\s*[a-z_A-Z]+=.*)*\\}$");

        /* check values */
        checkTextStartEnd(resp,"getValues()","[","]");
    }

    public @Test void testLinkTool() throws Exception {
        WebConversation conv = new WebConversation();
        String page = ROOT_URL+"link.vm";
        WebRequest req = new GetMethodWebRequest(page);
        WebResponse resp = conv.getResponse(req);

        /* check anchor(foo) and anchor */
        resp = submitWithParam(resp,"anchor","anchor","foo");
        checkText(resp,"anchor",page+"#foo");
        checkText(resp,"altanchor",page+"#foo");

        /* check path(bar) and path */
        resp = submitWithParam(resp,"path","path","bar");
        checkText(resp,"path","http://localhost:8081/bar");
        checkText(resp,"altpath","/link.vm");

        /* check relative(foo) */
        resp = submitWithParam(resp,"relative","relative","foo");
        checkText(resp,"relative","/foo");

        /* check absolute(bar) */
        resp = submitWithParam(resp,"absolute","absolute","bar");
        checkText(resp,"absolute",ROOT_URL + "bar");

        /* check contextURL */
        checkText(resp,"contextURL",ROOT_URL);

        /* check contextPath */
        checkText(resp,"contextPath","");

        /* check requestPath */
        checkText(resp,"requestPath","/link.vm");

        /* check baseRef */
        checkText(resp,"baseRef",page);

        /* check self */
        checkText(resp,"self",page);

        /* check encode */
        resp = submitWithParam(resp,"encode","encode",": /");
        checkText(resp,"encode","%3A+%2F");
    }

    public @Test void testParameterParserTool() throws Exception {
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"params.vm?foo=bar&b=false&n=55&d=1.2");
        WebResponse resp = conv.getResponse(req);

        /* check exists(foo) */
        resp = submitWithParam(resp,"exists","exists","foo");
        checkText(resp,"exists","true");

        /* check get(foo) */
        resp = submitWithParam(resp,"get","get","foo");
        checkText(resp,"get","bar");

        /* check getString(foo) */
        resp = submitWithParam(resp,"getString","getString","foo");
        checkText(resp,"getString","bar");

        /* check getBoolean(b) */
        resp = submitWithParam(resp,"getBoolean","getBoolean","b");
        checkText(resp,"getBoolean","false");

        /* check getNumber(n) */
        resp = submitWithParam(resp,"getNumber","getNumber","n");
        checkText(resp,"getNumber","55");

        /* check getDouble(d) */
        resp = submitWithParam(resp,"getDouble","getDouble","d");
        checkText(resp,"getDouble","1.2");

        /* check getInteger(n) */
        resp = submitWithParam(resp,"getInteger","getInteger","n");
        checkText(resp,"getInteger","55");

        /* check getStrings(foo) */
        resp = submitWithParam(resp,"getStrings","getStrings","foo");
        checkTextStart(resp,"getStrings","[Ljava.lang.String;@");

        /* check getBooleans(b) */
        resp = submitWithParam(resp,"getBooleans","getBooleans","b");
        checkTextStart(resp,"getBooleans","[Ljava.lang.Boolean;@");

        /* check getNumbers(n) */
        resp = submitWithParam(resp,"getNumbers","getNumbers","n");
        checkTextStart(resp,"getNumbers","[Ljava.lang.Number;@");

        /* check getDoubles(d) */
        resp = submitWithParam(resp,"getDoubles","getDoubles","d");
        checkTextStart(resp,"getDoubles","[D@");

        /* check getInts(n) */
        resp = submitWithParam(resp,"getInts","getInts","n");
        checkTextStart(resp,"getInts","[I@");

        /* check getString(bar,foo) */
        WebForm form = resp.getFormWithName("getString2");
        form.setParameter("getString1","'bar'");
        form.setParameter("getString2","'foo'");
        resp = form.submit();
        checkText(resp,"getString2","foo");

        /* TODO other getters with default values */

        /* check all */
        checkTextRegex(resp,"all","^\\{.*\\}$");
    }
}

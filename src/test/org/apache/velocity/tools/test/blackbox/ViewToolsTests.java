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
 * <p>Generic tools whitebox tests.</p>
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
        Matcher matcher = pattern.matcher(element.getText());
        assertTrue(matcher.matches());
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
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"browser.vm");
        WebResponse resp = conv.getResponse(req);

        /* check we are identified as a Java (HttpUnit) client */
        checkText(resp,"Java","true");
    }

    public @Test void testContextTool() throws Exception {
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"context.vm");
        WebResponse resp = conv.getResponse(req);

        /* check that getThis() is a ChainedContext instance */
        checkTextStart(resp,"this","org.apache.velocity.tools.view.context.ChainedContext");

        /* check contains('context') */
        resp = submitWithParam(resp,"contains","contains","context");
        checkText(resp,"contains","true");

        /* check get('context') */
        resp = submitWithParam(resp,"get","get","context");
        checkTextStart(resp,"get","org.apache.velocity.tools.view.tools.ContextTool");

        /* check keys (the only expected uppercase is in 'velocityCount') */
        checkTextRegex(resp,"keys","^\\[[a-z_C]+(?:,\\s*[a-z_C]+)*\\]$");

        /* check toolbox */
        checkTextRegex(resp,"toolbox","^\\{[a-z_C]+=.*(?:,\\s*[a-z_C]+=.*)*\\}$");

        /* check values */
        checkTextRegex(resp,"values","^\\[.*\\]$");
    }

    public @Test void testCookiesTool() throws Exception {
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"cookies.vm");
        WebResponse resp = conv.getResponse(req);

        /* check all */
        checkTextStart(resp,"all","[Ljavax.servlet.http.Cookie;");

        /* check get('JSESSIONID') */
        resp = submitWithParam(resp,"get","get","JSESSIONID");
        checkTextStart(resp,"get","javax.servlet.http.Cookie");

        /* check add('foo','bar') */
        WebForm form = resp.getFormWithName("add2");
        form.setParameter("add1","foo");
        form.setParameter("add2","bar");
        resp = form.submit();
        resp = submitWithParam(resp,"get","get","foo");
        checkTextStart(resp,"get","javax.servlet.http.Cookie");
    }

    public @Test void testLinkTool() throws Exception {
        WebConversation conv = new WebConversation();
        WebRequest req = new GetMethodWebRequest(ROOT_URL+"link.vm");
        WebResponse resp = conv.getResponse(req);

        /* check anchor(foo) and anchor */
        resp = submitWithParam(resp,"anchor","anchor","foo");
        checkText(resp,"anchor","#foo");
        checkText(resp,"altanchor","#foo");

        /* check uri(bar) and uri */
        resp = submitWithParam(resp,"uri","uri","bar");
        checkText(resp,"uri","bar");
        checkText(resp,"alturi","bar");

        /* check relative(foo) */
        resp = submitWithParam(resp,"relative","relative","foo");
        checkText(resp,"relative","/foo");

        /* check absolute(bar) */
        resp = submitWithParam(resp,"absolute","absolute","bar");
        checkText(resp,"absolute",ROOT_URL + "bar");

        /* check contextURL */
        checkText(resp,"contextURL",ROOT_URL.substring(0,ROOT_URL.length()-1));

        /* check contextPath */
        checkText(resp,"contextPath","");

        /* check requestPath */
        checkText(resp,"requestPath","/link.vm");

        /* check baseRef */
        checkText(resp,"baseRef",ROOT_URL+"link.vm");

        /* check self */
        checkText(resp,"self","/link.vm");

        /* check encodeURL */
        resp = submitWithParam(resp,"encodeURL","encodeURL",": /");
        checkText(resp,"encodeURL","%3A+%2F");
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

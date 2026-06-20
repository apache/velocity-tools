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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.velocity.tools.view.UAParser.UAEntity;

import jakarta.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.junit.Test;

/**
 * <p>Tests for BrowserTool</p>
 *
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Id$
 */
public class BrowserToolTests {

    private static final String TEST_OUTPUT_DIR = System.getProperty("test.output.dir");

    protected Map<String, String> readUAs(String filename) throws Exception
    {
        Map result = new TreeMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(TEST_OUTPUT_DIR + "/user-agents/" + filename));
        String line;
        while ((line = reader.readLine()) != null)
        {
            if (line.length() == 0) continue;
            int hash = line.indexOf("#");
            if (hash == -1) continue;
            result.put(line.substring(hash + 2), line.substring(0, hash - 1));
        }
        return result;
    }

    protected void checkBrowsers(BrowserTool tool, Map<String, String> uas)
    {
        int num = 0;
        for (Map.Entry<String, String> entry : uas.entrySet())
        {
            ++num;
            String expected = entry.getValue();
            String ua = entry.getKey();
            tool.setUserAgentString(ua);
            UAEntity browser = tool.getBrowser();
            StringBuilder builder = new StringBuilder();

            assertNotNull("entry " + num + "/" + uas.size()+": browser was not detected: browser={"+expected+"}, ua={"+ua+"}", browser);
            builder.append(browser.getName());
            if (browser.getMajorVersion() != -1)
            {
                builder.append(' ');
                builder.append(browser.getMajorVersion());
                if (browser.getMinorVersion() != -1)
                {
                    builder.append('.');
                    builder.append(browser.getMinorVersion());
                }
            }
            String found = builder.toString();
            assertTrue("entry " + num + "/" + uas.size() + ": wrong browser detected: browser={" + expected + "}, found={" + found + "}, ua={" + ua + "}", expected.toLowerCase().startsWith(found.toLowerCase()));
        }
    }

    protected void checkDevices(BrowserTool tool, Map<String, String> uas)
    {
        int num = 0;
        for (Map.Entry<String, String> entry : uas.entrySet())
        {
            ++num;
            String device = entry.getValue();
            String ua = entry.getKey();
            tool.setUserAgentString(ua);
            String found = tool.getDevice();
            assertTrue("entry " + num + "/" + uas.size() + ": wrong device detected: device={" + device + "}, found={" + found + "}, ua={" + ua + "}", device.equals(found));
        }
    }

    protected void checkOperatingSystems(BrowserTool tool, Map<String, String> uas)
    {
        int num = 0;
        for (Map.Entry<String, String> entry : uas.entrySet())
        {
            ++num;
            String expected = entry.getValue();
            String ua = entry.getKey();
            tool.setUserAgentString(ua);
            UAEntity os = tool.getOperatingSystem();
            assertNotNull("entry " + num + "/" + uas.size()+": operating system not detected: os={"+expected+"}, ua={" + ua + "}", os);
            assertEquals("entry " + num + "/" + uas.size()+": operating system not correctly detected: os={" + expected + "}, found={" + os.getName() + "}, ua={" + ua + "}", expected.toLowerCase(), os.getName().toLowerCase());
        }
    }

    public @Test void ctorBrowserTool() throws Exception
    {
        try
        {
            new BrowserTool();
        }
        catch (Exception e)
        {
            fail("Constructor 'BrowserTool()' failed due to: " + e);
        }
    }

    public @Test void testBrowserParsing() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        Map uas = readUAs("browsers.txt");
        checkBrowsers(tool, uas);
    }

    public @Test void testDeviceParsing() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        Map uas = readUAs("devices.txt");
        checkDevices(tool, uas);
    }

    public @Test void testOperatingSystemParsing() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        Map uas = readUAs("operating_systems.txt");
        checkOperatingSystems(tool, uas);
    }

    /* the is<Device>() booleans must agree with getDevice(), and a missing
       User-Agent header must not throw */
    public @Test void deviceBooleansAgreeWithDeviceString() throws Exception
    {
        BrowserTool tool = new BrowserTool();

        tool.setUserAgentString(null);
        assertFalse(tool.isMobile());
        assertFalse(tool.isTablet());
        assertFalse(tool.isDesktop());
        assertFalse(tool.isTV());

        tool.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        assertEquals("mobile", tool.getDevice());
        assertTrue(tool.isMobile());

        tool.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        assertEquals("desktop", tool.getDevice());
        assertTrue(tool.isDesktop());
    }

    private static HttpServletRequest requestWithHeaders(String... headerPairs)
    {
        HttpServletRequest req = createNiceMock(HttpServletRequest.class);
        for (int i = 0; i < headerPairs.length; i += 2)
        {
            expect(req.getHeader(headerPairs[i])).andStubReturn(headerPairs[i + 1]);
        }
        replay(req);
        return req;
    }

    /* a Firefox UA string but Chrome client hints: the hints win (alpha) */
    public @Test void clientHintsOverrideUserAgentString() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        tool.setRequest(requestWithHeaders(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Sec-CH-UA", "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not?A_Brand\";v=\"24\"",
            "Sec-CH-UA-Mobile", "?0",
            "Sec-CH-UA-Platform", "\"Windows\""));
        assertEquals("Chrome", tool.getBrowser().getName());
        assertEquals(120, tool.getBrowser().getMajorVersion());
        assertEquals("Windows", tool.getOperatingSystem().getName());
        assertTrue(tool.isChrome());
        assertTrue(tool.isDesktop());
        assertFalse(tool.isMobile());
    }

    public @Test void clientHintsMobileAndroid() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        tool.setRequest(requestWithHeaders(
            "Sec-CH-UA", "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not?A_Brand\";v=\"24\"",
            "Sec-CH-UA-Mobile", "?1",
            "Sec-CH-UA-Platform", "\"Android\""));
        assertEquals("mobile", tool.getDevice());
        assertTrue(tool.isMobile());
        assertEquals("Android", tool.getOperatingSystem().getName());
    }

    /* full version list is preferred (gives minor too); macOS is canonical, isOSX() aliases it */
    public @Test void clientHintsMacosAndFullVersionList() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        tool.setRequest(requestWithHeaders(
            "Sec-CH-UA", "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not?A_Brand\";v=\"24\"",
            "Sec-CH-UA-Full-Version-List", "\"Chromium\";v=\"120.0.6099.71\", \"Google Chrome\";v=\"120.0.6099.71\", \"Not?A_Brand\";v=\"24.0.0.0\"",
            "Sec-CH-UA-Mobile", "?0",
            "Sec-CH-UA-Platform", "\"macOS\"",
            "Sec-CH-UA-Platform-Version", "\"14.5.0\""));
        assertEquals("Chrome", tool.getBrowser().getName());
        assertEquals(120, tool.getBrowser().getMajorVersion());
        assertEquals(0, tool.getBrowser().getMinorVersion());
        assertEquals("macOS", tool.getOperatingSystem().getName());
        assertTrue(tool.isOSX());
    }

    /* an Android device reporting "not mobile" is a tablet */
    public @Test void clientHintsAndroidTablet() throws Exception
    {
        BrowserTool tool = new BrowserTool();
        tool.setRequest(requestWithHeaders(
            "Sec-CH-UA", "\"Chromium\";v=\"120\", \"Not?A_Brand\";v=\"24\"",
            "Sec-CH-UA-Mobile", "?0",
            "Sec-CH-UA-Platform", "\"Android\""));
        assertEquals("tablet", tool.getDevice());
        assertTrue(tool.isTablet());
    }

}

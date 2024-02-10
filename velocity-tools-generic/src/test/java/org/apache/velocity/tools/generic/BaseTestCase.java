package org.apache.velocity.tools.generic;

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

import java.io.StringWriter;
import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

/**
 * Base test case that provides utility methods for other tests.
 * (Adapted from Velocity Engine's BaseTestCase)
 *
 * @author Nathan Bubna
 * @version $Id: BaseTestCase.java 898032 2010-01-11 19:51:03Z nbubna $
 */
public abstract class BaseTestCase extends TestCase
{
    protected VelocityEngine engine;
    protected VelocityContext context;
    protected boolean DEBUG = false;
    protected MockLogger log;
    protected String stringRepoName = "string.repo";

    public BaseTestCase(String name)
    {
        super(name);

        // if we're just running one case, then have DEBUG
        // automatically set to true
        String testcase = System.getProperty("testcase");
        if (testcase != null)
        {
            DEBUG = testcase.equals(getClass().getName());
        }
    }

    protected void setUp() throws Exception
    {
        engine = new VelocityEngine();

        //by default, make the engine's log output go to the test-report
        log = new MockLogger(false, false);
        log.setEnabledLevel(MockLogger.LOG_LEVEL_INFO);
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);

        // use string resource loader by default, instead of file
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file,string");
        engine.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        engine.addProperty("resource.loader.string.repository.name", stringRepoName);
        engine.addProperty("resource.loader.string.repository.static", "false");

        setUpEngine(engine);

        context = new VelocityContext();
        setUpContext(context);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        // extension hook
    }

    protected void setUpContext(VelocityContext context)
    {
        // extension hook
    }

    protected StringResourceRepository getStringRepository()
    {
        StringResourceRepository repo =
            (StringResourceRepository)engine.getApplicationAttribute(stringRepoName);
        if (repo == null)
        {
            try
            {
                engine.init();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            repo =
                (StringResourceRepository)engine.getApplicationAttribute(stringRepoName);
        }
        return repo;
    }

    protected void addTemplate(String name, String template)
    {
        info("Template '"+name+"':  "+template);
        getStringRepository().putStringResource(name, template);
    }

    protected void removeTemplate(String name)
    {
        info("Removed: '"+name+"'");
        getStringRepository().removeStringResource(name);
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    protected void info(String msg)
    {
        if (DEBUG)
        {
            if (engine == null)
            {
                Velocity.getLog().info(msg);
            }
            else
            {
                engine.getLog().info(msg);
            }
        }
    }

    protected void info(String msg, Throwable t)
    {
        if (DEBUG)
        {
            if (engine == null)
            {
                Velocity.getLog().info(msg);
            }
            else
            {
                engine.getLog().info(msg, t);
            }
        }
    }

    public void testBase()
    {
        if (DEBUG && engine != null)
        {
            assertSchmoo("");
            assertSchmoo("abc\n123");
        }
    }

    /**
     * Compare an expected string with the given loaded template
     */
    protected void assertTmplEquals(String expected, String template)
    {
        info("Expected:  " + expected + " from '" + template + "'");

        StringWriter writer = new StringWriter();
        try
        {
            engine.mergeTemplate(template, "utf-8", context, writer);
        }
        catch (RuntimeException re)
        {
            info("RuntimeException!", re);
            throw re;
        }
        catch (Exception e)
        {
            info("Exception!", e);
            throw new RuntimeException(e);
        }

        info("Result:  " + writer.toString());
        assertEquals(expected, writer.toString());
    }

    /**
     * Ensure that a context value is as expected.
     */
    protected void assertContextValue(String key, Object expected)
    {
        info("Expected value of '"+key+"': "+expected);
        Object value = context.get(key);
        info("Result: "+value);
        assertEquals(expected, value);
    }

    /**
     * Ensure that a template renders as expected.
     */
    protected void assertEvalEquals(String expected, String template)
    {
        info("Expectation: "+expected);
        assertEquals(expected, evaluate(template));
    }

    /**
     * Ensure that the given string renders as itself when evaluated.
     */
    protected void assertSchmoo(String templateIsExpected)
    {
        assertEvalEquals(templateIsExpected, templateIsExpected);
    }

    /**
     * Ensure that an exception occurs when the string is evaluated.
     */
    protected Exception assertEvalException(String evil)
    {
        return assertEvalException(evil, null);
    }

    /**
     * Ensure that a specified type of exception occurs when evaluating the string.
     */
    protected Exception assertEvalException(String evil, Class exceptionType)
    {
        try
        {
            if (!DEBUG)
            {
                log.off();
            }
            if (exceptionType != null)
            {
                info("Expectation: "+exceptionType.getName());
            }
            evaluate(evil);
            fail("Template '"+evil+"' should have thrown an exception.");
        }
        catch (Exception e)
        {
            if (exceptionType != null && !exceptionType.isAssignableFrom(e.getClass()))
            {
                fail("Was expecting template '"+evil+"' to throw "+exceptionType+" not "+e);
            }
            return e;
        }
        finally
        {
            if (!DEBUG)
            {
                log.on();
            }
        }
        return null;
    }

    /**
     * Ensure that the error message of the expected exception has the proper location info.
     */
    protected Exception assertEvalExceptionAt(String evil, String template,
                                              int line, int col)
    {
        String loc = template+"[line "+line+", column "+col+"]";
        info("Expectation: Exception at "+loc);
        Exception e = assertEvalException(evil);

        info("Result: "+e.getClass().getName()+" - "+e.getMessage());
        if (e.getMessage().indexOf(loc) < 1)
        {
            fail("Was expecting exception at "+loc+" instead of "+e.getMessage());
        }
        return e;
    }

    /**
     * Only ensure that the error message of the expected exception
     * has the proper line and column info.
     */
    protected Exception assertEvalExceptionAt(String evil, int line, int col)
    {
         return assertEvalExceptionAt(evil, "", line, col);
    }

    /**
     * Evaluate the specified String as a template and return the result as a String.
     */
    protected String evaluate(String template)
    {
        StringWriter writer = new StringWriter();
        try
        {
            info("Template: "+template);

            // use template as its own name, since our templates are short
            // unless it's not that short, then shorten it...
            String name = (template.length() <= 15) ? template : template.substring(0,15);
            engine.evaluate(context, writer, name, template);

            String result = writer.toString();
            info("Result: "+result);
            return result;
        }
        catch (RuntimeException re)
        {
            info("RuntimeException!", re);
            throw re;
        }
        catch (Exception e)
        {
            info("Exception!", e);
            throw new RuntimeException(e);
        }
    }

}

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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.servlet.ServletContext;

import org.junit.Test;

/**
 * Tests {@link ServletUtils} shared-{@link VelocityView} initialization error
 * handling: an unrecoverable failure must be cached so dependents don't each
 * retry it (VELTOOLS-186).
 */
public class ServletUtilsTest extends BaseWebappMockTest
{
    /** A cached failure marker must be re-thrown fast, never retried, cause preserved. */
    @Test
    public void invalidMarkerFailsFastWithoutRetry()
    {
        ServletContext app = createMock(ServletContext.class);
        RuntimeException boom = new RuntimeException("init boom");
        expect(app.getAttribute(ServletUtils.VELOCITY_VIEW_KEY)).andStubAnswer(eval(boom));
        replay(app);

        try
        {
            ServletUtils.getVelocityView(app, false);
            fail("expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            assertSame(boom, e.getCause());
        }
        // createIfMissing=true must still fail fast, not attempt creation
        try
        {
            ServletUtils.getVelocityView(app, true);
            fail("expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            assertSame(boom, e.getCause());
        }
        verify(app);
    }

    /** A failing creation is cached; the next caller fails fast rather than re-creating. */
    @Test
    public void failedInitIsCachedAndNotRetried()
    {
        final Object[] stored = new Object[1];
        JeeConfig config = createMock(JeeConfig.class);
        ServletContext app = createMock(ServletContext.class);

        expect(config.findInitParameter(ServletUtils.SHARED_CONFIG_PARAM)).andStubAnswer(eval((String)null));
        expect(config.getServletContext()).andStubAnswer(eval(app));
        // an unknown class makes createView() fail with an IllegalArgumentException
        expect(config.findInitParameter(ServletUtils.ALT_VELOCITY_VIEW_KEY)).andStubAnswer(eval("no.such.VelocityView"));
        expect(app.getAttribute(ServletUtils.VELOCITY_VIEW_KEY)).andStubAnswer(() -> stored[0]);
        app.setAttribute(eq(ServletUtils.VELOCITY_VIEW_KEY), anyObject());
        expectLastCall().andStubAnswer(() -> { stored[0] = getCurrentArguments()[1]; return null; });
        replay(config, app);

        // first call: creation fails -> original exception, and the failure is cached
        try
        {
            ServletUtils.getVelocityView(config);
            fail("expected creation to fail");
        }
        catch (IllegalArgumentException expected) {}
        assertTrue("failure should be cached as a marker", stored[0] instanceof Throwable);

        // second call: must take the marker fast-fail path (IllegalStateException),
        // NOT re-run creation (which would throw IllegalArgumentException again)
        try
        {
            ServletUtils.getVelocityView(config);
            fail("expected fast-fail");
        }
        catch (IllegalStateException fastFail)
        {
            assertNotNull(fastFail.getCause());
        }
        verify(config, app);
    }
}

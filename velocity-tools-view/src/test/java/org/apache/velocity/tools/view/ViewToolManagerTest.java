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
package org.apache.velocity.tools.view;

import jakarta.servlet.ServletContext;

import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;

/**
 * Regression for the auto-configuring constructor: it used to call {@code autoConfigure()}
 * before assigning the servlet context, so {@code ServletUtils.getConfiguration(null)} NPE'd.
 */
public class ViewToolManagerTest
{
    @Test
    public void autoConfiguringConstructorAssignsContextBeforeAutoConfigure()
    {
        ServletContext app = createNiceMock(ServletContext.class);
        replay(app);
        ViewToolManager manager = new ViewToolManager(app);
        assertNotNull(manager);
    }
}

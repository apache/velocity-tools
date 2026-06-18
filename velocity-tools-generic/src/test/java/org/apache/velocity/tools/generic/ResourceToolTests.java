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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Test;

/**
 * <p>Tests for {@link ResourceTool}.</p>
 */
public class ResourceToolTests
{
    private ResourceTool text()
    {
        ResourceTool text = new ResourceTool();
        text.configure(new HashMap<String, Object>());
        return text;
    }

    // a missing key resolves to null, so the engine applies its own strict/non-strict semantics
    public @Test void missingKeyRendersNull()
    {
        assertNull(text().get("no.such.key.veltools167").toString());
    }

    // a null key (e.g. bare $text) still renders as the empty string
    public @Test void nullKeyRendersEmpty()
    {
        assertEquals("", text().get((Object) null).toString());
    }
}

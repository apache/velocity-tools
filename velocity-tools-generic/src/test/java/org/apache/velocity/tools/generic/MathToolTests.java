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

import java.math.BigInteger;

import org.apache.velocity.VelocityContext;

/**
 * This 
 */
public class MathToolTests extends BaseTestCase
{
    public MathToolTests(String name)
    {
        super(name);
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("math", new MathTool());
    }

    public void testMax()
    {
        assertEvalEquals("4.0", "$math.max(4,3.5)");
        assertEvalEquals("4.0", "$math.max(4,3.5,3)");
    }

    public void testRandomIntegralType()
    {
        // integral args render without a decimal point: Integer/Long, never Double
        assertEvalEquals("5", "$math.random(5,5)");
    }

    public void testRandomTypeNarrowing()
    {
        // VELTOOLS-196: ordinary ranges keep their type; beyond long falls back to Double
        assertTrue(new MathTool().random(5, 5) instanceof Integer);
        assertTrue(new MathTool().random(3000000000L, 3000000000L) instanceof Long);
        BigInteger big = new BigInteger("99999999999999999999");
        assertTrue(new MathTool().random(big, big) instanceof Double);
    }

    public void testMatchTypeOverflowNotCapped()
    {
        // VELTOOLS-196: an integral result beyond long must not saturate to Long.MAX;
        // it comes back as an (approximate) Double, never the Long.MAX constant
        Number r = new MathTool().mul(4000000000L, 4000000000L);   // 1.6e19 > Long.MAX
        assertTrue("expected Double, got " + r.getClass(), r instanceof Double);
        assertEquals(1.6e19, r.doubleValue(), 1.0e6);
    }
}

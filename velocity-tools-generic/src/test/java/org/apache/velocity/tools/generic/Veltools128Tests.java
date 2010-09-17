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

import org.apache.velocity.VelocityContext;

/**
 * This class tests VELOCITY-762.
 */
public class Veltools128Tests extends BaseTestCase
{
    public Veltools128Tests(String name)
    {
        super(name);
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("loop", new LoopTool());
    }

    public void testLoopToolSync()
    {
        String template = 
            "#foreach( $item in $loop.watch([1..3]).sync([3..5], 'other') )"+
            "$item:$loop.other "+
            "#end";
        assertEvalEquals("1:3 2:4 3:5 ", template);
    }

}

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

import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.AbstractPagerTool;
import org.apache.velocity.tools.view.tools.ParameterParser;

/**
 * This is meant to demonstrate how to extend the AbstractPagerTool.
 *
 * @author Nathan Bubna
 * @version $Id: MyPagerTool.java 479724 2006-11-27 18:49:37Z nbubna $
 */
public class MyPagerTool extends AbstractPagerTool
{
    /**
     * Override to create a session in which to store an item list.
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        context.getRequest().getSession(true);

        // then pass on to the super class
        super.init(obj);
    }

    public void setup(HttpServletRequest req)
    {
        ParameterParser pp = new ParameterParser(req);
        setIndex(pp.getInt("index", 0));
        setItemsPerPage(pp.getInt("show", DEFAULT_ITEMS_PER_PAGE));
    }

}

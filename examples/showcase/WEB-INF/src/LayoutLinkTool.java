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

import org.apache.velocity.tools.view.LinkTool;
import org.apache.velocity.tools.view.VelocityLayoutServlet;

/**
 * This is meant to demonstrate how to extend the LinkTool to
 * avoid the manual use of "magic" or common query parameters.
 * So instead of doing $link.param('layout', 'Table.vm') in your
 * template, you would just do $link.layout('Table').
 *
 * @author Nathan Bubna
 * @version $Id: LayoutLinkTool.java 479724 2006-11-27 18:49:37Z nbubna $
 */
public class LayoutLinkTool extends LinkTool
{

	public LayoutLinkTool layout(Object obj)
	{
        if (obj == null)
        {
            return null;
        }
        return layout(obj.toString());
    }

    public LayoutLinkTool layout(String layout)
    {
        if (layout != null && !layout.endsWith(".vm"))
        {
            layout += ".vm";
        }
        return (LayoutLinkTool)param(VelocityLayoutServlet.KEY_LAYOUT, layout);
	}

}

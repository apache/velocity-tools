/*
 * Copyright 2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.velocity.tools.view;


import org.apache.velocity.tools.view.context.ToolboxContext;


/**
 * Common interface for toolbox manager implementations.
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ToolboxManager.java,v 1.3 2004/02/18 20:08:29 nbubna Exp $
 */
public interface ToolboxManager
{


    /**
     * Adds a tool to be managed
     */
    void addTool(ToolInfo info);


    /**
     * Creates a {@link ToolboxContext} from the tools and data 
     * in this manager.  Tools that implement the ViewTool
     * interface should be initialized using the given initData.
     *
     * @param initData data used to initialize ViewTools
     * @return the created ToolboxContext
     */
    ToolboxContext getToolboxContext(Object initData);


}

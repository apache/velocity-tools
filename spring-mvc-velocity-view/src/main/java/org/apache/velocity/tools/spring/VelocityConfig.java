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
package org.apache.velocity.tools.spring;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.ViewToolManager;

/**
 * Configuration interface to be implemented by objects that hold the
 * {@link VelocityEngine} and Velocity Tools {@link ViewToolManager} shared by
 * the Spring MVC Velocity views. A single bean implementing it (typically
 * {@link VelocityConfigurer}) is looked up in the web application context by
 * {@link VelocityToolboxView}.
 */
public interface VelocityConfig
{
    /** @return the VelocityEngine the views render with */
    VelocityEngine getVelocityEngine();

    /** @return the tool manager that builds the toolbox-bearing render context */
    ViewToolManager getToolManager();
}

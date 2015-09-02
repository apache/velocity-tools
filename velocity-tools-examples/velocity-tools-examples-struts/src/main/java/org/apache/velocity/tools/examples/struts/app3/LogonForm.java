package org.apache.velocity.tools.examples.struts.app3;

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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


/**
 * Form bean for the user profile page.
 * This form has the following fields,
 * with default values in square brackets:
 * <ul>
 * <li><b>password</b> - Entered password value
 * <li><b>username</b> - Entered username value
 * </ul>
 *
 * @author Ted Husted
 * @version $Revision$ $Date$
 */

public final class LogonForm extends ActionForm
{




    // ------------------------------------------------ Instance Variables


    /**
     * The password.
     */
    private String password = null;


    /**
     * The username.
     */
    private String username = null;


    // ------------------------------------------------------ Properties

    /**
     * Return the password.
     */
    public String getPassword()
    {

        return (this.password);

    }


    /**
     * Set the password.
     *
     * @param password The new password
     */
    public void setPassword(String password)
    {

        this.password = password;

    }


    /**
     * Return the username.
     */
    public String getUsername()
    {

        return (this.username);

    }


    /**
     * Set the username.
     *
     * @param username The new username
     */
    public void setUsername(String username)
    {

        this.username = username;

    }


    // -------------------------------------------------- Public Methods


    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping,
        HttpServletRequest request)
    {

        setPassword(null);
        setUsername(null);

    }


    /**
     * Ensure that both fields have been input.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request)
    {

        ActionErrors errors = new ActionErrors();

        if ((username == null) || (username.length() < 1))
            errors.add("username", new ActionMessage("error.username.required"));

        if ((password == null) || (password.length() < 1))
            errors.add("password", new ActionMessage("error.password.required"));

        return errors;

    }

} // End LogonForm

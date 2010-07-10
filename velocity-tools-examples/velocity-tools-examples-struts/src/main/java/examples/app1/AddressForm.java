package examples.app1;

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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * <p>A simple form that allows a user to enter and modify an address.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id$
 */

public final class AddressForm extends ActionForm
{

    // ---- Form fields -------------------------------------------------

    private String action = "";

    private String firstname = "";

    private String lastname = "";

    private String street = "";

    private String zip = "";

    private String city = "";

    private String country = "";

    private String locale = "";

    private String[] languages;


    // ---- Accessor Methods --------------------------------------------

    public String getAction()
    {
        return action;
    }

    public void setAction(String s)
    {
        action = s;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String s)
    {
        firstname = s;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String s)
    {
        lastname = s;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String s)
    {
        street = s;
    }

    public String getZip()
    {
        return zip;
    }

    public void setZip(String s)
    {
        zip = s;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String s)
    {
        city = s;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String s)
    {
        country = s;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String s)
    {
        locale = s;
    }

    public String[] getLanguages()
    {
        return languages;
    }

    public void setLanguages(String[] s)
    {
        languages = s;
    }


    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request)
    {

        action = "";
        locale = "";
        firstname = "";
        lastname = "";
        street = "";
        zip = "";
        city = "";
        country = "";

    }

}



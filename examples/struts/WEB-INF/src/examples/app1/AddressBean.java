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

package examples.app1;


import java.util.Properties;


/**
 * <p>A simple bean that represent an address record.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: AddressBean.java,v 1.3 2004/03/12 19:41:01 marino Exp $
 */

public class AddressBean extends Object
{


    // ---- Fields ------------------------------------------------------
    private String firstname;

    private String lastname;

    private String street;

    private String zip;

    private String city;

    private String country;

    private String[] languages;


    // ---- Accessor Methods --------------------------------------------

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

    public String[] getLanguages()
    {
        return languages;
    }

    public void setLanguages(String[] languages)
    {
        this.languages = languages;
    }

    // Convenience method to simplify repopulation of select lists
    public Properties getLanguagesAsMap()
    {
        Properties p = new Properties();
        if (languages != null)
        {
            for (int i = 0; i < languages.length; i++)
                p.setProperty((String)languages[i], "SELECTED");
        }
        return p;
    }

}





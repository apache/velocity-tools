/*
 * Struts Example Application 1
 *  
 * This demonstrates the use of Velocity templates with the Struts framework.
 */


package examples.app1;


import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;


/**
 * <p>A simple form that allows a user to enter and modify an address.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: AddressForm.java,v 1.1 2003/03/06 00:05:18 nbubna Exp $
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



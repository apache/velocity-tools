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


/**
 * <p>A simple form that allows a user to enter and modify an address.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: AddressForm.java,v 1.1 2002/01/03 20:21:20 geirm Exp $
 */

public final class AddressForm extends ActionForm 
{

    // ---- Form fields -------------------------------------------------
 
    private String action = "";
    
    private String language = "";

    private String firstname = "";
    
    private String lastname = "";

    private String street = "";
    
    private String zip = "";
    
    private String city = "";
    
    private String country = "";
    

    // ---- Accessor Methods --------------------------------------------
    
    public String getAction()
    {
        return action;
    }
    
    public void setAction(String s)
    {
        action = s;
    }
            

    public String getLanguage()
    {
        return language;
    }
    
    public void setLanguage(String s)
    {
        language = s;
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

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        action = "";
        language = "";
        firstname = "";
        lastname = "";
        street = "";
        zip = "";
        city = "";
        country = "";

    }

}



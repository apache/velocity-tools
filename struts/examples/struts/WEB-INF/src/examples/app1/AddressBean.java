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
 * <p>A simple bean that represent an address record.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: AddressBean.java,v 1.1 2002/01/03 20:21:20 geirm Exp $
 */

public class AddressBean extends Object {


    // ---- Fields ------------------------------------------------------
    private String firstname;
    
    private String lastname;

    private String street;
    
    private String zip;
    
    private String city;
    
    private String country;
    

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

}





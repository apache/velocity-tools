/*
 * Struts Example Application 2
 *  
 * This demonstrates the use of Velocity templates with the Struts framework.
 */


package examples.app2;


import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


/**
 * <p>A simple form for demo purposes.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: DemoForm.java,v 1.1 2003/03/06 00:05:18 nbubna Exp $
 */

public final class DemoForm extends ActionForm 
{

    // ---- Form fields -------------------------------------------------
 
    private String language = "";
    

    // ---- Accessor Methods --------------------------------------------
    
    public String getLanguage()
    {
        return language;
    }
    
    public void setLanguage(String s)
    {
        language = s;
    }




    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) 
    {

        language = "";

    }

}



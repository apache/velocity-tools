/*
 * Struts Example Application 1
 *  
 * This demonstrates the use of Velocity templates with the Struts framework.
 */


package examples.app2;


import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * <p>A simple action used to demonstrate the view tools.</p>
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: DemoAction.java,v 1.1 2003/03/06 00:05:18 nbubna Exp $
 */
public class DemoAction extends Action 
{


    // --------------------------------------------------------- Public Methods

	/**
	 * Handle server requests.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
	 */
    public ActionForward perform(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException
    {
        String action;
	    HttpSession session;

        // Create serveral error messages to demontrate the output in a template
        ActionErrors errors = new ActionErrors();
        
        // Add some global errors
        errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error01"));
        errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error02"));

        // Add some specific errors
        errors.add("language", new ActionError("error10"));
        errors.add("language", new ActionError("error11"));
        
        // Save error messages to request attributes
        saveErrors(request, errors);

        // Create and save a new transaction token
        saveToken(request);

        // forward to edit formular
        return (mapping.findForward("home"));

    }
}


/*
 * Struts Example Application 1
 *  
 * This demonstrates the use of Velocity templates with the Struts framework.
 */


package examples.app1;


import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * <p>A simple action that handles the display and editing of an
 * addres record. This action works with both JSP and Velocity templates.
 * The type of template to be used is defined in the Struts configuration
 * file.</p>
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list address record, this is the default if no action parameter is specified
 *   <li>edit - edit address record
 *   <li>save - save address record
 * </ul>
 * 
 *
 * @author <a href="mailto:sidler@teamup.com"/>Gabe Sidler</a>
 * @version $Id: AddressAction.java,v 1.2 2003/07/22 05:16:40 nbubna Exp $
 */
public class AddressAction extends Action 
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
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException
    {
        String action;
        HttpSession session;

        ActionErrors errors = new ActionErrors();

        try
        {
            session = request.getSession();
    
            // fetch action from form
            action = ((AddressForm)form).getAction();

            servlet.log("[DEBUG] AddressAction at perform(): Action ist " + action);

            // Determine what to do
            if ( action.equals("edit") )
            {
                // forward to edit formular
                return (mapping.findForward("editAddress"));
    
            }
            else if (action.equals("save"))
            {
                // check if an address bean exits already
                AddressBean bean = (AddressBean)session.getAttribute("address");
                
                if (bean == null)
                {
                    bean = new AddressBean();
                    session.setAttribute("address", bean);
                }
                 
                // update bean with the new values submitted
                bean.setFirstname( ((AddressForm)form).getFirstname() );
                bean.setLastname( ((AddressForm)form).getLastname() );
                bean.setStreet( ((AddressForm)form).getStreet() );
                bean.setZip( ((AddressForm)form).getZip() );
                bean.setCity( ((AddressForm)form).getCity() );
                bean.setCountry( ((AddressForm)form).getCountry() );
                bean.setLanguages( ((AddressForm)form).getLanguages() );

                // forward to list
                return (mapping.findForward("showAddress"));
    
            }
            else
            {
                String locale = ((AddressForm)form).getLocale();
                if (locale.equals("Deutsch"))
                    session.setAttribute(Globals.LOCALE_KEY, new Locale("de", ""));
                else
                    session.setAttribute(Globals.LOCALE_KEY, new Locale("en", ""));
                
                // forward to edit formular
                return (mapping.findForward("showAddress"));                
            }
        }
        catch (Exception e)
        {
            //errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("lo053"));
            servlet.log("[ERROR] TskAction at final catch: " + e.getMessage());
            e.printStackTrace();

        }

        // Default if everthing else fails
        return (mapping.findForward("showAddress"));

    }
}


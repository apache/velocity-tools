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

package examples.app3;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Implementation of <strong>Action</strong> that validates a user logon.
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision: 1.4 $ $Date: 2004/03/12 19:41:11 $
 */

public final class LogonAction extends Action
{


// ---------------------------------------------------- Public Methods

    /**
     * Login the user.
     * The event is logged if the debug level is >= Constants.DEBUG.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The ActionForm bean for this request (if any)
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

      String username = ((LogonForm) form).getUsername();
      String password = ((LogonForm) form).getPassword();

      // Save our logged-in user in the session,
      // because we use it again later.
      HttpSession session = request.getSession();
      session.setAttribute(Constants.USER_KEY, form);

      // Log this event
      StringBuffer message = new StringBuffer("LogonAction: User '");
      message.append(username);
      message.append("' logged on in session ");
      message.append(session.getId());
      servlet.log(message.toString());

      // Forward control to the success URI
      // specified in struts-config.xml
      return (mapping.findForward(Constants.CONTINUE));

  }

}

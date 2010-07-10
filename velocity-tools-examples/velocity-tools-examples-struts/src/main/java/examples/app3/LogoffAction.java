package examples.app3;

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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * Implementation of <strong>Action</strong> that processes a
 * user logoff.
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision$ $Date$
 */

public final class LogoffAction extends Action
{

    // ---------------------------------------------------- Public Methods

    /**
     * Logoff the user.
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

      // Extract attributes we will need
      HttpSession session = request.getSession();
      LogonForm user = (LogonForm)
        session.getAttribute(Constants.USER_KEY);

      // Log this user logoff
      if (user != null)
      {
        StringBuffer message = new StringBuffer("LogoffAction: User '");
        message.append(user.getUsername());
        message.append("' logged off in session ");
        message.append(session.getId());
        servlet.log(message.toString());
      }

      else
      {

        StringBuffer message = new StringBuffer("LogoffAction: User '");
        message.append(session.getId());
        servlet.log(message.toString());
      }

      // Remove user login; invalidate session
      session.removeAttribute(Constants.USER_KEY);
      session.invalidate();

      // Forward control to the specified success URI
      return (mapping.findForward(Constants.CONTINUE));

    }

} // End LogoffAction

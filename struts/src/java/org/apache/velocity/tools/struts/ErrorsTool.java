/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.struts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.*;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.LogEnabledViewToolImpl;
import org.apache.velocity.tools.view.tools.ServletViewTool;


/**
 * <p>View tool to work with the Struts error messages.</p>
 *
 * <p>This class is equipped to be used with a toolbox manager, for example
 * the ServletToolboxManager included with VelServlet. The class extends 
 * ServletViewToolLogger to profit from the logging facilities of that class.
 * Furthermore, this class implements interface ServletViewTool, which allows
 * a toolbox manager to pass the required context information.</p>
 *
 * <p>This class is not thread-safe by design. A new instance is needed for
 * the processing of every template request.</p>
 *

 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 *
 * @version $Id: ErrorsTool.java,v 1.4 2002/04/15 18:30:28 sidler Exp $
 * 
 */
public class ErrorsTool extends LogEnabledViewToolImpl 
    implements ServletViewTool
{

    // --------------------------------------------- Properties ---------------

    /**
     * A reference to the ServletContext
     */ 
    protected ServletContext application;


    /**
     * A reference to the HttpServletRequest.
     */ 
    protected HttpServletRequest request;
    

    /**
     * A reference to the HttpSession.
     */ 
    protected HttpSession session;


    /**
     * A reference to the Struts message resources.
     */
    protected MessageResources resources;


    /**
     * A reference to the user's locale.
     */
    protected Locale locale;


    /**
     * A reference to the queued action messages.
     */
    protected ActionErrors errors;



    // --------------------------------------------- Constructors -------------

    /**
     * Returns a factory for instances of this class. Use method 
     * {@link #getInstance(ViewContext context)} to obtain instances 
     * of this class. Do not use instance obtained from this method
     * in templates. They are not properly initialized.
     */
    public ErrorsTool()
    {}
    
    
    /**
     * For internal use only! Use method {@link #getInstance(ViewContext context)} 
     * to obtain instances of the tool.
     */
    private ErrorsTool(ViewContext context)
    {
        this.request = context.getRequest();
        this.session = request.getSession(false);
        this.application = context.getServletContext();    

        resources = StrutsUtils.getMessageResources(application);
        locale = StrutsUtils.getLocale(request, session);
        errors = StrutsUtils.getActionErrors(request);
    }
    


    // ----------------------------------- Interface ServletViewTool ----------

    /**
     * Returns an initialized instance of this view tool.
     */
    public Object getInstance(ViewContext context)
    {
        return new ErrorsTool(context);
    }

    
    /**
     * <p>Returns the default life cycle for this tool. This is 
     * {@link ServletViewTool#REQUEST}. Do not overwrite this
     * per toolbox configuration. No alternative life cycles are 
     * supported by this tool</p>
     */
    public String getDefaultLifecycle()
    {
        return ServletViewTool.REQUEST; 
    }


    // --------------------------------------------- View Helpers -------------

    /**
     * <p>Returns <code>true</code> if there are action errors queued, 
     * otherwise <code>false</code>.</p>
     */
    public boolean exist() 
    {
        if (errors == null)
        {
            return false;
        }

        return !errors.empty();
    }


    /**
     * <p>Returns true if there are action errors queued for the specified 
     * category of errors, otherwise <code>false</code>.</p>
     *
     * @param property the category of errors to check for
     */
    public boolean exist(String property) 
    {
        if (errors == null)
        {
            return false;
        }
        return (errors.size(property) > 0);
    }


    /**
     * Returns the number of action errors queued.
     */
    public int getSize() 
    {
        if (errors == null)
        {
            return 0;
        }
        
        return errors.size();
    }


    /**
     * Returns the number of action errors queued for a particular property.
     *
     * @param property the category of errors to check for
     */
    public int getSize(String property) 
    {
        if (errors == null)
        {
            return 0;
        }

        return errors.size(property);
    }


    /**
     * Returns the set of localized error messages as an 
     * <code>java.util.ArrayList</code> of <code> java.lang.String</code> 
     * for all errors queued or <code>null</code> if no errors are queued.
     * If the message resources don't contain an error message for a 
     * particular error key, the key itself is used as error message.
     */
    public ArrayList getAll() 
    {
        return get(null);
    }


    /**
     * Returns the set of localized error messages as an 
     * <code>java.util.ArrayList</code> of <code> java.lang.String</code> 
     * for all errors queued of the specified category or <code>null</code> 
     * if no error are queued for the specified category. If the message 
     * resources don't contain an error message for a particular error key, 
     * the key itself is used as error message.
     *
     * @param property the category of errors to operate on
     */
    public ArrayList get(String property) 
    {
        if (errors == null || errors.empty())
        {
            return null;
        }
        
        if (resources == null) 
        {
            log(ERROR, "Message resources are not available.");
            //FIXME? should we return the list of error keys instead?
            return null;
        }
        
        Iterator errormsgs;
        if (property == null)
        {
            errormsgs = errors.get();
        }
        else
        {
            errormsgs = errors.get(property);
        }
        
        if (!(errormsgs.hasNext())) 
        {
            return null;
        }

        ArrayList list = new ArrayList();
         
        while (errormsgs.hasNext())
        {
            ActionError errormsg = (ActionError)errormsgs.next();
            String message = resources.getMessage(locale,
                                                  errormsg.getKey(), 
                                                  errormsg.getValues());
            if (message != null)
            {
                list.add(message);
            }
            else
            {
                // if error message cannot be found for a key, return key instead
                log(WARN, "Message for key " + errormsg.getKey() + " could not be found in message resources.");
                list.add(errormsg.getKey());
            }
        }
        return list;
    }


    /**
     * <p>Renders the queued error messages as a list. This method expects
     * the message keys <code>errors.header</code> and <code>errors.footer</code>
     * in the message resources. The value of the former is rendered before
     * the list of error messages and the value of the latter is rendered
     * after the error messages.</p>
     * 
     * @return The formatted error messages. If no error messages are queued, 
     * an empty string is returned.
     */
    public String getMsgs()
    {
        return StrutsUtils.errorMarkup(null, request, session, application);    
    }
     

    /**
     * <p>Renders the queued error messages of a particual category as a list. 
     * This method expects the message keys <code>errors.header</code> and 
     * <code>errors.footer</code> in the message resources. The value of the 
     * former is rendered before the list of error messages and the value of 
     * the latter is rendered after the error messages.</p>
     * 
     * @param property the category of errors to render
     * 
     * @return The formatted error messages. If no error messages are queued, 
     * an empty string is returned. 
     */
    public String getMsgs(String property)
    {
        return StrutsUtils.errorMarkup(property, request, session, application);    
    }

}

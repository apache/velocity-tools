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

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;


/**
 * This is to be used *only* until Struts 1.1 support is added
 * to the VelocityView library
 * 
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: ActionMessagesTool.java,v 1.1 2003/03/10 21:43:05 nbubna Exp $
 */
public class ActionMessagesTool implements ViewTool
{

    // --------------------------------------------- Properties ---------------

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
    protected ActionMessages actionMsgs;



    // --------------------------------------------- Constructors -------------

    /**
     * Default constructor. Tool must be initialized before use.
     */
    public ActionMessagesTool()
    {}
    
    
    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        if (!(obj instanceof ViewContext))
        {
            throw new IllegalArgumentException("Tool can only be initialized with a ViewContext");
        }

        ViewContext context = (ViewContext)obj;
        HttpServletRequest request = context.getRequest();
        HttpSession session = request.getSession(false);
        ServletContext application = context.getServletContext();    

        this.locale = getLocale(request, session);
        this.resources = 
            (MessageResources)application.getAttribute(Globals.MESSAGES_KEY);
        this.actionMsgs = 
            (ActionMessages)request.getAttribute(Globals.MESSAGE_KEY);
    }


    private static Locale getLocale(HttpServletRequest request, HttpSession session)
    {
        Locale locale = null;
        if (session!=null)
        {
            locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);
        }

        if ((locale==null) && (request!=null))
        {
            locale = request.getLocale();
        }

        return locale;
    }


    // --------------------------------------------- View Helpers -------------

    /**
     * <p>Returns <code>true</code> if there are action messages queued, 
     * otherwise <code>false</code>.</p>
     */
    public boolean exist() 
    {
        if (actionMsgs == null)
        {
            return false;
        }

        return !actionMsgs.isEmpty();
    }


    /**
     * <p>Returns true if there are action messages queued for the specified 
     * category of messages, otherwise <code>false</code>.</p>
     *
     * @param property the category of messages to check for
     */
    public boolean exist(String property) 
    {
        if (actionMsgs == null)
        {
            return false;
        }
        return (actionMsgs.size(property) > 0);
    }


    /**
     * Returns the number of action messages queued.
     */
    public int getSize() 
    {
        if (actionMsgs == null)
        {
            return 0;
        }
        
        return actionMsgs.size();
    }


    /**
     * Returns the number of action messages queued for a particular property.
     *
     * @param property the category of messages to check for
     */
    public int getSize(String property) 
    {
        if (actionMsgs == null)
        {
            return 0;
        }

        return actionMsgs.size(property);
    }


    /**
     * Returns the set of localized action messages as an 
     * <code>java.util.ArrayList</code> of <code> java.lang.String</code> 
     * for all actionMsgs queued or <code>null</code> if no messages are queued.
     * If the message resources don't contain a message for a 
     * particular key, the key itself is used as the message.
     */
    public ArrayList getAll() 
    {
        return get(null);
    }


    /**
     * Returns the set of localized error messages as an 
     * <code>java.util.ArrayList</code> of <code> java.lang.String</code> 
     * for all actionMsgs queued of the specified category or <code>null</code> 
     * if no error are queued for the specified category. If the message 
     * resources don't contain a message for a particular key, 
     * the key itself is used as the message.
     *
     * @param property the category of actionMsgs to operate on
     */
    public ArrayList get(String property) 
    {
        if (actionMsgs == null || actionMsgs.isEmpty())
        {
            return null;
        }
        
        if (resources == null) 
        {
            Velocity.error("Message resources are not available.");
            //FIXME? should we return the list of message keys instead?
            return null;
        }
        
        Iterator msgs;
        if (property == null)
        {
            msgs = actionMsgs.get();
        }
        else
        {
            msgs = actionMsgs.get(property);
        }
        
        if (!(msgs.hasNext())) 
        {
            return null;
        }

        ArrayList list = new ArrayList();
         
        while (msgs.hasNext())
        {
            ActionMessage msg = (ActionMessage)msgs.next();
            String message = resources.getMessage(locale,
                                                  msg.getKey(), 
                                                  msg.getValues());
            if (message != null)
            {
                list.add(message);
            }
            else
            {
                // if error message cannot be found for a key, return key instead
                Velocity.warn("Message for key " + msg.getKey() + " could not be found in message resources.");
                list.add(msg.getKey());
            }
        }
        return list;
    }


}


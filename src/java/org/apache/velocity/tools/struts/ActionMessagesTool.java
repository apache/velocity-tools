/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
import java.util.List;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.struts.StrutsUtils;

/**
 * <p>View tool to work with the Struts action messages.</p>
 * <p><pre>
 * Template example(s):
 *   #if( $actionmsgs.exist() )
 *     #foreach( $e in $actionmsgs.all )
 *       $e &lt;br&gt;
 *     #end
 *   #end
 *
 * Toolbox configuration:
 *
 * &lt;tool&gt;
 *   &lt;key&gt;actionmsgs&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.ActionMessagesTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 * 
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @since VelocityTools 1.1
 * @version $Id: ActionMessagesTool.java,v 1.6 2004/01/07 19:08:57 nbubna Exp $
 */
public class ActionMessagesTool extends MessageResourcesTool
{

    /** A reference to the queued action messages. */
    protected ActionMessages actionMsgs;


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
        //setup superclass instance members
        super.init(obj);

        this.actionMsgs = StrutsUtils.getActionMessages(this.request);
    }


    /*************************** Public Methods ***********************/

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
     * <p>
     * This a convenience method and the equivalent of 
     * <code>$actionmsgs.get($actionmsgs.globalName)</code>. 
     * </p>
     * <p>
     * Returns the set of localized action messages as an 
     * list of strings for all action messages queued of the 
     * global category or <code>null</code> if no messages
     * are queued for the specified category. If the message 
     * resources don't contain an action message for a
     * particular message key, the key itself is used.
     * </p>
     *
     * @return a list of all messages stored under the "global" property
     */
    public List getGlobal() 
    {
        return get(getGlobalName());
    }


    /**
     * Returns the set of localized action messages as an 
     * <code>java.util.List</code> of strings for all actionMsgs 
     * queued or <code>null</code> if no messages are queued.
     * If the message resources don't contain a message for a 
     * particular key, the key itself is used as the message.
     */
    public List getAll() 
    {
        return get(null);
    }


    /**
     * Returns a List of all queued action messages using
     * the specified message resource bundle.
     *
     * @param bundle the message resource bundle to use
     * @see #getAll()
     */
    public List getAll(String bundle)
    {
        return get(null, bundle);
    }


    /**
     * Returns the set of localized action messages as an 
     * <code>java.util.List</code> of strings for all actionMsgs 
     * queued of the specified category or <code>null</code> 
     * if no messages are queued for the specified category. If the 
     * message resources don't contain a message for a particular 
     * key, the key itself is used as the message.
     *
     * @param property the category of actionMsgs to operate on
     */
    public List get(String property) 
    {
        return get(property, null);
    }


    /**
     * Returns the set of localized action messages as a 
     * <code>java.util.List</code> of strings for all action messages 
     * queued of the specified category or <code>null</code> 
     * if no action messages are queued for the specified category. If the 
     * message resources don't contain an action message for a particular 
     * action key, the key itself is used as action message.
     *
     * @param property the category of actionMsgs to operate on
     * @param bundle the message resource bundle to use
     */
    public List get(String property, String bundle) 
    {
        if (actionMsgs == null || actionMsgs.isEmpty())
        {
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
        
        if (!msgs.hasNext()) 
        {
            return null;
        }

        MessageResources res = getResources(bundle);
        List list = new ArrayList();
         
        while (msgs.hasNext())
        {
            ActionMessage msg = (ActionMessage)msgs.next();

            String message = null;
            if (res != null)
            {
                message = 
                    res.getMessage(this.locale, msg.getKey(), msg.getValues());

                if (message == null)
                {
                    Velocity.warn("ActionMessagesTool: Message for key " + 
                                  msg.getKey() + 
                                  " could not be found in message resources.");
                }
            }
            else
            {
                // if the resource bundle wasn't found, use the key
                message = msg.getKey();
            }
            list.add(message);
        }
        return list;
    }


    /**
     * Returns the default "GLOBAL" category name that can be used for
     * messages that are not associated with a particular property.
     */
    public String getGlobalName()
    {
        return ActionMessages.GLOBAL_MESSAGE;
    }

}

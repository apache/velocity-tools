package org.apache.velocity.tools.struts;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * <p>
 * This tool deals with Struts action messages.  A few important aspects about action
 * messages are:</p>
 * <ul>
 *     <li>Action message strings are looked up in the message resources. Support
 *         for internationalized messages is provided.</li>
 *     <li>Action messages can have up to five replacement parameters.</li>
 *     <li>Actions have an attribute <code>property</code> that describes the category of
 *         message. This allows the view designer to place action messages precisely where they are
 *         wanted. Several methods of this tool provide a parameter
 *         <code>property</code> that allows to select a specific category of messages to operate
 *         on. Without the <code>property</code> parameter, methods operate on all action messages.</li>
 * </ul>
 * 
 * <p>See the Struts User's Guide, section
 * <a href="http://struts.apache.org/1.3.8/userGuide/building_view.html">Building View Components</a>
 * for more information on this topic.</p>
 * 
 * <p><pre>
 * Template example(s):
 *   #if( $messages.exist() )
 *     #foreach( $e in $messages.all )
 *       $e &lt;br&gt;
 *     #end
 *   #end
 *
 * Toolbox configuration:
 * &lt;tools&gt;
 *   &lt;toolbox scope="request"&gt;
 *     &lt;tool class="org.apache.velocity.tools.struts.ActionMessagesTool"/&gt;
 *   &lt;/toolbox&gt;
 * &lt;/tools&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author Nathan Bubna
 * @since VelocityTools 1.1
 * @version $Id$
 */
@DefaultKey("messages")
@ValidScope(Scope.REQUEST)
public class ActionMessagesTool extends MessageResourcesTool
{
    /** A reference to the queued action messages. */
    protected ActionMessages actionMsgs;


    protected ActionMessages getActionMessages()
    {
        if (this.actionMsgs == null)
        {
            this.actionMsgs = StrutsUtils.getMessages(this.request);
        }
        return this.actionMsgs;
    }


    /*************************** Public Methods ***********************/

    /**
     * <p>Returns <code>true</code> if there are action messages queued,
     * otherwise <code>false</code>.</p>
     */
    public boolean exist()
    {
        if (getActionMessages() == null)
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
        if (getActionMessages() == null)
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
        if (getActionMessages() == null)
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
        if (getActionMessages() == null)
        {
            return 0;
        }
        return actionMsgs.size(property);
    }


    /**
     * <p>
     * This a convenience method and the equivalent of
     * <code>$messages.get($messages.globalName)</code>.
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
        ActionMessages actionMsgs = getActionMessages();
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
            if (res != null && msg.isResource())
            {
                message =
                    res.getMessage(getLocale(), msg.getKey(), msg.getValues());

                if (message == null)
                {
                    LOG.warn("ActionMessagesTool : Message for key " + msg.getKey() +
                             " could not be found in message resources.");
                }
            }

            if (message == null)
            {
                // if the resource bundle wasn't found or
                // ActionMessage.isResource() returned false or the key
                // wasn't found in the resources, then use the key
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

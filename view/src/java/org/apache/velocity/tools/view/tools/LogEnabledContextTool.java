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

package org.apache.velocity.tools.view.tools;


/**
 * <p>An interface that marks a context tool class as capable of logging.</p>
 * 
 * <p>The only method defined by this interface is 
 * <code>{@link #setLogger(ContextToolLogger logger)}</code>. This method allows 
 * a toolbox manager to pass an instance of a logger object to the context 
 * tool. This logger can be used subsequently to log messages.</p>
 * 
 * <p>The following implementation guidelines are to be noted:</p>
 * <ul>
 *   <li>Implementing this interface by a context tool does not garantuee
 *       that a logger will be passed. It is up to the implementation of the
 *       toolbox manager if a logger is passed or not.</li>
 *   <li>If a logger is passed using setLogger(), it will be passed only
 *       to one instance of a context tool class. It has to be made sure, that
 *       a reference to the logger is stored in a static variable, such that
 *       the logger is available to all instances of the class.</li>
 *   <li>Logging in context tools typically should be used only for serious 
 *       errors. Most application environments already implement usage
 *       logging, etc.</li>
 * </ul>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabriel Sidler</a>
 *
 * @version $Id: LogEnabledContextTool.java,v 1.1 2002/04/02 16:46:31 sidler Exp $
 * 
 */
public interface LogEnabledContextTool 
{

    /**
     * <p>Sets a logger instance for this class of context tools.</p>
     *
     * <p>This logger can be used subsequently by instances to to log 
     * messages to the logging infrastructor of th underlying framework.</p>
     *
     * @param logger the logger instance to be set
     */
    public void setLogger(ContextToolLogger logger);

}

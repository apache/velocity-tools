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
 
package org.apache.velocity.tools.view.servlet;

import javax.servlet.ServletContext;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Resource loader that uses the ServletContext of a webapp to
 * load Velocity templates.  (it's much easier to use with servlets than
 * the standard FileResourceLoader, in particular the use of war files
 * is transparent).
 *
 * The default search path is '/' (relative to the webapp root), but
 * you can change this behaviour by specifying one or more paths
 * by mean of as many webapp.resource.loader.path properties as needed
 * in the velocity.properties file.
 *
 * All paths must be relative to the root of the webapp.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:claude@savoirweb.com">Claude Brisson</a>
 * @version $Id: WebappLoader.java,v 1.6 2003/07/04 18:28:25 nbubna Exp $
 */
public class WebappLoader extends ResourceLoader
{

    /** The root paths for templates (relative to webapp's root). */
    protected String[] paths = null;

    protected ServletContext servletContext = null;

    /**
     *  This is abstract in the base class, so we need it.
     *  <br>
     *  NOTE: this expects that the ServletContext has already
     *        been placed in the runtime's application attributes
     *        under its full class name (i.e. "javax.servlet.ServletContext").
     *
     * @param configuration the {@link ExtendedProperties} associated with 
     *        this resource loader.
     */
    public void init(ExtendedProperties configuration)
    {
        rsvc.info("WebappLoader : initialization starting.");
        
        /* get configured paths */
        paths = configuration.getStringArray("path");
        if (paths == null || paths.length == 0)
        {
            paths = new String[1];
            paths[0] = "/";
        }
        else
        {
            /* make sure the paths end with a '/' */
            for (int i=0; i < paths.length; i++)
            {
                if (!paths[i].endsWith("/"))
                {
                    paths[i] += '/';
                }
                rsvc.info("WebappLoader : added template path - '" + paths[i] + "'");
            }
        }

        /* get the ServletContext */
        Object obj = rsvc.getApplicationAttribute(ServletContext.class.getName());
        if (obj instanceof ServletContext)
        {
            servletContext = (ServletContext)obj;
        }
        else
        {
            rsvc.error("WebappLoader : unable to retrieve ServletContext");
        }

        rsvc.info("WebappLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in  classpath.
     */
    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        InputStream result = null;
        
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException ("WebappLoader : No template name provided");
        }
        
        /* since the paths always ends in '/',
         * make sure the name never ends in one */
        while (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        Exception exception = null;
        for (int i=0; i < paths.length; i++)
        {
            try 
            {
                result = servletContext.getResourceAsStream(paths[i] + name);

                /* exit the loop if we found the template */
                if (result != null)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                /* only save the first one for later throwing */
                if (exception == null)
                {
                    exception = e;
                }
            }
        }

        /* if we never found the template */
        if (result == null)
        {
            String msg;
            if (exception == null)
            {
                msg = "WebappLoader : Resource '" + name + "' not found.";
            }
            else
            {
                msg = exception.getMessage();
            }
            /* convert to a general Velocity ResourceNotFoundException */
            throw new ResourceNotFoundException(msg);
        }

        return result;
    }
    
    /**
     * Defaults to return false.
     */
    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    /**
     * Defaults to return 0
     */
    public long getLastModified(Resource resource)
    {
        return 0;
    }
}

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
 * @version $Id: WebappLoader.java,v 1.7 2004/02/18 20:07:02 nbubna Exp $
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

package org.apache.velocity.tools.config;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.log.Log;

/**
 * Provides support for reading a configuration file from a specified path,
 * This frees the user from having to obtain an InputStream themselves.
 *
 * @author Nathan Bubna
 * @version $Id: XmlFactoryConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public abstract class FileFactoryConfiguration extends FactoryConfiguration
{
    /**
     * <p>Reads an configuration from an {@link InputStream}.</p>
     * 
     * @param input the InputStream to read from
     */
    public abstract void read(InputStream input) throws IOException;

    /**
     * <p>Reads a configuration file from the specified file path
     * and sets up the configuration from that. If the file does not
     * exist, an {@link IllegalArgumentException} will be thrown.</p>
     *
     * @param path the path to the file to be read from
     */
    public void read(String path)
    {
        read(path, true);
    }

    public void read(String path, boolean required)
    {
        read(path, required, null);
    }

    public void read(String path, boolean required, Log log)
    {
        if (path == null)
        {
            throw new NullPointerException("Path value cannot be null");
        }
        if (log != null && log.isTraceEnabled())
        {
            log.trace("Attempting to read configuration file at: "+path);
        }

        // ok, try to load the file
        InputStream inputStream = null;
        try
        {
            // first, try the file system
            File file = new File(path);
            if (file.exists())
            {
                try
                {
                    inputStream = new FileInputStream(file);
                }
                catch (FileNotFoundException fnfe)
                {
                    // we should not be able to get here
                    // since we already checked whether the file exists
                    throw new IllegalStateException(fnfe);
                }
            }
            else // try the classpath
            {
                inputStream = getClass().getResourceAsStream(path);
            }

            if (inputStream == null)
            {
                String msg = "Could not find configuration file at: "+path;
                if (log != null)
                {
                    log.debug(msg);
                }
                if (required)
                {
                    throw new ResourceNotFoundException(msg);
                }
            }
            else
            {
                read(inputStream);
            }
        }
        catch (IOException ioe)
        {
            String msg = "InputStream could not be read from file at: "+path;
            if (log != null)
            {
                log.debug(msg, ioe);
            }
            if (required)
            {
                throw new RuntimeException(msg, ioe);
            }
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                if (log != null)
                {
                    log.error("Failed to close input stream for "+path, ioe);
                }
            }
        }
    }

}

package org.apache.velocity.tools.generic;

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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.ClassUtils;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.InvalidScope;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>Provides methods to import arbitrary local or remote resources as strings, generic version.</p>
 * <p>Based on ImportSupport from the JSTL taglib by Shawn Bayern</p>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $$
 */
@InvalidScope({Scope.APPLICATION, Scope.SESSION, Scope.REQUEST}) /* this tool is not meant to be used directly*/
public class ImportSupport extends SafeConfig
{
    protected static final String VALID_SCHEME_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

    /** Configuration key for XmlTool and JsonTool, used to specify a local resource
     */
    public static final String RESOURCE_KEY = "resource";

    /** Configuration key for ImportTool, XmlTool and JsonTool, used to specify a local or remote source URL
     */
    public static final String URL_KEY = "url";

    //*********************************************************************
    // URL importation logic

    /*
     * Overall strategy:  we have two entry points, acquireString() and
     * acquireReader().  The latter passes data through unbuffered if
     * possible (but note that it is not always possible -- specifically
     * for cases where we must use the RequestDispatcher.  The remaining
     * methods handle the common.core logic of loading either a URL or a local
     * resource.
     *
     * We consider the 'natural' form of remote URLs to be Readers and
     * local URLs to be Strings.  Thus, to avoid doing extra work,
     * acquireString() and acquireReader() delegate to one another as
     * appropriate.  (Perhaps I could have spelled things out more clearly,
     * but I thought this implementation was instructive, not to mention
     * somewhat cute...)
     *
     * CB: Changes with VelocityTools 3.0 implementation: ImportSupport is now splitted in two classes,
     * o.a.v.tools.generic.ImportSupport and o.a.v.tools.view.ViewImportSupport inheriting the former one.
     * In the generic version, only remote urls are supported, while the view version will work as aforementioned.
     */

    /**
     * Configure import support
     * @param values configuration values
     */
    protected void configure(ValueParser values)
    {
        super.configure(values);
    }

    /**
     * Sets or clears safe mode
     * @param safe flag value
     */
    @Override
    public void setSafeMode(boolean safe)
    {
        super.setSafeMode(safe);
    }

    /**
     *
     * @param url the URL resource to return as string
     * @return the URL resource as string
     * @throws IOException if operation failed
     */
    public String acquireString(String url) throws IOException
    {
        getLog().debug("acquire URL {}", url);
        if (isRemoteURL(url))
        {
            return acquireRemoteURLString(url);
        }
        else
        {
            return acquireLocalURLString(url);
        }
    }

    /**
     * Aquire the content of a remote URL.
     * @param url remote URL
     * @return the URL resource as string
     * @throws IOException if operation failed
     */
    protected String acquireRemoteURLString(String url) throws IOException
    {
        // delegate to our peer
        BufferedReader r = null;
        try
        {
            r = new BufferedReader(acquireRemoteURLReader(url));
            StringBuilder sb = new StringBuilder();
            int i;
            // under JIT, testing seems to show this simple loop is as fast
            // as any of the alternatives
            while ((i = r.read()) != -1)
            {
                sb.append((char)i);
            }
            return sb.toString();
        }
        finally
        {
            if(r != null)
            {
                try
                {
                    r.close();
                }
                catch (IOException ioe)
                {
                    getLog().error("Could not close reader.", ioe);
                }
            }
        }
    }

    /**
     * Aquire the content of a local URL.
     * @param url local URL
     * @return the URL resource as string
     * @throws IOException if operation failed
     */
    protected String acquireLocalURLString(String url) throws IOException
    {
        throw new IOException("Only remote URLs are supported");
    }

    /**
     * Acquire a reader to an URL
     * @param url the URL to read
     * @return a Reader for the InputStream created from the supplied URL
     * @throws IOException if operation failed
     */
    public Reader acquireReader(String url) throws IOException
    {
        getLog().debug("acquire URL {}", url);
        if (isRemoteURL(url))
        {
            return acquireRemoteURLReader(url);
        }
        else
        {
            return acquireLocalURLReader(url);
        }
    }

    /**
     * Acquire a reader to a remote URL
     * @param url the URL to read
     * @return a Reader for the InputStream created from the supplied URL
     * @throws IOException if operation failed
     */
    protected Reader acquireRemoteURLReader(String url) throws  IOException
    {
        // remote URL
        URLConnection uc = null;
        HttpURLConnection huc = null;
        InputStream i = null;

        try
        {
            // handle remote URLs ourselves, using java.net.URL
            URL u = ConversionUtils.toURL(url);
            uc = u.openConnection();
            i = uc.getInputStream();

            // check response code for HTTP URLs, per spec,
            if (uc instanceof HttpURLConnection)
            {
                huc = (HttpURLConnection)uc;

                int status = huc.getResponseCode();
                if (status < 200 || status > 299)
                {
                    throw new IOException(status + " " + url);
                }
            }

            // okay, we've got a stream; encode it appropriately
            Reader r = null;
            String charSet;

            // charSet extracted according to RFC 2045, section 5.1
            String contentType = uc.getContentType();
            if (contentType != null)
            {
                charSet = ImportSupport.getContentTypeAttribute(contentType, "charset");
                if (charSet == null)
                {
                    charSet = RuntimeConstants.ENCODING_DEFAULT;
                }
            }
            else
            {
                charSet = RuntimeConstants.ENCODING_DEFAULT;
            }

            try
            {
                r = new InputStreamReader(i, charSet);
            }
            catch (UnsupportedEncodingException ueex)
            {
                r = new InputStreamReader(i, RuntimeConstants.ENCODING_DEFAULT);
            }

            if (huc == null)
            {
                return r;
            }
            else
            {
                return new SafeClosingHttpURLConnectionReader(r, huc);
            }
        }
        catch (IOException ex)
        {
            if (i != null)
            {
                try
                {
                    i.close();
                }
                catch (IOException ioe)
                {
                    getLog().error("Could not close InputStream", ioe);
                }
            }

            if (huc != null)
            {
                huc.disconnect();
            }
            throw new IOException("Problem accessing the remote URL \""
                + url + "\". " + ex);
        }
        catch (RuntimeException ex)
        {
            if (i != null)
            {
                try
                {
                    i.close();
                }
                catch (IOException ioe)
                {
                    getLog().error("Could not close InputStream", ioe);
                }
            }

            if (huc != null)
            {
                huc.disconnect();
            }
            // because the spec makes us
            throw new IOException("Problem accessing the remote URL \"" + url + "\" :" + ex.getMessage(), ex);
        }
    }

    /**
     * Acquire a reader to a local URL - non applicable to the generic version of ImportSupport
     * @param url the URL to read
     * @return a Reader for the InputStream created from the supplied URL
     * @throws IOException if operation failed
     */
    protected Reader acquireLocalURLReader(String url) throws  IOException
    {
        throw new IOException("Only remote URLs are supported");
    }

    protected static class SafeClosingHttpURLConnectionReader extends Reader
    {
        private final HttpURLConnection huc;
        private final Reader wrappedReader;

        SafeClosingHttpURLConnectionReader(Reader r, HttpURLConnection huc)
        {
            this.wrappedReader = r;
            this.huc = huc;
        }

        public void close() throws IOException
        {
            if(null != huc)
            {
                huc.disconnect();
            }

            wrappedReader.close();
        }

        // Pass-through methods.
        public void mark(int readAheadLimit) throws IOException
        {
            wrappedReader.mark(readAheadLimit);
        }

        public boolean markSupported()
        {
            return wrappedReader.markSupported();
        }

        public int read() throws IOException
        {
            return wrappedReader.read();
        }

        public int read(char[] buf) throws IOException
        {
            return wrappedReader.read(buf);
        }

        public int read(char[] buf, int off, int len) throws IOException
        {
            return wrappedReader.read(buf, off, len);
        }

        public boolean ready() throws IOException
        {
            return wrappedReader.ready();
        }

        public void reset() throws IOException
        {
            wrappedReader.reset();
        }

        public long skip(long n) throws IOException
        {
            return wrappedReader.skip(n);
        }
    }

    //*********************************************************************
    // Public utility methods

    /**
     * Returns whether an URL is remote or local
     *
     * @param url the url to check out
     * @return wether the URL is remote
     */
    public static boolean isRemoteURL(String url)
    {
        return getProtocol(url) != null;
    }

    /**
     * Returns protocol, or null for a local URL
     *
     * @param url the url to check out
     * @return found protocol or null for a local URL
     */
    public static String getProtocol(String url)
    {
        // a null URL is not remote, by our definition
        if (url == null)
        {
            return null;
        }

        // do a fast, simple check first
        int colonPos;
        if ((colonPos = url.indexOf(':')) == -1)
        {
            return null;
        }

        // if we DO have a colon, make sure that every character
        // leading up to it is a valid scheme character
        for (int i = 0; i < colonPos; i++)
        {
            if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1)
            {
                return null;
            }
        }
        // if so, we've got a remote url
        return url.substring(0, colonPos);
    }

    /**
     * Get the value associated with a content-type attribute.
     * Syntax defined in RFC 2045, section 5.1.
     *
     * @param input the string containing the attributes
     * @param name the name of the content-type attribute
     * @return the value associated with a content-type attribute
     */
    public static String getContentTypeAttribute(String input, String name)
    {
        int begin;
        int end;
        int index = input.toUpperCase().indexOf(name.toUpperCase());
        if (index == -1)
        {
            return null;
        }
        index = index + name.length(); // positioned after the attribute name
        index = input.indexOf('=', index); // positioned at the '='
        if (index == -1)
        {
            return null;
        }
        index += 1; // positioned after the '='
        input = input.substring(index).trim();

        if (input.charAt(0) == '"')
        {
            // attribute value is a quoted string
            begin = 1;
            end = input.indexOf('"', begin);
            if (end == -1)
            {
                return null;
            }
        }
        else
        {
            begin = 0;
            end = input.indexOf(';');
            if (end == -1)
            {
                end = input.indexOf(' ');
            }
            if (end == -1)
            {
                end = input.length();
            }
        }
        return input.substring(begin, end).trim();
    }

    //*********************************************************************
    // Fetch local resource

    /**
     * Fetch a local resource, first trying with a file (or a webapp resource for the view flavor)
     * then with a classpath entry.
     * @param resource the resource to read
     * @return the content of the resource
     */
    public String getResourceString(String resource)
    {
        String ret = null;
        try
        {
            Reader rawReader = getResourceReader(resource);
            if (rawReader != null)
            {
                BufferedReader reader = new BufferedReader(rawReader);
                StringBuilder sb = new StringBuilder();
                int i;
                // under JIT, testing seems to show this simple loop is as fast
                // as any of the alternatives
                while ((i = reader.read()) != -1)
                {
                    sb.append((char) i);
                }
                ret = sb.toString();
            }
        }
        catch (IOException ioe)
        {
            getLog().error("could not load resource {}", resource, ioe);
        }
        return ret;
    }

    /**
     * Get a reader of a local resource, first trying with a file (or a webapp resource for the view flavor)
     * then with a classpath entry.
     * @param resource the resource to read
     * @return a reader of the resource
     */
    public Reader getResourceReader(String resource)
    {
        getLog().debug("get resource {}", resource);
        URL url = null;
        Reader reader = null;
        try
        {
            url = getFileResource(resource);
            if (url == null)
            {
                url = getClasspathResource(resource);
            }
            if (url != null)
            {
                URLConnection uc = url.openConnection();
                InputStream is = uc.getInputStream();
                String charSet;
                // charSet extracted according to RFC 2045, section 5.1
                String contentType = uc.getContentType();
                if (contentType != null)
                {
                    charSet = ImportSupport.getContentTypeAttribute(contentType, "charset");
                    if (charSet == null)
                    {
                        charSet = RuntimeConstants.ENCODING_DEFAULT;
                    }
                }
                else
                {
                    charSet = RuntimeConstants.ENCODING_DEFAULT;
                }
                reader = new InputStreamReader(is, charSet);
            }
        }
        catch (Exception e)
        {
            getLog().error("could not get resource {}", resource, e);
        }
        return reader;
    }

    /**
     * Overridable local file URL builder.
     * @param resource the resource to read
     * @return the content of the resource
     * @throws Exception if operation failed
     */
    protected URL getFileResource(String resource) throws Exception
    {
        URL url = null;
        File file = new File(resource);
        if (file.exists() && file.isFile() && file.canRead())
        {
            url = file.toURI().toURL();
        }
        return url;
    }

    /**
     * Classpath entry URL builder
     * @param resource the resource to read
     * @return the content of the resource
     * @throws Exception if operation failed
     */
    protected URL getClasspathResource(String resource) throws Exception
    {
        return ClassUtils.getResource(resource, this);
    }
}

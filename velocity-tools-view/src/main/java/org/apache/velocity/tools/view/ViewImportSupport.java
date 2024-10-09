package org.apache.velocity.tools.view;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.generic.ImportSupport;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * <p>Provides methods to import arbitrary local or remote resources as strings.</p>
 * <p>Based on ImportSupport from the JSTL taglib by Shawn Bayern</p>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @author Claude Brisson
 * @since VelocityTools 3.0
 * @version $Revision$ $Date$
 */
public class ViewImportSupport extends ImportSupport
{
    protected ServletContext application;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    // --------------------------------------- Setup Methods -------------

    protected void configure(ValueParser values)
    {
        super.configure(values);
        HttpServletRequest request = (HttpServletRequest)values.get(ViewContext.REQUEST);
        if (request != null)
        {
            setRequest(request);
        }
        HttpServletResponse response = (HttpServletResponse)values.get(ViewContext.RESPONSE);
        if (response != null)
        {
            setResponse(response);
        }
        ServletContext servletContext = (ServletContext)values.get(ViewContext.SERVLET_CONTEXT_KEY);
        if (servletContext != null)
        {
            setServletContext(servletContext);
        }
    }

    /**
     * Sets the current {@link HttpServletRequest}. This is required
     * for this tool to operate and will throw a NullPointerException
     * if this is not set or is set to {@code null}.
     * @param request servlet request
     */
    public void setRequest(HttpServletRequest request)
    {
        if (request == null)
        {
            throw new NullPointerException("request should not be null");
        }
        this.request = request;
    }

    /**
     * Sets the current {@link HttpServletResponse}. This is required
     * for this tool to operate and will throw a NullPointerException
     * if this is not set or is set to {@code null}.
     * @param response servlet response
     */
    public void setResponse(HttpServletResponse response)
    {
        if (response == null)
        {
            throw new NullPointerException("response should not be null");
        }
        this.response = response;
    }

    /**
     * Sets the {@link ServletContext}. This is required
     * for this tool to operate and will throw a NullPointerException
     * if this is not set or is set to {@code null}.
     * @param application servlet context
     */
    public void setServletContext(ServletContext application)
    {
        if (application == null)
        {
            throw new NullPointerException("servlet context should not be null");
        }
        this.application = application;
    }

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
     */

    /**
     *
     * @param url the remote URL resource to return as string
     * @return the URL resource as string
     * @throws IOException if thrown by underlying code
     */
    protected String acquireRemoteURLString(String url) throws IOException
    {
        if (isSafeMode())
        {
            getLog().warn("safe mode prevented reading resource from remote url: {} ", url);
            return null;
        }
        return super.acquireRemoteURLString(url);
    }

    /**
     *
     * @param url the local URL resource to return as string
     * @return the URL resource as string
     * @throws IOException if not allowed or if thrown by underlying code
     */
    protected String acquireLocalURLString(String url) throws IOException
    {
        // URL is local, so we must be an HTTP request
        if (!(request instanceof HttpServletRequest
            && response instanceof HttpServletResponse))
        {
            throw new IOException("Local import from non-HTTP request not allowed");
        }

        // retrieve an appropriate ServletContext
        // normalize the URL if we have an HttpServletRequest
        if (!url.startsWith("/"))
        {
            String sp = ((HttpServletRequest)request).getServletPath();
            url = sp.substring(0, sp.lastIndexOf('/')) + '/' + url;
        }

        // strip the session id from the url
        url = stripSession(url);

        // According to the 3.1 Servlet API specification, the query string parameters of the URL to include
        // take *precedence* over the original query string parameters. It means that:
        // - we must merge both query strings
        // - we must set aside the cached request toolbox during the include
        url = mergeQueryStrings(url);
        Object parentToolbox = request.getAttribute(Toolbox.KEY);
        request.removeAttribute(Toolbox.KEY);

        // from this context, get a dispatcher
        RequestDispatcher rd = application.getRequestDispatcher(url);
        if (rd == null)
        {
            throw new IOException("Couldn't get a RequestDispatcher for \""
                + url + "\"");
        }

        // include the resource, using our custom wrapper
        ImportResponseWrapper irw =
            new ImportResponseWrapper((HttpServletResponse)response);
        try
        {
            rd.include(request, irw);
        }
        catch (IOException ex)
        {
            throw new IOException("Problem importing the local URL \"" + url + "\": " + ex.getMessage(), ex);
        }
        catch (ServletException se)
        {
            throw new IOException("Problem importing the local URL \"" + url + "\": " + se.getMessage(), se);

        }
        finally
        {
            request.setAttribute(Toolbox.KEY, parentToolbox);
        }
        /* let RuntimeExceptions go through */

        // disallow inappropriate response codes per JSTL spec
        if (irw.getStatus() < 200 || irw.getStatus() > 299)
        {
            throw new IOException("Invalid response code '" + irw.getStatus()
                + "' for \"" + url + "\"");
        }

        // recover the response String from our wrapper
        return irw.getString();
    }

    /**
     *
     * @param url the URL to read
     * @return a Reader for the InputStream created from the supplied URL
     * @throws IOException if not allowed or thrown by underlying code
     */
    protected Reader acquireRemoteURLReader(String url) throws IOException
    {
        if (isSafeMode())
        {
            getLog().warn("safe mode prevented reading resource from remote url: {}", url);
            return null;
        }
        return super.acquireRemoteURLReader(url);
    }

    /**
     *
     * @param url the URL to read
     * @return a Reader for the InputStream created from the supplied URL
     * @throws IOException if thrown by underlying code
     */
    protected Reader acquireLocalURLReader(String url) throws IOException
    {
        return new StringReader(acquireLocalURLString(url));
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


    /** Wraps responses to allow us to retrieve results as Strings. */
    protected static class ImportResponseWrapper extends HttpServletResponseWrapper
    {
        /*
         * We provide either a Writer or an OutputStream as requested.
         * We actually have a true Writer and an OutputStream backing
         * both, since we don't want to use a character encoding both
         * ways (Writer -> OutputStream -> Writer).  So we use no
         * encoding at all (as none is relevant) when the target resource
         * uses a Writer.  And we decode the OutputStream's bytes
         * using OUR tag's 'charEncoding' attribute, or ISO-8859-1
         * as the default.  We thus ignore setLocale() and setContentType()
         * in this wrapper.
         *
         * In other words, the target's asserted encoding is used
         * to convert from a Writer to an OutputStream, which is typically
         * the medium through with the target will communicate its
         * ultimate response.  Since we short-circuit that mechanism
         * and read the target's characters directly if they're offered
         * as such, we simply ignore the target's encoding assertion.
         */

        /** The Writer we convey. */
        private StringWriter sw;

        /** A buffer, alternatively, to accumulate bytes. */
        private ByteArrayOutputStream bos;

        /** 'True' if getWriter() was called; false otherwise. */
        private boolean isWriterUsed;

        /** 'True if getOutputStream() was called; false otherwise. */
        private boolean isStreamUsed;

        /** The HTTP status set by the target. */
        private int status = 200;

        //************************************************************
        // Constructor and methods

        /**
         * Constructs a new ImportResponseWrapper.
         * @param response the response to wrap
         */
        public ImportResponseWrapper(HttpServletResponse response)
        {
            super(response);
        }

        /**
         * @return a Writer designed to buffer the output.
         */
        public PrintWriter getWriter()
        {
            if (isStreamUsed)
            {
                throw new IllegalStateException("Unexpected internal error during import: "
                                                + "Target servlet called getWriter(), then getOutputStream()");
            }
            isWriterUsed = true;
            if (sw == null)
            {
                sw = new StringWriter();
            }
            return new PrintWriter(sw);
        }

        /**
         * @return a ServletOutputStream designed to buffer the output.
         */
        public ServletOutputStream getOutputStream()
        {
            if (isWriterUsed)
            {
                throw new IllegalStateException("Unexpected internal error during import: "
                                                + "Target servlet called getOutputStream(), then getWriter()");
            }
            isStreamUsed = true;
            if (bos == null)
            {
                bos = new ByteArrayOutputStream();
            }
            ServletOutputStream sos = new ServletOutputStream()
                {
                    @Override
                    public void write(int b) throws IOException
                    {
                        bos.write(b);
                    }

                    @Override
                    public boolean isReady()
                    {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener)
                    {
                        // nop
                    }
                };
            return sos;
        }

        /** Has no effect.
         * @param x ignored
         */
        public void setContentType(String x)
        {
            // ignore
        }

        /** Has no effect.
         * @param x ignored
         */
        public void setLocale(Locale x)
        {
            // ignore
        }

        /**
         * Sets the status of the response
         * @param status the status code
         */
        public void setStatus(int status)
        {
            this.status = status;
        }

        /**
         * @return the status of the response
         */
        public int getStatus()
        {
            return status;
        }

        /**
         * Retrieves the buffered output, using the containing tag's
         * 'charEncoding' attribute, or the tag's default encoding,
         * <b>if necessary</b>.
         * @return the buffered output
         * @throws UnsupportedEncodingException if the encoding is not supported
         */
        public String getString() throws UnsupportedEncodingException
        {
            if (isWriterUsed)
            {
                return sw.toString();
            }
            else if (isStreamUsed)
            {
                return bos.toString(this.getCharacterEncoding());
            }
            else
            {
                return ""; // target didn't write anything
            }
        }
    }

    //*********************************************************************
    // Public utility methods

    /**
     * Strips a servlet session ID from <tt>url</tt>.  The session ID
     * is encoded as a URL "path parameter" beginning with "jsessionid=".
     * We thus remove anything we find between ";jsessionid=" (inclusive)
     * and either EOS or a subsequent ';' (exclusive).
     *
     * @param url the url to strip the session id from
     * @return the stripped url
     */
    public static String stripSession(String url)
    {
        StringBuilder u = new StringBuilder(url);
        int sessionStart;
        while ((sessionStart = u.toString().indexOf(";jsessionid=")) != -1)
        {
            int sessionEnd = u.toString().indexOf(";", sessionStart + 1);
            if (sessionEnd == -1)
            {
                sessionEnd = u.toString().indexOf("?", sessionStart + 1);
            }
            if (sessionEnd == -1)
            {
                // still
                sessionEnd = u.length();
            }
            u.delete(sessionStart, sessionEnd);
        }
        return u.toString();
    }

    //*********************************************************************
    // Merge query strings

    /**
     * Merge original parameters into the query string
     *
     * @param url the url to include
     * @return the merged url
     */
    protected String mergeQueryStrings(String url)
    {
        Map<String, String[]> originalParameters = request.getParameterMap();
        if (originalParameters.size() > 0)
        {
            StringBuilder builder = new StringBuilder(url);
            Set<String> newParameterNames = new HashSet<String>();
            int qm = url.indexOf('?');
            if (qm != -1)
            {

                String[] newParameters = url.substring(qm + 1).split("&");
                for (String newParam : newParameters)
                {
                    int eq = newParam.indexOf('=');
                    if (eq != -1)
                    {
                        newParam = newParam.substring(eq);
                    }
                    newParameterNames.add(newParam);
                }
            }
            char separator = ( qm == -1 ? '?' : '&' );
            for (Map.Entry<String, String[]> entry : originalParameters.entrySet())
            {
                String key = entry.getKey();
                if (!newParameterNames.contains(key))
                {
                    key = URLEncoder.encode(key);
                    for (String value : entry.getValue())
                    {
                        builder.append(separator);
                        separator = '&';
                        builder.append(key).append('=').append(URLEncoder.encode(value));
                    }
                }
            }
            url = builder.toString();
        }
        return url;
    }

    //*********************************************************************
    // Fetch local resource

    @Override
    protected URL getFileResource(String resource) throws Exception
    {
        return application.getResource(resource);
    }


}

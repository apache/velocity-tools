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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.tools.LinkTool;
import org.apache.velocity.tools.struts.StrutsUtils;

import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.SecureActionConfig;
import org.apache.struts.action.SecurePlugIn;
import org.apache.struts.Globals;

/**
 * <p>Tool to be able to use Struts SSL Extensions with Velocity</p>
 * <p>It has the same interface as StrutsLinkTool and can function as a substitute if Struts 1.1 and SSL Ext are installed. </p>
 * <p>Usage:
 * <pre>
 * Template example:
 * &lt;!-- Use just like a regular StrutsLinkTool --&gt;
 * $link.setAction("nameOfAction")
 * $link.setForward("nameOfForward")
 *
 * If the action or forward is marked as secure, or not, 
 * in your struts-config then the link will be rendered
 * with https or http accordingly.
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;link&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.SecureLinkTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre>
 * </p>
 * @since VelocityTools 1.1
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @version $Revision: 1.6 $ $Date: 2003/11/06 00:26:54 $
 */
public class SecureLinkTool extends LinkTool
{

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String STD_HTTP_PORT = "80";
    private static final String STD_HTTPS_PORT = "443";


    /**
     * <p>Returns a copy of the link with the given action name
     * converted into a server-relative URI reference. This method
     * does not check if the specified action really is defined.
     * This method will overwrite any previous URI reference settings
     * but will copy the query string.</p>
     *
     * @param action an action path as defined in struts-config.xml
     *
     * @return a new instance of StrutsLinkTool
     */
    public SecureLinkTool setAction(String action)
    {
        String link = StrutsUtils.getActionMappingURL(application, request, action);
        return (SecureLinkTool)copyWith(computeURL(request, application, link));
    }

    /**
     * <p>Returns a copy of the link with the given global forward name
     * converted into a server-relative URI reference. If the parameter
     * does not map to an existing global forward name, <code>null</code>
     * is returned. This method will overwrite any previous URI reference
     * settings but will copy the query string.</p>
     *
     * @param forward a global forward name as defined in struts-config.xml
     *
     * @return a new instance of StrutsLinkTool
     */
    public SecureLinkTool setForward(String forward)
    {
        String url = StrutsUtils.getForwardURL(request, application, forward);
        if (url == null)
        {
            return null;
        }
        return (SecureLinkTool)copyWith(computeURL(request, application, url));
    }

    public String computeURL(HttpServletRequest request,
                             ServletContext app, String link)
    {
        StringBuffer url = new StringBuffer(link);

        String contextPath = request.getContextPath();

        if (SecurePlugIn.getAppSslExtEnable(app) &&
            url.toString().startsWith(contextPath))
        {
            // Initialize the scheme and ports we are using
            String usingScheme = request.getScheme();
            String usingPort = String.valueOf(request.getServerPort());

            // Get the servlet context relative link URL
            String linkString = url.toString().substring(contextPath.length());

            // See if link references an action somewhere in our app
            SecureActionConfig secureConfig = getActionConfig(request, app, linkString);

            // If link is an action, find the desired port and scheme
            if (secureConfig != null &&
                !SecureActionConfig.ANY.equalsIgnoreCase(secureConfig.getSecure()))
            {
                String desiredScheme = Boolean.valueOf(secureConfig.getSecure()).booleanValue() ?
                    HTTPS : HTTP;
                String desiredPort = Boolean.valueOf(secureConfig.getSecure()).booleanValue() ?
                    SecurePlugIn.getAppHttpsPort(app) : SecurePlugIn.getAppHttpPort(app);

                // If scheme and port we are using do not match the ones we want
                if (!desiredScheme.equals(usingScheme) ||
                    !desiredPort.equals(usingPort))
                {
                    url.insert(0, startNewUrlString(request, desiredScheme, desiredPort));

                    // This is a hack to help us overcome the problem that some
                    // older browsers do not share sessions between http & https
                    if (url.toString().indexOf(";jsessionid=") < 0)
                    {
                        // Add the session identifier
                        url = new StringBuffer(toEncoded(url.toString(),
                                               request.getSession().getId()));
                    }
                }
            }
        }
        return url.toString();
    }

    /**
     * Finds the configuration definition for the specified action link
     *
     * @param request the current request.
     * @param app the current ServletContext.
     * @param linkString The action we are searching for, specified as a
     *        link. (i.e. may include "..")
     * @return The SecureActionConfig object entry for this action,
     *         or null if not found
     */
    private static SecureActionConfig getActionConfig(HttpServletRequest request,
                                                      ServletContext app,
                                                      String linkString)
    {
        ModuleConfig moduleConfig = StrutsUtils.selectModule(linkString, app);

        // Strip off the subapp path, if any
        linkString = linkString.substring(moduleConfig.getPrefix().length());

        // Use our servlet mapping, if one is specified
        String servletMapping = (String)app.getAttribute(Globals.SERVLET_KEY);

        int starIndex = (servletMapping != null) ? servletMapping.indexOf('*') : -1;
        if (starIndex == -1)
        {
            // No servlet mapping or no usable pattern defined, short circuit
            return null;
        }

        String prefix = servletMapping.substring(0, starIndex);
        String suffix = servletMapping.substring(starIndex + 1);

        // Strip off the jsessionid, if any
        int jsession = linkString.indexOf(";jsessionid=");
        if (jsession >= 0)
        {
            linkString = linkString.substring(0, jsession);
        }

        // Strip off the anchor, if any
        int anchor = linkString.indexOf("#");
        if (anchor >= 0)
        {
            linkString = linkString.substring(0, anchor);
        }

        // Strip off the query string, if any
        int question = linkString.indexOf("?");
        if (question >= 0)
        {
            linkString = linkString.substring(0, question);
        }

        // Unable to establish this link as an action, short circuit
        if (!(linkString.startsWith(prefix) &&
              linkString.endsWith(suffix)))
        {
            return null;
        }

        // Chop off prefix and suffix
        linkString = linkString.substring(prefix.length());
        linkString = linkString.substring(0, linkString.length() - suffix.length());
        if (!linkString.startsWith("/"))
        {
            linkString = "/" + linkString;
        }

        SecureActionConfig secureConfig =
            (SecureActionConfig)moduleConfig.findActionConfig(linkString);

        return secureConfig;
    }

    /**
     * Builds the protocol, server name, and port portion of the new URL
     * @param request The current request
     * @param desiredScheme  The scheme (http or https) to be used in the new URL
     * @param desiredPort The port number to be used in th enew URL
     * @return The new URL as a StringBuffer
     */
    private static StringBuffer startNewUrlString(HttpServletRequest request,
                                                  String desiredScheme,
                                                  String desiredPort)
    {
        StringBuffer url = new StringBuffer();
        String serverName = request.getServerName();
        url.append(desiredScheme).append("://").append(serverName);

        if ((HTTP.equals(desiredScheme) && !STD_HTTP_PORT.equals(desiredPort)) ||
            (HTTPS.equals(desiredScheme) && !STD_HTTPS_PORT.equals(desiredPort)))
        {
            url.append(":").append(desiredPort);
        }
        return url;
    }

    /**
     * Return the specified URL with the specified session identifier
     * suitably encoded.
     *
     * @param url URL to be encoded with the session id
     * @param sessionId Session id to be included in the encoded URL
     * @return the specified URL with the specified session identifier suitably encoded
     */
    public String toEncoded(String url, String sessionId)
    {
        if (url == null || sessionId == null)
        {
            return (url);
        }

        String path = url;
        String query = "";
        String anchor = "";
        int pound = url.indexOf('#');
        if (pound >= 0)
        {
            path = url.substring(0, pound);
            anchor = url.substring(pound);
        }
        int question = path.indexOf('?');
        if (question >= 0)
        {
            query = path.substring(question);
            path = path.substring(0, question);
        }
        StringBuffer sb = new StringBuffer(path);
        // jsessionid can't be first.
        if (sb.length() > 0)
        {
            sb.append(";jsessionid=");
            sb.append(sessionId);
        }
        sb.append(query);
        sb.append(anchor);
        return sb.toString();
    }

}

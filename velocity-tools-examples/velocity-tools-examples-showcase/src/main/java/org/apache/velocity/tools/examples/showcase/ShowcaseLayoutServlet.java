package org.apache.velocity.tools.examples.showcase;

import org.apache.velocity.tools.view.VelocityLayoutServlet;

import jakarta.servlet.http.HttpServletRequest;

public class ShowcaseLayoutServlet extends VelocityLayoutServlet
{
    private static final long serialVersionUID = -8743195198276764930L;

    /**
     * Searches for a non-default layout to be used for this request.
     * This implementation checks the request parameters and attributes.
     */
    protected String findLayout(HttpServletRequest request)
    {
        // check if an alternate layout has been specified
        // by way of the request parameters
        String layout = request.getParameter(KEY_LAYOUT);
        return layout == null ? super.findLayout(request) : layout;
    }

}

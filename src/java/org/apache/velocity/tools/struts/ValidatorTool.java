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
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.ValidatorUtil;
import org.apache.commons.validator.Var;

import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.validator.Resources;
import org.apache.struts.validator.ValidatorPlugIn;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * <p>Title: ValidatorTool</p>
 * <p>Description: View tool to work with the Struts Validator to
 *    produce client side javascript validation.</p>
 * <p>Usage:
 * <pre>
 * Template example:
 *
 * <!-- javascript form validation -->
 * #set ($foo = $validator.setFormName("nameofyourform"))
 * $validator.javascript <-- spits out the dynamic javascript
 *
 * or simply
 *
 * $validator.getJavascript("nameOfYourForm")
 *
 * Toolbox configuration:
 *
 * <tool>
 *   <key>validator</key>
 *   <scope>request</scope>
 *   <class>package org.apache.velocity.tools.struts.ValidatorTool</class>
 * </tool>
 * </pre>
 *
 * <p>This is an adaptation of the JavascriptValidator Tag
 * from the Struts 1.1 validator library.</p>
 *
 * @author David Winterfeldt
 * @author David Graham
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/14 22:08:57 $
 */
public class ValidatorTool implements ViewTool {

    /** A reference to the ViewContext */
    protected ViewContext context;

    /** A reference to the ServletContext */
    protected ServletContext app;

    /** A reference to the HttpServletRequest. */
    protected HttpServletRequest request;

    /** A reference to the HttpSession. */
    protected HttpSession session;


    private static final String HTML_BEGIN_COMMENT = "\n<!-- Begin \n";
    private static final String HTML_END_COMMENT = "//End --> \n";

    private boolean staticJavascript = true;
    private boolean dynamicJavascript = true;
    private boolean htmlComment = true;
    private boolean cdata = true;
    private boolean xhtml = false;
    private String formName = null;
    private String methodName = null;
    private String src = null;
    private int page = 0;


    /**
     * Default constructor. Tool must be initialized before use.
     */
    public ValidatorTool() {}


    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        if (!(obj instanceof ViewContext))
        {
            throw new IllegalArgumentException("Tool can only be initialized with a ViewContext");
        }

        this.context = (ViewContext)obj;
        this.request = context.getRequest();
        this.session = request.getSession(false);
        this.app = context.getServletContext();

        Boolean b = (Boolean)context.getAttribute(ViewContext.XHTML);
        if (b != null)
        {
            this.xhtml = b.booleanValue();
        }
    }


    /****************** get/set accessors ***************/

    /**
     * Gets the key (form name) that will be used
     * to retrieve a set of validation rules to be
     * performed on the bean passed in for validation.
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Sets the key (form name) that will be used
     * to retrieve a set of validation rules to be
     * performed on the bean passed in for validation.
     * Specifying a form name places a
     * <script> </script> tag around the javascript.
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * Gets the current page number of a multi-part form.
     * Only field validations with a matching page numer
     * will be generated that match the current page number.
     * Only valid when the formName attribute is set.
     */
    public int getPage()
    {
        return page;
    }

    /**
     * Sets the current page number of a multi-part form.
     * Only field validations with a matching page numer
     * will be generated that match the current page number.
     * Only valid when the formName attribute is set.
     */
    public void setPage(int page)
    {
        this.page = page;
    }

    /**
     * Gets the method name that will be used for the Javascript
     * validation method name if it has a value.  This overrides
     * the auto-generated method name based on the key (form name)
     * passed in.
     */
    public String getMethod()
    {
        return methodName;
    }

    /**
     * Sets the method name that will be used for the Javascript
     * validation method name if it has a value.  This overrides
     * the auto-generated method name based on the key (form name)
     * passed in.
     */
    public void setMethod(String methodName)
    {
        this.methodName = methodName;
    }

    /**
     * Gets whether or not to generate the static
     * JavaScript.  If this is set to 'true', which
     * is the default, the static JavaScript will be generated.
     */
    public boolean getStaticJavascript()
    {
        return this.staticJavascript;
    }

    /**
     * Sets whether or not to generate the static
     * JavaScript.  If this is set to 'true', which
     * is the default, the static JavaScript will be generated.
     */
    public void setStaticJavascript(boolean staticJavascript)
    {
        this.staticJavascript = staticJavascript;
    }

    /**
     * Gets whether or not to generate the dynamic
     * JavaScript.  If this is set to 'true', which
     * is the default, the dynamic JavaScript will be generated.
     */
    public boolean getDynamicJavascript()
    {
        return this.dynamicJavascript;
    }

    /**
     * Sets whether or not to generate the dynamic
     * JavaScript.  If this is set to 'true', which
     * is the default, the dynamic JavaScript will be generated.
     */
    public void setDynamicJavascript(boolean dynamicJavascript)
    {
        this.dynamicJavascript = dynamicJavascript;
    }

    /**
     * Gets whether or not to delimit the
     * JavaScript with html comments.  If this is set to 'true', which
     * is the default, html comments will surround the JavaScript.
     */
    public boolean getHtmlComment()
    {
        return this.htmlComment;
    }

    /**
     * Sets whether or not to delimit the
     * JavaScript with html comments.  If this is set to 'true', which
     * is the default, html comments will surround the JavaScript.
     */
    public void setHtmlComment(boolean htmlComment)
    {
        this.htmlComment = htmlComment;
    }

    /**
     * Gets the src attribute's value when defining
     * the html script element.
     */
    public String getSrc()
    {
        return src;
    }

    /**
     * Sets the src attribute's value (used to include
     * an external script resource) when defining
     * the html script element. The src attribute is only recognized
     * when the formName attribute is specified.
     */
    public void setSrc(String src)
    {
        this.src = src;
    }

    /**
     * Returns the cdata setting "true" or "false".
     * @return boolean - "true" if JavaScript will be hidden in a CDATA section
     */
    public boolean getCdata()
    {
        return cdata;
    }

    /**
     * Sets the cdata status.
     * @param cdata The cdata to set
     */
    public void setCdata(boolean cdata)
    {
        this.cdata = cdata;
    }


    /****************** methods that aren't just accessors ***************/

    /**
     * Render the JavaScript for to perform validations based on the form name.
     */
    public String getJavascript(String formName) throws Exception
    {
        // set the form name
        setFormName(formName);

        StringBuffer results = new StringBuffer();

        ModuleConfig config = RequestUtils.getModuleConfig(request, app);
        ValidatorResources resources =
            (ValidatorResources)app.getAttribute(ValidatorPlugIn.VALIDATOR_KEY +
                                                 config.getPrefix());

        Locale locale = StrutsUtils.getLocale(request, session);

        Form form = resources.get(locale, formName);
        if (form != null)
        {
            if(this.dynamicJavascript)
            {
                results.append(getDynamicJavascript(resources, locale, form));
            }
            else if (this.staticJavascript)
            {
                results.append(getStartElement());
                if (this.htmlComment)
                {
                    results.append(HTML_BEGIN_COMMENT);
                }
            }
        }

        if (this.staticJavascript)
        {
            results.append(getJavascriptStaticMethods(resources));
        }

        if (form != null && (this.dynamicJavascript || this.staticJavascript))
        {
            results.append(getJavascriptEnd());
        }

        return results.toString();
    }


    /**
     * Generates the dynamic JavaScript for the form.
     */
    protected String getDynamicJavascript(ValidatorResources resources,
                                          Locale locale,
                                          Form form)
    {
        StringBuffer results = new StringBuffer();

        MessageResources messages =
            StrutsUtils.getMessageResources(request, app);

        List actions = createActionList(resources, form);

        String methods = createMethods(actions);
        results.append(getJavascriptBegin(methods));

        for (Iterator i = actions.iterator(); i.hasNext();)
        {
            ValidatorAction va = (ValidatorAction)i.next();
            String jscriptVar = null;
            String functionName = null;

            if (va.getJsFunctionName() != null && va.getJsFunctionName().length() > 0)
            {
                functionName = va.getJsFunctionName();
            }
            else
            {
                functionName = va.getName();
            }

            results.append("    function ");
            results.append(functionName);
            results.append(" () { \n");

            for (Iterator x = form.getFields().iterator(); x.hasNext();)
            {
                Field field = (Field)x.next();

                // Skip indexed fields for now until there is
                // a good way to handle error messages (and the length
                // of the list (could retrieve from scope?))
                if (field.isIndexed()
                    || field.getPage() != page
                    || !field.isDependency(va.getName()))
                {
                    continue;
                }

                String message =
                    Resources.getMessage(messages, locale, va, field);

                if (message == null)
                {
                    message = "";
                }

                jscriptVar = this.getNextVar(jscriptVar);

                results.append("     this.");
                results.append(jscriptVar);
                results.append(" = new Array(\"");
                results.append(field.getKey());
                results.append("\", \"");
                results.append(message);
                results.append("\", ");
                results.append("new Function (\"varName\", \"");

                Map vars = field.getVars();
                // Loop through the field's variables.
                Iterator varsIterator = vars.keySet().iterator();
                while (varsIterator.hasNext())
                {
                    String varName = (String)varsIterator.next();
                    Var var = (Var)vars.get(varName);
                    String varValue = var.getValue();
                    String jsType = var.getJsType();

                    // skip requiredif variables field, fieldIndexed, fieldTest, fieldValue
                    if (varName.startsWith("field"))
                    {
                        continue;
                    }

                    // these are appended no matter what jsType is
                    results.append("this.");
                    results.append(varName);

                    String escapedVarValue =
                        ValidatorUtil.replace(varValue, "\\", "\\\\");

                    if (Var.JSTYPE_INT.equalsIgnoreCase(jsType))
                    {
                        results.append("=");
                        results.append(escapedVarValue);
                        results.append("; ");
                    }
                    else if (Var.JSTYPE_REGEXP.equalsIgnoreCase(jsType))
                    {
                        results.append("=/");
                        results.append(escapedVarValue);
                        results.append("/; ");
                    }
                    else if (Var.JSTYPE_STRING.equalsIgnoreCase(jsType))
                    {
                        results.append("='");
                        results.append(escapedVarValue);
                        results.append("'; ");
                    }
                    // So everyone using the latest format
                    // doesn't need to change their xml files immediately.
                    else if ("mask".equalsIgnoreCase(varName))
                    {
                        results.append("=/");
                        results.append(escapedVarValue);
                        results.append("/; ");
                    }
                    else
                    {
                        results.append("='");
                        results.append(escapedVarValue);
                        results.append("'; ");
                    }
                }
                results.append(" return this[varName];\"));\n");
            }
            results.append("    } \n\n");
        }
        return results.toString();
    }


    /**
     * Creates the JavaScript methods list from the given actions.
     * @param actions A List of ValidatorAction objects.
     * @return JavaScript methods.
     */
    protected String createMethods(List actions)
    {
        String methodOperator = " && ";

        StringBuffer methods = null;
        for (Iterator i = actions.iterator(); i.hasNext();)
        {
            ValidatorAction va = (ValidatorAction)i.next();
            if (methods == null)
            {
                methods = new StringBuffer(va.getMethod());
            }
            else
            {
                methods.append(methodOperator);
                methods.append(va.getMethod());
            }
            methods.append("(form)");
        }
        return methods.toString();
    }


    /**
     * Get List of actions for the given Form.
     * @return A sorted List of ValidatorAction objects.
     */
    protected List createActionList(ValidatorResources resources, Form form)
    {
        List actionMethods = new ArrayList();
        // Get List of actions for this Form
        for (Iterator i = form.getFields().iterator(); i.hasNext();)
        {
            Field field = (Field)i.next();
            for (Iterator x = field.getDependencies().iterator(); x.hasNext();)
            {
                Object o = x.next();
                if (o != null && !actionMethods.contains(o))
                {
                    actionMethods.add(o);
                }
            }
        }

        List actions = new ArrayList();

        // Create list of ValidatorActions based on actionMethods
        for (Iterator i = actionMethods.iterator(); i.hasNext();)
        {
            String depends = (String) i.next();
            ValidatorAction va = resources.getValidatorAction(depends);

            // throw nicer NPE for easier debugging
            if (va == null)
            {
                throw new NullPointerException(
                    "Depends string \"" + depends +
                    "\" was not found in validator-rules.xml.");
            }

            String javascript = va.getJavascript();
            if (javascript != null && javascript.length() > 0)
            {
                actions.add(va);
            }
            else
            {
                i.remove();
            }
        }

        //TODO? make an instance of this class a static member
        Comparator comp = new ValidatorActionComparator();
        Collections.sort(actions, comp);
        return actions;
    }


    /**
     * Returns the opening script element and some initial javascript.
     */
    protected String getJavascriptBegin(String methods)
    {
        StringBuffer sb = new StringBuffer();
        String name = formName.substring(0, 1).toUpperCase() +
                      formName.substring(1, formName.length());

        sb.append(getStartElement());

        if (this.xhtml && this.cdata)
        {
            sb.append("<![CDATA[\r\n");
        }

        if (!this.xhtml && this.htmlComment)
        {
            sb.append(HTML_BEGIN_COMMENT);
        }
        sb.append("\n     var bCancel = false; \n\n");

        if (methodName == null || methodName.length() == 0)
        {
            sb.append("    function validate");
            sb.append(name);
        }
        else
        {
            sb.append("    function ");
            sb.append(methodName);
        }
        sb.append("(form) {");
        //FIXME? anyone know why all these spaces need to be here?
        sb.append("                                                                   \n");
        sb.append("        if (bCancel) \n");
        sb.append("      return true; \n");
        sb.append("        else \n");

        // Always return true if there aren't any Javascript validation methods
        if (methods == null || methods.length() == 0)
        {
            sb.append("       return true; \n");
        }
        else
        {
            sb.append("       return ");
            sb.append(methods);
            sb.append("; \n");
        }
        sb.append("   } \n\n");

        return sb.toString();
    }


    protected String getJavascriptStaticMethods(ValidatorResources resources)
    {
        StringBuffer sb = new StringBuffer("\n\n");

        Iterator actions = resources.getValidatorActions().values().iterator();
        while (actions.hasNext())
        {
            ValidatorAction va = (ValidatorAction) actions.next();
            if (va != null)
            {
                String javascript = va.getJavascript();
                if (javascript != null && javascript.length() > 0)
                {
                    sb.append(javascript + "\n");
                }
            }
        }
        return sb.toString();
    }


    /**
     * Returns the closing script element.
     */
    protected String getJavascriptEnd()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");

        if (!this.xhtml && this.htmlComment)
        {
            sb.append(HTML_END_COMMENT);
        }

        if (this.xhtml && this.cdata)
        {
            sb.append("]]>\r\n");
        }
        sb.append("</script>\n\n");

        return sb.toString();
    }


    /**
     * The value <code>null</code> will be returned at the end of the sequence.
     *     ex: "zz" will return <code>null</code>
     */
    private String getNextVar(String input)
    {
        if (input == null)
        {
            return "aa";
        }

        input = input.toLowerCase();

        for (int i = input.length(); i > 0; i--)
        {
            int pos = i - 1;

            char c = input.charAt(pos);
            c++;

            if (c <= 'z')
            {
                if (i == 0)
                {
                    return c + input.substring(pos, input.length());
                }
                else if (i == input.length())
                {
                    return input.substring(0, pos) + c;
                }
                else
                {
                    return input.substring(0, pos) + c +
                           input.substring(pos, input.length() - 1);
                }
            }
            else
            {
                input = replaceChar(input, pos, 'a');
            }
        }
        return null;
     }


    /**
     * Replaces a single character in a <code>String</code>
     */
    private String replaceChar(String input, int pos, char c)
    {
        if (pos == 0)
        {
            return c + input.substring(pos, input.length());
        }
        else if (pos == input.length())
        {
            return input.substring(0, pos) + c;
        }
        else
        {
            return input.substring(0, pos) + c +
                   input.substring(pos, input.length() - 1);
        }
    }


    /**
     * Constructs the beginning <script> element depending on xhtml status.
     */
    private String getStartElement()
    {
        StringBuffer start = new StringBuffer("<script type=\"text/javascript\"");

        // there is no language attribute in xhtml
        if (!this.xhtml)
        {
            start.append(" language=\"Javascript1.1\"");
        }

        if (this.src != null)
        {
            start.append(" src=\"" + src + "\"");
        }

        start.append("> \n");
        return start.toString();
    }


    /**
     * Inner class for use when creating dynamic javascript
     */
    protected class ValidatorActionComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            ValidatorAction va1 = (ValidatorAction)o1;
            ValidatorAction va2 = (ValidatorAction)o2;

            String vad1 = va1.getDepends();
            String vad2 = va2.getDepends();

            if ((vad1 == null || vad1.length() == 0)
                && (vad2 == null || vad2.length() == 0))
            {
                return 0;
            }
            else if ((vad1 != null && vad1.length() > 0)
                     && (vad2 == null || vad2.length() == 0))
            {
                return 1;
            }
            else if ((vad1 == null || vad1.length() == 0)
                     && (vad2 != null && vad2.length() > 0))
            {
                return -1;
            }
            else
            {
                return va1.getDependencies().size() - va2.getDependencies().size();
            }
        }
    }

}
<%--
 * Copyright 2004 The Apache Software Foundation.
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
 *
 * $Id$
--%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<html>
    <head>
	    <title>Struts App5: Struts ValidatorTool Demo (JSP Version)</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    </head>

    <body>
        <h2>Struts App5: Struts ValidatorTool Demo (JSP Version)</h2>
        <p>A demonstration of the Velocity ValidatorTool provided for Struts support.</p>

	<html:errors/>

        <html:form action="do_submit_jsp" method="post" onsubmit="return validateEmailForm(this)">
            <html:text property="email"/>&nbsp;
            <html:submit/>
	</html:form>

    </body>
    <!-- if this line is commented out, the form will be validated server-side ... go ahead, try it :) -->
    <html:javascript formName="emailForm"/>
</html>

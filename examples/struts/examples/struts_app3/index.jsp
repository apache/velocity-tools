<%--
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: index.jsp,v 1.3 2004/02/20 13:31:48 marino Exp $
--%>
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%--

If user is logged in, display "Welcome ${username}!"
Else display "Welcome World!"
Display link to log in page; maintain session id if needed.
If user is logged in, display a link to the sign-out page.

Note: Only the minimum required html or Sruts custom tags
are used in this example.

--%>

<html>
    <head>
        <title>Welcome World!!</title>
        <html:base/>
    </head>

    <body>
        <logic:present name="user">
        <h3>Welcome <bean:write name="user" property="username"/>! (JSP Version)</h3>
        </logic:present>

        <logic:notPresent scope="session" name="user">
        <h3>Welcome World! (JSP Version)</h3>
        </logic:notPresent>

        <html:errors/>

        <ul>
            <li><html:link forward="logon_jsp">Sign in</html:link></li>
            <logic:present name="user">
            <li><html:link forward="logoff_jsp">Sign out</html:link></li>
            </logic:present>
        </ul>

        <html:link forward="welcome_vm">Switch to Velocity</html:link><br>
        <html:link forward="index_jsp_src">View Template</html:link><br>

    </body>

</html>


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
 * $Id$
--%>
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html>
    <head>
        <title>Sign in, Please!</title>
        <html:base/>
    </head>

    <body>

        <html:errors/>

        <h3>Sign in, Please! (JSP Version)</h3>

        <html:form action="logonSubmit_jsp" focus="username">

        <table border="0">

            <tr>
                <th align="right">
                    Username:
                </th>
                <td align="left">
                    <html:text property="username"/>
                </td>
            </tr>

            <tr>
                <th align="right">
                    Password:
                </th>
                <td align="left">
                    <html:password property="password"/>
                </td>
            </tr>

            <tr>
                <td align="right">
                    <html:submit property="submit" value="Submit"/>
                </td>
                <td align="left">
                    <html:reset/>
                </td>
            </tr>

        </table>

        </html:form>

        <html:link forward="logon_jsp_src">View Template</html:link><br>


    </body>
</html>


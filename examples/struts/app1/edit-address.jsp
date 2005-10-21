<%--
 * Copyright 2003-2005 The Apache Software Foundation.
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
<html>
    <head>
    	<title>App1</title>
    	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    </head>

    <body>

        <p><b><bean:message key="edit"/> (JSP <bean:message key="version"/>)</b></p>

        <html:form action="address1.do">
        <input type="hidden" name="action" value="list">

        <table width="300" border="1" cellspacing="" cellpadding="5">
        	<tr>
        		<td><bean:message key="firstname"/>:</td>
        		<td><html:text name="address" property="firstname"/></td>
        	</tr>
        	<tr>
        		<td><bean:message key="lastname"/></td>
        		<td><html:text name="address" property="lastname"/></td>
        	</tr>
        	<tr>
        		<td><bean:message key="street"/></td>
        		<td><html:text name="address" property="street"/></td>
        	</tr>
        	<tr>
        		<td><bean:message key="zip"/></td>
        		<td><html:text name="address" property="zip"/></td>
        	</tr>
        	<tr>
        		<td><bean:message key="city"/></td>
        		<td><html:text name="address" property="city"/></td>
        	</tr>
        	<tr>
        		<td><bean:message key="country"/></td>
        		<td><html:text name="address" property="country"/></td>
        	</tr>
            <tr>
                <td><bean:message key="languages"/></td>
                <td>
                    <html:select name="address" property="languages" multiple="true" size="5">
                        <html:option value="chinese" key="chinese"/>
                        <html:option value="english" key="english"/>
                        <html:option value="french"  key="french"/>
                        <html:option value="german"  key="german"/>
                        <html:option value="russian" key="russian"/>
                        <html:option value="spanish" key="spanish"/>
                    </html:select>
                    <br><bean:message key="multiple"/>
                </td>
            </tr>
        </table>
        <br>

        <html:submit property="Submit" onclick="addressForm.action.value='save'; document.address.submit(); return false;"><bean:message key="save"/></html:submit>
        <html:submit property="Submit2" onclick="addressForm.action.value='list'; document.address.submit(); return false;"><bean:message key="cancel"/></html:submit>
        </html:form>

        <br>
        <html:link forward="editAddressSrcJsp">Template</html:link>

    </body>
</html>
